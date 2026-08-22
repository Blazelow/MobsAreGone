package com.blazelow.mobsaregone.events;

import com.blazelow.mobsaregone.MobsAreGone;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = MobsAreGone.MOD_ID)
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
     * Final safety net: prevents any blacklisted entity from entering the level,
     * including entities that bypass FinalizeSpawnEvent.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (MobsAreGone.isBlocked(event.getEntity().getType())) {
            event.setCanceled(true);
        }
    }
}
