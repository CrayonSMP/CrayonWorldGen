package space.qouve.crayonworldgen.models;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.World;

/**
 * Mutable context holding generation state during structure placement.
 */
public class WorldGenContext {

    private final World world;
    private BlockVector3 origin;
    private Extent extent;
    private boolean isPatch;

    public WorldGenContext(World world, BlockVector3 origin) {
        this(world, origin, false);
    }

    public WorldGenContext(World world, BlockVector3 origin, boolean isPatch) {
        this.world = world;
        this.origin = origin;
        this.isPatch = isPatch;
    }

    public World world() {
        return world;
    }

    public BlockVector3 origin() {
        return origin;
    }

    public void setOrigin(BlockVector3 origin) {
        this.origin = origin;
    }

    public Extent extent() {
        return extent;
    }

    public void setExtent(Extent extent) {
        this.extent = extent;
    }

    public boolean isPatch() {
        return isPatch;
    }

    public void setPatch(boolean patch) {
        this.isPatch = patch;
    }

    /**
     * Erstellt einen isolierten Kontext für eine Sub-Struktur innerhalb eines Patches.
     */
    public WorldGenContext createPatchContext(BlockVector3 patchOrigin, Extent patchExtent) {
        WorldGenContext patchContext = new WorldGenContext(this.world, patchOrigin, true);
        patchContext.setExtent(patchExtent);
        return patchContext;
    }
}