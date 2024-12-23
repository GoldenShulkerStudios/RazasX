package me.ewahv1.plugin.Listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RazaSelectInventory implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Verificar que el inventario es el de selección de raza
        if (event.getView().getTitle().equalsIgnoreCase("§8Selecciona tu Raza")) {
            event.setCancelled(true); // Cancela el evento para que no puedan tomar el ítem

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            String razaSeleccionada = null;

            switch (clickedItem.getType()) {
                case RED_STAINED_GLASS_PANE:
                    razaSeleccionada = "Carmesi";
                    break;
                case BLUE_STAINED_GLASS_PANE:
                    razaSeleccionada = "Leviathan";
                    break;
                case GREEN_STAINED_GLASS_PANE:
                    razaSeleccionada = "Humano";
                    break;
                case BLACK_STAINED_GLASS_PANE:
                    razaSeleccionada = "Draconico";
                    break;
                case YELLOW_STAINED_GLASS_PANE:
                    razaSeleccionada = "Elfo";
                    break;
                default:
                    return;
            }

            if (razaSeleccionada != null) {
                player.closeInventory();
                AsignarRazaPlayer.asignarRaza(player, razaSeleccionada);
            }
        }
    }
}
