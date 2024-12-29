package me.ewahv1.plugin.Listeners.Pergaminos;

import me.ewahv1.plugin.Utils.PergaminoHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.scheduler.BukkitRunnable;
import me.ewahv1.plugin.Main;

import java.io.File;
import java.util.List;

public class HumanoT1 implements PergaminoHandler {

    private final Main plugin;

    public HumanoT1(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onUse(Player player, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Activando pergamino HumanoT1 para jugador: " + player.getName());

        if (isInCooldown(player)) {
            long remaining = getCooldownRemaining(player);
            sendActionBar(player,
                    ChatColor.RED + "Espera " + ChatColor.YELLOW + remaining + ChatColor.RED + " segundos.");
            plugin.getLogger()
                    .info("[DEBUG] El jugador " + player.getName() + " está en cooldown para este pergamino.");
            return;
        }

        applyGlowingGreen(player);
        applyConfiguredEffects(player);
        setCooldown(player);

        // Notificar al jugador
        String pergaminoName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "(desconocido)";
        player.sendMessage(ChatColor.GREEN + "Has activado el pergamino " + ChatColor.GOLD + pergaminoName
                + ChatColor.GREEN + " exitosamente.");
    }

    private void applyGlowingGreen(Player player) {
        FileConfiguration config = getConfig();
        int glowingDuration = config.getInt("Pergaminos.Humano.Tier1.glowing_player_during", 1) * 20;

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GlowingGreen");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("GlowingGreen");
            team.setColor(ChatColor.GREEN);
        }

        final Team finalTeam = team;
        finalTeam.addEntry(player.getName());
        player.setGlowing(true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGlowing(false);
            finalTeam.removeEntry(player.getName());
        }, glowingDuration);
    }

    private void applyConfiguredEffects(Player player) {
        FileConfiguration config = getConfig();
        String basePath = "Pergaminos.Humano.Tier1.";
        int effectDuration = config.getInt(basePath + "duration_effects", 10) * 20; // Convertir a ticks

        List<String> effects = config.getStringList(basePath + "effects");
        for (String effectName : effects) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
            if (effectType != null) {
                player.addPotionEffect(new PotionEffect(effectType, effectDuration, 0));
                plugin.getLogger().info("[DEBUG] Efecto aplicado: " + effectName);
            } else {
                plugin.getLogger().warning("[DEBUG] Efecto desconocido: " + effectName);
            }
        }
    }

    private boolean isInCooldown(Player player) {
        return player.hasMetadata("Humano_cooldown") &&
                System.currentTimeMillis() - player.getMetadata("Humano_cooldown").get(0).asLong() < getConfigCooldown()
                        * 1000;
    }

    private void setCooldown(Player player) {
        player.setMetadata("Humano_cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    private long getCooldownRemaining(Player player) {
        long lastUse = player.getMetadata("Humano_cooldown").get(0).asLong();
        long cooldownTime = getConfigCooldown() * 1000;
        return (cooldownTime - (System.currentTimeMillis() - lastUse)) / 1000;
    }

    private int getConfigCooldown() {
        FileConfiguration config = getConfig();
        return config.getInt("Pergaminos.Humano.Tier1.cooldown", 5); // Tiempo de cooldown en segundos
    }

    private FileConfiguration getConfig() {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("No se encontró el archivo Pergaminos.yml.");
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
