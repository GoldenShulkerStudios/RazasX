package me.ewahv1.plugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class RazaCommand implements CommandExecutor {

    private final File configFile;

    public RazaCommand(File pluginFolder) {
        this.configFile = new File(pluginFolder, "InventarioRazaSelect.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        // Verifica si el archivo de configuración existe
        if (!configFile.exists()) {
            player.sendMessage("§cEl archivo de configuración de Razas no se encontró.");
            return true;
        }

        // Cargar el archivo YAML
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Construir el inventario basado en el archivo
        String title = Objects.requireNonNull(config.getString("panels.SelectRaza.title", "Inventario de Razas"))
                .replace("&", "§");
        int rows = config.getInt("panels.SelectRaza.rows", 1);
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);

        // Obtener los ítems y agregarlos al inventario
        if (config.contains("panels.SelectRaza.item")) {
            for (String key : config.getConfigurationSection("panels.SelectRaza.item").getKeys(false)) {
                int slot = Integer.parseInt(key);
                String materialName = config.getString("panels.SelectRaza.item." + key + ".material", "STONE");
                int stack = config.getInt("panels.SelectRaza.item." + key + ".stack", 1);
                String name = config.getString("panels.SelectRaza.item." + key + ".name", "");
                List<String> lore = config.getStringList("panels.SelectRaza.item." + key + ".lore");

                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null) {
                    Bukkit.getLogger().warning("Material inválido en el slot " + slot + ": " + materialName);
                    continue;
                }

                ItemStack item = new ItemStack(material, stack);
                ItemMeta meta = item.getItemMeta();

                // Configurar el nombre del ítem si no está vacío
                if (meta != null) {
                    if (!name.isEmpty()) {
                        meta.setDisplayName(name.replace("&", "§"));
                    }

                    // Configurar el lore si está presente
                    if (!lore.isEmpty()) {
                        for (int i = 0; i < lore.size(); i++) {
                            lore.set(i, lore.get(i).replace("&", "§")); // Convertir colores en el lore
                        }
                        meta.setLore(lore);
                    }

                    item.setItemMeta(meta);
                }

                inventory.setItem(slot, item);
            }
        }

        // Mostrar el inventario al jugador
        player.openInventory(inventory);
        return true;
    }
}
