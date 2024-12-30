package me.ewahv1.plugin.Listeners.Atributos;

import me.ewahv1.plugin.Utils.DamageManager;
import me.ewahv1.plugin.Utils.RazaManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class AtributosRazaDraconicos implements Listener {

    private final DamageManager damageManager;
    private final RazaManager razaManager;

    public AtributosRazaDraconicos(Plugin plugin) {
        // Inicializar DamageManager y RazaManager
        this.damageManager = new DamageManager(plugin);
        this.razaManager = new RazaManager(plugin);

        // Registrar manejador de daño para Dracónico
        damageManager.registrarHandler("Draconico", this::procesarDañoDraconico);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Verificar que la entidad es un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Verificar si la raza del jugador es "Draconico"
        String raza = razaManager.obtenerRaza(player);
        if (raza == null || !raza.equalsIgnoreCase("Draconico")) {
            return;
        }

        // Delegar la gestión del daño al DamageManager
        damageManager.manejarDaño(event);
    }

    /**
     * Lógica específica para la raza Dracónico.
     *
     * @param event  El evento de daño.
     * @param player El jugador que recibió el daño.
     */
    private void procesarDañoDraconico(EntityDamageEvent event, Player player) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        double originalDamage = event.getDamage();

        // Resistencias: Fuego, lava, explosiones, Fire Aspect y Flame
        if (cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.LAVA ||
                cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {

            double reducedDamage = originalDamage * 0.75; // 25% menos de daño
            event.setDamage(reducedDamage);
            Bukkit.getLogger().info(player.getName() + " (Dracónico) resistió daño. Nuevo daño: " + reducedDamage);
            return;
        }

        // Debilidad: Agua (sumergido o lluvia)
        if (isInWater(player) || isInRain(player)) {
            double increasedDamage = originalDamage * 1.25; // 25% más de daño
            event.setDamage(increasedDamage);
            Bukkit.getLogger().info(player.getName() + " (Dracónico) recibió daño incrementado por agua. Nuevo daño: "
                    + increasedDamage);
        }
    }

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Verificar raza del jugador
        String raza = razaManager.obtenerRaza(player);
        if (raza == null || !raza.equalsIgnoreCase("Draconico")) {
            return;
        }

        // Verificar si el daño proviene de un arma con Fire Aspect o Flame
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            ItemStack weapon = damager.getInventory().getItemInMainHand();

            if (weapon.containsEnchantment(org.bukkit.enchantments.Enchantment.FIRE_ASPECT) ||
                    weapon.containsEnchantment(org.bukkit.enchantments.Enchantment.FLAME)) {
                double reducedDamage = event.getDamage() * 0.75; // 25% menos de daño
                event.setDamage(reducedDamage);
                Bukkit.getLogger().info(player.getName()
                        + " (Dracónico) resistió daño por Fire Aspect/Flame. Nuevo daño: " + reducedDamage);
            }
        }
    }

    /**
     * Verifica si el jugador está en agua (sumergido).
     *
     * @param player El jugador a verificar.
     * @return true si está en agua, false en caso contrario.
     */
    private boolean isInWater(Player player) {
        Material blockType = player.getLocation().getBlock().getType();
        return blockType == Material.WATER || blockType == Material.BUBBLE_COLUMN;
    }

    /**
     * Verifica si el jugador está bajo la lluvia.
     *
     * @param player El jugador a verificar.
     * @return true si está bajo la lluvia, false en caso contrario.
     */
    private boolean isInRain(Player player) {
        World world = player.getWorld();
        return world.hasStorm() && world.getHighestBlockAt(player.getLocation()).getY() <= player.getLocation().getY();
    }
}
