package space.qouve.crayonworldgen.services;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import space.qouve.crayonworldgen.CrayonWorldGen;
import space.qouve.crayonworldgen.models.StructureRecord;
import space.qouve.crayonworldgen.models.WorldGenConfig;
import space.qouve.crayonworldgen.utils.WorldGenUtil;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class WorldGenService {

    private final CrayonWorldGen plugin;
    private final Map<String, WorldGenConfig> configs = new HashMap<>();

    public WorldGenService(CrayonWorldGen plugin) {
        this.plugin = plugin;
    }

    public void loadConfigurations() {
        configs.clear();
        File structuresDir = new File(plugin.getDataFolder(), "structures");
        File schematicsDir = new File(plugin.getDataFolder(), "schematics");

        if (!structuresDir.exists()) {
            structuresDir.mkdirs();
            if (plugin.getResource("structures/example.yml") != null) {
                plugin.saveResource("structures/example.yml", false);
            }
        }

        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
            if (plugin.getResource("schematics/example.schem") != null) {
                plugin.saveResource("schematics/example.schem", false);
            }
        }

        List<File> configFiles = new ArrayList<>();
        collectYamlFiles(structuresDir, configFiles);

        for (File file : configFiles) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

                for (String key : yaml.getKeys(false)) {
                    var section = yaml.getConfigurationSection(key);
                    if (section != null) {
                        WorldGenConfig config = WorldGenUtil.loadConfig(
                                key, section, plugin.getDataFolder()
                        );
                        if (config != null && config.enabled()) {
                            configs.put(key, config);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load structure config: " + file.getName(), e);
            }
        }
        plugin.getLogger().info("Loaded " + configs.size() + " world generation structures.");
    }

    private void collectYamlFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectYamlFiles(file, fileList);
            } else if (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")) {
                fileList.add(file);
            }
        }
    }

    public List<String> getRegisteredStructures() {
        return new ArrayList<>(configs.keySet());
    }

    public String getStructureInfo(String id) {
        WorldGenConfig config = configs.get(id);
        if (config == null) {
            return null;
        }
        return config.toFormattedInfo();
    }

    /**
     * Retrieves all structure records saved in the specified World's PersistentDataContainer.
     */
    public List<StructureRecord> getStructureRecords(World world) {
        if (world == null) {
            return Collections.emptyList();
        }

        PersistentDataContainer pdc = world.getPersistentDataContainer();
        NamespacedKey pdcKey = new NamespacedKey(plugin, "structure_records");

        if (!pdc.has(pdcKey, PersistentDataType.LIST.strings())) {
            return Collections.emptyList();
        }

        List<String> rawRecords = pdc.get(pdcKey, PersistentDataType.LIST.strings());
        if (rawRecords == null) {
            return Collections.emptyList();
        }

        List<StructureRecord> records = new ArrayList<>();
        for (String raw : rawRecords) {
            StructureRecord record = StructureRecord.deserialize(raw);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    /**
     * Searches for the nearest previously-generated instance of a structure by reading
     * the persistent data directly from the world container.
     */
    public Location locateStructure(World world, Location origin, String id) {
        if (world == null || origin == null || id == null) {
            return null;
        }

        List<StructureRecord> records = getStructureRecords(world);
        StructureRecord closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (StructureRecord record : records) {
            if (!record.key().equalsIgnoreCase(id)) {
                continue;
            }

            double distSq = squaredDistance(origin, record);
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = record;
            }
        }

        if (closest == null) {
            return null;
        }

        return new Location(world, closest.x() + 0.5, closest.y(), closest.z() + 0.5);
    }

    private double squaredDistance(Location origin, StructureRecord record) {
        double dx = origin.getX() - record.x();
        double dy = origin.getY() - record.y();
        double dz = origin.getZ() - record.z();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Aggregates spawn metrics directly from the PersistentDataContainers of all loaded worlds.
     */
    public Map<String, Integer> getSpawnCounts() {
        Map<String, Integer> spawnCounts = new HashMap<>();
        for (World world : plugin.getServer().getWorlds()) {
            List<StructureRecord> records = getStructureRecords(world);
            for (StructureRecord record : records) {
                spawnCounts.merge(record.key(), 1, Integer::sum);
            }
        }
        return Collections.unmodifiableMap(spawnCounts);
    }

    public Map<String, WorldGenConfig> getConfigs() {
        return Collections.unmodifiableMap(configs);
    }
}