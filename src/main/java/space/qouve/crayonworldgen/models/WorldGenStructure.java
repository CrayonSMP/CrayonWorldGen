package space.qouve.crayonworldgen.models;

import com.sk89q.worldedit.extent.clipboard.Clipboard;

/**
 * Holds loaded schematic clipboard data paired with structure configuration.
 */
public record WorldGenStructure(
        Clipboard clipboard,
        WorldGenConfig config
) {
}
