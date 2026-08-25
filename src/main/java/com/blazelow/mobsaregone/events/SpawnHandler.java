package com.blazelow.mobsaregone.events;

import com.blazelow.mobsaregone.MobsAreGone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.List;

@EventBusSubscriber(modid = MobsAreGone.MOD_ID)
public class SpawnHandler {

    /**
     * Blocks normal mob spawning.
     *
     * This is the primary protection for natural spawning,
     * including mobs created during chunk/world generation.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (MobsAreGone.isBlocked(event.getEntity().getType())) {
            event.setSpawnCancelled(true);
        }
    }

    /**
     * Blocks ANY blacklisted entity from joining the world.
     *
     * This is intentionally not restricted to bosses.
     * It catches entities created through spawn eggs, commands,
     * unusual modded spawning paths, and other entity-creation paths.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();

        if (MobsAreGone.isBlocked(entity.getType())) {
            event.setCanceled(true);
        }
    }

    /**
     * Removes blacklisted entities that may have survived
     * the normal spawn/join events during chunk generation or loading.
     *
     * The cleanup is deliberately delayed because NeoForge warns that
     * ChunkEvent.Load can fire before the chunk reaches FULL status.
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (MobsAreGone.getBlockedCount() == 0) {
            return;
        }

        ChunkAccess chunk = event.getChunk();

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int maxX = chunk.getPos().getMaxBlockX() + 1;
        int maxZ = chunk.getPos().getMaxBlockZ() + 1;

        AABB chunkBounds = new AABB(
                minX,
                level.getMinBuildHeight(),
                minZ,
                maxX,
                level.getMaxBuildHeight(),
                maxZ
        );

        /*
         * Do the actual entity lookup after the chunk-load event has
         * finished. This avoids interacting with the entity/chunk system
         * while the chunk is still being promoted.
         */
        level.getServer().execute(() -> {
            List<Entity> blockedEntities = level.getEntities(
                    (Entity) null,
                    chunkBounds,
                    entity -> MobsAreGone.isBlocked(entity.getType())
            );

            for (Entity entity : blockedEntities) {
                entity.discard();
            }
        });
    }
}
