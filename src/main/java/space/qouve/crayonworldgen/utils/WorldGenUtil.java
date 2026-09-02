package space.qouve.crayonworldgen.utils;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import space.qouve.crayonworldgen.CrayonWorldGen;
import space.qouve.crayonworldgen.models.BiomeConfig;
import space.qouve.crayonworldgen.models.StructureRecord;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenConfig;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility helper methods for configuration parsing and PDC interactions.
 */
public final class WorldGenUtil {

    private WorldGenUtil() {
    }

    public static StructureRotation getRandomRotation() {
        StructureRotation[] rotations = StructureRotation.values();
        return rotations[ThreadLocalRandom.current().nextInt(rotations.length)];
    }

    public static String normalizeBiomeKey(String rawKey) {
        if (rawKey == null) {
            return null;
        }
        String lower = rawKey.toLowerCase();
        int colonIndex = lower.indexOf(':');
        return colonIndex >= 0 ? lower.substring(colonIndex + 1) : lower;
    }

    public static WorldGenConfig loadConfig(String key, ConfigurationSection section, File dataFolder) {
        String schematicPath = section.getString("schematic", section.getString("file"));
        File schematicFile = new File(dataFolder, "schematics/" + schematicPath);

        double globalChance = section.getDouble("chance", 100.0);
        List<String> worlds = section.getStringList("worlds");
        List<String> biomes = section.getStringList("biomes").stream()
                .map(WorldGenUtil::normalizeBiomeKey)
                .toList();
        boolean enabled = section.getBoolean("enabled", true);
        boolean globalOverrideAir = section.getBoolean("override-air", false);
        int attemptsPerChunk = Math.max(1, section.getInt("attempts-per-chunk", 4));

        String rotationStr = section.getString("rotation", "NONE").toUpperCase();
        StructureRotation rotation;
        if ("RANDOM".equals(rotationStr)) {
            rotation = getRandomRotation();
        } else {
            try {
                rotation = StructureRotation.valueOf(rotationStr);
            } catch (IllegalArgumentException e) {
                rotation = StructureRotation.NONE;
            }
        }

        Map<WorldGenBehavior, ConfigurationSection> globalBehaviors = parseBehaviorList(
                section.getMapList("behaviors")
        );

        ConfigurationSection globalBehaviorsSec = section.getConfigurationSection("global-behaviors");
        if (globalBehaviorsSec != null) {
            for (String behaviorKey : globalBehaviorsSec.getKeys(false)) {
                WorldGenBehavior behavior = WorldGenBehavior.byId(behaviorKey);
                if (behavior != null) {
                    globalBehaviors.put(behavior, globalBehaviorsSec.getConfigurationSection(behaviorKey));
                }
            }
        }

        Map<String, BiomeConfig> biomeConfigsMap = new LinkedHashMap<>();

        ConfigurationSection biomesSec = section.getConfigurationSection("biome-behaviors");
        if (biomesSec == null) {
            biomesSec = section.getConfigurationSection("biome-configs");
        }

        if (biomesSec != null) {
            for (String rawBiomeKey : biomesSec.getKeys(false)) {
                ConfigurationSection biomeSubSec = biomesSec.getConfigurationSection(rawBiomeKey);
                if (biomeSubSec != null) {
                    double biomeChance = biomeSubSec.getDouble("chance", globalChance);
                    boolean biomeOverrideAir = biomeSubSec.getBoolean("override-air", globalOverrideAir);
                    int biomeAttempts = biomeSubSec.getInt("attempts-per-chunk", attemptsPerChunk);

                    Map<WorldGenBehavior, ConfigurationSection> resolvedBiomeBehaviors = new LinkedHashMap<>(globalBehaviors);
                    Map<WorldGenBehavior, ConfigurationSection> specificBehaviors = parseBehaviorList(
                            biomeSubSec.getMapList("behaviors")
                    );
                    resolvedBiomeBehaviors.putAll(specificBehaviors);

                    String normalizedBiomeKey = normalizeBiomeKey(rawBiomeKey);
                    biomeConfigsMap.put(normalizedBiomeKey, new BiomeConfig(biomeChance, biomeOverrideAir, resolvedBiomeBehaviors, biomeAttempts));
                }
            }
        }

        for (String biome : biomes) {
            biomeConfigsMap.putIfAbsent(biome, new BiomeConfig(globalChance, globalOverrideAir, globalBehaviors, attemptsPerChunk));
        }

        return new WorldGenConfig(
                key,
                schematicFile,
                rotation,
                globalChance,
                worlds,
                biomes,
                biomeConfigsMap,
                globalBehaviors,
                globalOverrideAir,
                enabled,
                section,
                attemptsPerChunk
        );
    }

    public static Map<WorldGenBehavior, ConfigurationSection> parseBehaviorList(List<Map<?, ?>> behaviorList) {
        Map<WorldGenBehavior, ConfigurationSection> map = new LinkedHashMap<>();
        if (behaviorList == null) {
            return map;
        }

        for (Map<?, ?> entry : behaviorList) {
            for (Map.Entry<?, ?> mapEntry : entry.entrySet()) {
                String id = String.valueOf(mapEntry.getKey());
                WorldGenBehavior behavior = WorldGenBehavior.byId(id);

                if (behavior != null) {
                    ConfigurationSection behaviorSec = null;
                    Object rawValue = mapEntry.getValue();

                    if (rawValue instanceof Map<?, ?> innerMap) {
                        MemoryConfiguration memConfig = new MemoryConfiguration();
                        for (Map.Entry<?, ?> innerEntry : innerMap.entrySet()) {
                            memConfig.set(String.valueOf(innerEntry.getKey()), innerEntry.getValue());
                        }
                        behaviorSec = memConfig;
                    }

                    map.put(behavior, behaviorSec);
                }
            }
        }
        return map;
    }

    /**
     * Stores the spawned structure record directly into the World's PersistentDataContainer
     * so it persists permanently across server restarts and can be instantly queried.
     */
    public static void saveToPDC(CrayonWorldGen plugin, World world, BlockVector3 pos, String key) {
        PersistentDataContainer pdc = world.getPersistentDataContainer();
        NamespacedKey pdcKey = new NamespacedKey(plugin, "structure_records");

        List<String> records = new ArrayList<>();
        if (pdc.has(pdcKey, PersistentDataType.LIST.strings())) {
            List<String> retrieved = pdc.get(pdcKey, PersistentDataType.LIST.strings());
            if (retrieved != null) {
                records = new ArrayList<>(retrieved);
            }
        }

        StructureRecord record = new StructureRecord(
                UUID.randomUUID(),
                key,
                System.currentTimeMillis(),
                pos.x(), pos.y(), pos.z()
        );

        records.add(record.serialize());
        pdc.set(pdcKey, PersistentDataType.LIST.strings(), records);
    }
}