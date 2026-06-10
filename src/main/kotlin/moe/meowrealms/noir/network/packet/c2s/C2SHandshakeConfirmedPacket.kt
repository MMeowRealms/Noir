package moe.meowrealms.noir.network.packet.c2s

import moe.meowrealms.noir.network.ClientConnectionManager
import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class C2SHandshakeConfirmedPacket(
    var version: String = ClientConnectionManager.VERSION
): Packet {
    override fun direction(): EnumDirection {
        return EnumDirection.C_T_S
    }

    override fun handle(handler: PacketHandler) {
        handler.handleHandshakeConfirmed(this)
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.version = buffer.readUtf()
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeUtf(this.version)
    }
}