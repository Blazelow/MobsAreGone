package com.blazelow.mobsaregone.events;

import com.blazelow.mobsaregone.MobsAreGone;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = MobsAreGone.MOD_ID)
public class BossBarHandler {

    private static final ResourceKey<Level> THE_END = ResourceKey.create(
        Registries.DIMENSION,
        Identifier.withDefaultNamespace("the_end")
    );

    /*
     * Cache the reflected field once instead of searching EnderDragonFight's
     * fields every game tick.
     */
    private static final Field DRAGON_BOSS_EVENT_FIELD = findDragonBossEventField();

    /**
     * Hide the Ender Dragon boss bar when The End loads.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(THE_END)) return;
        if (!MobsAreGone.isBlocked(EntityType.ENDER_DRAGON)) return;

        hideDragonBar(level);
    }

    /**
     * If an Ender Dragon somehow attempts to join the level, hide its
     * boss bar immediately. SpawnHandler will cancel the entity itself.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof EnderDragon
                && MobsAreGone.isBlocked(event.getEntity().getType())) {

            if (event.getLevel() instanceof ServerLevel level) {
                hideDragonBar(level);
            }
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
        if (DRAGON_BOSS_EVENT_FIELD == null) {
            return null;
        }

        try {
            return (ServerBossEvent) DRAGON_BOSS_EVENT_FIELD.get(fight);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            MobsAreGone.LOGGER.error(
                "MobsAreGone >> Could not access dragon boss bar: {}",
                e.getMessage()
            );
            return null;
        }
    }

    private static Field findDragonBossEventField() {
        for (Field field : EnderDragonFight.class.getDeclaredFields()) {
            if (field.getType() == ServerBossEvent.class) {
                try {
                    field.setAccessible(true);
                    return field;
                } catch (Exception e) {
                    MobsAreGone.LOGGER.error(
                        "MobsAreGone >> Could not access dragon boss bar field: {}",
                        e.getMessage()
                    );
                    return null;
                }
            }
        }

        MobsAreGone.LOGGER.warn(
            "MobsAreGone >> Could not find EnderDragonFight boss bar field."
        );

        return null;
    }
}
