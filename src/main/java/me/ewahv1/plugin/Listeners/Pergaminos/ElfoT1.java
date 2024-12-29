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
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Team;
import org.bukkit.scheduler.BukkitRunnable;
import me.ewahv1.plugin.Main;

import java.io.File;
import java.util.List;

public class ElfoT1 implements PergaminoHandler, Listener {

    private final Main plugin;

    public ElfoT1(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onUse(Player player, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Activando pergamino Elfo T1 para jugador: " + player.getName());

        if (isInCooldown(player)) {
            long remaining = getCooldownRemaining(player);
            sendActionBar(player,
                    ChatColor.RED + "Espera " + ChatColor.YELLOW + remaining + ChatColor.RED + " segundos.");
            plugin.getLogger()
                    .info("[DEBUG] El jugador " + player.getName() + " está en cooldown para este pergamino.");
            return;
        }

        applyEffects(player);
        applyGlowingYellow(player);
        player.setMetadata("ElfoT1_active", new FixedMetadataValue(plugin, true));
        setCooldown(player);

        // Duración del efecto de flechas dobles
        int durationEffects = getConfig().getInt("Pergaminos.Elfo.Tier1.duration_effects", 10) * 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                player.removeMetadata("ElfoT1_active", plugin);
                plugin.getLogger().info("[DEBUG] Efecto de daño x2 en proyectiles terminado para: " + player.getName());
            }
        }.runTaskLater(plugin, durationEffects);

        // Notificar al jugador
        String pergaminoName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "(desconocido)";
        player.sendMessage(ChatColor.GREEN + "Has activado el pergamino " + ChatColor.GOLD + pergaminoName
                + ChatColor.GREEN + " exitosamente.");
    }

    private void applyGlowingYellow(Player player) {
        FileConfiguration config = getConfig();
        int glowingDuration = config.getInt("Pergaminos.Elfo.Tier1.glowing_player_during", 1) * 20;

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GlowingYellow");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("GlowingYellow");
            team.setColor(ChatColor.GOLD);
        }

        final Team finalTeam = team; // Declaración final para usar en la lambda
        finalTeam.addEntry(player.getName());
        player.setGlowing(true);

        // Remover glowing después de la duración configurada
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGlowing(false);
            finalTeam.removeEntry(player.getName());
        }, glowingDuration);
    }

    private void applyEffects(Player player) {
        FileConfiguration config = getConfig();

        String basePath = "Pergaminos.Elfo.Tier1.";
        int durationEffects = config.getInt(basePath + "duration_effects", 10) * 20; // Convertir a ticks

        List<String> effects = config.getStringList(basePath + "effects");
        for (String effectName : effects) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
            if (effectType != null) {
                player.addPotionEffect(new PotionEffect(effectType, durationEffects, 0));
                plugin.getLogger().info("[DEBUG] Efecto aplicado: " + effectName);
            } else {
                plugin.getLogger().warning("[DEBUG] Efecto desconocido: " + effectName);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity projectile = event.getEntity();

        if (projectile instanceof Projectile proj) { // Casting seguro
            ProjectileSource source = proj.getShooter();

            if (source instanceof Player player && player.hasMetadata("ElfoT1_active")) {
                FileConfiguration config = getConfig();
                int slowDuration = config.getInt("Pergaminos.Elfo.Tier1.duration_slow_target", 5) * 20;

                if (event.getHitEntity() instanceof LivingEntity target) {
                    double baseDamage = proj.getMetadata("damage").stream()
                            .mapToDouble(meta -> meta.asDouble())
                            .findFirst()
                            .orElse(1.0); // Valor predeterminado si no hay metadata

                    double newDamage = baseDamage * 2.0; // Duplicar el daño
                    target.damage(newDamage);

                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slowDuration, 1));
                    plugin.getLogger().info("[DEBUG] Proyectil impactó:");
                    plugin.getLogger().info("[DEBUG] Objetivo: " + target.getName());
                    plugin.getLogger().info("[DEBUG] Daño base: " + baseDamage);
                    plugin.getLogger().info("[DEBUG] Nuevo daño con x2: " + newDamage);
                }
            }
        }
    }

    private boolean isInCooldown(Player player) {
        return player.hasMetadata("Elfo_cooldown") &&
                System.currentTimeMillis() - player.getMetadata("Elfo_cooldown").get(0).asLong() < getConfigCooldown()
                        * 1000;
    }

    private void setCooldown(Player player) {
        player.setMetadata("Elfo_cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    private long getCooldownRemaining(Player player) {
        long lastUse = player.getMetadata("Elfo_cooldown").get(0).asLong();
        long cooldownTime = getConfigCooldown() * 1000;
        return (cooldownTime - (System.currentTimeMillis() - lastUse)) / 1000;
    }

    private int getConfigCooldown() {
        FileConfiguration config = getConfig();
        return config.getInt("Pergaminos.Elfo.Tier1.cooldown", 5); // Tiempo de cooldown en segundos
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
