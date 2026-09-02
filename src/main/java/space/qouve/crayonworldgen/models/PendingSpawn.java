package space.qouve.crayonworldgen.models;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.Objects;

/**
 * Represents a structure scheduled for placement in a world.
 */
public record PendingSpawn(
        String worldName,
        BlockVector3 position,
        WorldGenConfig config
) {
    public PendingSpawn {
        Objects.requireNonNull(worldName, "worldName must not be null");
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(config, "config must not be null");
    }
}
