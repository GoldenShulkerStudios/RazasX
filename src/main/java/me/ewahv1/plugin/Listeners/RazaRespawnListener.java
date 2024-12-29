package me.ewahv1.plugin.Listeners;

import me.ewahv1.plugin.Utils.RazaManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.List;

public class RazaRespawnListener implements Listener {

    private final Plugin plugin;
    private final RazaManager razaManager;

    public RazaRespawnListener(Plugin plugin) {
        this.plugin = plugin;
        this.razaManager = new RazaManager(plugin);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Obtener raza del jugador usando RazaManager
        String razaSeleccionada = razaManager.obtenerRaza(player);
        if (razaSeleccionada == null) {
            player.sendMessage("§cNo se pudo encontrar tu raza asignada.");
            plugin.getLogger().warning("No se encontró una raza asignada para el jugador " + player.getName());
            return;
        }

        plugin.getLogger().info("Raza encontrada para el jugador " + player.getName() + ": " + razaSeleccionada);

        // Cargar configuración de Razas desde Razas.yml
        File razasFile = new File(plugin.getDataFolder(), "Razas.yml");
        if (!razasFile.exists()) {
            player.sendMessage("§cNo se encontró la configuración de Razas.");
            plugin.getLogger().severe("No se encontró el archivo Razas.yml.");
            return;
        }

        YamlConfiguration razasConfig = YamlConfiguration.loadConfiguration(razasFile);

        if (!razasConfig.contains(razaSeleccionada)) {
            player.sendMessage("§cNo se encontró la configuración de la raza " + razaSeleccionada + ".");
            plugin.getLogger().warning("La configuración de la raza " + razaSeleccionada + " no está en Razas.yml.");
            return;
        }

        // Retrasar la aplicación de atributos y efectos
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            aplicarAtributosYefectos(player, razaSeleccionada, razasConfig);
        }, 10L); // 10 ticks = 0.5 segundos
    }

    private void aplicarAtributosYefectos(Player player, String raza, YamlConfiguration razasConfig) {
        plugin.getLogger()
                .info("Aplicando atributos y efectos para la raza " + raza + " al jugador " + player.getName());

        // Asignar base_health
        double baseHealth = razasConfig.getDouble(raza + ".base_health", 20);
        player.setMaxHealth(baseHealth);
        player.setHealth(baseHealth);
        plugin.getLogger().info("Vida base asignada a " + baseHealth + " para el jugador " + player.getName());

        // Verificar si hay efectos configurados
        List<String> effects = razasConfig.getStringList(raza + ".effects");
        if (effects == null || effects.isEmpty()) {
            plugin.getLogger().warning("No se encontraron efectos configurados para la raza " + raza + ".");
            return;
        }

        plugin.getLogger().info("Efectos encontrados para la raza " + raza + ": " + effects);

        // Aplicar efectos de poción
        for (String effectName : effects) {
            plugin.getLogger().info("Procesando el efecto: " + effectName + " para el jugador " + player.getName());

            PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
            if (effectType != null) {
                player.removePotionEffect(effectType); // Eliminar efecto previo si existe
                player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, 0, true, false)); // Aplicar
                                                                                                         // efecto
                plugin.getLogger()
                        .info("Efecto " + effectName + " aplicado correctamente al jugador " + player.getName());
            } else {
                plugin.getLogger().warning("Efecto inválido o desconocido: " + effectName + " para la raza " + raza);
            }
        }

        player.sendMessage("§aSe han reaplicado los atributos y efectos de tu raza: " + raza + ".");
    }
}
