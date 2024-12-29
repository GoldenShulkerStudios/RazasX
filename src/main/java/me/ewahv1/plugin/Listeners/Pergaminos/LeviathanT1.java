package me.ewahv1.plugin.Listeners.Pergaminos;

import me.ewahv1.plugin.Utils.PergaminoHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import me.ewahv1.plugin.Main;

import java.io.File;
import java.util.List;

public class LeviathanT1 implements PergaminoHandler {

    private final Main plugin;

    public LeviathanT1(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onUse(Player player, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Activando pergamino LeviathanT1 para jugador: " + player.getName());

        if (isInCooldown(player)) {
            long remaining = getCooldownRemaining(player);
            sendActionBar(player,
                    ChatColor.RED + "Espera " + ChatColor.YELLOW + remaining + ChatColor.RED + " segundos.");
            plugin.getLogger()
                    .info("[DEBUG] El jugador " + player.getName() + " está en cooldown para este pergamino.");
            return;
        }

        int glowingDuration = getConfigValue("glowing_player_during", 10) * 20;
        int effectDuration = getConfigValue("duration_effects", 10) * 20;

        // Iniciar animación de burbujas
        startWaterBubbleAnimation(player, () -> {
            applyGlowingEffect(player, glowingDuration);
            applyConfiguredEffects(player, effectDuration);
        });

        setCooldown(player);

        // Notificar al jugador
        String pergaminoName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "(desconocido)";
        player.sendMessage(ChatColor.GREEN + "Has activado el pergamino " + ChatColor.GOLD + pergaminoName
                + ChatColor.GREEN + " exitosamente.");
    }

    private void startWaterBubbleAnimation(Player player, Runnable onComplete) {
        new BukkitRunnable() {
            double height = 0.0; // Altura inicial desde los pies del jugador
            int iterations = 10; // Duración en ticks (10 ticks = medio segundo)
            int currentIteration = 0;

            @Override
            public void run() {
                if (currentIteration >= iterations) { // Cuando se alcance el número de iteraciones
                    this.cancel(); // Detener la animación
                    onComplete.run(); // Ejecutar la acción que viene después de la animación
                    return;
                }

                // Generar burbujas en un radio alrededor del jugador
                double radius = 1; // Radio del círculo (hazlo más pequeño para compactar el círculo)
                // Ejemplo: para hacerlo más compacto, cambia esto a 1.0 o 0.8
                int particles = 20; // Número de partículas por círculo
                // Disminuye el número de partículas para hacer el círculo más disperso
                double angleIncrement = (2 * Math.PI) / particles; // Incremento del ángulo para cada partícula

                for (int i = 0; i < particles; i++) { // Iterar para crear partículas en el círculo
                    double angle = i * angleIncrement; // Calcular el ángulo actual
                    double x = radius * Math.cos(angle); // Coordenada X basada en el radio y el ángulo
                    double z = radius * Math.sin(angle); // Coordenada Z basada en el radio y el ángulo

                    // Generar una partícula en la posición calculada
                    player.getWorld().spawnParticle(Particle.BUBBLE,
                            player.getLocation().add(x, height, z), // Posición de la partícula
                            1, // Cantidad de partículas
                            0, 0, 0, 0); // Sin dispersión adicional
                }

                height += 0.1; // Subir las burbujas gradualmente (disminuir para hacer la subida más lenta)
                currentIteration++; // Incrementar la iteración actual
            }
        }.runTaskTimer(plugin, 0, 1); // Iniciar la tarea con un retraso de 0 ticks y repetir cada tick
    }

    private void applyGlowingEffect(Player player, int glowingDuration) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GlowingAqua");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("GlowingAqua");
            team.setColor(ChatColor.AQUA);
        }

        final Team finalTeam = team;
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
                    plugin.getLogger().info("[DEBUG] Efecto aplicado: " + effectName);
                } else {
                    plugin.getLogger().warning("[DEBUG] Efecto desconocido: " + effectName);
                }
            }
        }
    }

    private boolean isInCooldown(Player player) {
        return player.hasMetadata("Leviathan_cooldown") &&
                System.currentTimeMillis()
                        - player.getMetadata("Leviathan_cooldown").get(0).asLong() < getConfigCooldown()
                                * 1000;
    }

    private void setCooldown(Player player) {
        player.setMetadata("Leviathan_cooldown", new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    private long getCooldownRemaining(Player player) {
        long lastUse = player.getMetadata("Leviathan_cooldown").get(0).asLong();
        long cooldownTime = getConfigCooldown() * 1000;
        return (cooldownTime - (System.currentTimeMillis() - lastUse)) / 1000;
    }

    private int getConfigCooldown() {
        FileConfiguration config = getConfig();
        return config.getInt("Pergaminos.Leviathan.Tier1.cooldown", 3); // Tiempo de cooldown en segundos
    }

    private FileConfiguration getConfig() {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("[DEBUG] Archivo Pergaminos.yml no encontrado.");
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private int getConfigValue(String key, int defaultValue) {
        FileConfiguration config = getConfig();
        String basePath = "Pergaminos.Leviathan.Tier1.";
        return config.getInt(basePath + key, defaultValue);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
