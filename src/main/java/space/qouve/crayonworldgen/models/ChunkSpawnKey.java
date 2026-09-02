package space.qouve.crayonworldgen.models;

import java.util.Objects;

/**
 * Immutable identifier key for a chunk location in a specific world.
 */
public record ChunkSpawnKey(String worldName, int chunkX, int chunkZ) {

    public ChunkSpawnKey {
        Objects.requireNonNull(worldName, "worldName must not be null");
    }
}
