package com.blazelow.mobsaregone;

import com.blazelow.mobsaregone.command.ReloadCommand;
import com.blazelow.mobsaregone.config.BlockedEntitiesConfig;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mod("mobsaregone")
public class MobsAreGone {

    public static final String MOD_ID = "mobsaregone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Set<EntityType<?>> blockedEntities = Collections.emptySet();

    public MobsAreGone(IEventBus modEventBus) {
        LOGGER.info("MobsAreGone >> Initializing...");
    }

    public static boolean isBlocked(EntityType<?> type) {
        return blockedEntities.contains(type);
    }

    public static void setBlockedEntities(Set<EntityType<?>> entities) {
        blockedEntities = entities;
    }

    public static int getBlockedCount() {
        return blockedEntities.size();
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            LOGGER.info("MobsAreGone >> Loading blocked entities list...");
            blockedEntities = new HashSet<>(BlockedEntitiesConfig.load());
            LOGGER.info("MobsAreGone >> Blocking {} entity type(s).", blockedEntities.size());
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            ReloadCommand.register(event.getDispatcher());
            LOGGER.info("MobsAreGone >> Registered /mobsaregone reload command.");
        }
    }
}
