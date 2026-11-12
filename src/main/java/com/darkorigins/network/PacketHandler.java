package com.darkorigins.network;

import com.darkorigins.DarkOriginsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(DarkOriginsMod.MODID, "messages"))
                .networkProtocolVersion(() -> "1.1")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE.messageBuilder(SelectRaceC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SelectRaceC2SPacket::new)
                .encoder(SelectRaceC2SPacket::toBytes)
                .consumerMainThread(SelectRaceC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(SyncRaceS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncRaceS2CPacket::new)
                .encoder(SyncRaceS2CPacket::toBytes)
                .consumerMainThread(SyncRaceS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(UseAbilityC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UseAbilityC2SPacket::new)
                .encoder(UseAbilityC2SPacket::toBytes)
                .consumerMainThread(UseAbilityC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}