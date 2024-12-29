package me.ewahv1.plugin.Listeners.Pergaminos;

import me.ewahv1.plugin.Utils.PergaminoHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import me.ewahv1.plugin.Main;

import java.io.File;

public class DraconicoT1 implements PergaminoHandler {

    private final Main plugin;

    public DraconicoT1(Main plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration getConfig() {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("No se encontró el archivo Pergaminos.yml.");
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private int getConfigInt(String path, int defaultValue) {
        FileConfiguration config = getConfig();
        return config != null ? config.getInt(path, defaultValue) : defaultValue;
    }

    private double getConfigDouble(String path, double defaultValue) {
        FileConfiguration config = getConfig();
        return config != null ? config.getDouble(path, defaultValue) : defaultValue;
    }

    @Override
    public void onUse(Player player, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Activando pergamino DraconicoT1 para jugador: " + player.getName());

        if (isInCooldown(player)) {
            long remaining = getCooldownRemaining(player);
            sendActionBar(player,
                    ChatColor.RED + "Espera " + ChatColor.YELLOW + remaining + ChatColor.RED + " segundos.");
            plugin.getLogger()
                    .info("[DEBUG] El jugador " + player.getName() + " está en cooldown para este pergamino.");
            return;
        }

        applyGlowingToPlayer(player);
        setCooldown(player);
    }

    private void applyGlowingToPlayer(Player player) {
        int glowingPlayerDuration = getConfigInt("Pergaminos.Draconico.Tier1.glowing_player_during", 1) * 20;

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GlowingBlack");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("GlowingBlack");
            team.setColor(ChatColor.DARK_GRAY);
        }

        final Team finalTeam = team;

        finalTeam.addEntry(player.getName());
        player.setGlowing(true);

        sendActionBar(player, ChatColor.GREEN + "Has usado el pergamino 'Bola de Fuego'.");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGlowing(false);
            finalTeam.removeEntry(player.getName());

            launchFireball(player);
            sendActionBar(player, ChatColor.GOLD + "¡Lanzaste 'Bola de Fuego'!");
        }, glowingPlayerDuration);
    }

    private void launchFireball(Player player) {
        double initialSpeed = getConfigDouble("Pergaminos.Draconico.Tier1.initial_speed", 2.0);
        double gravityForce = getConfigDouble("Pergaminos.Draconico.Tier1.gravity_force", 0.2);
        double damage = getConfigDouble("Pergaminos.Draconico.Tier1.damage", 10.0);
        double explosionPower = getConfigDouble("Pergaminos.Draconico.Tier1.power_explosión", 4.0);

        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        Fireball fireball = player.getWorld().spawn(eyeLocation.add(direction.multiply(2)), Fireball.class);
        fireball.setShooter(player);
        fireball.setDirection(direction.multiply(initialSpeed));
        fireball.setYield((float) explosionPower);
        fireball.setIsIncendiary(false);

        fireball.setMetadata("damage", new FixedMetadataValue(plugin, damage));

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (fireball.isDead() || fireball.isOnGround()) {
                return;
            }
            Vector velocity = fireball.getVelocity();
            velocity.setY(velocity.getY() - gravityForce);
            fireball.setVelocity(velocity);
        }, 1L, 1L);
    }

    private boolean isInCooldown(Player player) {
        return player.hasMetadata("Draconico_cooldown") &&
                System.currentTimeMillis()
                        - player.getMetadata("Draconico_cooldown").get(0)
                                .asLong() < getConfigInt("Pergaminos.Draconico.Tier1.cooldown", 5) * 1000;
    }

    private void setCooldown(Player player) {
        player.setMetadata("Draconico_cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    private long getCooldownRemaining(Player player) {
        long lastUse = player.getMetadata("Draconico_cooldown").get(0).asLong();
        long cooldownTime = getConfigInt("Pergaminos.Draconico.Tier1.cooldown", 5) * 1000;
        return (cooldownTime - (System.currentTimeMillis() - lastUse)) / 1000;
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
