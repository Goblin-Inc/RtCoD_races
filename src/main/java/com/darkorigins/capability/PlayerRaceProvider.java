package com.darkorigins.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRaceProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerRace> PLAYER_RACE = CapabilityManager.get(new CapabilityToken<>() {});
    private PlayerRace playerRace = null;
    private final LazyOptional<PlayerRace> optional = LazyOptional.of(this::createPlayerRace);

    private PlayerRace createPlayerRace() {
        if (this.playerRace == null) this.playerRace = new PlayerRace();
        return this.playerRace;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_RACE ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerRace().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerRace().loadNBTData(nbt);
    }
}