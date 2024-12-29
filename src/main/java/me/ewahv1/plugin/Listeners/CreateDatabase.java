package me.ewahv1.plugin.Listeners;

import me.ewahv1.plugin.Main; // Importar la clase principal
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.bukkit.plugin.java.JavaPlugin;

public class CreateDatabase {

    public static Connection getConnection() throws Exception {
        String host = Main.getInstance().getConfig().getString("datasource.mySQLHost");
        String port = Main.getInstance().getConfig().getString("datasource.mySQLPort");
        String database = Main.getInstance().getConfig().getString("datasource.mySQLDatabase");
        String username = Main.getInstance().getConfig().getString("datasource.mySQLUsername");
        String password = Main.getInstance().getConfig().getString("datasource.mySQLPassword");

        if (host == null || port == null || database == null || username == null || password == null) {
            throw new Exception("La configuración de la base de datos está incompleta.");
        }

        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
    }

    public static void verificarYCrearDatabase(JavaPlugin plugin) {
        try (Connection connection = getConnection();
                Statement statement = connection.createStatement()) {

            // Crear la tabla 'Razas' con los campos necesarios
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Razas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "raza VARCHAR(50), " +
                    "uuid VARCHAR(36), " +
                    "nick VARCHAR(16))");
        } catch (Exception e) {
            plugin.getLogger().severe("Error al verificar y crear la base de datos.");
            e.printStackTrace();
        }
    }
}
