package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;

/**
 * Adjusts the context Y-position to the highest world surface block.
 */
public class HighestBlockBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        World world = context.world();
        BlockVector3 origin = context.origin();

        int offset = config != null ? config.getInt("offset", 1) : 1;
        int highestY = world.getHighestBlockYAt(origin.x(), origin.z(), HeightMap.WORLD_SURFACE);
        BlockVector3 adjustedOrigin = BlockVector3.at(origin.x(), highestY + offset, origin.z());

        context.setOrigin(adjustedOrigin);
        return true;
    }
}
