package space.qouve.crayonworldgen.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.World;
import space.qouve.crayonworldgen.models.SchematicRotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility wrapper for WorldEdit clipboard loading and placement operations.
 */
public final class WorldEditUtil {

    private WorldEditUtil() {
    }

    public static Clipboard loadSchematic(File file) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            throw new IllegalArgumentException("Unknown schematic format for file: " + file.getName());
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        }
    }

    public static Extent pasteSchematic(World world, Clipboard clipboard, BlockVector3 position,
                                        SchematicRotation rotation, boolean ignoreAir) {
        com.sk89q.worldedit.world.World adapterWorld = BukkitAdapter.adapt(world);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(adapterWorld)) {
            ClipboardHolder holder = new ClipboardHolder(clipboard);

            if (rotation != null && rotation != SchematicRotation.NONE) {
                AffineTransform transform = new AffineTransform();
                double degrees = switch (rotation) {
                    case CLOCKWISE_90 -> 90;
                    case CLOCKWISE_180 -> 180;
                    case COUNTERCLOCKWISE_90 -> 270;
                    case RANDOM -> ThreadLocalRandom.current().nextInt(0, 4) * 90;
                    default -> 0;
                };
                transform = transform.rotateY(degrees);
                holder.setTransform(transform);
            }

            Mask ignoreBarriers = Masks.negate(new BlockTypeMask(clipboard, BlockTypes.BARRIER, BlockTypes.STRUCTURE_VOID));

            Operation operation = holder.createPaste(editSession)
                    .to(position)
                    .ignoreAirBlocks(ignoreAir)
                    .maskSource(ignoreBarriers)
                    .build();

            Operations.complete(operation);
            return editSession;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BlockState parseBlockState(String input) {
        try {
            ParserContext context = new ParserContext();
            context.setActor(null);
            return WorldEdit.getInstance().getBlockFactory().parseFromInput(input, context).toImmutableState();
        } catch (Exception e) {
            return null;
        }
    }

    public static void setBlockState(World world, BlockVector3 position, BlockState state) {
        com.sk89q.worldedit.world.World adapterWorld = BukkitAdapter.adapt(world);
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(adapterWorld)) {
            editSession.setBlock(position, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}