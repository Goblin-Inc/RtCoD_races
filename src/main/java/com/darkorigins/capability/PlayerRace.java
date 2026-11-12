package com.darkorigins.capability;

import com.darkorigins.race.RaceType;
import net.minecraft.nbt.CompoundTag;

public class PlayerRace {
    private RaceType race = RaceType.NONE;
    private boolean hasChosen = false;
    private int abilityCooldown = 0;

    public RaceType getRace() { return race; }
    public void setRace(RaceType race) {
        this.race = race;
        this.hasChosen = (race != RaceType.NONE);
    }

    public boolean hasChosen() { return hasChosen; }
    public void setHasChosen(boolean hasChosen) { this.hasChosen = hasChosen; }

    public int getCooldown() { return abilityCooldown; }
    public void setCooldown(int cd) { this.abilityCooldown = Math.max(0, cd); }

    public void copyFrom(PlayerRace source) {
        this.race = source.race;
        this.hasChosen = source.hasChosen;
        this.abilityCooldown = 0;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putString("RaceId", race.name());
        nbt.putBoolean("HasChosen", hasChosen);
        nbt.putInt("Cooldown", abilityCooldown);
    }

    public void loadNBTData(CompoundTag nbt) {
        try {
            this.race = RaceType.valueOf(nbt.getString("RaceId"));
        } catch (IllegalArgumentException e) {
            this.race = RaceType.NONE;
        }
        this.hasChosen = nbt.getBoolean("HasChosen");
        this.abilityCooldown = nbt.getInt("Cooldown");
    }
}