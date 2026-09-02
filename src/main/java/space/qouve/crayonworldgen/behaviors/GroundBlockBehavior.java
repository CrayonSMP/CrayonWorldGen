package space.qouve.crayonworldgen.behaviors;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import space.qouve.crayonworldgen.models.WorldGenBehavior;
import space.qouve.crayonworldgen.models.WorldGenContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates that the top solid ground block (e.g., GRASS_BLOCK) at the target X/Z coordinates matches allowed materials.
 */
public class GroundBlockBehavior implements WorldGenBehavior {

    @Override
    public boolean execute(WorldGenContext context, ConfigurationSection config) {
        if (config == null) {
            return true;
        }

        List<String> allowedBlockNames = config.contains("materials")
                ? config.getStringList("materials")
                : config.getStringList("blocks");
        if (allowedBlockNames.isEmpty()) {
            return true;
        }

        Set<Material> allowedMaterials = allowedBlockNames.stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        World world = context.world();
        BlockVector3 origin = context.origin();
        int x = origin.x();
        int z = origin.z();

        // Höchste solide Y-Position an den X/Z-Koordinaten ermitteln
        int highestY = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING);
        Block current = world.getBlockAt(x, highestY, z);

        // Falls auf der Oberfläche nicht-solide Blöcke wie Gras, Blumen oder Schnee liegen,
        // schrittweise nach unten gehen, bis der erste ECHTE solide Block getroffen wird.
        while (current.getY() > world.getMinHeight() && !current.getType().isSolid()) {
            current = current.getRelative(BlockFace.DOWN);
        }

        // 'current' ist jetzt garantiert der oberste solide Block (z. B. GRASS_BLOCK)
        return allowedMaterials.contains(current.getType());
    }
}