package space.qouve.crayonworldgen.models;

import org.bukkit.block.structure.StructureRotation;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.Map;

public record WorldGenConfig(
        String structureKey,
        File schematicFile,
        SchematicRotation rotation,
        double chance,
        List<String> worlds,
        List<String> biomes,
        Map<String, BiomeConfig> biomeConfigs,
        Map<WorldGenBehavior, ConfigurationSection> globalBehaviors,
        boolean overrideAir,
        boolean enabled,
        ConfigurationSection rootConfigSection,
        int attemptsPerChunk
) {
    public String toFormattedInfo() {
        String fileName = schematicFile != null ? schematicFile.getName() : "None";
        String rotationName = rotation != null ? rotation.name() : "NONE";
        String biomesList = (biomes != null && !biomes.isEmpty())
                ? String.join(", ", biomes)
                : "All";

        return String.format(
                "Key: %s\nEnabled: %b\nSchematic: %s\nRotation: %s\nChance: %.4f\nAttempts/Chunk: %d\nOverride Air: %b\nBiomes: %s",
                structureKey,
                enabled,
                fileName,
                rotationName,
                chance,
                attemptsPerChunk,
                overrideAir,
                biomesList
        );
    }
}