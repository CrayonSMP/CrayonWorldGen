package space.qouve.crayonworldgen;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.BiomeConfig;
import space.qouve.crayonworldgen.models.PendingSpawn;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;
import space.qouve.crayonworldgen.utils.WorldEditUtil;
import space.qouve.crayonworldgen.utils.WorldGenUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Handles paste processing and behavior execution for pending structure spawns.
 */
public class StructureSpawnProcessor {

    private final CrayonWorldGen plugin;

    public StructureSpawnProcessor(CrayonWorldGen plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes placement logic and behavior chain for a single pending spawn.
     */
    public void processSpawn(PendingSpawn spawn) {
        String key = spawn.config().structureKey();
        World world = plugin.getServer().getWorld(spawn.worldName());
        if (world == null) {
            return;
        }

        Clipboard clipboard;
        try {
            clipboard = WorldEditUtil.loadSchematic(spawn.config().schematicFile());
        } catch (IOException e) {
            return;
        }

        WorldGenContext context = new WorldGenContext(world, spawn.position());

        // Consistently retrieve biome key
        String biomeKey = world.getBiome(context.origin().x(), context.origin().y(), context.origin().z())
                .getKey().getKey().toLowerCase();

        BiomeConfig biomeConfig = spawn.config().biomeConfigs().get(biomeKey);
        Map<WorldGenBehavior, ConfigurationSection> behaviors = (biomeConfig != null && biomeConfig.behaviors() != null)
                ? biomeConfig.behaviors()
                : spawn.config().globalBehaviors();

        // Execute behavior chain ONCE prior to schematic paste
        if (behaviors != null) {
            for (Map.Entry<WorldGenBehavior, ConfigurationSection> entry : behaviors.entrySet()) {
                if (!entry.getKey().execute(context, entry.getValue())) {
                    return;
                }
            }
        }

        boolean overrideAir = (biomeConfig != null) ? biomeConfig.overrideAir() : spawn.config().overrideAir();
        Extent extent = WorldEditUtil.pasteSchematic(
                world,
                clipboard,
                context.origin(),
                spawn.config().rotation(),
                !overrideAir
        );

        if (extent == null) {
            return;
        }

        context.setExtent(extent);

        // Save position to persistent world data
        WorldGenUtil.saveToPDC(plugin, world, context.origin(), key);
    }
}