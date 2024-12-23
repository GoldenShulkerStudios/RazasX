package me.ewahv1.plugin.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class RazaRespawnListener implements Listener {

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        boolean useSQL = AsignarRazaPlayer.getPlugin().getConfig().getBoolean("sql");

        AsignarRazaPlayer.getPlugin().getLogger().info("El jugador " + player.getName() + " ha respawneado.");
        AsignarRazaPlayer.getPlugin().getLogger()
                .info("Modo de almacenamiento configurado: " + (useSQL ? "SQL" : "YAML"));

        // Obtener raza del jugador
        String razaSeleccionada = useSQL ? obtenerRazaDesdeSQL(player) : obtenerRazaDesdeYAML(player);
        if (razaSeleccionada == null) {
            player.sendMessage("§cNo se pudo encontrar tu raza asignada.");
            AsignarRazaPlayer.getPlugin().getLogger()
                    .warning("No se encontró una raza asignada para el jugador " + player.getName());
            return;
        }

        AsignarRazaPlayer.getPlugin().getLogger()
                .info("Raza encontrada para el jugador " + player.getName() + ": " + razaSeleccionada);

        // Cargar configuración de razas desde razas.yml
        File razasFile = new File(AsignarRazaPlayer.getPlugin().getDataFolder(), "Razas.yml");
        if (!razasFile.exists()) {
            player.sendMessage("§cNo se encontró la configuración de razas.");
            AsignarRazaPlayer.getPlugin().getLogger().severe("No se encontró el archivo razas.yml.");
            return;
        }

        YamlConfiguration razasConfig = YamlConfiguration.loadConfiguration(razasFile);

        if (!razasConfig.contains(razaSeleccionada)) {
            player.sendMessage("§cNo se encontró la configuración de la raza " + razaSeleccionada + ".");
            AsignarRazaPlayer.getPlugin().getLogger()
                    .warning("La configuración de la raza " + razaSeleccionada + " no está en razas.yml.");
            return;
        }

        // Retrasar la aplicación de atributos y efectos
        Bukkit.getScheduler().runTaskLater(AsignarRazaPlayer.getPlugin(), () -> {
            aplicarAtributosYefectos(player, razaSeleccionada, razasConfig);
        }, 10L); // 10 ticks = 0.5 segundos
    }

    private void aplicarAtributosYefectos(Player player, String raza, YamlConfiguration razasConfig) {
        AsignarRazaPlayer.getPlugin().getLogger()
                .info("Aplicando atributos y efectos para la raza " + raza + " al jugador " + player.getName());

        // Asignar base_health
        double baseHealth = razasConfig.getDouble(raza + ".base_health", 20);
        player.setMaxHealth(baseHealth);
        player.setHealth(baseHealth);
        AsignarRazaPlayer.getPlugin().getLogger()
                .info("Vida base asignada a " + baseHealth + " para el jugador " + player.getName());

        // Verificar si hay efectos configurados
        List<String> effects = razasConfig.getStringList(raza + ".effects");
        if (effects == null || effects.isEmpty()) {
            AsignarRazaPlayer.getPlugin().getLogger()
                    .warning("No se encontraron efectos configurados para la raza " + raza + ".");
            return;
        }

        AsignarRazaPlayer.getPlugin().getLogger()
                .info("Efectos encontrados para la raza " + raza + ": " + effects);

        // Aplicar efectos de poción
        for (String effectName : effects) {
            AsignarRazaPlayer.getPlugin().getLogger()
                    .info("Procesando el efecto: " + effectName + " para el jugador " + player.getName());

            PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
            if (effectType != null) {
                player.removePotionEffect(effectType); // Eliminar efecto previo si existe
                player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, 0, true, false)); // Aplicar
                                                                                                         // efecto
                AsignarRazaPlayer.getPlugin().getLogger()
                        .info("Efecto " + effectName + " aplicado correctamente al jugador " + player.getName());
            } else {
                AsignarRazaPlayer.getPlugin().getLogger()
                        .warning("Efecto inválido o desconocido: " + effectName + " para la raza " + raza);
            }
        }

        player.sendMessage("§aSe han reaplicado los atributos y efectos de tu raza: " + raza + ".");
    }

    private String obtenerRazaDesdeSQL(Player player) {
        String raza = null;
        AsignarRazaPlayer.getPlugin().getLogger().info("Obteniendo raza desde SQL para el jugador " + player.getName());

        try (Connection connection = CreateDatabase.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT raza FROM razas WHERE uuid = ?")) {

            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    raza = resultSet.getString("raza");
                    AsignarRazaPlayer.getPlugin().getLogger().info("Raza encontrada en SQL: " + raza);
                } else {
                    AsignarRazaPlayer.getPlugin().getLogger()
                            .warning("No se encontró ninguna raza en SQL para el jugador " + player.getName());
                }
            }
        } catch (Exception e) {
            player.sendMessage("§cHubo un error al consultar tu raza en la base de datos.");
            AsignarRazaPlayer.getPlugin().getLogger()
                    .severe("Error al obtener la raza desde SQL para el jugador " + player.getName());
            e.printStackTrace();
        }
        return raza;
    }

    private String obtenerRazaDesdeYAML(Player player) {
        // Ruta al archivo dentro de la carpeta "razas"
        File playerRazasFile = new File(AsignarRazaPlayer.getPlugin().getDataFolder(),
                "PlayerRazas.yml");

        // Verificar si el archivo existe
        if (!playerRazasFile.exists()) {
            AsignarRazaPlayer.getPlugin().getLogger()
                    .warning("No se encontró el archivo PlayerRazas.yml en la carpeta 'razas'.");
            return null;
        }

        // Cargar la configuración YAML
        YamlConfiguration playerRazasConfig = YamlConfiguration.loadConfiguration(playerRazasFile);

        // Buscar el UUID del jugador dentro de las razas
        String uuid = player.getUniqueId().toString();
        for (String raza : playerRazasConfig.getConfigurationSection("razas").getKeys(false)) {
            if (playerRazasConfig.contains("razas." + raza + "." + uuid)) {
                AsignarRazaPlayer.getPlugin().getLogger()
                        .info("Raza encontrada en YAML para el jugador " + player.getName() + ": " + raza);
                return raza;
            }
        }

        AsignarRazaPlayer.getPlugin().getLogger()
                .warning("No se encontró ninguna raza en YAML para el jugador " + player.getName());
        return null;
    }
}
