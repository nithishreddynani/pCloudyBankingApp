package com.pcloudy.bankingg

import java.util.UUID

//data class BankCard(
//    val id: String = UUID.randomUUID().toString(),
//    val cardNumber: String,
//    val cardHolder: String,
//    val expiryDate: String,
//    val cvv: String,
//    val cardType: CardType = CardType.DEBIT,
//    val lastFourDigits: String = cardNumber.takeLast(4),
//    val balance: Double = 0.0,
//    val status: CardStatus = CardStatus.ACTIVE
//)
//
//enum class CardType {
//    DEBIT, CREDIT, PREPAID
//}

data class BankCard(
    val id: String = UUID.randomUUID().toString(),
    val cardNumber: String,
    val cardHolderName: String,
    val expiryDate: String,
    val cvv: String,
    val cardType: CardType = CardType.DEBIT
)

enum class CardType {
    DEBIT, CREDIT, PREPAID
}

enum class CardStatus {
    ACTIVE, BLOCKED, EXPIRED
}

