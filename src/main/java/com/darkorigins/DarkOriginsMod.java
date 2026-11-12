package com.darkorigins;

import com.darkorigins.capability.PlayerRace;
import com.darkorigins.network.PacketHandler;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DarkOriginsMod.MODID)
public class DarkOriginsMod {
    public static final String MODID = "rtcod_races";

    public DarkOriginsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCaps);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
    }

    private void registerCaps(final RegisterCapabilitiesEvent event) {
        event.register(PlayerRace.class);
    }
}