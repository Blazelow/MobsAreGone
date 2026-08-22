package com.blazelow.mobsaregone.events;

import com.blazelow.mobsaregone.MobsAreGone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.List;

@EventBusSubscriber(
    modid = MobsAreGone.MOD_ID,
    bus = EventBusSubscriber.Bus.GAME
)
public class SpawnHandler {

    /**
     * Blocks normal mob spawning.
     *
     * This handles the normal Minecraft mob spawning pipeline.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (MobsAreGone.isBlocked(event.getEntity().getType())) {
            event.setSpawnCancelled(true);
        }
    }

    /**
     * Blocks entities when they attempt to join the world.
     *
     * This also catches things such as spawn eggs and other
     * entity creation paths that reach EntityJoinLevelEvent.
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
     * Removes blacklisted entities that were created while a chunk
     * was being generated or loaded.
     *
     * Some persistent entities, especially animals such as bees,
     * rabbits, etc., can be created during chunk generation rather
     * than going through the normal FinalizeSpawnEvent path.
     *
     * This only checks the newly loaded chunk, rather than scanning
     * the entire world every tick.
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        // Nothing to do if the blacklist is empty.
        if (MobsAreGone.getBlockedCount() == 0) {
            return;
        }

        LevelChunk chunk = event.getChunk();

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int maxX = chunk.getPos().getMaxBlockX() + 1;
        int maxZ = chunk.getPos().getMaxBlockZ() + 1;

        AABB chunkBounds = new AABB(
            minX,
            level.getMinY(),
            minZ,
            maxX,
            level.getMaxY(),
            maxZ
        );

        List<Entity> blockedEntities = level.getEntities(
            null,
            chunkBounds,
            entity -> MobsAreGone.isBlocked(entity.getType())
        );

        for (Entity entity : blockedEntities) {
            entity.discard();
        }
    }
}
