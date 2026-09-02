package space.qouve.crayonworldgen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldGenCommand implements CommandExecutor, TabCompleter {

    private final CrayonWorldGen plugin;

    public WorldGenCommand(CrayonWorldGen plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("crayonworldgen.admin")) {
            sender.sendMessage(Component.text("You do not have permission to execute this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getService().loadConfigurations();
                sender.sendMessage(Component.text("[CrayonWorldGen] Configuration successfully reloaded.", NamedTextColor.GREEN));
            }
            case "list" -> handleList(sender);
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /crayonworldgen info <structure_id>", NamedTextColor.YELLOW));
                    return true;
                }
                handleInfo(sender, args[1]);
            }
            case "locate" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /crayonworldgen locate <structure_id>", NamedTextColor.YELLOW));
                    return true;
                }
                handleLocate(player, args[1]);
            }
            case "debug" -> handleDebug(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleList(CommandSender sender) {
        List<String> structures = plugin.getService().getRegisteredStructures();

        if (structures.isEmpty()) {
            sender.sendMessage(Component.text("[CrayonWorldGen] No structures registered.", NamedTextColor.YELLOW));
            return;
        }

        sender.sendMessage(Component.text("--- Registered Structures ---", NamedTextColor.GOLD, TextDecoration.BOLD));

        for (String id : structures) {
            String configDetails = plugin.getService().getStructureInfo(id);
            if (configDetails == null) configDetails = "No info available";

            Component entry = Component.text("• ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(id, NamedTextColor.GREEN))
                    .hoverEvent(HoverEvent.showText(Component.text(configDetails, NamedTextColor.LIGHT_PURPLE)))
                    .clickEvent(ClickEvent.suggestCommand("/crayonworldgen info " + id));

            sender.sendMessage(entry);
        }
    }

    private void handleInfo(CommandSender sender, String structureId) {
        String info = plugin.getService().getStructureInfo(structureId);
        if (info == null) {
            sender.sendMessage(Component.text("Structure '" + structureId + "' not found.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("--- Structure Info: " + structureId + " ---", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(info, NamedTextColor.GRAY));
    }

    private void handleLocate(Player player, String structureId) {
        player.sendMessage(Component.text("Searching for nearest " + structureId + "...", NamedTextColor.GRAY));

        Location loc = plugin.getService().locateStructure(player.getWorld(), player.getLocation(), structureId);
        if (loc == null) {
            player.sendMessage(Component.text("Could not find structure '" + structureId + "' nearby.", NamedTextColor.RED));
            return;
        }

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        Component tpMessage = Component.text("Nearest " + structureId + " is at ", NamedTextColor.GREEN)
                .append(Component.text("[" + x + ", " + y + ", " + z + "]", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                .hoverEvent(HoverEvent.showText(Component.text("Click to teleport", NamedTextColor.YELLOW)))
                .clickEvent(ClickEvent.runCommand("/tp " + x + " " + y + " " + z));

        player.sendMessage(tpMessage);
    }

    private void handleDebug(CommandSender sender) {
        Map<String, Integer> spawnCounts = plugin.getService().getSpawnCounts();

        sender.sendMessage(Component.text("--- Structure Spawn Statistics ---", NamedTextColor.GOLD));
        if (spawnCounts.isEmpty()) {
            sender.sendMessage(Component.text("No structure spawn data recorded.", NamedTextColor.GRAY));
            return;
        }

        spawnCounts.forEach((id, count) -> sender.sendMessage(Component.text("• ", NamedTextColor.DARK_GRAY)
                .append(Component.text(id + ": ", NamedTextColor.AQUA))
                .append(Component.text(count + " spawned", NamedTextColor.WHITE))));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("--- CrayonWorldGen Admin Commands ---", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/crayonworldgen reload ", NamedTextColor.YELLOW).append(Component.text("- Reload configurations", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/crayonworldgen list ", NamedTextColor.YELLOW).append(Component.text("- List structures with hover info", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/crayonworldgen info <id> ", NamedTextColor.YELLOW).append(Component.text("- View structure config details", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/crayonworldgen locate <id> ", NamedTextColor.YELLOW).append(Component.text("- Find nearest structure location", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/crayonworldgen debug ", NamedTextColor.YELLOW).append(Component.text("- View structure spawn metrics", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("crayonworldgen.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filterCompletions(Arrays.asList("reload", "list", "info", "locate", "debug"), args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("locate"))) {
            return filterCompletions(plugin.getService().getRegisteredStructures(), args[1]);
        }

        return List.of();
    }

    private List<String> filterCompletions(List<String> options, String input) {
        String lowerInput = input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}