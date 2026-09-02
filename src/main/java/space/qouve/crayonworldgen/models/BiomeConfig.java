package space.qouve.crayonworldgen.models;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

/**
 * Holds biome-specific configuration parameters and behavior mappings.
 */
public record BiomeConfig(
        double chance,
        boolean overrideAir,
        Map<WorldGenBehavior, ConfigurationSection> behaviors,
        Integer attemptsPerChunk
) {
}
