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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class AtributosRazaCarmesi implements Listener {

    private final DamageManager damageManager;
    private final RazaManager razaManager;

    public AtributosRazaCarmesi(Plugin plugin) {
        // Inicializar DamageManager y RazaManager
        this.damageManager = new DamageManager(plugin);
        this.razaManager = new RazaManager(plugin);

        // Registrar lógica específica para Carmesi en DamageManager
        damageManager.registrarHandler("Carmesi", this::procesarDañoCarmesi);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Verificar que la entidad es un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Verificar si la raza del jugador es "Carmesi"
        String raza = razaManager.obtenerRaza(player);
        if (raza == null || !raza.equalsIgnoreCase("Carmesi")) {
            return;
        }

        // Delegar la gestión del daño al DamageManager
        damageManager.manejarDaño(event);
    }

    /**
     * Lógica específica para la raza Carmesi.
     *
     * @param event  El evento de daño.
     * @param player El jugador que recibió el daño.
     */
    private void procesarDañoCarmesi(EntityDamageEvent event, Player player) {
        double originalDamage = event.getDamage();
        EntityDamageEvent.DamageCause cause = event.getCause();

        Bukkit.getLogger().info("[DEBUG] El jugador " + player.getName() + " (Carmesi) recibió daño: " + cause
                + ", cantidad: " + originalDamage);

        // Resistencia: no se aplica ninguna resistencia
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Obtener raza del jugador usando RazaManager
        String raza = razaManager.obtenerRaza(player);
        if (raza == null || !raza.equalsIgnoreCase("Carmesi")) {
            return;
        }

        // Verificar si el daño proviene de cualquier herramienta de madera
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            ItemStack weapon = damager.getInventory().getItemInMainHand();
            if (isWoodenTool(weapon.getType())) {
                double originalDamage = event.getDamage();
                double increasedDamage = originalDamage * 1.25; // 25% más de daño
                event.setDamage(increasedDamage);
                Bukkit.getLogger().info("[DEBUG] Daño aumentado por herramienta de madera: " + increasedDamage);
            }
        }
    }

    /**
     * Verifica si el material es una herramienta de madera.
     *
     * @param material El material a verificar.
     * @return true si es una herramienta de madera, false en caso contrario.
     */
    private boolean isWoodenTool(Material material) {
        return material == Material.WOODEN_SWORD
                || material == Material.WOODEN_AXE
                || material == Material.WOODEN_PICKAXE
                || material == Material.WOODEN_SHOVEL
                || material == Material.WOODEN_HOE;
    }
}
