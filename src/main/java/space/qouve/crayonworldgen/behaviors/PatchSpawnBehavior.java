package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.World;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.SchematicRotation;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenBehaviors;
import space.qouve.crayonworldgen.models.WorldGenContext;
import space.qouve.crayonworldgen.utils.WorldEditUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PatchSpawnBehavior implements WorldGenBehavior {

    public PatchSpawnBehavior() {
    }

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        if (config == null || context == null) {
            return true;
        }

        // REKURSIONS-SCHUTZ: Verhindert Endlosschleifen bei Sub-Strukturen
        if (context.isPatch()) {
            return true;
        }

        World world = context.world();
        Extent extent = context.extent();
        if (world == null || extent == null) {
            return true;
        }

        int radius = config.getInt("radius", 10);
        int minAmount = config.getInt("min-amount", 3);
        int maxAmount = config.getInt("max-amount", 4);
        int amount = ThreadLocalRandom.current().nextInt(minAmount, maxAmount + 1);

        List<String> schematicNames = config.getStringList("structures");
        if (schematicNames.isEmpty()) {
            return true;
        }

        boolean overrideAir = config.getBoolean("override-air", false);
        boolean randomRotation = config.getBoolean("random-rotation", true);

        BlockVector3 min = extent.getMinimumPoint();
        BlockVector3 max = extent.getMaximumPoint();
        int centerX = (min.x() + max.x()) / 2;
        int centerZ = (min.z() + max.z()) / 2;

        File folder = new File("plugins/CrayonWorldGen/structures");
        ConfigurationSection globalBehaviorsSection = config.getParent();

        for (int i = 0; i < amount; i++) {
            int rx = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int rz = ThreadLocalRandom.current().nextInt(-radius, radius + 1);

            int targetX = centerX + rx;
            int targetZ = centerZ + rz;
            int targetY = world.getHighestBlockYAt(targetX, targetZ);

            String selectedSchem = schematicNames.get(ThreadLocalRandom.current().nextInt(schematicNames.size()));
            File file = new File(folder, selectedSchem);
            if (!file.exists()) {
                file = new File(selectedSchem);
            }
            if (!file.exists()) {
                continue;
            }

            try {
                Clipboard clipboard = WorldEditUtil.loadSchematic(file);

                SchematicRotation rotation = SchematicRotation.NONE;
                if (randomRotation) {
                    SchematicRotation[] rotations = SchematicRotation.values();
                    rotation = rotations[ThreadLocalRandom.current().nextInt(rotations.length)];
                }

                BlockVector3 patchOrigin = BlockVector3.at(targetX, targetY, targetZ);

                // Erzeugt isolierten Sub-Kontext (isPatch = true)
                WorldGenContext patchContext = context.createPatchContext(patchOrigin, clipboard);

                // Statischer Abruf aller Behaviors über WorldGenBehaviors.getById(key)
                boolean cancelPlacement = false;
                if (globalBehaviorsSection != null) {
                    for (String key : globalBehaviorsSection.getKeys(false)) {
                        if (key.equalsIgnoreCase("patch-spawn") || key.equalsIgnoreCase("patch")) {
                            continue;
                        }

                        WorldGenBehavior behavior = WorldGenBehaviors.getById(key);
                        if (behavior != null) {
                            ConfigurationSection behaviorConfig = globalBehaviorsSection.getConfigurationSection(key);
                            boolean success = behavior.execute(patchContext, behaviorConfig);
                            if (!success) {
                                cancelPlacement = true;
                                break;
                            }
                        }
                    }
                }

                if (cancelPlacement) {
                    continue;
                }

                // Platzieren der Sub-Struktur
                WorldEditUtil.pasteSchematic(
                        world,
                        clipboard,
                        patchContext.origin(),
                        rotation,
                        !overrideAir
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}