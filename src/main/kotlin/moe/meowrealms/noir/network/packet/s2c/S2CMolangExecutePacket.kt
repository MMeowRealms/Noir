package moe.meowrealms.noir.network.packet.s2c

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class S2CMolangExecutePacket(
    var onEntityIds: IntArray,
    var expression: String
): Packet {
    override fun direction(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun handle(handler: PacketHandler) {
        TODO("Not yet implemented on server side!")
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.onEntityIds = buffer.readVarIntArray()
        this.expression = buffer.readUtf()
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeVarIntArray(this.onEntityIds)
        buffer.writeUtf(this.expression)
    }

}