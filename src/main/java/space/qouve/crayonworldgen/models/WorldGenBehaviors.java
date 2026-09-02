package space.qouve.crayonworldgen.models;

import space.qouve.crayonworldgen.behaviors.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping behavior key strings to implementation instances.
 */
public final class WorldGenBehaviors {

    private static final Map<String, WorldGenBehavior> REGISTRY = new HashMap<>();

    static {
        register("extend-to-ground", new ExtendToGroundBehavior());
        register("ground-block", new GroundBlockBehavior());
        register("height", new HeightBehavior());
        register("highest-block", new HighestBlockBehavior());
        register("patch-spawn", new PatchSpawnBehavior());
        register("sky-structure", new SkyStructureBehavior());
    }

    private WorldGenBehaviors() {
    }

    public static void register(String id, WorldGenBehavior behavior) {
        REGISTRY.put(id.toLowerCase(), behavior);
    }

    public static WorldGenBehavior getById(String id) {
        if (id == null) {
            return null;
        }
        return REGISTRY.get(id.toLowerCase());
    }
}
