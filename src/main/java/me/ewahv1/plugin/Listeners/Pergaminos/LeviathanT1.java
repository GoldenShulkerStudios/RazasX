package me.ewahv1.plugin.Listeners.Pergaminos;

import me.ewahv1.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.io.File;

public class LeviathanT1 {

    private final Main plugin;

    public LeviathanT1(Main plugin) {
        this.plugin = plugin;
    }

    public void onUse(Player player, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Activando pergamino LeviathanT1 para jugador: " + player.getName());

        int glowingDuration = (int) getConfigValue("glowing_player_during", 1.0) * 20;
        int effectDuration = (int) getConfigValue("duration_effects", 10.0) * 20;

        // Efecto visual de burbujas
        startWaterBubbleAnimation(player);

        // Aplicar efectos tras la animación
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyGlowingEffect(player, glowingDuration);
            applyConfiguredEffects(player, effectDuration);
        }, glowingDuration);
    }

    private void startWaterBubbleAnimation(Player player) {
        new BukkitRunnable() {
            double height = 0;
            int currentIteration = 0;
            final int iterations = 20;

            @Override
            public void run() {
                if (currentIteration >= iterations) {
                    this.cancel();
                    return;
                }

                // Animación
                currentIteration++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void applyGlowingEffect(Player player, int glowingDuration) {
        String teamName = "GlowingAqua";
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            plugin.getLogger().warning("[DEBUG] No se pudo obtener el ScoreboardManager.");
            return;
        }

        Scoreboard board = manager.getMainScoreboard();
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.setColor(ChatColor.AQUA);
        }

        final Team finalTeam = team; // Convertimos la variable en final

        finalTeam.addEntry(player.getName());
        player.setGlowing(true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGlowing(false);
            finalTeam.removeEntry(player.getName());
        }, glowingDuration);
    }

    private void applyConfiguredEffects(Player player, int effectDuration) {
        FileConfiguration config = getConfig();
        String basePath = "Pergaminos.Leviathan.Tier1.";

        if (config.contains(basePath + "effects")) {
            for (String effectName : config.getStringList(basePath + "effects")) {
                PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
                if (effectType != null) {
                    player.addPotionEffect(new PotionEffect(effectType, effectDuration, 0));
                } else {
                    plugin.getLogger().warning("[DEBUG] Efecto desconocido: " + effectName);
                }
            }
        }
    }

    private FileConfiguration getConfig() {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("[DEBUG] Archivo Pergaminos.yml no encontrado.");
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private double getConfigValue(String key, double defaultValue) {
        FileConfiguration config = getConfig();
        String basePath = "Pergaminos.Leviathan.Tier1.";
        return config.getDouble(basePath + key, defaultValue);
    }
}
