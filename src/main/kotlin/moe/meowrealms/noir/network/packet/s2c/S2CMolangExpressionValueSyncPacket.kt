package moe.meowrealms.noir.network.packet.s2c

import it.unimi.dsi.fastutil.floats.FloatArrayList
import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class S2CMolangExpressionValueSyncPacket(
    var entityId: Int,
    var expressionValues: FloatArrayList
): Packet {
    override fun direction(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun handle(handler: PacketHandler) {
        TODO("Not yet implemented on server side!")
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.entityId = buffer.readVarInt()
        this.expressionValues = FloatArrayList()

        val size = buffer.readByte().toInt()
        for (i in 0 until size) {
            this.expressionValues.add(buffer.readFloat())
        }
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeVarInt(entityId)

        buffer.writeByte(this.expressionValues.size)
        for (i in this.expressionValues) {
            buffer.writeFloat(i)
        }
    }
}