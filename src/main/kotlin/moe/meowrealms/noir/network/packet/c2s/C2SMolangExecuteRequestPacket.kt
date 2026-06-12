package moe.meowrealms.noir.network.packet.c2s

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class C2SMolangExecuteRequestPacket(
    var expression: String,
    var onEntityId: Int
) : Packet{
    override fun direction(): EnumDirection {
        return EnumDirection.C_T_S
    }

    override fun handle(handler: PacketHandler) {
        handler.handleMolangExecuteRequest(this)
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.expression = buffer.readUtf()
        this.onEntityId = buffer.readVarInt()
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeUtf(this.expression)
        buffer.writeVarInt(this.onEntityId)
    }
}