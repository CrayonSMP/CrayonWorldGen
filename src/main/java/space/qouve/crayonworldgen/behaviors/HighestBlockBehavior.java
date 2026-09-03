package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;

/**
 * Adjusts the context Y-position to the highest solid ground surface block.
 */
public class HighestBlockBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        World world = context.world();
        BlockVector3 origin = context.origin();

        int offset = config != null ? config.getInt("offset", 0) : 0;
        boolean placeOnSurface = config != null ? config.getBoolean("place-on-surface", true) : true;

        int highestY = world.getHighestBlockYAt(origin.x(), origin.z(), HeightMap.WORLD_SURFACE_WG);
        Block current = world.getBlockAt(origin.x(), highestY, origin.z());

        if (placeOnSurface) {
            while (current.getY() > world.getMinHeight() && !current.getType().isSolid()) {
                current = current.getRelative(BlockFace.DOWN);
            }
        } else {
            while (current.getY() > world.getMinHeight() && current.isEmpty()) {
                current = current.getRelative(BlockFace.DOWN);
            }
        }

        int solidGroundY = current.getY();

        if (current.getType().isSolid()) {
            solidGroundY = current.getY() + 1;
        }

        int finalY = solidGroundY + offset;

        if (finalY < world.getMinHeight()) {
            finalY = world.getMinHeight();
        }

        BlockVector3 adjustedOrigin = BlockVector3.at(origin.x(), finalY, origin.z());
        context.setOrigin(adjustedOrigin);


        return true;
    }
}