package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;

public class FlatSurfaceBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        World world = context.world();
        BlockVector3 origin = context.origin();

        int width = config != null ? config.getInt("width", 4) : 4;

        int minOffset = -(width / 2);
        int maxOffset = minOffset + width - 1;

        Integer referenceSurfaceY = null;

        for (int xOffset = minOffset; xOffset <= maxOffset; xOffset++) {
            for (int zOffset = minOffset; zOffset <= maxOffset; zOffset++) {
                int checkX = origin.x() + xOffset;
                int checkZ = origin.z() + zOffset;

                int solidY = getSolidGroundY(world, checkX, checkZ);

                if (solidY == -1) {
                    return false;
                }

                if (referenceSurfaceY == null) {
                    referenceSurfaceY = solidY;
                } else if (solidY != referenceSurfaceY) {
                    return false;
                }
            }
        }

        return true;
    }

    private int getSolidGroundY(World world, int x, int z) {
        int highestY = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING);
        Block current = world.getBlockAt(x, highestY, z);

        while (current.getY() > world.getMinHeight() && !current.getType().isSolid()) {
            current = current.getRelative(BlockFace.DOWN);
        }

        return current.getType().isSolid() ? current.getY() : -1;
    }
}