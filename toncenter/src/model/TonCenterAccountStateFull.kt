@file:UseSerializers(
    ByteStringBase64Serializer::class,
    BigIntAsStringSerializer::class,
    HashBytesAsBase64Serializer::class,
    ExtraCurrencyCollectionSerializer::class,
    CoinsSerializer::class,
)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.HashBytes
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.toncenter.internal.serializers.BigIntAsStringSerializer
import org.ton.sdk.toncenter.internal.serializers.CoinsSerializer
import org.ton.sdk.toncenter.internal.serializers.ExtraCurrencyCollectionSerializer
import org.ton.sdk.toncenter.internal.serializers.HashBytesAsBase64Serializer

/**
 * Represents the full state of an account.
 *
 * This class provides all relevant associated with an account,
 * including balances, state hashes, transactions, and contract code and data.
 *
 * @property address The address of the account.
 * @property accountStateHash A hash representing the current state of the account.
 * @property balance The main currency balance of the account.
 * @property extraCurrencies A map representing additional balances in other currencies.
 * @property status The current status of the account.
 * @property lastTransactionHash The hash of the last transaction for the account.
 * @property lastTransactionLt The logical time of the last transaction for the account.
 * @property dataHash A hash of the account's data.
 * @property codeHash A hash of the account's code.
 * @property dataBoc The serialized account in BoC.
 * @property codeBoc The serialized account code in BoC.
 * @property contractMethods A list of integer identifiers representing the supported methods of the account's contract, if any.
 * @property interfaces A list of string identifiers representing the supported interfaces of the account's contract.
 */
@Serializable
public class TonCenterAccountStateFull(
    public val address: String,
    public val accountStateHash: HashBytes,
    public val balance: Coins,
    public val extraCurrencies: ExtraCurrencyCollection,
    public val status: String,
    public val lastTransactionHash: HashBytes,
    public val lastTransactionLt: Long,
    public val dataHash: HashBytes,
    public val codeHash: HashBytes,
    public val dataBoc: ByteString,
    public val codeBoc: ByteString,
    public val contractMethods: List<Int>?,
    public val interfaces: List<String>
)
