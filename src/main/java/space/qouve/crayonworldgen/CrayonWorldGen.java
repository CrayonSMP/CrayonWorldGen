package space.qouve.crayonworldgen;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import space.qouve.crayonworldgen.services.WorldGenService;
import space.qouve.crayonworldgen.utils.StructureSpawnQueue;

/**
 * Main plugin class managing lifecycle initialization and service access.
 */
public final class CrayonWorldGen extends JavaPlugin {

    private WorldGenService service;
    private StructureSpawnQueue spawnQueue;

    @Override
    public void onEnable() {
        this.spawnQueue = new StructureSpawnQueue();
        this.service = new WorldGenService(this);

        this.service.loadConfigurations();

        for (World world : getServer().getWorlds()) {
            world.getPopulators().add(new StructurePopulator(this));
        }

        getServer().getPluginManager().registerEvents(new WorldGenListener(this), this);

        var command = getCommand("crayonworldgen");
        if (command != null) {
            WorldGenCommand executor = new WorldGenCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("CrayonWorldGen enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (spawnQueue != null) {
            spawnQueue.clear();
        }
        getLogger().info("CrayonWorldGen disabled.");
    }

    public WorldGenService getService() {
        return service;
    }

    public StructureSpawnQueue getSpawnQueue() {
        return spawnQueue;
    }
}