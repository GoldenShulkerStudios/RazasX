package me.ewahv1.plugin.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class DamageManager {

    private final Plugin plugin;
    private final RazaManager razaManager;
    private final Map<String, DamageHandler> razaHandlers;

    public DamageManager(Plugin plugin) {
        this.plugin = plugin;
        this.razaManager = new RazaManager(plugin);
        this.razaHandlers = new HashMap<>();
    }

    /**
     * Registra un manejador de daño para una raza específica.
     * 
     * @param raza    El nombre de la raza.
     * @param handler El manejador de daño para esa raza.
     */
    public void registrarHandler(String raza, DamageHandler handler) {
        razaHandlers.put(raza.toLowerCase(), handler);
        Bukkit.getLogger().info("[DEBUG] Registrado manejador de daño para la raza: " + raza);
    }

    /**
     * Gestiona el evento de daño recibido por un jugador.
     * 
     * @param event El evento de daño.
     */
    public void manejarDaño(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        // Obtener la raza del jugador
        String raza = razaManager.obtenerRaza(player);
        if (raza == null) {
            Bukkit.getLogger().info("[DEBUG] El jugador " + player.getName() + " no tiene raza asignada.");
            return;
        }

        // Buscar y delegar al manejador de la raza correspondiente
        DamageHandler handler = razaHandlers.get(raza.toLowerCase());
        if (handler != null) {
            Bukkit.getLogger().info("[DEBUG] Delegando manejo de daño a la raza: " + raza);
            handler.procesarDaño(event, player);
        } else {
            Bukkit.getLogger().info("[DEBUG] No se encontró un manejador de daño para la raza: " + raza);
        }
    }

    /**
     * Interfaz para los manejadores de daño de cada raza.
     */
    public interface DamageHandler {
        void procesarDaño(EntityDamageEvent event, Player player);
    }
}
