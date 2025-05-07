package com.pcloudy.bankingg

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.pcloudy.bankingg.databinding.FragmentCardsBinding
import kotlinx.coroutines.launch
import java.util.Calendar

//class CardsFragment : Fragment() {
//    private var _binding: FragmentCardsBinding? = null
//    private val binding get() = _binding!!
//
//    private val cardsViewModel: CardsViewModel by viewModels()
//    private lateinit var cardsAdapter: CardsAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentCardsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupRecyclerView()
//        setupAddCardButton()
//        observeViewModel()
//    }
//
//    private fun setupRecyclerView() {
//        cardsAdapter = CardsAdapter(
//            onCardClick = { card -> showCardDetails(card) },
//            onBlockCard = { card -> blockCard(card) }
//        )
//        binding.cardsRecyclerView.apply {
//            adapter = cardsAdapter
//            layoutManager = LinearLayoutManager(context)
//        }
//    }
//
//    private fun setupAddCardButton() {
//        binding.buttonAddCard.setOnClickListener {
//            showAddCardDialog()
//        }
//    }
//
//    private fun showAddCardDialog() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_add_card, null)
//
//        // Get dialog inputs
//        val cardNumberInput = dialogView.findViewById<TextInputEditText>(R.id.cardNumberInput)
//        val cardHolderInput = dialogView.findViewById<TextInputEditText>(R.id.cardHolderInput)
//        val expiryDateInput = dialogView.findViewById<TextInputEditText>(R.id.expiryDateInput)
//        val cvvInput = dialogView.findViewById<TextInputEditText>(R.id.cvvInput)
//
//        // Setup expiry date input formatting
//        setupExpiryDateFormatting(expiryDateInput)
//
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Add New Card")
//            .setView(dialogView)
//            .setPositiveButton("Add") { _, _ ->
//                val cardNumber = cardNumberInput.text.toString().trim().replace(" ", "")
//                val cardHolder = cardHolderInput.text.toString().trim()
//                val expiryDate = expiryDateInput.text.toString().trim()
//                val cvv = cvvInput.text.toString().trim()
//
//                // Validate and add card
//                validateAndAddCard(cardNumber, cardHolder, expiryDate, cvv)
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun setupExpiryDateFormatting(expiryDateInput: TextInputEditText) {
//        expiryDateInput.addTextChangedListener(object : TextWatcher {
//            private var isFormatting = false
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                if (isFormatting) return
//
//                isFormatting = true
//
//                // Remove all non-digit characters
//                val digits = s.toString().replace("/", "").filter { it.isDigit() }
//
//                // Limit to 4 digits
//                val formattedDigits = digits.take(4)
//
//                // Format with slash
//                val formatted = when (formattedDigits.length) {
//                    3 -> "${formattedDigits.take(2)}/${formattedDigits.last()}"
//                    4 -> "${formattedDigits.take(2)}/${formattedDigits.takeLast(2)}"
//                    else -> formattedDigits
//                }
//
//                // Set formatted text
//                expiryDateInput.setText(formatted)
//                expiryDateInput.setSelection(formatted.length)
//
//                isFormatting = false
//            }
//        })
//    }
//
//    private fun validateAndAddCard(
//        cardNumber: String,
//        cardHolder: String,
//        expiryDate: String,
//        cvv: String
//    ) {
//        // Validation logic
//        val validationResult = validateCardDetails(cardNumber, cardHolder, expiryDate, cvv)
//
//        if (validationResult.first) {
//            // Create and add card
//            val card = BankCard(
//                cardNumber = cardNumber,
//                cardHolder = cardHolder,
//                expiryDate = expiryDate,
//                cvv = cvv
//            )
//
//            cardsViewModel.addCard(card)
//        } else {
//            // Show error
//            Snackbar.make(
//                binding.root,
//                validationResult.second,
//                Snackbar.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    private fun validateCardDetails(
//        cardNumber: String,
//        cardHolder: String,
//        expiryDate: String,
//        cvv: String
//    ): Pair<Boolean, String> {
//        // Card Number Validation
//        if (cardNumber.length != 16 || !cardNumber.all { it.isDigit() }) {
//            return Pair(false, "Invalid card number")
//        }
//
//        // Card Holder Name Validation
//        if (cardHolder.length < 3) {
//            return Pair(false, "Invalid card holder name")
//        }
//
//        // Expiry Date Validation
//        if (!validateExpiryDate(expiryDate)) {
//            return Pair(false, "Invalid expiry date (MM/YY)")
//        }
//
//        // CVV Validation
//        if (cvv.length !in 3..4 || !cvv.all { it.isDigit() }) {
//            return Pair(false, "Invalid CVV")
//        }
//
//        return Pair(true, "")
//    }
//
//    private fun validateExpiryDate(expiryDate: String): Boolean {
//        // Remove any non-digit characters
//        val cleanedDate = expiryDate.replace("/", "")
//
//        // Check if it's exactly 4 digits
//        if (cleanedDate.length != 4) return false
//
//        // Extract month and year
//        val month = cleanedDate.take(2).toIntOrNull() ?: return false
//        val year = cleanedDate.takeLast(2).toIntOrNull() ?: return false
//
//        // Validate month
//        if (month < 1 || month > 12) return false
//
//        // Get current date
//        val calendar = Calendar.getInstance()
//        val currentYear = calendar.get(Calendar.YEAR) % 100
//        val currentMonth = calendar.get(Calendar.MONTH) + 1
//
//        // Check if the date is in the future
//        return year > currentYear || (year == currentYear && month >= currentMonth)
//    }
//
//    private fun showCardDetails(card: BankCard) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Card Details")
//            .setMessage("""
//                Card Number: **** **** **** ${card.cardNumber.takeLast(4)}
//                Holder: ${card.cardHolder}
//                Expiry: ${card.expiryDate}
//                Type: ${card.cardType}
//            """.trimIndent())
//            .setPositiveButton("OK", null)
//            .show()
//    }
//
//    private fun blockCard(card: BankCard) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Block Card")
//            .setMessage("Are you sure you want to block this card?")
//            .setPositiveButton("Block") { _, _ ->
//                cardsViewModel.blockCard(card)
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            cardsViewModel.cardsState.collect { state ->
//                when (state) {
//                    is CardsUiState.Loading -> {
//                        binding.progressBar.visibility = View.VISIBLE
//                    }
//                    is CardsUiState.Success -> {
//                        binding.progressBar.visibility = View.GONE
//                        cardsAdapter.submitList(state.cards)
//                    }
//                    is CardsUiState.Error -> {
//                        binding.progressBar.visibility = View.GONE
//                        Snackbar.make(
//                            binding.root,
//                            state.message,
//                            Snackbar.LENGTH_SHORT
//                        ).show()
//                    }
//                    is CardsUiState.Empty -> {
//                        binding.progressBar.visibility = View.GONE
//                        // Show empty state view if needed
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

class CardsFragment : Fragment() {
    private var _binding: FragmentCardsBinding? = null
    private val binding get() = _binding!!

    private val cardsViewModel: CardsViewModel by viewModels()
    private lateinit var cardsAdapter: CardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAddCardButton()
        observeViewModel()

        // Fetch cards when fragment is created
        cardsViewModel.fetchCards()
    }

    private fun setupRecyclerView() {
        cardsAdapter = CardsAdapter(
            onCardClick = { card -> showCardDetails(card) },
            onDeleteCard = { card -> deleteCard(card) }
        )
        binding.cardsRecyclerView.apply {
            adapter = cardsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupAddCardButton() {
        binding.fabAddCard.setOnClickListener {
            showAddCardDialog()
        }
    }

    private fun showAddCardDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_card, null)

        // Get dialog inputs
        val cardNumberInput = dialogView.findViewById<TextInputEditText>(R.id.cardNumberInput)
        val cardHolderInput = dialogView.findViewById<TextInputEditText>(R.id.cardHolderInput)
        val expiryDateInput = dialogView.findViewById<TextInputEditText>(R.id.expiryDateInput)
        val cvvInput = dialogView.findViewById<TextInputEditText>(R.id.cvvInput)

        // Setup expiry date input formatting
        setupExpiryDateFormatting(expiryDateInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Card")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val cardNumber = cardNumberInput.text.toString().trim().replace(" ", "")
                val cardHolder = cardHolderInput.text.toString().trim()
                val expiryDate = expiryDateInput.text.toString().trim()
                val cvv = cvvInput.text.toString().trim()

                // Validate and add card
                validateAndAddCard(cardNumber, cardHolder, expiryDate, cvv)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupExpiryDateFormatting(expiryDateInput: TextInputEditText) {
        expiryDateInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                // Remove all non-digit characters
                val digits = s.toString().replace("/", "").filter { it.isDigit() }

                // Limit to 4 digits
                val formattedDigits = digits.take(4)

                // Format with slash
                val formatted = when (formattedDigits.length) {
                    3 -> "${formattedDigits.take(2)}/${formattedDigits.last()}"
                    4 -> "${formattedDigits.take(2)}/${formattedDigits.takeLast(2)}"
                    else -> formattedDigits
                }

                // Set formatted text
                expiryDateInput.setText(formatted)
                expiryDateInput.setSelection(formatted.length)

                isFormatting = false
            }
        })
    }

    private fun validateAndAddCard(
        cardNumber: String,
        cardHolder: String,
        expiryDate: String,
        cvv: String
    ) {
        // Validation logic
        val validationResult = validateCardDetails(cardNumber, cardHolder, expiryDate, cvv)

        if (validationResult.first) {
            // Create and add card
            val card = BankCard(
                cardNumber = cardNumber,
                cardHolderName = cardHolder,
                expiryDate = expiryDate,
                cvv = cvv
            )

            cardsViewModel.addCard(card)
        } else {
            // Show error
            Snackbar.make(
                binding.root,
                validationResult.second,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateCardDetails(
        cardNumber: String,
        cardHolder: String,
        expiryDate: String,
        cvv: String
    ): Pair<Boolean, String> {
        // Card Number Validation
        if (cardNumber.length != 16 || !cardNumber.all { it.isDigit() }) {
            return Pair(false, "Invalid card number")
        }

        // Card Holder Name Validation
        if (cardHolder.length < 3) {
            return Pair(false, "Invalid card holder name")
        }

        // Expiry Date Validation
        if (!validateExpiryDate(expiryDate)) {
            return Pair(false, "Invalid expiry date (MM/YY)")
        }

        // CVV Validation
        if (cvv.length !in 3..4 || !cvv.all { it.isDigit() }) {
            return Pair(false, "Invalid CVV")
        }

        return Pair(true, "")
    }

    private fun validateExpiryDate(expiryDate: String): Boolean {
        // Remove any non-digit characters
        val cleanedDate = expiryDate.replace("/", "")

        // Check if it's exactly 4 digits
        if (cleanedDate.length != 4) return false

        // Extract month and year
        val month = cleanedDate.take(2).toIntOrNull() ?: return false
        val year = cleanedDate.takeLast(2).toIntOrNull() ?: return false

        // Validate month
        if (month < 1 || month > 12) return false

        // Get current date
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR) % 100
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        // Check if the date is in the future
        return year > currentYear || (year == currentYear && month >= currentMonth)
    }

    private fun showCardDetails(card: BankCard) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Card Details")
            .setMessage("""
                Card Number: **** **** **** ${card.cardNumber.takeLast(4)}
                Holder: ${card.cardHolderName}
                Expiry: ${card.expiryDate}
                Type: ${card.cardType}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun deleteCard(card: BankCard) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Card")
            .setMessage("Are you sure you want to delete this card?")
            .setPositiveButton("Delete") { _, _ ->
                cardsViewModel.deleteCard(card.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            cardsViewModel.cardsState.collect { state ->
                when (state) {
                    is CardsUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.cardsRecyclerView.visibility = View.GONE
                    }
                    is CardsUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.cardsRecyclerView.visibility = View.VISIBLE
                        cardsAdapter.submitList(state.cards)

                        // Show/hide empty state view
                        binding.emptyStateView.visibility =
                            if (state.cards.isEmpty()) View.VISIBLE else View.GONE
                    }
                    is CardsUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.cardsRecyclerView.visibility = View.VISIBLE
                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is CardsUiState.Empty -> {
                        binding.progressBar.visibility = View.GONE
                        binding.cardsRecyclerView.visibility = View.GONE
                        binding.emptyStateView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}