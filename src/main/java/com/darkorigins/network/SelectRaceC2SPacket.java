package com.darkorigins.network;

import com.darkorigins.capability.PlayerRaceProvider;
import com.darkorigins.event.ModEvents;
import com.darkorigins.race.RaceType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectRaceC2SPacket {
    private final RaceType selectedRace;

    public SelectRaceC2SPacket(RaceType race) { this.selectedRace = race; }
    public SelectRaceC2SPacket(FriendlyByteBuf buf) { this.selectedRace = buf.readEnum(RaceType.class); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeEnum(selectedRace); }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && selectedRace != RaceType.NONE) {
                player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                    cap.setRace(selectedRace);
                    cap.setCooldown(0);
                    player.removeAllEffects();
                    player.setAbsorptionAmount(0.0f);
                    ModEvents.applyRaceAttributes(player, selectedRace);
                    player.setHealth(player.getMaxHealth());
                    player.refreshDimensions();
                    PacketHandler.sendToPlayer(new SyncRaceS2CPacket(selectedRace, false), player);
                    player.sendSystemMessage(Component.literal("✔ Вы выбрали расу: " + selectedRace.displayName).withStyle(ChatFormatting.GREEN));
                });
            }
        });
        return true;
    }
}