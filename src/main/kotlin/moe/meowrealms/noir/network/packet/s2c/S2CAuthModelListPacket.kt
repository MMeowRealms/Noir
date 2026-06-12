package moe.meowrealms.noir.network.packet.s2c

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class S2CAuthModelListPacket(
    var authModels: Set<String>
) : Packet{
    override fun direction(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun handle(handler: PacketHandler) {
        TODO("Not yet implemented on server side!")
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.authModels = HashSet()

        for (i in 0 until buffer.readVarInt()) {
            (this.authModels as MutableSet<String>).add(buffer.readUtf())
        }
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeVarInt(this.authModels.size)

        for (model in this.authModels) {
            buffer.writeUtf(model)
        }
    }

}