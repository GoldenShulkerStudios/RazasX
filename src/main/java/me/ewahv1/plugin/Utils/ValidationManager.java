package me.ewahv1.plugin.Utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.event.block.Action;

import java.io.File;

public class ValidationManager implements Listener {

    private final Plugin plugin;
    private final RazaManager razaManager;

    public ValidationManager(Plugin plugin) {
        this.plugin = plugin;
        this.razaManager = new RazaManager(plugin);
    }

    public boolean isValidInteraction(PlayerInteractEvent event) {
        return event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
    }

    public boolean isValidPergamino(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER)
            return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData() || !meta.hasDisplayName())
            return false;

        return getPergaminoConfig(meta.getDisplayName(), meta.getCustomModelData()) != null;
    }

    public String getPergaminoRaza(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;

        PergaminoConfig config = getPergaminoConfig(meta.getDisplayName(), meta.getCustomModelData());
        return config != null ? config.getRaza() : null;
    }

    public boolean isPlayerOfRace(Player player, String race) {
        String playerRace = razaManager.obtenerRaza(player);
        return race.equalsIgnoreCase(playerRace);
    }

    public boolean isInCooldown(Player player, String race, ItemStack item) {
        PergaminoConfig config = getPergaminoConfig(item);
        if (config == null)
            return false;

        int cooldownTime = config.getCooldown() * 1000;
        if (!player.hasMetadata(race + "_cooldown"))
            return false;

        long lastUse = player.getMetadata(race + "_cooldown").get(0).asLong();
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }

    public void sendCooldownMessage(Player player, String race, ItemStack item) {
        PergaminoConfig config = getPergaminoConfig(item);
        if (config == null)
            return;

        int cooldownTime = config.getCooldown() * 1000;
        long lastUse = player.getMetadata(race + "_cooldown").get(0).asLong();
        long timeLeft = (cooldownTime - (System.currentTimeMillis() - lastUse)) / 1000;

        sendActionBar(player, ChatColor.RED + "Espera " + ChatColor.YELLOW + timeLeft + ChatColor.RED
                + " segundos para usar este pergamino.");
    }

    public void activatePergamino(Player player, String race, ItemStack item) {
        player.setMetadata(race + "_cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis()));

        String pergaminoName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "(desconocido)";
        sendActionBar(player, ChatColor.GREEN + "Has usado el pergamino " + ChatColor.GOLD + pergaminoName
                + ChatColor.GREEN + " con éxito.");
        plugin.getLogger()
                .info(player.getName() + " ha usado un pergamino de raza " + race + " llamado " + pergaminoName + ".");
    }

    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }

    private PergaminoConfig getPergaminoConfig(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;
        return getPergaminoConfig(meta.getDisplayName(), meta.getCustomModelData());
    }

    private PergaminoConfig getPergaminoConfig(String displayName, int customModelData) {
        File config = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!config.exists())
            return null;

        FileConfiguration pergaminosConfig = YamlConfiguration.loadConfiguration(config);

        for (String raza : pergaminosConfig.getConfigurationSection("Pergaminos").getKeys(false)) {
            for (String tier : pergaminosConfig.getConfigurationSection("Pergaminos." + raza).getKeys(false)) {
                String name = ChatColor.translateAlternateColorCodes('&',
                        pergaminosConfig.getString("Pergaminos." + raza + "." + tier + ".name"));
                int cmd = pergaminosConfig.getInt("Pergaminos." + raza + "." + tier + ".customModelData");

                if (displayName.equals(name) && customModelData == cmd) {
                    int cooldown = pergaminosConfig.getInt("Pergaminos." + raza + "." + tier + ".cooldown", 0);
                    return new PergaminoConfig(raza, tier, cooldown);
                }
            }
        }
        return null;
    }

    private static class PergaminoConfig {
        private final String raza;
        private final String tier;
        private final int cooldown;

        public PergaminoConfig(String raza, String tier, int cooldown) {
            this.raza = raza;
            this.tier = tier;
            this.cooldown = cooldown;
        }

        public String getRaza() {
            return raza;
        }

        public String getTier() {
            return tier;
        }

        public int getCooldown() {
            return cooldown;
        }
    }
}
