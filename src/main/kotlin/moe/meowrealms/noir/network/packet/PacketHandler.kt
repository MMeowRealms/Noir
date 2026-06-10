package moe.meowrealms.noir.network.packet

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.c2s.C2SHandshakeConfirmedPacket
import moe.meowrealms.noir.network.packet.c2s.C2SModelDataPayload

interface PacketHandler {
    fun receivingDirection(): EnumDirection

    fun sendingDirection(): EnumDirection

    fun send(packet: Packet)

    fun handleClientModelSyncPayload(packet: C2SModelDataPayload)

    fun handleHandshakeConfirmed(packet: C2SHandshakeConfirmedPacket)
}