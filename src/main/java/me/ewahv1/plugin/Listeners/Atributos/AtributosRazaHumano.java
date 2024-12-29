package me.ewahv1.plugin.Listeners.Atributos;

import me.ewahv1.plugin.Utils.DamageManager;
import me.ewahv1.plugin.Utils.RazaManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public class AtributosRazaHumano implements Listener {

    private final DamageManager damageManager;
    private final RazaManager razaManager;

    public AtributosRazaHumano(Plugin plugin) {
        // Crear e inicializar DamageManager
        this.damageManager = new DamageManager(plugin);
        this.razaManager = new RazaManager(plugin);

        // Registrar la lógica específica de manejo de daño para Humanos
        damageManager.registrarHandler("Humano", this::procesarDañoHumano);
        Bukkit.getLogger().info("[DEBUG] Manejador de daño registrado para la raza: Humano");
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
        if (raza == null || !raza.equalsIgnoreCase("Humano")) {
            return;
        }

        // Delegar la gestión del daño al DamageManager
        damageManager.manejarDaño(event);
    }

    /**
     * Lógica específica para la raza Humanos.
     *
     * @param event  El evento de daño.
     * @param player El jugador que recibió el daño.
     */
    private void procesarDañoHumano(EntityDamageEvent event, Player player) {
        // Ejemplo básico: log de depuración sin aplicar modificaciones al daño
        Bukkit.getLogger()
                .info("[DEBUG] Procesando daño para " + player.getName() + " (Humano). Causa: " + event.getCause());
        // Puedes agregar lógica personalizada aquí si lo deseas en el futuro
    }
}
