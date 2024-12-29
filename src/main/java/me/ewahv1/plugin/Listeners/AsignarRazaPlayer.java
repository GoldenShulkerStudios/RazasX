package me.ewahv1.plugin.Listeners;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AsignarRazaPlayer {

    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static void asignarRaza(Player player, String razaSeleccionada) {
        boolean useSQL = plugin.getConfig().getBoolean("sql");
        plugin.getLogger().info("Iniciando proceso para asignar raza...");
        plugin.getLogger().info("Modo de almacenamiento configurado: " + (useSQL ? "SQL" : "YAML"));

        if (useSQL) {
            CreateDatabase.verificarYCrearDatabase(plugin);
            asignarRazaSQL(player, razaSeleccionada);
        } else {
            verificarOCrearArchivoYAML();
            asignarRazaYAML(player, razaSeleccionada);
        }
    }

    private static void verificarOCrearArchivoYAML() {
        File file = new File(plugin.getDataFolder(), "PlayerRazas.yml");
        if (!file.exists()) {
            plugin.getLogger().info("El archivo PlayerRazas.yml no existe. Creándolo...");
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.createSection("Razas");
                config.save(file);
                plugin.getLogger().info("Archivo PlayerRazas.yml creado exitosamente.");
            } catch (IOException e) {
                plugin.getLogger().severe("Error al crear el archivo PlayerRazas.yml");
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("El archivo PlayerRazas.yml ya existe.");
        }
    }

    private static void asignarRazaYAML(Player player, String razaSeleccionada) {
        File file = new File(plugin.getDataFolder(), "PlayerRazas.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("Razas")) {
            config.createSection("Razas");
        }

        plugin.getLogger().info("Comprobando si el jugador ya pertenece a una raza...");
        if (yaPerteneceARaza(player.getUniqueId().toString(), config)) {
            player.sendMessage("§cYa perteneces a una raza. Contacta a un administrador para cambiarla.");
            plugin.getLogger().warning("El jugador " + player.getName() + " ya tiene asignada una raza.");
            return;
        }

        config.set("Razas." + razaSeleccionada + "." + player.getUniqueId() + ".nick", player.getName());
        plugin.getLogger().info("Asignando la raza " + razaSeleccionada + " al jugador " + player.getName());

        try {
            config.save(file);
            player.sendMessage("§aRaza asignada correctamente: " + razaSeleccionada);
            plugin.getLogger()
                    .info("La raza " + razaSeleccionada + " fue asignada correctamente a " + player.getName());
            IniciarJugadorRaza.entregarPergamino(player, razaSeleccionada);
        } catch (IOException e) {
            player.sendMessage("§cError al guardar la información de tu raza.");
            plugin.getLogger().severe("Error al guardar los datos de la raza en PlayerRazas.yml.");
            e.printStackTrace();
        }
    }

    private static boolean yaPerteneceARaza(String uuid, YamlConfiguration config) {
        if (!config.contains("Razas")) {
            plugin.getLogger().info("La sección 'Razas' no existe en PlayerRazas.yml.");
            return false;
        }

        for (String raza : config.getConfigurationSection("Razas").getKeys(false)) {
            if (config.contains("Razas." + raza + "." + uuid)) {
                plugin.getLogger().info("El UUID " + uuid + " ya pertenece a la raza " + raza);
                return true;
            }
        }
        plugin.getLogger().info("El UUID " + uuid + " no pertenece a ninguna raza.");
        return false;
    }

    private static void asignarRazaSQL(Player player, String razaSeleccionada) {
        plugin.getLogger().info("Asignando raza en la base de datos para el jugador " + player.getName());
        try (Connection connection = CreateDatabase.getConnection()) {
            String query = "SELECT raza FROM Razas WHERE uuid = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, player.getUniqueId().toString());

            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                String razaActual = result.getString("raza");
                player.sendMessage(
                        "§cYa perteneces a la raza: " + razaActual + ". Contacta a un administrador para cambiarla.");
                plugin.getLogger()
                        .warning("El jugador " + player.getName() + " ya tiene asignada la raza: " + razaActual);
                return;
            }

            query = "INSERT INTO Razas (raza, uuid, nick) VALUES (?, ?, ?)";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, razaSeleccionada);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.setString(3, player.getName());
            stmt.executeUpdate();

            player.sendMessage("§aRaza asignada correctamente: " + razaSeleccionada);
            plugin.getLogger()
                    .info("Raza " + razaSeleccionada + " asignada correctamente al jugador " + player.getName());
            IniciarJugadorRaza.entregarPergamino(player, razaSeleccionada);
        } catch (Exception e) {
            player.sendMessage("§cError al asignar tu raza.");
            plugin.getLogger().severe("Error al asignar la raza en la base de datos.");
            e.printStackTrace();
        }
    }
}
