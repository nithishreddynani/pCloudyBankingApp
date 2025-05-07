package com.pcloudy.bankingg

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//class CardsViewModel : ViewModel() {
//    private val cardsRepository = CardsRepository()
//
//    private val _cardsState = MutableStateFlow<CardsUiState>(CardsUiState.Loading)
//    val cardsState: StateFlow<CardsUiState> = _cardsState.asStateFlow()
//
//    fun fetchCards() {
//        viewModelScope.launch {
//            _cardsState.value = CardsUiState.Loading
//            try {
//                val cards = cardsRepository.getAllCards()
//                _cardsState.value = if (cards.isEmpty()) {
//                    CardsUiState.Empty
//                } else {
//                    CardsUiState.Success(cards)
//                }
//            } catch (e: Exception) {
//                _cardsState.value = CardsUiState.Error(
//                    "Error fetching cards: ${e.localizedMessage}"
//                )
//            }
//        }
//    }
//
//    fun addCard(card: BankCard) {
//        viewModelScope.launch {
//            try {
//                cardsRepository.addCard(card)
//                fetchCards()
//            } catch (e: Exception) {
//                _cardsState.value = CardsUiState.Error(
//                    "Error adding card: ${e.localizedMessage}"
//                )
//            }
//        }
//    }
//
//    fun blockCard(card: BankCard) {
//        viewModelScope.launch {
//            try {
//                cardsRepository.blockCard(card)
//                fetchCards()
//            } catch (e: Exception) {
//                _cardsState.value = CardsUiState.Error(
//                    "Error blocking card: ${e.localizedMessage}"
//                )
//            }
//        }
//    }
//}
//
//// Sealed class for UI state
//sealed class CardsUiState {
//    object Loading : CardsUiState()
//    data class Success(val cards: List<BankCard>) : CardsUiState()
//    data class Error(val message: String) : CardsUiState()
//    object Empty : CardsUiState()
//}

class CardsViewModel(application: Application) : AndroidViewModel(application) {
    private val cardRepository = CardsRepository(application)

    private val _cardsState = MutableStateFlow<CardsUiState>(CardsUiState.Loading)
    val cardsState: StateFlow<CardsUiState> = _cardsState.asStateFlow()

    fun fetchCards() {
        viewModelScope.launch {
            val cards = cardRepository.getAllCards()
            _cardsState.value = when {
                cards.isEmpty() -> CardsUiState.Empty
                else -> CardsUiState.Success(cards)
            }
        }
    }

    fun addCard(card: BankCard) {
        viewModelScope.launch {
            try {
                cardRepository.addCard(card)
                fetchCards()
            } catch (e: Exception) {
                _cardsState.value = CardsUiState.Error("Failed to add card: ${e.localizedMessage}")
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            try {
                // Delete the card
                cardRepository.deleteCard(cardId)

                // Fetch updated cards
                fetchCards()
            } catch (e: Exception) {
                _cardsState.value = CardsUiState.Error("Failed to delete card: ${e.localizedMessage}")
            }
        }
    }
}
// Sealed class for UI state
sealed class CardsUiState {
    object Loading : CardsUiState()
    data class Success(val cards: List<BankCard>) : CardsUiState()
    data class Error(val message: String) : CardsUiState()
    object Empty : CardsUiState()
}