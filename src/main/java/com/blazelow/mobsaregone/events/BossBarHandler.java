package com.blazelow.mobsaregone.events;

import com.blazelow.mobsaregone.MobsAreGone;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.server.level.ServerBossEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = MobsAreGone.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class BossBarHandler {

    private static final ResourceKey<Level> THE_END = ResourceKey.create(
        Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_end")
    );

    /**
     * Hide the bar as soon as The End loads.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(THE_END)) return;
        if (!MobsAreGone.isBlocked(EntityType.ENDER_DRAGON)) return;
        hideDragonBar(level);
    }

    /**
     * Keep hiding it every tick in case EnderDragonFight resets visibility.
     * Only runs if the dragon is blacklisted.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(THE_END)) return;
        if (!MobsAreGone.isBlocked(EntityType.ENDER_DRAGON)) return;
        hideDragonBar(level);
    }

    /**
     * Cancel the dragon entity itself if it somehow tries to join.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof EnderDragon dragon
                && MobsAreGone.isBlocked(dragon.getType())) {
            hideDragonBar((ServerLevel) event.getLevel());
            event.setCanceled(true);
        }
    }

    private static void hideDragonBar(ServerLevel level) {
        EnderDragonFight fight = level.getDragonFight();
        if (fight == null) return;
        ServerBossEvent bar = getDragonBossEvent(fight);
        if (bar != null && bar.isVisible()) {
            bar.setVisible(false);
        }
    }

    private static ServerBossEvent getDragonBossEvent(EnderDragonFight fight) {
        try {
            for (Field field : EnderDragonFight.class.getDeclaredFields()) {
                if (field.getType() == ServerBossEvent.class) {
                    field.setAccessible(true);
                    return (ServerBossEvent) field.get(fight);
                }
            }
        } catch (Exception e) {
            MobsAreGone.LOGGER.error("MobsAreGone >> Could not access dragon boss bar field: {}", e.getMessage());
        }
        return null;
    }
}
