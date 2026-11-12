package com.darkorigins.client;

import com.darkorigins.DarkOriginsMod;
import com.darkorigins.network.PacketHandler;
import com.darkorigins.network.UseAbilityC2SPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static final KeyMapping ABILITY_KEY = new KeyMapping(
            "key.darkorigins.ability",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.darkorigins"
    );

    @Mod.EventBusSubscriber(modid = DarkOriginsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ABILITY_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = DarkOriginsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (Minecraft.getInstance().screen == null && ABILITY_KEY.consumeClick()) {
                PacketHandler.sendToServer(new UseAbilityC2SPacket());
            }
        }
    }
}