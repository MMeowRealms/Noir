package moe.meowrealms.noir.network.packet.s2c

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class S2CModelDataPayloadPacket(
    var payload: ByteArray
) : Packet {
    override fun direction(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun handle(handler: PacketHandler) {
        TODO("Not implemented on server side")
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.payload = ByteArray(buffer.readableBytes())
        buffer.readBytes(payload)
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeBytes(payload)
    }
}