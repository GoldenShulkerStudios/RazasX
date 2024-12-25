package me.ewahv1.plugin.Listeners.Pergaminos;

import me.ewahv1.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class LeviathanT1 implements Listener {
    private final Main plugin;

    public LeviathanT1(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUseScroll(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Verificar si el item es válido y si es el pergamino de Leviathan Tier 1
        if (item == null || !item.hasItemMeta() || item.getItemMeta().getCustomModelData() != 5) {
            return; // No es el pergamino de Leviathan Tier 1
        }

        // Verificar si el jugador es de raza Leviathan
        if (!isLeviathan(player)) {
            sendActionBar(player, "&cNo puedes usar este pergamino, no eres de la raza Leviathan.");
            return;
        }

        FileConfiguration config = loadYamlFile("Pergaminos.yml");
        if (config == null)
            return;

        String basePath = "Pergaminos.Leviathan.Tier1.";
        int cooldown = config.getInt(basePath + "cooldown", 3);

        // Verificar cooldown
        if (player.hasMetadata("leviathan_t1_cooldown")) {
            long lastUse = player.getMetadata("leviathan_t1_cooldown").get(0).asLong();
            long timeSinceLastUse = (System.currentTimeMillis() - lastUse) / 1000;
            if (timeSinceLastUse < cooldown) {
                int remaining = (int) (cooldown - timeSinceLastUse);
                sendActionBar(player, "&cEl pergamino está en cooldown. Tiempo restante: " + remaining + "s");
                return;
            }
        }

        // Aplicar el efecto del pergamino
        sendActionBar(player, "&aHas usado el pergamino &9Revitalización de Agua&7.");
        player.setMetadata("leviathan_t1_cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis()));

        // Aplicar efectos
        applyScrollEffect(player, config, basePath);
    }

    private boolean isLeviathan(Player player) {
        FileConfiguration config = loadYamlFile("PlayerRazas.yml");
        if (config == null)
            return false;

        String playerUUID = player.getUniqueId().toString();
        return config.contains("razas.Leviathan." + playerUUID);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent
                        .fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }

    private void applyScrollEffect(Player player, FileConfiguration config, String basePath) {
        int glowingDuration = config.getInt(basePath + "glowing_player_during", 1) * 20;
        int effectDuration = config.getInt(basePath + "duration_effects", 10) * 20;

        // Animación de burbujas
        startWaterBubbleAnimation(player);

        // Aplicar efectos tras las burbujas
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyAquaGlowing(player, glowingDuration);
            player.setGlowing(true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setGlowing(false);
                removeFromTeam(player, "GlowingAqua");
            }, glowingDuration);

            applyEffects(player, config, basePath);
        }, glowingDuration);
    }

    private void applyAquaGlowing(Player player, int duration) {
        String teamName = "GlowingAqua";
        org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setColor(ChatColor.AQUA);
        }

        team.addEntry(player.getName());
    }

    private void removeFromTeam(Player player, String teamName) {
        org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);

        if (team != null && team.hasEntry(player.getName())) {
            team.removeEntry(player.getName());
        }
    }

    private void applyEffects(Player player, FileConfiguration config, String basePath) {
        for (String effectName : config.getStringList(basePath + "effects")) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
            if (effectType != null) {
                int duration = config.getInt(basePath + "duration_effects", 10) * 20;
                player.addPotionEffect(new PotionEffect(effectType, duration, 0));
            } else {
                plugin.getLogger().warning("Efecto desconocido en " + effectName);
            }
        }
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

                Location loc = player.getLocation();
                for (int i = 0; i < 360; i += 20) {
                    double radians = Math.toRadians(i);
                    double x = Math.cos(radians) * 1.0;
                    double z = Math.sin(radians) * 1.0;
                    Location particleLocation = loc.clone().add(x, height, z);
                    player.getWorld().spawnParticle(Particle.BUBBLE, particleLocation, 5, 0, 0, 0, 0);
                }

                height += 0.05;
                currentIteration++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private FileConfiguration loadYamlFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.getLogger().warning("No se encontró el archivo " + fileName);
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}