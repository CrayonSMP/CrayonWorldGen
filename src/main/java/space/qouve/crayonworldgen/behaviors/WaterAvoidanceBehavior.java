package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;

/**
 * Prevents structure placement if water is present within or near the structure.
 * Supports buffer zones around the structure.
 */
public class WaterAvoidanceBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        if (config == null) {
            return checkForWater(context, false, false, 0, false);
        }

        boolean avoidWaterSources = config.getBoolean("avoid-water-sources", true);
        boolean avoidFlowingWater = config.getBoolean("avoid-flowing-water", false);
        int bufferRadius = config.getInt("buffer-radius", 0);
        boolean checkGroundOnly = config.getBoolean("check-ground-only", false);

        return checkForWater(context, avoidWaterSources, avoidFlowingWater, bufferRadius, checkGroundOnly);
    }

    private boolean checkForWater(WorldGenContext context, boolean avoidSources, boolean avoidFlowing, int bufferRadius, boolean checkGroundOnly) {
        Extent extent = context.extent();
        if (extent == null) {
            return true;
        }

        BlockVector3 min = extent.getMinimumPoint();
        BlockVector3 max = extent.getMaximumPoint();

        int startX = min.x() - bufferRadius;
        int startZ = min.z() - bufferRadius;
        int endX = max.x() + bufferRadius;
        int endZ = max.z() + bufferRadius;
        int startY = checkGroundOnly ? context.world().getMinHeight() : min.y() - bufferRadius;
        int endY = checkGroundOnly ? context.world().getMaxHeight() : max.y() + bufferRadius;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockVector3 pos = BlockVector3.at(x, y, z);

                    if (checkGroundOnly) {
                        // Prüfe nur die unterste Schicht der Struktur
                        if (y > min.y() && y > max.y() - 2) {
                            continue;
                        }
                    }

                    BlockState state = extent.getBlock(pos);
                    BlockType blockType = state.getBlockType();

                    if (isWater(blockType, avoidSources, avoidFlowing)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isWater(BlockType blockType, boolean avoidSources, boolean avoidFlowing) {
        String blockId = blockType.id();

        if (avoidSources && blockType == BlockType.REGISTRY.get("minecraft:water")) {
            return true;
        }

        if (avoidFlowing && blockType == BlockType.REGISTRY.get("minecraft:flowing_water")) {
            return true;
        }

        if (avoidSources && (blockId.equals("minecraft:water") || blockId.equals("water"))) {
            return true;
        }

        if (avoidFlowing && (blockId.equals("minecraft:flowing_water") || blockId.equals("flowing_water"))) {
            return true;
        }

        return false;
    }
}