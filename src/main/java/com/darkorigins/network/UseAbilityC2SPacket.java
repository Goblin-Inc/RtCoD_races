package com.darkorigins.network;

import com.darkorigins.capability.PlayerRaceProvider;
import com.darkorigins.race.RaceType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class UseAbilityC2SPacket {
    public UseAbilityC2SPacket() {}
    public UseAbilityC2SPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                RaceType race = cap.getRace();
                if (!cap.hasChosen() || race == RaceType.NONE) return;

                if (cap.getCooldown() > 0) {
                    int secLeft = (cap.getCooldown() + 19) / 20;
                    player.displayClientMessage(
                            Component.literal("⏳ Способность перезаряжается: " + secLeft + " сек.").withStyle(ChatFormatting.RED),
                            true
                    );
                    return;
                }

                executeRaceAbility(player, race);
                cap.setCooldown(race.cooldownSeconds * 20);
                player.displayClientMessage(
                        Component.literal("✦ Использована способность расы: " + race.displayName).withStyle(ChatFormatting.GOLD),
                        true
                );
            });
        });
        return true;
    }

    private static void executeRaceAbility(ServerPlayer p, RaceType race) {
        ServerLevel level = p.serverLevel();
        Vec3 look = p.getLookAngle();

        switch (race) {
            case HUMAN -> {
                p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                p.removeEffect(MobEffects.WEAKNESS);
                p.removeEffect(MobEffects.POISON);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                level.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
            }
            case ELYTRIAN -> {
                p.setDeltaMovement(look.x * 0.8, 1.35, look.z * 0.8);
                p.hurtMarked = true;
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0));
                level.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY(), p.getZ(), 25, 0.4, 0.2, 0.4, 0.15);
                level.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0f, 1.2f);
            }
            case PHANTOM -> {
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                level.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
                level.playSound(null, p.blockPosition(), SoundEvents.PHANTOM_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.9f);
            }
            case ARACHNID -> {
                List<LivingEntity> targets = getEnemiesAround(p, 5.0);
                for (LivingEntity t : targets) {
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
                    t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    BlockPos bp = t.blockPosition();
                    if (level.getBlockState(bp).isAir()) {
                        level.setBlockAndUpdate(bp, Blocks.COBWEB.defaultBlockState());
                    }
                }
                level.playSound(null, p.blockPosition(), SoundEvents.SPIDER_STEP, SoundSource.PLAYERS, 1.2f, 0.8f);
            }
            case SHULK -> {
                p.openMenu(new SimpleMenuProvider(
                        (id, inv, pl) -> ChestMenu.threeRows(id, inv, pl.getEnderChestInventory()),
                        Component.literal("Панцирь Шалкера")
                ));
                level.playSound(null, p.blockPosition(), SoundEvents.SHULKER_OPEN, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            case FELINE -> {
                p.setDeltaMovement(look.x * 1.6, 0.45, look.z * 1.6);
                p.hurtMarked = true;
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 1));
                level.playSound(null, p.blockPosition(), SoundEvents.CAT_HISS, SoundSource.PLAYERS, 1.0f, 1.1f);
            }
            case ELF -> {
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 1));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                level.sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.5, p.getZ(), 40, 0.6, 0.6, 0.6, 0.2);
                level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.0f);
            }
            case DARK_ELF -> {
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1));
                for (LivingEntity t : getEnemiesAround(p, 7.0)) {
                    t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    t.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                level.sendParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1, p.getZ(), 35, 1.2, 0.5, 1.2, 0.05);
                level.playSound(null, p.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
            case ORC -> {
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 1));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1));
                for (LivingEntity t : getEnemiesAround(p, 4.5)) {
                    Vec3 diff = t.position().subtract(p.position()).normalize().scale(1.1);
                    t.setDeltaMovement(diff.x, 0.4, diff.z);
                }
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, p.getX(), p.getY() + 1.5, p.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                level.playSound(null, p.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0f, 0.9f);
            }
            case DWARF -> {
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
                p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 2));
                level.playSound(null, p.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8f, 0.8f);
            }
            case GOBLIN -> {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 3));
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120, 0));
                level.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 0.5, p.getZ(), 30, 0.4, 0.4, 0.4, 0.05);
                level.playSound(null, p.blockPosition(), SoundEvents.WITCH_CELEBRATE, SoundSource.PLAYERS, 1.0f, 1.4f);
            }
            case VAMPIRE -> {
                List<LivingEntity> targets = getEnemiesAround(p, 5.5);
                float totalDrained = 0f;
                for (LivingEntity t : targets) {
                    t.hurt(p.damageSources().playerAttack(p), 4.0f);
                    totalDrained += 3.0f;
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, t.getX(), t.getY() + 1, t.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
                }
                if (totalDrained > 0) {
                    float missing = p.getMaxHealth() - p.getHealth();
                    if (missing > 0) {
                        p.heal(Math.min(missing, totalDrained));
                        totalDrained -= missing;
                    }
                    if (totalDrained > 0) {
                        p.setAbsorptionAmount(Math.min(6.0f, p.getAbsorptionAmount() + totalDrained));
                    }
                }
                level.playSound(null, p.blockPosition(), SoundEvents.PHANTOM_BITE, SoundSource.PLAYERS, 1.0f, 0.7f);
            }
            case WEREWOLF -> {
                p.setDeltaMovement(look.x * 1.3, 0.35, look.z * 1.3);
                p.hurtMarked = true;
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                level.playSound(null, p.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1.5f, 0.7f);
            }
            case NEPHILIM -> {
                AABB area = p.getBoundingBox().inflate(7.0);
                for (Player ally : level.getEntitiesOfClass(Player.class, area)) {
                    ally.heal(8.0f);
                    ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));
                }
                for (LivingEntity e : getEnemiesAround(p, 7.0)) {
                    if (e.getMobType() == MobType.UNDEAD) {
                        e.setSecondsOnFire(8);
                        e.hurt(p.damageSources().magic(), 8.0f);
                    }
                }
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 45, 1.5, 1.0, 1.5, 0.1);
                level.playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 1.3f);
            }
            case GIANT -> {
                for (LivingEntity t : getEnemiesAround(p, 6.0)) {
                    t.hurt(p.damageSources().playerAttack(p), 8.0f);
                    t.setDeltaMovement(t.getDeltaMovement().x, 0.85, t.getDeltaMovement().z);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 6, 1.5, 0.1, 1.5, 0.0);
                level.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.7f);
            }
            case DRAGONBORN -> {
                for (int i = 1; i <= 7; i++) {
                    Vec3 point = p.getEyePosition().add(look.scale(i));
                    level.sendParticles(ParticleTypes.FLAME, point.x, point.y - 0.2, point.z, 12, 0.35, 0.35, 0.35, 0.04);
                    AABB hitBox = new AABB(point.x - 1.2, point.y - 1.2, point.z - 1.2, point.x + 1.2, point.y + 1.2, point.z + 1.2);
                    for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != p)) {
                        t.setSecondsOnFire(7);
                        t.hurt(p.damageSources().onFire(), 6.0f);
                    }
                }
                level.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2f, 0.8f);
            }
            case LIZARDFOLK -> {
                p.clearFire();
                p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                p.removeEffect(MobEffects.WEAKNESS);
                p.removeEffect(MobEffects.WITHER);
                p.heal(6.0f);
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1));
                level.playSound(null, p.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            case HIGHLANDER -> {
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
                for (LivingEntity t : getEnemiesAround(p, 4.0)) {
                    t.hurt(p.damageSources().playerAttack(p), 9.0f);
                }
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + look.x, p.getY() + 1.0, p.getZ() + look.z, 6, 1.0, 0.2, 1.0, 0.0);
                level.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.8f);
            }
            case MERFOLK -> {
                p.clearFire();
                p.heal(6.0f);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
                for (LivingEntity t : getEnemiesAround(p, 5.5)) {
                    Vec3 push = t.position().subtract(p.position()).normalize().scale(1.4);
                    t.setDeltaMovement(push.x, 0.4, push.z);
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                }
                level.sendParticles(ParticleTypes.SPLASH, p.getX(), p.getY() + 1, p.getZ(), 50, 1.5, 0.5, 1.5, 0.2);
                level.playSound(null, p.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            case SHADOWBORN -> {
                Vec3 start = p.getEyePosition();
                Vec3 end = start.add(look.scale(12.0));
                BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
                Vec3 targetPos = hit.getLocation().subtract(look.scale(0.6));
                level.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 30, 0.3, 0.6, 0.3, 0.2);
                p.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                p.fallDistance = 0.0f;
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0));
                level.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.9f);
            }
            default -> {}
        }
    }

    private static List<LivingEntity> getEnemiesAround(ServerPlayer player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
    }
}