package me.ewahv1.plugin.Listeners.Atributos;

import me.ewahv1.plugin.Utils.DamageManager;
import me.ewahv1.plugin.Utils.RazaManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class AtributosRazaLeviathan implements Listener {

    private final DamageManager damageManager;
    private final RazaManager razaManager;

    public AtributosRazaLeviathan(Plugin plugin) {
        // Inicializar DamageManager y RazaManager
        this.damageManager = new DamageManager(plugin);
        this.razaManager = new RazaManager(plugin);

        // Registrar lógica específica para Leviathan en DamageManager
        damageManager.registrarHandler("Leviathan", this::procesarDañoLeviathan);
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Obtener raza del jugador usando RazaManager
        String raza = razaManager.obtenerRaza(player);
        if (raza == null || !raza.equalsIgnoreCase("Leviathan")) {
            return;
        }

        // Verificar si está comiendo carne de zombie
        ItemStack consumedItem = event.getItem();
        if (consumedItem.getType() == Material.ROTTEN_FLESH) {
            Bukkit.getLogger().info(player.getName() + " (Leviathan) está comiendo carne de zombie.");

            // Cancelar efectos negativos (hambre) y otorgar doble saturación
            event.setCancelled(true); // Cancelar el consumo normal
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 8)); // Incrementar comida (doble de 4)
            player.setSaturation(Math.min(20, player.getSaturation() + 4)); // Incrementar saturación

            // Reemplazar el ítem consumido para reflejar la acción
            if (consumedItem.getAmount() > 1) {
                consumedItem.setAmount(consumedItem.getAmount() - 1);
            } else {
                player.getInventory().removeItem(consumedItem);
            }

            Bukkit.getLogger().info(player.getName() + " (Leviathan) ha consumido carne de zombie con beneficios.");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Verificar que la entidad es un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Verificar si la raza del jugador es "Leviathan"
        String raza = razaManager.obtenerRaza(player);
        if (raza == null || !raza.equalsIgnoreCase("Leviathan")) {
            return;
        }

        // Delegar la gestión del daño al DamageManager
        damageManager.manejarDaño(event);
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Verificar si la raza del jugador es "Leviathan"
        String raza = razaManager.obtenerRaza(player);
        if (raza == null || !raza.equalsIgnoreCase("Leviathan")) {
            return;
        }

        // Verificar si el daño proviene de un tridente
        if (event.getDamager() instanceof org.bukkit.entity.Trident) {
            double originalDamage = event.getDamage();
            double reducedDamage = originalDamage * 0.75; // 25% menos de daño
            event.setDamage(reducedDamage);
            Bukkit.getLogger()
                    .info(player.getName() + " (Leviathan) resistió daño de tridente. Nuevo daño: " + reducedDamage);
        }
    }

    /**
     * Lógica específica para la raza Leviathan.
     *
     * @param event  El evento de daño.
     * @param player El jugador que recibió el daño.
     */
    private void procesarDañoLeviathan(EntityDamageEvent event, Player player) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        double originalDamage = event.getDamage();

        // Debilidades al fuego
        if (cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.LAVA) {
            double increasedDamage = originalDamage * 1.25; // 25% más de daño
            event.setDamage(increasedDamage);
            Bukkit.getLogger().info(player.getName() + " (Leviathan) recibió daño aumentado de " + cause + " a "
                    + increasedDamage);
        }
    }
}
