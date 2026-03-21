package com.blazelow.mobsaregone.config;

import com.blazelow.mobsaregone.MobsAreGone;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class BlockedEntitiesConfig {

    private static final Gson GSON = new Gson();
    private static final String CONFIG_FILE = "mobsaregone-blacklist.json";

    public static Set<EntityType<?>> load() {
        Path configFile = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);

        if (!Files.exists(configFile)) {
            copyDefaultConfig(configFile);
        }

        List<String> ids = readIds(configFile);
        if (ids == null || ids.isEmpty()) return Collections.emptySet();

        Set<EntityType<?>> result = new HashSet<>();
        for (String id : ids) {
            if (id == null || id.isBlank()) continue;
            parseEntityType(id.trim()).ifPresent(result::add);
        }
        return result;
    }

    private static void copyDefaultConfig(Path target) {
        try (InputStream in = BlockedEntitiesConfig.class
                .getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                MobsAreGone.LOGGER.warn("MobsAreGone >> Bundled default config not found.");
                return;
            }
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            MobsAreGone.LOGGER.info("MobsAreGone >> Created default config at: {}", target);
        } catch (IOException e) {
            MobsAreGone.LOGGER.error("MobsAreGone >> Could not write default config: {}", e.getMessage());
        }
    }

    private static List<String> readIds(Path configFile) {
        try (Reader reader = new InputStreamReader(
                new FileInputStream(configFile.toFile()), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            return GSON.fromJson(reader, listType);
        } catch (IOException | JsonParseException e) {
            MobsAreGone.LOGGER.error("MobsAreGone >> Failed to read config: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static Optional<EntityType<?>> parseEntityType(String id) {
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(rl);
            if (type.isEmpty()) {
                MobsAreGone.LOGGER.warn("MobsAreGone >> Unknown entity '{}' — skipping.", id);
            }
            return type;
        } catch (Exception e) {
            MobsAreGone.LOGGER.warn("MobsAreGone >> Invalid entity ID '{}' — skipping.", id);
            return Optional.empty();
        }
    }
}
