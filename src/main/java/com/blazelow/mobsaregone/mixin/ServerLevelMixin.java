package com.blazelow.mobsaregone.mixin;

import com.blazelow.mobsaregone.MobsAreGone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    // Filters blacklisted entities from world-generation loading.
    @Redirect(
        method = "addWorldGenChunkEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;addWorldGenChunkEntities(Ljava/util/stream/Stream;)V"
        )
    )
    private void mobsAreGone$filterWorldGenEntities(
            PersistentEntitySectionManager<Entity> entityManager,
            Stream<Entity> entities) {

        entityManager.addWorldGenChunkEntities(
            entities.filter(entity -> !MobsAreGone.isBlocked(entity.getType()))
        );
    }
}
