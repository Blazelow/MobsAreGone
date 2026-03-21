package com.blazelow.mobsaregone.events;

import com.blazelow.mobsaregone.MobsAreGone;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = MobsAreGone.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SpawnHandler {

    /**
     * Handles natural/spawner spawns for all regular mobs.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (MobsAreGone.isBlocked(event.getEntity().getType())) {
            event.setSpawnCancelled(true);
        }
    }

    /**
     * Handles boss mobs (Ender Dragon, Wither) which bypass FinalizeSpawnEvent.
     * The boss bar is hidden via the bundled resource pack (transparent textures).
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if ((event.getEntity() instanceof EnderDragon || event.getEntity() instanceof WitherBoss)
                && MobsAreGone.isBlocked(event.getEntity().getType())) {
            event.setCanceled(true);
        }
    }
}
