package me.ewahv1.plugin.Listeners.Atributos;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import me.ewahv1.plugin.Listeners.AsignarRazaPlayer;

import java.io.File;

public class AtributosRazaLeviathan extends AsignarRazaPlayer implements Listener {

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Obtener raza del jugador
        String raza = obtenerRazaJugador(player);
        if (raza == null || !raza.equalsIgnoreCase("Leviathan")) {
            return;
        }

        // Verificar si está comiendo carne de zombie
        ItemStack consumedItem = event.getItem();
        if (consumedItem.getType() == Material.ROTTEN_FLESH) {
            Bukkit.getLogger().info(player.getName() + " (Leviathan) está comiendo carne de zombie.");

            // Cancelar efectos negativos (hambre) y otorgar doble saturación
            event.setCancelled(true); // Cancelar el consumo normal
            player.getFoodLevel();
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + 8)); // Incrementar comida (doble de 4)
            player.setSaturation(Math.min(20, player.getSaturation() + 4)); // Incrementar saturación
            Bukkit.getLogger().info(player.getName() + " (Leviathan) ha comido carne de zombie con efecto mejorado.");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Verificar si el daño lo está recibiendo un jugador
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();

        // Obtener raza del jugador
        String raza = obtenerRazaJugador(player);
        if (raza == null || !raza.equalsIgnoreCase("Leviathan")) {
            return;
        }

        // Verificar debilidades al fuego
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.LAVA) {
            double originalDamage = event.getDamage();
            double increasedDamage = originalDamage * 1.25; // 25% más de daño
            event.setDamage(increasedDamage);
            Bukkit.getLogger().info(player.getName() + " (Leviathan) recibió daño aumentado de " + cause + " a "
                    + increasedDamage);
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
