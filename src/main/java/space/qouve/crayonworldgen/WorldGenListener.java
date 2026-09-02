package space.qouve.crayonworldgen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import space.qouve.crayonworldgen.models.PendingSpawn;

import java.util.List;

/**
 * Listens for chunk load events to trigger pending structure placement.
 */
public class WorldGenListener implements Listener {

    private final CrayonWorldGen plugin;
    private final StructureSpawnProcessor processor;

    public WorldGenListener(CrayonWorldGen plugin) {
        this.plugin = plugin;
        this.processor = new StructureSpawnProcessor(plugin);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        String worldName = event.getWorld().getName();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();

        List<PendingSpawn> pendingSpawns = plugin.getSpawnQueue().pollSpawns(worldName, chunkX, chunkZ);
        if (pendingSpawns == null || pendingSpawns.isEmpty()) {
            return;
        }

        for (PendingSpawn spawn : pendingSpawns) {
            processor.processSpawn(spawn);
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        event.getWorld().getPopulators().add(new StructurePopulator(plugin));
    }
}