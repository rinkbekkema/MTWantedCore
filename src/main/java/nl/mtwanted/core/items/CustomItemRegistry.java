package nl.mtwanted.core.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class CustomItemRegistry {

    private final Map<String, ItemStack> items = new HashMap<>();

    public void load(ConfigurationSection section) {
        items.clear();
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection itemSec = section.getConfigurationSection(id);
            if (itemSec == null) continue;

            String materialName = itemSec.getString("material");
            if (materialName == null) continue;

            Material material = Material.matchMaterial(materialName);
            if (material == null) continue;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            // ✅ SAFE CustomModelData handling (your requested logic, fixed)
            Object cmdObj = itemSec.get("custom_model_data");
            int cmd = 0;

            if (cmdObj instanceof Number) {
                cmd = ((Number) cmdObj).intValue();
            } else if (cmdObj instanceof String) {
                try {
                    cmd = Integer.parseInt((String) cmdObj);
                } catch (NumberFormatException ignored) {
                }
            }

            if (cmd > 0) {
                meta.setCustomModelData(cmd);
            }

            if (itemSec.contains("name")) {
                meta.setDisplayName(itemSec.getString("name"));
            }

            if (itemSec.contains("lore")) {
                meta.setLore(itemSec.getStringList("lore"));
            }

            item.setItemMeta(meta);
            items.put(id.toLowerCase(), item);
        }
    }

    public ItemStack get(String id) {
        ItemStack item = items.get(id.toLowerCase());
        return item == null ? null : item.clone();
    }

    public boolean exists(String id) {
        return items.containsKey(id.toLowerCase());
    }
}
