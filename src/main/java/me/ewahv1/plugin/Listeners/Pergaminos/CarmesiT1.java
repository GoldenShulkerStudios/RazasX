package me.ewahv1.plugin.Listeners.Pergaminos;

import me.ewahv1.plugin.Utils.PergaminoHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import me.ewahv1.plugin.Utils.RazaManager;

import java.io.File;

public class CarmesiT1 implements PergaminoHandler {

    private final JavaPlugin plugin;

    public CarmesiT1(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onUse(Player player, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Intentando activar el pergamino Carmesí T1 para " + player.getName());

        // Lógica del pergamino (sin cambios)
        if (!isPlayerOfRace(player)) {
            sendActionBar(player, ChatColor.RED + "Este pergamino solo puede ser usado por la raza Carmesí.");
            plugin.getLogger().info("[DEBUG] El jugador " + player.getName() + " no pertenece a la raza Carmesí.");
            return;
        }

        if (isInCooldown(player)) {
            long remaining = getCooldownRemaining(player);
            sendActionBar(player,
                    ChatColor.RED + "Espera " + ChatColor.YELLOW + remaining + ChatColor.RED + " segundos.");
            plugin.getLogger()
                    .info("[DEBUG] El jugador " + player.getName() + " está en cooldown para este pergamino.");
            return;
        }

        int range = getConfigValue("range", 10);

        Entity target = getTargetEntity(player, range);
        if (target == null || !(target instanceof LivingEntity)) {
            sendActionBar(player, ChatColor.RED + "No estás apuntando a una entidad válida.");
            plugin.getLogger().info("[DEBUG] No se encontró un objetivo válido para el pergamino Carmesí.");
            return;
        }

        applyGlowingEffect((LivingEntity) target, player);
        setCooldown(player);
    }

    private void applyGlowingEffect(LivingEntity target, Player player) {
        int glowingDuration = getConfigValue("glowing_target_during", 1) * 20;
        double damage = getConfigValue("damage", 5.0);

        Team team = getOrCreateTeam("GlowingRed", ChatColor.RED);

        if (target instanceof Player) {
            team.addEntry(((Player) target).getName());
        } else {
            team.addEntry(target.getUniqueId().toString());
        }

        target.setGlowing(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            target.setGlowing(false);
            team.removeEntry(target instanceof Player ? ((Player) target).getName() : target.getUniqueId().toString());

            if (target.isValid()) {
                target.damage(damage);
                plugin.getLogger().info("[DEBUG] Daño aplicado al objetivo: " + damage);
            }

            generateParticlesToPlayer(target, player);
        }, glowingDuration);
    }

    private void generateParticlesToPlayer(LivingEntity target, Player player) {
        Location targetLocation = target.getLocation().add(0, 1, 0);
        World world = target.getWorld();

        new BukkitRunnable() {
            private final double speed = 0.5;

            @Override
            public void run() {
                Location playerLocation = player.getLocation().add(0, 1, 0);
                Vector direction = playerLocation.toVector().subtract(targetLocation.toVector()).normalize();
                targetLocation.add(direction.multiply(speed));

                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, targetLocation, 5,
                        new Particle.DustTransition(Color.RED, Color.WHITE, 1.5f));

                if (targetLocation.distanceSquared(playerLocation) < 1.0) {
                    applyGlowingToPlayer(player);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void applyGlowingToPlayer(Player player) {
        int glowingPlayerDuration = getConfigValue("glowing_player_during", 1) * 20;
        double heal = getConfigValue("heal", 5.0);

        Team team = getOrCreateTeam("GlowingRed", ChatColor.RED);
        team.addEntry(player.getName());
        player.setGlowing(true);

        player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
        plugin.getLogger().info("[DEBUG] Salud del jugador incrementada en: " + heal);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGlowing(false);
            team.removeEntry(player.getName());
        }, glowingPlayerDuration);
    }

    private Team getOrCreateTeam(String name, ChatColor color) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
            team.setColor(color);
        }
        return team;
    }

    private Entity getTargetEntity(Player player, int range) {
        return player.getNearbyEntities(range, range, range).stream()
                .filter(e -> e instanceof LivingEntity && player.hasLineOfSight(e))
                .min((e1, e2) -> Double.compare(player.getLocation().distanceSquared(e1.getLocation()),
                        player.getLocation().distanceSquared(e2.getLocation())))
                .orElse(null);
    }

    private boolean isPlayerOfRace(Player player) {
        String race = new RazaManager(plugin).obtenerRaza(player);
        return "Carmesi".equalsIgnoreCase(race);
    }

    private boolean isInCooldown(Player player) {
        return player.hasMetadata("Carmesi_cooldown") &&
                System.currentTimeMillis()
                        - player.getMetadata("Carmesi_cooldown").get(0).asLong() < getConfigValue("cooldown", 5) * 1000;
    }

    private void setCooldown(Player player) {
        player.setMetadata("Carmesi_cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    private long getCooldownRemaining(Player player) {
        long lastUse = player.getMetadata("Carmesi_cooldown").get(0).asLong();
        long cooldownTime = getConfigValue("cooldown", 5) * 1000;
        return (cooldownTime - (System.currentTimeMillis() - lastUse)) / 1000;
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    private <T> T getConfigValue(String key, T defaultValue) {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("[DEBUG] Archivo Pergaminos.yml no encontrado.");
            return defaultValue;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String basePath = "Pergaminos.Carmesi.Tier1.";

        if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(config.getInt(basePath + key, (Integer) defaultValue));
        } else if (defaultValue instanceof Double) {
            return (T) Double.valueOf(config.getDouble(basePath + key, (Double) defaultValue));
        } else if (defaultValue instanceof String) {
            return (T) config.getString(basePath + key, (String) defaultValue);
        }
        return defaultValue;
    }
}
