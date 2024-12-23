package me.ewahv1.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import me.ewahv1.plugin.Commands.RazaCommand;
import me.ewahv1.plugin.ConfigFiles.ConfigFile;
import me.ewahv1.plugin.Listeners.AsignarRazaPlayer;
import me.ewahv1.plugin.Listeners.RazaSelectInventory;
import me.ewahv1.plugin.Listeners.Atributos.AtributosRazaCarmesi;
import me.ewahv1.plugin.Listeners.Atributos.AtributosRazaDraconicos;
import me.ewahv1.plugin.Listeners.Atributos.AtributosRazaElfo;
import me.ewahv1.plugin.Listeners.Atributos.AtributosRazaHumano;
import me.ewahv1.plugin.Listeners.Atributos.AtributosRazaLeviathan;
import me.ewahv1.plugin.Listeners.RazaRespawnListener;
import me.ewahv1.plugin.Listeners.Pergaminos.CarmesiT1;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        // Configurar el plugin en AsignarRazaPlayer
        AsignarRazaPlayer.setPlugin(this);

        // Crear archivos de configuración al iniciar el plugin
        ConfigFile.createConfig(this);

        // Registrar el comando /razas
        getCommand("razas").setExecutor(new RazaCommand(getDataFolder()));

        // Registrar listeners
        getServer().getPluginManager().registerEvents(new RazaSelectInventory(), this);
        getServer().getPluginManager().registerEvents(new RazaRespawnListener(), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaDraconicos(), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaCarmesi(), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaLeviathan(), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaHumano(), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaElfo(), this);
        getServer().getPluginManager().registerEvents(new CarmesiT1(this), this);

        getLogger().info("Gurumisland plugin activado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Gurumisland plugin desactivado correctamente.");
    }

    /**
     * Obtener la instancia del plugin
     * 
     * @return instancia de Main
     */
    public static Main getInstance() {
        return instance;
    }
}
