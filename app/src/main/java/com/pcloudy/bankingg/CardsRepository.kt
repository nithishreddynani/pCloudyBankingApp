package com.pcloudy.bankingg

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//class CardsRepository {
//    private val cards = mutableListOf<BankCard>()
//
//    fun getAllCards(): List<BankCard> = cards.toList()
//
//    fun addCard(card: BankCard) {
//        // Validate card details
//        validateCard(card)
//        cards.add(card)
//    }
//
//    fun blockCard(card: BankCard) {
//        val index = cards.indexOfFirst { it.id == card.id }
//        if (index != -1) {
//            cards[index] = card.copy(status = CardStatus.BLOCKED)
//        }
//    }
//
//    private fun validateCard(card: BankCard) {
//        // Basic card validation
//        require(card.cardNumber.length == 16) { "Invalid card number" }
//        require(card.cvv.length == 3) { "Invalid CVV" }
//        require(card.expiryDate.matches("\\d{2}/\\d{2}".toRegex())) { "Invalid expiry date" }
//    }
//}

class CardsRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("BANK_CARDS", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addCard(card: BankCard) {
        // Get existing cards
        val existingCards = getAllCards().toMutableList()

        // Add new card if not already exists
        if (existingCards.none { it.id == card.id }) {
            existingCards.add(card)

            // Save to SharedPreferences
            val cardsJson = gson.toJson(existingCards)
            sharedPreferences.edit().putString("CARDS_LIST", cardsJson).apply()
        }
    }

    fun getAllCards(): List<BankCard> {
        val cardsJson = sharedPreferences.getString("CARDS_LIST", null)
        return if (cardsJson != null) {
            val type = object : TypeToken<List<BankCard>>() {}.type
            gson.fromJson(cardsJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteCard(cardId: String) {
        // Get existing cards
        val existingCards = getAllCards().toMutableList()

        // Remove the card with the specific ID
        existingCards.removeAll { it.id == cardId }

        // Save updated list
        val cardsJson = gson.toJson(existingCards)
        sharedPreferences.edit().putString("CARDS_LIST", cardsJson).apply()
    }

    fun clearAllCards() {
        sharedPreferences.edit().remove("CARDS_LIST").apply()
    }
}