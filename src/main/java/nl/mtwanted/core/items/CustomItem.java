package nl.mtwanted.core.items;

import org.bukkit.Material;

public record CustomItem(
        String id,
        String namespace,
        Material baseMaterial,
        int customModelData,
        String model
) {}