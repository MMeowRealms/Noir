package moe.meowrealms.noir.network.packet.s2c

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class S2CStarModelListPacket(
    var starModels: Set<String>
) : Packet{
    override fun direction(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun handle(handler: PacketHandler) {
        TODO("Not yet implemented on server side!")
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.starModels = HashSet()

        for (i in 0 until buffer.readVarInt()) {
            (this.starModels as MutableSet<String>).add(buffer.readUtf())
        }
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeVarInt(this.starModels.size)
        for (model in this.starModels) {
            buffer.writeUtf(model)
        }
    }
}