package me.ewahv1.plugin.Listeners.Pergaminos;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DraconicoT1 implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private String itemName;
    private int customModelData;
    private int cooldownTime;
    private int range;
    private float poderExplosion;

    public DraconicoT1(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigValues();
    }

    private void loadConfigValues() {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("No se encontró el archivo Pergaminos.yml. Usando valores predeterminados.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String basePath = "Pergaminos.Draconico.Tier1.";

        itemName = ChatColor.translateAlternateColorCodes('&',
                config.getString(basePath + "name", "&8&lBola de Fuego"));
        customModelData = config.getInt(basePath + "customModelData", 2);
        cooldownTime = config.getInt(basePath + "cooldown", 3) * 1000;
        range = config.getInt(basePath + "range", 10);
        poderExplosion = (float) config.getDouble(basePath + "power_explosión", 4.0);

        plugin.getLogger().info("Configuración Dracónico cargada: name=" + itemName +
                ", customModelData=" + customModelData +
                ", cooldown=" + cooldownTime + "ms, range=" + range + ", power_explosión=" + poderExplosion);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && isValidPergamino(item)) {

            // Verificación de raza Dracónico
            if (!isDraconico(player)) {
                sendActionBar(player, ChatColor.DARK_RED + "Solo los " + ChatColor.GOLD + "Dracónicos"
                        + ChatColor.DARK_RED + " pueden usar este pergamino.");
                return;
            }

            // Verificación de cooldown
            if (isInCooldown(player)) {
                long timeLeft = (cooldowns.get(player.getUniqueId()) + cooldownTime - System.currentTimeMillis())
                        / 1000;
                sendActionBar(player, ChatColor.RED + "Espera " + ChatColor.YELLOW + timeLeft + ChatColor.RED
                        + " segundos para usar " + ChatColor.GOLD + "'Bola de Fuego'" + ChatColor.RED + ".");
                return;
            }

            // Activación del pergamino
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            sendActionBar(player, ChatColor.GREEN + "Has usado el pergamino: " + ChatColor.GOLD + "'Bola de Fuego'"
                    + ChatColor.GREEN + " con éxito.");

            // Aplicar efecto
            applyGlowingToPlayer(player);
        }
    }

    private void applyGlowingToPlayer(Player player) {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        int glowingPlayerDuration = config.getInt("Pergaminos.Draconico.Tier1.glowing_player_during", 1) * 20; // Convertir
                                                                                                               // a
                                                                                                               // ticks

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GlowingBlack");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("GlowingBlack");
            team.setColor(ChatColor.DARK_GRAY);
        }

        final Team finalTeam = team;

        finalTeam.addEntry(player.getName());
        player.setGlowing(true);

        sendActionBar(player, ChatColor.GREEN + "Has usado el pergamino 'Bola de Fuego'.");

        // Programar el lanzamiento del proyectil después de terminar el glowing
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGlowing(false);
            finalTeam.removeEntry(player.getName());

            // Acción: lanzar el proyectil
            launchFireball(player);
            sendActionBar(player, ChatColor.GOLD + "¡Lanzaste 'Bola de Fuego'!");
        }, glowingPlayerDuration);
    }

    private void launchFireball(Player player) {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String basePath = "Pergaminos.Draconico.Tier1.";
        double initialSpeed = config.getDouble(basePath + "initial_speed", 2.0);
        double gravityForce = config.getDouble(basePath + "gravity_force", 0.2);
        double damage = config.getDouble(basePath + "damage", 10.0);
        double explosionPower = config.getDouble(basePath + "power_explosión", 4.0);

        plugin.getLogger().info("[DEBUG] Lanzando bola de fuego:");
        plugin.getLogger().info("Velocidad inicial: " + initialSpeed);
        plugin.getLogger().info("Fuerza de gravedad: " + gravityForce);
        plugin.getLogger().info("Daño directo: " + damage);
        plugin.getLogger().info("Poder de explosión: " + explosionPower);

        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        double angle = Math.toRadians(45);
        direction.setY(Math.tan(angle));

        Fireball fireball = player.getWorld().spawn(eyeLocation.add(direction.multiply(2)), Fireball.class);
        fireball.setShooter(player);
        fireball.setDirection(direction.multiply(initialSpeed));
        fireball.setYield((float) explosionPower);
        fireball.setIsIncendiary(false);

        fireball.setMetadata("damage", new FixedMetadataValue(plugin, damage));

        plugin.getLogger().info("[DEBUG] Bola de fuego lanzada desde: " + eyeLocation.toString());

        // Gravedad personalizada con cancelación del Task
        final BukkitTask[] task = new BukkitTask[1]; // Usar un array para capturar el task
        task[0] = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (fireball.isDead() || fireball.isOnGround()) {
                    plugin.getLogger().info("[DEBUG] Bola de fuego destruida o en el suelo. Deteniendo el task.");
                    task[0].cancel(); // Cancelamos el task almacenado en el array
                    return;
                }

                Vector velocity = fireball.getVelocity();
                velocity.setY(velocity.getY() - gravityForce);
                fireball.setVelocity(velocity);

                plugin.getLogger().info("[DEBUG] Nueva velocidad de la bola de fuego: " + velocity.toString());
            }
        }, 1L, 1L);
    }

    @EventHandler
    public void onFireballExplosion(EntityExplodeEvent event) {
        try {
            Entity entity = event.getEntity();
            if (entity instanceof Fireball) {
                Fireball fireball = (Fireball) entity;

                if (fireball.getShooter() instanceof Player) {
                    Player shooter = (Player) fireball.getShooter();

                    double damage = fireball.hasMetadata("damage")
                            ? fireball.getMetadata("damage").get(0).asDouble()
                            : 10.0;

                    plugin.getLogger().info("[DEBUG] Bola de fuego explotó. Daño directo: " + damage);

                    for (Entity nearbyEntity : fireball.getNearbyEntities(5, 5, 5)) {
                        if (nearbyEntity instanceof Player && !nearbyEntity.equals(shooter)) {
                            ((Player) nearbyEntity).damage(damage, shooter);
                            plugin.getLogger()
                                    .info("[DEBUG] Jugador afectado: " + nearbyEntity.getName() + " - Daño: " + damage);
                        }
                    }

                    event.blockList().clear();
                    plugin.getLogger().info("[DEBUG] Bloques afectados por la explosión eliminados.");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[DEBUG] Error manejando la explosión: " + e.getMessage());
        }
    }

    private boolean isValidPergamino(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER)
            return false;

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData
                && itemName.equals(meta.getDisplayName());
    }

    private boolean isDraconico(Player player) {
        File file = new File(plugin.getDataFolder(), "PlayerRazas.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("No se encontró el archivo PlayerRazas.yml.");
            return false;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String playerUUID = player.getUniqueId().toString();

        return config.contains("razas.Draconico." + playerUUID);
    }

    private boolean isInCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId()))
            return false;
        return System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < cooldownTime;
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
