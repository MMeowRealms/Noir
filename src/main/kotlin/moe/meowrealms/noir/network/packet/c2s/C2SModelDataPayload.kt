package moe.meowrealms.noir.network.packet.c2s

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class C2SModelDataPayload(
    var payload: ByteArray
) : Packet {
    override fun direction(): EnumDirection {
        return EnumDirection.C_T_S
    }

    override fun handle(handler: PacketHandler) {
        handler.handleClientModelSyncPayload(this)
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.payload = ByteArray(buffer.readableBytes())
        buffer.readBytes(payload)
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeBytes(payload)
    }
}