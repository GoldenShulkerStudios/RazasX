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
import me.ewahv1.plugin.Utils.InteractManager;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {

    private static Main instance;

    private static final String PLUGIN_ACTIVATED_MESSAGE = ChatColor.AQUA
            + "==========================================\n" +
            ChatColor.GREEN + "       RazasX Plugin Activado\n" +
            ChatColor.YELLOW + "          Versión: " + ChatColor.WHITE + "%VERSION%\n" +
            ChatColor.YELLOW + "          Autor: " + ChatColor.WHITE + "GoldenShulkerStudios\n" +
            ChatColor.AQUA + "==========================================";

    private static final String PLUGIN_DEACTIVATED_MESSAGE = ChatColor.RED
            + "==========================================\n" +
            ChatColor.DARK_RED + "       RazasX Plugin Desactivado\n" +
            ChatColor.RED + "==========================================";

    @Override
    public void onEnable() {
        instance = this;

        // Configurar el plugin
        AsignarRazaPlayer.setPlugin(this);
        ConfigFile.createConfig(this);

        // Registrar el comando /Razas
        getCommand("Razas").setExecutor(new RazaCommand(getDataFolder()));

        // Registrar listeners
        registerListeners();

        // Enviar mensaje de inicio personalizado
        sendStartupMessage();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AtributosRazaCarmesi(this), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaDraconicos(this), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaElfo(this), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaHumano(this), this);
        getServer().getPluginManager().registerEvents(new AtributosRazaLeviathan(this), this);
        getServer().getPluginManager().registerEvents(new RazaSelectInventory(), this);
        getServer().getPluginManager().registerEvents(new RazaRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractManager(this), this);
    }

    private void sendStartupMessage() {
        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender()
                .sendMessage(PLUGIN_ACTIVATED_MESSAGE.replace("%VERSION%", getDescription().getVersion()));
        getServer().getConsoleSender().sendMessage("");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage(PLUGIN_DEACTIVATED_MESSAGE);
        getServer().getConsoleSender().sendMessage("");
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
