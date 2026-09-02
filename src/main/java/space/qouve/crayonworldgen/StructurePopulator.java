package space.qouve.crayonworldgen;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import space.qouve.crayonworldgen.models.BiomeConfig;
import space.qouve.crayonworldgen.models.PendingSpawn;
import space.qouve.crayonworldgen.models.WorldGenConfig;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles structure spawn calculations during chunk generation populator phase.
 */
public class StructurePopulator extends BlockPopulator {

    private final CrayonWorldGen plugin;
    private final StructureSpawnProcessor processor;

    public StructurePopulator(CrayonWorldGen plugin) {
        this.plugin = plugin;
        this.processor = new StructureSpawnProcessor(plugin);
    }

    @Override
    public void populate(World world, Random random, Chunk source) {
        int chunkX = source.getX();
        int chunkZ = source.getZ();

        for (WorldGenConfig config : plugin.getService().getConfigs().values()) {
            String key = config.structureKey();

            if (!config.enabled()) {
                continue;
            }

            // Check if the structure is allowed in this world
            if (config.worlds() != null && !config.worlds().isEmpty() && !config.worlds().contains(world.getName())) {
                continue;
            }

            Biome centerBiome = source.getBlock(8, 64, 8).getBiome();
            String biomeKey = centerBiome.getKey().getKey().toLowerCase();

            // If a biome whitelist is configured, only proceed when the current biome is in it.
            if (config.biomes() == null || config.biomes().isEmpty() || !config.biomes().contains(biomeKey)) {
                continue;
            }

            BiomeConfig biomeConfig = config.biomeConfigs().get(biomeKey);
            double chance = (biomeConfig != null) ? biomeConfig.chance() : config.chance();
            int attempts = (biomeConfig != null && biomeConfig.attemptsPerChunk() != null)
                    ? biomeConfig.attemptsPerChunk()
                    : config.attemptsPerChunk();

            for (int attempt = 0; attempt < attempts; attempt++) {
                double roll = ThreadLocalRandom.current().nextDouble(100.0);
                if (roll < chance) {
                    int x = (chunkX << 4) + ThreadLocalRandom.current().nextInt(16);
                    int z = (chunkZ << 4) + ThreadLocalRandom.current().nextInt(16);
                    int y = world.getHighestBlockYAt(x, z);

                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    PendingSpawn spawn = new PendingSpawn(world.getName(), pos, config);


                    // Schedule immediate processing on the main server thread for newly generated chunks
                    Bukkit.getScheduler().runTask(plugin, () -> processor.processSpawn(spawn));
                }
            }
        }
    }
}