package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Positions structures in the sky at a random height offset above the surface.
 */
public class SkyStructureBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        if (config == null) {
            return true;
        }

        int minHeightAboveGround = config.getInt("min-height-above-ground", 20);
        int maxHeightAboveGround = config.getInt("max-height-above-ground", 50);

        BlockVector3 origin = context.origin();
        int groundY = context.world().getHighestBlockYAt(origin.x(), origin.z());

        int offset = ThreadLocalRandom.current().nextInt(
                minHeightAboveGround,
                Math.max(minHeightAboveGround + 1, maxHeightAboveGround + 1)
        );

        int targetY = Math.min(context.world().getMaxHeight() - 1, groundY + offset);
        context.setOrigin(BlockVector3.at(origin.x(), targetY, origin.z()));

        return true;
    }
}
