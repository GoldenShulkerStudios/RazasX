package me.ewahv1.plugin.Listeners.Atributos;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import me.ewahv1.plugin.Listeners.AsignarRazaPlayer;

import java.io.File;

public class AtributosRazaHumano extends AsignarRazaPlayer implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Verificar si el daño lo está recibiendo un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        // Obtener raza del jugador
        String raza = obtenerRazaJugador(player);
        if (raza == null || !raza.equalsIgnoreCase("Humano")) {
            return;
        }

        // Mensaje de depuración
        Bukkit.getLogger().info("El jugador " + player.getName()
                + " tiene la raza: Humano. No se aplican resistencias ni debilidades.");
    }

    private String obtenerRazaJugador(Player player) {
        // Ruta al archivo dentro de la carpeta "razas"
        File playerRazasFile = new File(AsignarRazaPlayer.getPlugin().getDataFolder(), "PlayerRazas.yml");

        // Verificar si el archivo existe
        if (!playerRazasFile.exists()) {
            Bukkit.getLogger()
                    .warning("El archivo PlayerRazas.yml no existe. No se puede determinar la raza del jugador.");
            return null;
        }

        // Cargar la configuración YAML
        YamlConfiguration playerRazasConfig = YamlConfiguration.loadConfiguration(playerRazasFile);

        // Buscar el UUID del jugador dentro de las razas
        String uuid = player.getUniqueId().toString();
        for (String raza : playerRazasConfig.getConfigurationSection("razas").getKeys(false)) {
            if (playerRazasConfig.contains("razas." + raza + "." + uuid)) {
                Bukkit.getLogger().info("Raza encontrada para el jugador " + player.getName() + ": " + raza);
                return raza;
            }
        }

        Bukkit.getLogger().info("No se encontró raza para el jugador " + player.getName());
        return null;
    }
}
