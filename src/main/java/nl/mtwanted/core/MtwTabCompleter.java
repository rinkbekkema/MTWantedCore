package nl.mtwanted.core;

import nl.mtwanted.core.items.CustomItemRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class MtwTabCompleter implements TabCompleter {

    private final CustomItemRegistry registry;

    public MtwTabCompleter(CustomItemRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("pack");
            out.add("give");
            out.add("list");
            out.add("reload");
            return out;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (var item : registry.all()) out.add(item.id());
            return out;
        }
        return out;
    }
}