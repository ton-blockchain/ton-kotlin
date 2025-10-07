package org.ton.api.adnl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
public data class AdnlAddressList(
    @get:JvmName("addrs")
    val addrs: List<AdnlAddress> = emptyList(),

    @get:JvmName("version")
    val version: Int = 0,

    @SerialName("reinit_date")
    @get:JvmName("reinitDate")
    val reinitDate: Int = 0,

    @get:JvmName("priority")
    val priority: Int = 0,

    @SerialName("expire_at")
    @get:JvmName("expireAt")
    val expireAt: Int = 0
) : TlObject<AdnlAddressList> {
    override fun tlCodec(): TlCodec<AdnlAddressList> = Companion

    public companion object : TlConstructor<AdnlAddressList>(
        schema = "adnl.addressList addrs:(vector adnl.Address) version:int reinit_date:int priority:int expire_at:int = adnl.AddressList"
    ) {
        override fun encode(writer: TlWriter, value: AdnlAddressList) {
            writer.writeVector(value.addrs) {
                write(AdnlAddress, it)
            }
            writer.writeInt(value.version)
            writer.writeInt(value.reinitDate)
            writer.writeInt(value.priority)
            writer.writeInt(value.expireAt)
        }

        override fun decode(reader: TlReader): AdnlAddressList {
            val addrs = reader.readVector {
                read(AdnlAddress)
            }
            val version = reader.readInt()
            val reinitDate = reader.readInt()
            val priority = reader.readInt()
            val expireAt = reader.readInt()
            return AdnlAddressList(addrs, version, reinitDate, priority, expireAt)
        }
    }
}
