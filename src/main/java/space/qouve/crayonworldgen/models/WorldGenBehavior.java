package space.qouve.crayonworldgen.models;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Functional interface for custom world generation behaviors.
 */
@FunctionalInterface
public interface WorldGenBehavior {

    /**
     * Executes behavior logic on the given context.
     *
     * @return true if execution should continue, false to abort generation.
     */
    boolean execute(WorldGenContext context, ConfigurationSection config);

    /**
     * Resolves behavior type from a string identifier.
     */
    static WorldGenBehavior byId(String id) {
        return WorldGenBehaviors.getById(id);
    }
}
