package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public class TonCenterTransactionsResponse(
    public val transactions: List<TonCenterTransaction>,
    public val addressBook: TonCenterAddressBook
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TonCenterTransactionsResponse) return false
        if (transactions != other.transactions) return false
        if (addressBook != other.addressBook) return false
        return true
    }

    override fun hashCode(): Int {
        var result = transactions.hashCode()
        result = 31 * result + addressBook.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("TonCenterTransactionsResponse(")
        append("transactions=$transactions, ")
        append("addressBook=$addressBook")
        append(")")
    }
}
