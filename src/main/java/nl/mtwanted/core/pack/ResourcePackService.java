package nl.mtwanted.core.pack;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.util.UUID;

public final class ResourcePackService {

    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ResourcePackService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendTo(Player player) {
        if (!plugin.getConfig().getBoolean("resource-pack.enabled", true)) {
            player.sendMessage("§cResource pack sending is disabled in config.");
            return;
        }

        String url = plugin.getConfig().getString("resource-pack.url", "");
        String sha1 = plugin.getConfig().getString("resource-pack.sha1", "");
        boolean required = plugin.getConfig().getBoolean("resource-pack.required", true);
        String promptRaw = plugin.getConfig().getString("resource-pack.prompt", "<yellow>Please accept the resource pack.</yellow>");

        if (url.isBlank() || sha1.isBlank()) {
            player.sendMessage("§cResource pack url/sha1 not configured.");
            return;
        }

        Component prompt = mm.deserialize(promptRaw);

        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(
                UUID.nameUUIDFromBytes(url.getBytes()),
                URI.create(url),
                sha1
        );

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(info)
                .prompt(prompt)
                .required(required)
                .build();

        player.sendResourcePacks(request);
        player.sendMessage("§aResource pack request sent.");
    }
}