package me.ewahv1.plugin.Listeners.Atributos;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.ewahv1.plugin.Listeners.AsignarRazaPlayer;

import java.io.File;

public class AtributosRazaElfo extends AsignarRazaPlayer implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Verificar si el daño lo está recibiendo un jugador
        if (!(event.getEntity() instanceof Player)) {
            Bukkit.getLogger().info("Evento de daño ignorado: la entidad no es un jugador.");
            return;
        }
        Player player = (Player) event.getEntity();

        // Obtener raza del jugador
        String raza = obtenerRazaJugador(player);
        if (raza == null || !raza.equalsIgnoreCase("Elfo")) {
            return;
        }

        Bukkit.getLogger().info("El jugador " + player.getName() + " tiene la raza: Elfo.");

        // Procesar resistencias y debilidades
        procesarResistenciasYDebilidades(event, player);
    }

    private void procesarResistenciasYDebilidades(EntityDamageEvent event, Player player) {
        DamageCause cause = event.getCause();
        double originalDamage = event.getDamage();

        Bukkit.getLogger().info("El jugador " + player.getName() + " recibió daño de tipo: " + cause
                + " con daño original: " + originalDamage);

        // Resistencia a flechas
        if (cause == DamageCause.PROJECTILE) {
            double reducedDamage = originalDamage * 0.75; // 25% menos de daño
            event.setDamage(reducedDamage);
            Bukkit.getLogger()
                    .info(player.getName() + " (Elfo) recibió daño reducido de " + cause + " a " + reducedDamage);
            return;
        }

        // Debilidad al veneno
        if (cause == DamageCause.POISON) {
            double increasedDamage = originalDamage * 1.25; // 25% más de daño
            event.setDamage(increasedDamage);
            Bukkit.getLogger()
                    .info(player.getName() + " (Elfo) recibió daño aumentado de " + cause + " a " + increasedDamage);
        }
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
