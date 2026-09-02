package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;

/**
 * Ensures structure origin falls within a configured Y-level range.
 */
public class HeightBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        if (config == null) {
            return true;
        }

        int minY = config.getInt("min", config.getInt("min-y", context.world().getMinHeight()));
        int maxY = config.getInt("max", config.getInt("max-y", context.world().getMaxHeight()));
        int originY = context.origin().y();

        return originY >= minY && originY <= maxY;
    }
}
