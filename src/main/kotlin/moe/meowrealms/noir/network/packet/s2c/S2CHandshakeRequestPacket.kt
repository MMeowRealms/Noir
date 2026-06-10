package moe.meowrealms.noir.network.packet.s2c

import moe.meowrealms.noir.network.ClientConnectionManager
import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class S2CHandshakeRequestPacket(
    var version: String = ClientConnectionManager.VERSION
) : Packet {
    override fun direction(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun handle(handler: PacketHandler) {
        TODO("Not yet implemented on server side!")
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.version = buffer.readUtf()
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeUtf(this.version)
    }
}