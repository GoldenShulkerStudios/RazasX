package me.ewahv1.plugin.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Interfaz para manejar pergaminos según la raza.
 */
public interface PergaminoHandler {
    void onUse(Player player, ItemStack item);
}
