package nl.mtwanted.core.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class CustomItemRegistry {

    private final JavaPlugin plugin;
    private final NamespacedKey itemIdKey;
    private final Map<String, CustomItem> itemsById = new LinkedHashMap<>();

    public CustomItemRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemIdKey = new NamespacedKey(plugin, "custom_item_id");
    }

    public void reload() {
        itemsById.clear();

        File file = new File(plugin.getDataFolder(), "items.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        var list = yml.getMapList("items");
        for (var entry : list) {
            String id = Objects.toString(entry.get("id"), "").toLowerCase();
            String namespace = Objects.toString(entry.get("namespace"), "");
            String base = Objects.toString(entry.get("base"), "");
            int cmd = (int) entry.getOrDefault("custom_model_data", 0);
            String model = Objects.toString(entry.get("model"), "");

            if (id.isBlank() || base.isBlank() || cmd <= 0) continue;

            Material mat;
            try {
                mat = Material.valueOf(base.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown material in items.yml: " + base + " (id=" + id + ")");
                continue;
            }

            itemsById.put(id, new CustomItem(id, namespace, mat, cmd, model));
        }
    }

    public int size() {
        return itemsById.size();
    }

    public Collection<CustomItem> all() {
        return Collections.unmodifiableCollection(itemsById.values());
    }

    public Optional<CustomItem> get(String id) {
        return Optional.ofNullable(itemsById.get(id.toLowerCase(Locale.ROOT)));
    }

    public ItemStack createItemStack(CustomItem item, int amount) {
        ItemStack stack = new ItemStack(item.baseMaterial(), amount);
        ItemMeta meta = stack.getItemMeta();

        // Custom model
        meta.setCustomModelData(item.customModelData());

        // Optional safe tag
        boolean tagItems = plugin.getConfig().getBoolean("custom-items.tag-items", true);
        if (tagItems) {
            meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, item.id());
        }

        // Simple display name (can be replaced with MiniMessage/Adventure)
        meta.setDisplayName("§e" + item.id().replace('_', ' '));

        stack.setItemMeta(meta);
        return stack;
    }

    public Optional<String> readTaggedId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return Optional.empty();
        ItemMeta meta = stack.getItemMeta();
        String id = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        return Optional.ofNullable(id);
    }

    public NamespacedKey itemIdKey() {
        return itemIdKey;
    }
}