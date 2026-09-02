package space.qouve.crayonworldgen.utils;

import space.qouve.crayonworldgen.models.ChunkSpawnKey;
import space.qouve.crayonworldgen.models.PendingSpawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe queue managing pending structure spawns per chunk.
 */
public class StructureSpawnQueue {

    private final Map<ChunkSpawnKey, List<PendingSpawn>> queueMap = new java.util.concurrent.ConcurrentHashMap<>();

    public void addSpawn(String worldName, int chunkX, int chunkZ, PendingSpawn spawn) {
        ChunkSpawnKey key = new ChunkSpawnKey(worldName, chunkX, chunkZ);
        queueMap.computeIfAbsent(key, k -> new ArrayList<>()).add(spawn);
    }

    public List<PendingSpawn> pollSpawns(String worldName, int chunkX, int chunkZ) {
        ChunkSpawnKey key = new ChunkSpawnKey(worldName, chunkX, chunkZ);
        return queueMap.remove(key);
    }

    public void clear() {
        queueMap.clear();
    }
}
