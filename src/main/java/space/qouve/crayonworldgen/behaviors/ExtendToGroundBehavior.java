package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;
import space.qouve.crayonworldgen.utils.WorldEditUtil;

/**
 * Behavior that extends structure support blocks down to solid ground.
 */
public class ExtendToGroundBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        World world = context.world();
        BlockVector3 origin = context.origin();
        Extent extent = context.extent();

        if (extent == null || config == null) {
            return true;
        }

        int maxDepth = config.getInt("max-depth", 10);
        BlockState targetState = WorldEditUtil.parseBlockState(config.getString("block", "DIRT"));

        if (targetState == null) {
            return true;
        }

        int minX = extent.getMinimumPoint().x();
        int maxX = extent.getMaximumPoint().x();
        int minZ = extent.getMinimumPoint().z();
        int maxZ = extent.getMaximumPoint().z();
        int minY = extent.getMinimumPoint().y();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockVector3 bottomPos = BlockVector3.at(x, minY, z);
                BlockState stateAtBottom = extent.getBlock(bottomPos);

                if (!stateAtBottom.getBlockType().getMaterial().isAir()) {
                    extendColumnDown(world, x, minY - 1, z, maxDepth, targetState);
                }
            }
        }
        return true;
    }

    /**
     * Fills blocks downwards until solid ground or max depth is reached.
     */
    private void extendColumnDown(World world, int x, int startY, int z, int maxDepth, BlockState targetState) {
        for (int depth = 0; depth < maxDepth; depth++) {
            int currentY = startY - depth;
            if (currentY < world.getMinHeight()) {
                break;
            }

            if (!world.getBlockAt(x, currentY, z).getType().isAir()) {
                break;
            }

            WorldEditUtil.setBlockState(world, BlockVector3.at(x, currentY, z), targetState);
        }
    }
}
