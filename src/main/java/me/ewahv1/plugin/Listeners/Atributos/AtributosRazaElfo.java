package me.ewahv1.plugin.Listeners.Atributos;

import me.ewahv1.plugin.Utils.DamageManager;
import me.ewahv1.plugin.Utils.RazaManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public class AtributosRazaElfo implements Listener {

    private final DamageManager damageManager;
    private final RazaManager razaManager;

    public AtributosRazaElfo(Plugin plugin) {
        this.damageManager = new DamageManager(plugin);
        this.razaManager = new RazaManager(plugin);

        // Registrar la lógica específica de manejo de daño para Elfo
        damageManager.registrarHandler("Elfo", this::procesarResistenciasYDebilidades); // Usar minúsculas
        Bukkit.getLogger().info("[DEBUG] Registrado manejador de daño para la raza: Elfo");
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
        if (raza == null || !raza.equalsIgnoreCase("Elfo")) {
            return;
        }

        // Delegar la gestión del daño al DamageManager
        damageManager.manejarDaño(event);
    }

    private void procesarResistenciasYDebilidades(EntityDamageEvent event, Player player) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        double originalDamage = event.getDamage();

        // Resistencia a flechas (PROJECTILE)
        if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            double reducedDamage = originalDamage * 0.75;
            event.setDamage(reducedDamage);
            Bukkit.getLogger()
                    .info(player.getName() + " (Elfo) recibió daño reducido por PROJECTILE: " + reducedDamage);
            return;
        }

        // Debilidad al veneno (POISON)
        if (cause == EntityDamageEvent.DamageCause.POISON) {
            double increasedDamage = originalDamage * 1.25;
            event.setDamage(increasedDamage);
            Bukkit.getLogger().info(player.getName() + " (Elfo) recibió daño aumentado por POISON: " + increasedDamage);
        }
    }
}
