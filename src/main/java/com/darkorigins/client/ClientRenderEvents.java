package com.darkorigins.client;

import com.darkorigins.DarkOriginsMod;
import com.darkorigins.capability.PlayerRaceProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DarkOriginsMod.MODID, value = Dist.CLIENT)
public class ClientRenderEvents {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
            float scale = cap.getRace().scale;
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            if (scale != 1.0f && cap.hasChosen()) {
                poseStack.scale(scale, scale, scale);
            }
        });
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
            event.getPoseStack().popPose();
        });
    }
}