package nl.mtwanted.core.furniture;

import nl.mtwanted.core.items.CustomItem;
import nl.mtwanted.core.items.CustomItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class FurnitureListener implements Listener {

    private final JavaPlugin plugin;
    private final CustomItemRegistry registry;

    public FurnitureListener(JavaPlugin plugin, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!plugin.getConfig().getBoolean("furniture.enabled", true)) return;

        ItemStack inHand = e.getItemInHand();
        Optional<String> taggedId = registry.readTaggedId(inHand);
        if (taggedId.isEmpty()) return;

        String id = taggedId.get().toLowerCase();
        var allowed = new HashSet<>(plugin.getConfig().getStringList("furniture.placeable-ids"));
        if (!allowed.contains(id)) return;

        Optional<CustomItem> opt = registry.get(id);
        if (opt.isEmpty()) return;

        // Cancel normal placement
        e.setCancelled(true);

        Block clicked = e.getBlockPlaced();
        Location baseLoc = clicked.getLocation().add(0.5, 0.0, 0.5);

        // Spawn ItemDisplay that shows the exact same ItemStack model
        World w = clicked.getWorld();
        ItemDisplay display = w.spawn(baseLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(registry.createItemStack(opt.get(), 1));
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setPersistent(true);
            ent.getPersistentDataContainer().set(registry.itemIdKey(), PersistentDataType.STRING, id);
        });

        // Remove the "ghost" placed block
        clicked.setType(Material.AIR);

        // Consume 1 item
        ItemStack hand = e.getPlayer().getInventory().getItem(e.getHand());
        if (hand != null) {
            hand.setAmount(Math.max(0, hand.getAmount() - 1));
        }

        e.getPlayer().sendMessage("§aPlaced furniture: §e" + id);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneakBreak(PlayerInteractEvent e) {
        if (!plugin.getConfig().getBoolean("furniture.enabled", true)) return;
        if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_AIR) return;

        Player p = e.getPlayer();
        if (!p.isSneaking()) return;

        double range = plugin.getConfig().getDouble("furniture.break-range", 5.0);

        // Find nearest ItemDisplay in front of player
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Location center = eye.clone().add(dir.clone().multiply(Math.min(2.0, range)));

        var nearby = p.getWorld().getNearbyEntities(center, range, range, range, ent -> ent instanceof ItemDisplay);
        ItemDisplay closest = null;
        double best = Double.MAX_VALUE;

        for (Entity ent : nearby) {
            ItemDisplay d = (ItemDisplay) ent;
            if (!d.getPersistentDataContainer().has(registry.itemIdKey(), PersistentDataType.STRING)) continue;

            double dist = d.getLocation().distanceSquared(eye);
            if (dist < best) {
                best = dist;
                closest = d;
            }
        }

        if (closest == null) return;

        String id = closest.getPersistentDataContainer().get(registry.itemIdKey(), PersistentDataType.STRING);
        if (id == null) return;

        // Drop the item back
        Optional<CustomItem> opt = registry.get(id);
        if (opt.isPresent()) {
            p.getWorld().dropItemNaturally(closest.getLocation(), registry.createItemStack(opt.get(), 1));
        }

        closest.remove();
        p.sendMessage("§aRemoved furniture: §e" + id);
    }
}