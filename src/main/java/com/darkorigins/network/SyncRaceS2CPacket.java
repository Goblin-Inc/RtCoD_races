package com.darkorigins.network;

import com.darkorigins.client.RaceSelectionScreen;
import com.darkorigins.capability.PlayerRaceProvider;
import com.darkorigins.race.RaceType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRaceS2CPacket {
    private final RaceType race;
    private final boolean openGui;

    public SyncRaceS2CPacket(RaceType race, boolean openGui) {
        this.race = race;
        this.openGui = openGui;
    }

    public SyncRaceS2CPacket(FriendlyByteBuf buf) {
        this.race = buf.readEnum(RaceType.class);
        this.openGui = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(race);
        buf.writeBoolean(openGui);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                    cap.setRace(race);
                    player.refreshDimensions();
                });
                if (openGui) {
                    Minecraft.getInstance().setScreen(new RaceSelectionScreen(true));
                }
            }
        }));
        return true;
    }
}