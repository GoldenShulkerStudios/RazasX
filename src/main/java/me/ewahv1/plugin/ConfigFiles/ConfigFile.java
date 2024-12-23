package me.ewahv1.plugin.ConfigFiles;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigFile {

    public static void createConfig(JavaPlugin plugin) {
        // Crear config.yml si no existe
        plugin.saveDefaultConfig();

        // Crear la carpeta del plugin si no existe
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Crear y cargar InventarioRazaSelect.yml
        File razaSelectFile = new File(plugin.getDataFolder(), "InventarioRazaSelect.yml");
        if (!razaSelectFile.exists()) {
            plugin.saveResource("InventarioRazaSelect.yml", false);
        } else {
            YamlConfiguration.loadConfiguration(razaSelectFile);
        }

        // Crear y cargar razas.yml
        File razasFile = new File(plugin.getDataFolder(), "razas.yml");
        if (!razasFile.exists()) {
            plugin.saveResource("razas.yml", false);
        } else {
            YamlConfiguration.loadConfiguration(razasFile);
        }

        // Crear y cargar Pergaminos.yml
        File pergaminosFile = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!pergaminosFile.exists()) {
            plugin.saveResource("Pergaminos.yml", false);
        } else {
            YamlConfiguration.loadConfiguration(pergaminosFile);
        }

        // Validar si los archivos se cargaron correctamente (opcional)
        plugin.getLogger().info("Archivos de configuración cargados correctamente:");
        plugin.getLogger().info("- config.yml");
        plugin.getLogger().info("- InventarioRazaSelect.yml");
        plugin.getLogger().info("- razas.yml");
        plugin.getLogger().info("- Pergaminos.yml");
    }
}
