package nl.mtwanted.core;

import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.mtwanted.core.items.CustomItem;
import nl.mtwanted.core.items.CustomItemRegistry;
import nl.mtwanted.core.pack.ResourcePackService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class MtwCommand implements CommandExecutor {

    private final MTWantedCore plugin;
    private final CustomItemRegistry registry;
    private final ResourcePackService packService;

    public MtwCommand(MTWantedCore plugin, CustomItemRegistry registry, ResourcePackService packService) {
        this.plugin = plugin;
        this.registry = registry;
        this.packService = packService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§e/mtw pack §7- send resource pack");
            sender.sendMessage("§e/mtw give <id> [player] [amount] §7- give a custom item");
            sender.sendMessage("§e/mtw list [filter] §7- list custom item ids");
            sender.sendMessage("§e/mtw reload §7- reload config + items.yml");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("mtw.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                plugin.reloadConfig();
                registry.reload();
                sender.sendMessage("§aReloaded config + items.yml. Items: " + registry.size());
                return true;
            }
            case "pack" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("§cOnly players can run this.");
                    return true;
                }
                packService.sendTo(p);
                return true;
            }
            case "list" -> {
                String filter = args.length >= 2 ? args[1].toLowerCase() : "";
                sender.sendMessage("§eCustom items:");
                int shown = 0;
                for (CustomItem item : registry.all()) {
                    if (!filter.isEmpty() && !item.id().contains(filter)) continue;
                    sender.sendMessage("§7- §f" + item.id() + " §8(" + item.baseMaterial() + " cmd=" + item.customModelData() + ")");
                    shown++;
                    if (shown >= 60) {
                        sender.sendMessage("§8… (too many, refine your filter)");
                        break;
                    }
                }
                return true;
            }
            case "give" -> {
                if (!sender.hasPermission("mtw.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /mtw give <id> [player] [amount]");
                    return true;
                }

                String id = args[1].toLowerCase();
                Optional<CustomItem> opt = registry.get(id);
                if (opt.isEmpty()) {
                    sender.sendMessage("§cUnknown id: " + id + ". Try /mtw list " + id);
                    return true;
                }

                Player target;
                int amount = 1;

                if (args.length >= 3) {
                    Player found = Bukkit.getPlayerExact(args[2]);
                    target = found != null ? found : (sender instanceof Player sp ? sp : null);
                    if (target == null) {
                        sender.sendMessage("§cPlayer not found and you are not a player.");
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player sp)) {
                        sender.sendMessage("§cUsage: /mtw give <id> <player> [amount]");
                        return true;
                    }
                    target = sp;
                }

                if (args.length >= 4) {
                    try {
                        amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
                    } catch (NumberFormatException ignored) { }
                }

                CustomItem item = opt.get();
                target.getInventory().addItem(registry.createItemStack(item, amount));
                sender.sendMessage("§aGave §f" + amount + "x §e" + item.id() + " §ato §f" + target.getName());
                return true;
            }
            default -> {
                sender.sendMessage("§cUnknown subcommand.");
                return true;
            }
        }
    }
}