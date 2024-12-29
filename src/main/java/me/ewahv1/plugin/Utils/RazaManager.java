package me.ewahv1.plugin.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class RazaManager {

    private final Plugin plugin;
    private final Logger logger;

    public RazaManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Obtiene la raza de un jugador según la configuración.
     *
     * @param player El jugador para el cual obtener la raza.
     * @return La raza del jugador o null si no se encuentra.
     */
    public String obtenerRaza(Player player) {
        boolean useSQL = plugin.getConfig().getBoolean("sql", false);

        logger.info("Modo de almacenamiento configurado: " + (useSQL ? "SQL" : "YAML"));

        return useSQL ? obtenerRazaDesdeSQL(player) : obtenerRazaDesdeYAML(player);
    }

    /**
     * Obtiene la raza desde la base de datos SQL.
     *
     * @param player El jugador para el cual obtener la raza.
     * @return La raza del jugador o null si no se encuentra.
     */
    private String obtenerRazaDesdeSQL(Player player) {
        String raza = null;

        try (Connection connection = crearConexionSQL();
                PreparedStatement statement = connection.prepareStatement("SELECT raza FROM Razas WHERE uuid = ?")) {

            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    raza = resultSet.getString("raza");
                    logger.info("Raza encontrada en SQL para el jugador " + player.getName() + ": " + raza);
                } else {
                    logger.warning("No se encontró ninguna raza en SQL para el jugador " + player.getName());
                }
            }

        } catch (Exception e) {
            logger.severe("Error al obtener la raza desde SQL para el jugador " + player.getName());
            e.printStackTrace();
        }

        return raza;
    }

    /**
     * Obtiene la raza desde el archivo YAML.
     *
     * @param player El jugador para el cual obtener la raza.
     * @return La raza del jugador o null si no se encuentra.
     */
    private String obtenerRazaDesdeYAML(Player player) {
        File playerRazasFile = new File(plugin.getDataFolder(), "PlayerRazas.yml");

        if (!playerRazasFile.exists()) {
            logger.warning("No se encontró el archivo PlayerRazas.yml.");
            return null;
        }

        FileConfiguration playerRazasConfig = YamlConfiguration.loadConfiguration(playerRazasFile);

        String uuid = player.getUniqueId().toString();
        String path = "Razas";

        if (playerRazasConfig.contains(path)) {
            for (String raza : playerRazasConfig.getConfigurationSection(path).getKeys(false)) {
                if (playerRazasConfig.getString(path + "." + raza + "." + uuid) != null) {
                    logger.info("Raza encontrada en YAML para el jugador " + player.getName() + ": " + raza);
                    return raza;
                }
            }
        }

        logger.warning("No se encontró ninguna raza en YAML para el jugador " + player.getName());
        return null;
    }

    /**
     * Crea una conexión a la base de datos usando las credenciales del config.yml.
     *
     * @return Una conexión activa a la base de datos.
     * @throws Exception Si ocurre un error al conectarse.
     */
    private Connection crearConexionSQL() throws Exception {
        FileConfiguration config = plugin.getConfig();

        String host = config.getString("datasource.mySQLHost", "localhost");
        String port = config.getString("datasource.mySQLPort", "3306");
        String database = config.getString("datasource.mySQLDatabase", "minecraft");
        String user = config.getString("datasource.mySQLUsername", "root");
        String password = config.getString("datasource.mySQLPassword", "");

        if (host == null || database == null || user == null || password == null) {
            throw new IllegalArgumentException("Configuración de base de datos incompleta en config.yml.");
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        return DriverManager.getConnection(url, user, password);
    }
}
