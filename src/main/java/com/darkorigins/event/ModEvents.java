package com.darkorigins.event;

import com.darkorigins.DarkOriginsMod;
import com.darkorigins.capability.PlayerRaceProvider;
import com.darkorigins.network.PacketHandler;
import com.darkorigins.network.SyncRaceS2CPacket;
import com.darkorigins.race.RaceType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DarkOriginsMod.MODID)
public class ModEvents {
    private static final UUID HEALTH_UUID = UUID.fromString("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d");
    private static final UUID SPEED_UUID  = UUID.fromString("b2c3d4e5-f6a7-4b5c-9d0e-1f2a3b4c5d6e");
    private static final UUID ATTACK_UUID = UUID.fromString("c3d4e5f6-a7b8-4c5d-0e1f-2a3b4c5d6e7f");
    private static final UUID ARMOR_UUID  = UUID.fromString("d4e5f6a7-b8c9-4d5e-1f2a-3b4c5d6e7f8a");
    private static final UUID MANA_UUID   = UUID.fromString("e5f6a7b8-c9d0-4e5f-2a3b-4c5d6e7f8a9b");
    private static final UUID SPELL_UUID  = UUID.fromString("f6a7b8c9-d0e1-4f5a-3b4c-5d6e7f8a9b0c");

    // Команда /race для открытия меню смены расы прямо в мире
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("race")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                                PacketHandler.sendToPlayer(new SyncRaceS2CPacket(cap.getRace(), true), player);
                            });
                            return 1;
                        })
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(s -> s.hasPermission(2))
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    target.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                                        PacketHandler.sendToPlayer(new SyncRaceS2CPacket(cap.getRace(), true), target);
                                    });
                                    return 1;
                                })
                        )
        );
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerRaceProvider.PLAYER_RACE).isPresent()) {
                event.addCapability(new ResourceLocation(DarkOriginsMod.MODID, "properties"), new PlayerRaceProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(oldStore -> {
            event.getEntity().getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(newStore -> {
                newStore.copyFrom(oldStore);
            });
        });
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                if (!cap.hasChosen()) {
                    PacketHandler.sendToPlayer(new SyncRaceS2CPacket(RaceType.NONE, true), player);
                } else {
                    applyRaceAttributes(player, cap.getRace());
                    player.refreshDimensions();
                    PacketHandler.sendToPlayer(new SyncRaceS2CPacket(cap.getRace(), false), player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                applyRaceAttributes(player, cap.getRace());
                player.setHealth(player.getMaxHealth());
                player.refreshDimensions();
                PacketHandler.sendToPlayer(new SyncRaceS2CPacket(cap.getRace(), false), player);
            });
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                RaceType r = cap.getRace();
                player.refreshDimensions();
                PacketHandler.sendToPlayer(new SyncRaceS2CPacket(r, false), player);
                if (r == RaceType.HUMAN || r == RaceType.ORC || r == RaceType.GIANT) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                }
            });
        }
    }

    public static void applyRaceAttributes(Player player, RaceType race) {
        setModifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_UUID, "RaceHealth", race.healthBonus, AttributeModifier.Operation.ADDITION);
        setModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_UUID, "RaceSpeed", race.speedBonus, AttributeModifier.Operation.MULTIPLY_BASE);
        setModifier(player.getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_UUID, "RaceAttack", race.attackBonus, AttributeModifier.Operation.ADDITION);
        setModifier(player.getAttribute(Attributes.ARMOR), ARMOR_UUID, "RaceArmor", race.armorBonus, AttributeModifier.Operation.ADDITION);

        applyIronSpellsAttribute(player, "max_mana", MANA_UUID, switch (race) {
            case ELF -> 150.0;
            case DARK_ELF -> 100.0;
            case NEPHILIM, DRAGONBORN -> 50.0;
            case ORC, GIANT, HIGHLANDER -> -50.0;
            default -> 0.0;
        }, AttributeModifier.Operation.ADDITION);

        applyIronSpellsAttribute(player, "spell_power", SPELL_UUID, switch (race) {
            case ELF -> 0.25;
            case DARK_ELF -> 0.15;
            case NEPHILIM -> 0.15;
            case ORC, GIANT -> -0.30;
            default -> 0.0;
        }, AttributeModifier.Operation.MULTIPLY_BASE);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyIronSpellsAttribute(Player player, String attrPath, UUID uuid, double amount, AttributeModifier.Operation op) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", attrPath));
        if (attr != null) {
            setModifier(player.getAttribute(attr), uuid, "RaceSpellBonus", amount, op);
        }
    }

    private static void setModifier(AttributeInstance instance, UUID uuid, String name, double amount, AttributeModifier.Operation op) {
        if (instance == null) return;
        instance.removeModifier(uuid);
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(uuid, name, amount, op));
        }
    }

    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof Player player) {
            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                float scale = cap.getRace().scale;
                if (scale != 1.0f && cap.hasChosen()) {
                    EntityDimensions base = event.getNewSize();
                    event.setNewSize(base.scale(scale));
                    event.setNewEyeHeight(event.getNewEyeHeight() * scale);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        event.getEntity().getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
            if (cap.getRace() == RaceType.HUMAN) {
                event.getOrb().value = Math.round(event.getOrb().value * 1.25f);
            }
        });
    }

    @SubscribeEvent
    public static void onPotionAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                if (cap.getRace() == RaceType.ELF) {
                    MobEffectInstance effect = event.getEffectInstance();
                    if (effect.getEffect().isBeneficial() && effect.getDuration() < 12000) {
                        effect.update(new MobEffectInstance(
                                effect.getEffect(),
                                (int) (effect.getDuration() * 1.5),
                                Math.min(effect.getAmplifier() + 1, 2),
                                effect.isAmbient(),
                                effect.isVisible()
                        ));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onFoodEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player && event.getItem().isEdible()) {
            FoodProperties food = event.getItem().getFoodProperties(player);
            if (food == null) return;
            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                if (cap.getRace() == RaceType.VAMPIRE && !food.isMeat()) {
                    player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - food.getNutrition()));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player p = event.player;

        p.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
            RaceType race = cap.getRace();
            if (!cap.hasChosen()) return;

            // Отсчёт кулдауна способности на сервере
            if (!p.level().isClientSide && cap.getCooldown() > 0) {
                cap.setCooldown(cap.getCooldown() - 1);
            }

            // Физика каждый тик
            if (race == RaceType.ARACHNID && p.horizontalCollision && !p.isSpectator()) {
                Vec3 motion = p.getDeltaMovement();
                double climbSpeed = p.isCrouching() ? 0.0 : 0.2D;
                p.setDeltaMovement(motion.x, Math.max(motion.y, climbSpeed), motion.z);
                p.fallDistance = 0.0F;
            }

            if (race == RaceType.ELYTRIAN) {
                if (!p.onGround() && !p.isInWater() && p.getDeltaMovement().y < -0.5D && p.isShiftKeyDown()) {
                    Vec3 look = p.getLookAngle();
                    Vec3 vel = p.getDeltaMovement();
                    p.setDeltaMovement(vel.x * 0.95 + look.x * 0.08, -0.18D, vel.z * 0.95 + look.z * 0.08);
                    p.fallDistance = 0.0F;
                }
            }

            if (p.level().isClientSide || p.tickCount % 20 != 0) return;

            // --- ФИКС УПЫРЯ: Лимит 3 доп. сердца (6.0f поглощения) и быстрое спадение ---
            if (race == RaceType.VAMPIRE) {
                float abs = p.getAbsorptionAmount();
                if (abs > 6.0f) {
                    p.setAbsorptionAmount(6.0f);
                } else if (abs > 0.0f && p.tickCount % 40 == 0) {
                    // Теряет по полсердца (1.0f) каждые 2 секунды
                    p.setAbsorptionAmount(Math.max(0.0f, abs - 1.0f));
                }
            }

            BlockPos pos = p.blockPosition();
            Level level = p.level();
            boolean isDay = level.isDay();
            boolean inSun = isDay && level.canSeeSky(pos) && !p.isInWaterRainOrBubble();
            float temp = level.getBiome(pos).value().getBaseTemperature();

            switch (race) {
                case FELINE -> p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
                case VAMPIRE, PHANTOM -> {
                    p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
                    if (inSun && p.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                        p.setSecondsOnFire(3);
                    }
                    if (race == RaceType.PHANTOM && level.getMaxLocalRawBrightness(pos) <= 5) {
                        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
                    }
                }
                case ELYTRIAN -> {
                    if (!level.canSeeSky(pos) && !level.getBlockState(pos.above(2)).isAir() && !level.getBlockState(pos.above(3)).isAir()) {
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));
                    }
                }
                case DARK_ELF -> {
                    p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
                    if (inSun) {
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false));
                    } else if (level.getMaxLocalRawBrightness(pos) <= 5) {
                        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
                    }
                }
                case WEREWOLF -> {
                    if (!isDay) {
                        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
                    } else {
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false));
                    }
                }
                case DWARF -> {
                    p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 1, false, false));
                    p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
                    if (pos.getY() < 55) {
                        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
                    }
                    // Дебафф в воде вместо мгновенного утопления: Замедление II + Утомление III (эффект Стража)
                    if (p.isInWater()) {
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
                        p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 2, false, true));
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
                    }
                }
                case GOBLIN -> p.addEffect(new MobEffectInstance(MobEffects.LUCK, 40, 1, false, false));
                case SHULK -> {
                    p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false));
                    p.causeFoodExhaustion(0.15f);
                }
                case GIANT -> p.causeFoodExhaustion(0.25f);
                case NEPHILIM -> {
                    p.removeEffect(MobEffects.WITHER);
                    if (level.dimension() == Level.NETHER) {
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false));
                    }
                    if (p.tickCount % 80 == 0) {
                        AABB area = p.getBoundingBox().inflate(6.0D);
                        List<Player> allies = level.getEntitiesOfClass(Player.class, area);
                        for (Player ally : allies) {
                            ally.heal(2.0F);
                            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, true));
                        }
                    }
                }
                case DRAGONBORN -> {
                    p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
                    if (temp < 0.15f) {
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false));
                    }
                }
                case LIZARDFOLK -> {
                    p.removeEffect(MobEffects.POISON);
                    if (p.isInWater() || temp > 0.9f) {
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
                    } else if (temp < 0.2f) {
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false));
                    }
                }
                case MERFOLK -> {
                    p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, false, false));
                    if (p.isInWaterRainOrBubble()) {
                        p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
                    } else if (level.dimension() == Level.NETHER || (temp >= 1.5f && inSun)) {
                        p.hurt(p.damageSources().dryOut(), 1.5f);
                    }
                }
                case HIGHLANDER -> {
                    p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false));
                    if (pos.getY() >= 100) {
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, false));
                    }
                }
                case SHADOWBORN -> {
                    int light = level.getMaxLocalRawBrightness(pos);
                    if (light <= 5) {
                        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
                    } else if (light >= 14) {
                        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false));
                        p.hurt(p.damageSources().onFire(), 1.0f);
                    }
                }
                default -> {}
            }
        });
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker) {
            attacker.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                RaceType r = cap.getRace();
                LivingEntity target = event.getEntity();

                switch (r) {
                    case VAMPIRE -> {
                        // Фикс вампиризма: лечит основное ХП, а излишек уходит в Кровавый щит с жёстким лимитом в 3 сердца (6.0 единиц)
                        float vampHeal = event.getAmount() * 0.30f;
                        float missingHealth = attacker.getMaxHealth() - attacker.getHealth();
                        if (missingHealth > 0) {
                            float healNow = Math.min(missingHealth, vampHeal);
                            attacker.heal(healNow);
                            vampHeal -= healNow;
                        }
                        if (vampHeal > 0) {
                            float currentAbs = attacker.getAbsorptionAmount();
                            attacker.setAbsorptionAmount(Math.min(6.0f, currentAbs + vampHeal));
                        }
                    }
                    case ORC -> {
                        if (attacker.getHealth() < attacker.getMaxHealth() * 0.4f) {
                            event.setAmount(event.getAmount() * 1.40f);
                        }
                    }
                    case ARACHNID -> {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
                        BlockPos targetPos = target.blockPosition();
                        if (target.level().getBlockState(targetPos).isAir()) {
                            target.level().setBlockAndUpdate(targetPos, Blocks.COBWEB.defaultBlockState());
                        }
                    }
                    case DRAGONBORN -> target.setSecondsOnFire(4);
                    case HIGHLANDER -> {
                        ItemStack stack = attacker.getMainHandItem();
                        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().toLowerCase();
                        boolean isAxe = stack.getItem() instanceof AxeItem
                                || stack.is(ItemTags.AXES)
                                || itemId.contains("axe")
                                || itemId.contains("halberd")
                                || itemId.contains("greataxe");
                        if (isAxe) {
                            event.setAmount(event.getAmount() * 1.40f);
                        }
                    }
                    case SHADOWBORN -> {
                        if (attacker.isShiftKeyDown() || attacker.level().getMaxLocalRawBrightness(target.blockPosition()) <= 9) {
                            Vec3 behind = target.position().subtract(target.getLookAngle().normalize().scale(1.5));
                            attacker.teleportTo(behind.x, target.getY(), behind.z);
                            event.setAmount(event.getAmount() * 1.25f);
                        }
                    }
                    default -> {}
                }
            });
        }

        if (event.getEntity() instanceof Player victim) {
            victim.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                if (cap.getRace() == RaceType.ELF) {
                    String msgId = event.getSource().getMsgId().toLowerCase();
                    if (event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)
                            || msgId.contains("magic")
                            || msgId.contains("spell")
                            || msgId.contains("indirectmagic")) {
                        event.setAmount(event.getAmount() * 0.5f);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.getCapability(PlayerRaceProvider.PLAYER_RACE).ifPresent(cap -> {
                RaceType r = cap.getRace();
                if (r == RaceType.FELINE || r == RaceType.NEPHILIM || r == RaceType.ELYTRIAN) {
                    event.setCanceled(true);
                } else if (r == RaceType.HIGHLANDER) {
                    event.setDistance(Math.max(0, event.getDistance() - 3.0f));
                }
            });
        }
    }
}