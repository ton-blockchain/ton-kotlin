@file:UseSerializers(
    ByteStringBase64Serializer::class,
    BigIntAsStringSerializer::class,
    HashBytesAsBase64Serializer::class,
    ExtraCurrencyCollectionSerializer::class,
    AddressStdAsBase64Serializer::class,
    CoinsSerializer::class,
)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.*
import kotlin.jvm.JvmName

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
    @get:JvmName("address")
    public val address: AddressStd,
    @get:JvmName("accountStateHash")
    public val accountStateHash: HashBytes,
    @get:JvmName("balance")
    public val balance: Coins,
    @get:JvmName("extraCurrencies")
    public val extraCurrencies: ExtraCurrencyCollection,
    @get:JvmName("status")
    public val status: String,
    @get:JvmName("lastTransactionHash")
    public val lastTransactionHash: HashBytes,
    @get:JvmName("lastTransactionLt")
    public val lastTransactionLt: Long,
    @get:JvmName("dataHash")
    public val dataHash: HashBytes,
    @get:JvmName("codeHash")
    public val codeHash: HashBytes,
    @get:JvmName("dataBoc")
    public val dataBoc: ByteString,
    @get:JvmName("codeBoc")
    public val codeBoc: ByteString,
    @get:JvmName("contractMethods")
    public val contractMethods: List<Int>?,
    @get:JvmName("interfaces")
    public val interfaces: List<String>
)
