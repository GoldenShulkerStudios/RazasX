package me.ewahv1.plugin.Listeners.Pergaminos;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CarmesiT1 implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private String itemName;
    private int customModelData;
    private int cooldownTime;
    private int range;

    public CarmesiT1(JavaPlugin plugin) {
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
        String basePath = "Pergaminos.Carmesi.Tier1.";

        itemName = ChatColor.translateAlternateColorCodes('&',
                config.getString(basePath + "name", "&c&lTransfusión de Sangre"));
        customModelData = config.getInt(basePath + "customModelData", 1);
        cooldownTime = config.getInt(basePath + "cooldown", 10) * 1000;
        range = config.getInt(basePath + "range", 10);

        plugin.getLogger().info("Configuración Carmesí cargada: name=" + itemName +
                ", customModelData=" + customModelData +
                ", cooldown=" + cooldownTime + "ms, range=" + range);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && isValidPergamino(item)) {
            plugin.getLogger().info(player.getName() + " está intentando usar el pergamino.");

            if (!isCarmesi(player)) {
                sendActionBar(player, ChatColor.RED + "Solo los Carmesí pueden usar este pergamino.");
                plugin.getLogger().info(ChatColor.RED + player.getName() + " no pertenece a la raza Carmesí.");
                return;
            }

            if (isInCooldown(player)) {
                long timeLeft = (cooldowns.get(player.getUniqueId()) + cooldownTime - System.currentTimeMillis())
                        / 1000;
                sendActionBar(player, ChatColor.RED + "Espera " + ChatColor.YELLOW + timeLeft + ChatColor.RED
                        + " segundos para usar " + ChatColor.DARK_RED + "Transfusión de Sangre" + ChatColor.RED + ".");
                return;
            }

            Entity target = getTargetEntity(player, range);
            if (target == null || !(target instanceof LivingEntity)) {
                sendActionBar(player, ChatColor.RED + "No estás apuntando a una entidad válida.");
                plugin.getLogger().info(ChatColor.YELLOW + player.getName()
                        + " intentó usar el pergamino, pero no apuntó a un objetivo válido.");
                return;
            }

            // Poner al jugador en cooldown
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

            // Mensaje y registro de éxito
            sendActionBar(player, ChatColor.GREEN + "Has usado el pergamino " + ChatColor.DARK_RED
                    + "'Transfusión de Sangre'" + ChatColor.GREEN + ".");
            plugin.getLogger()
                    .info(ChatColor.GREEN + player.getName() + " ha usado el pergamino 'Transfusión de Sangre'.");

            // Aplicar efecto al objetivo
            applyGlowingEffect((LivingEntity) target, player);
        }
    }

    private void applyGlowingEffect(LivingEntity target, Player player) {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        int glowingDuration = config.getInt("Pergaminos.Carmesi.Tier1.glowing_target_during", 1) * 20; // Convertir a
                                                                                                       // ticks
        double damage = config.getDouble("Pergaminos.Carmesi.Tier1.damage", 5.0); // Daño configurado

        // Agregar al equipo "GlowingRed"
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GlowingRed");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("GlowingRed");
            team.setColor(ChatColor.RED);
        }

        Team finalTeam = team; // Copia efectiva de la referencia
        if (target instanceof Player) {
            finalTeam.addEntry(((Player) target).getName());
        } else {
            finalTeam.addEntry(target.getUniqueId().toString());
        }

        // Aplicar glowing
        target.setGlowing(true);

        // Programar tarea para eliminar el efecto glowing después del tiempo
        // especificado
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            target.setGlowing(false);
            finalTeam.removeEntry(
                    target instanceof Player ? ((Player) target).getName() : target.getUniqueId().toString());

            // Reducir la salud del objetivo después de que termine el glowing
            if (target.isValid()) {
                target.damage(damage);
            }

            // Generar partículas después de que termine el efecto glowing
            generateParticlesToPlayer(target, player);
        }, glowingDuration);
    }

    private void generateParticlesToPlayer(LivingEntity target, Player player) {
        Location targetLocation = target.getLocation().add(0, 1, 0);
        World world = target.getWorld();

        // Bandera para controlar si las partículas ya tocaron al jugador
        final boolean[] particlesReachedPlayer = { false };

        // Contenedor mutable para el ID de la tarea
        final int[] taskId = { 0 };

        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            private int ticks = 0;
            private final int maxTicks = 40; // Tiempo máximo para que las partículas lleguen al jugador
            private final double speed = 0.5; // Velocidad de las partículas

            @Override
            public void run() {
                // Si ya se alcanzó al jugador, detener la tarea
                if (particlesReachedPlayer[0]) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }

                Location playerLocation = player.getLocation().add(0, 1, 0);

                // Verificar si las partículas han alcanzado al jugador o si se ha excedido el
                // tiempo
                if (ticks >= maxTicks || targetLocation.distanceSquared(playerLocation) < 1.0) {
                    particlesReachedPlayer[0] = true; // Marcar que las partículas llegaron al jugador
                    Bukkit.getScheduler().cancelTask(taskId[0]); // Detener la tarea

                    // Aplicar glowing al jugador después de que las partículas lo toquen
                    applyGlowingToPlayer(player);
                    return;
                }

                // Calcular la nueva dirección hacia el jugador
                Vector direction = playerLocation.subtract(targetLocation).toVector().normalize();
                targetLocation.add(direction.multiply(speed));

                // Generar partículas en la nueva ubicación
                world.spawnParticle(Particle.DUST, targetLocation, 5,
                        new Particle.DustOptions(Color.RED, 1.5f));

                ticks++;
            }
        }, 0L, 1L).getTaskId(); // Obtener el identificador de la tarea para detenerla
    }

    private void applyGlowingToPlayer(Player player) {
        File file = new File(plugin.getDataFolder(), "Pergaminos.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        int glowingPlayerDuration = config.getInt("Pergaminos.Carmesi.Tier1.glowing_player_during", 1) * 20; // Convertir
                                                                                                             // a ticks
        double heal = config.getDouble("Pergaminos.Carmesi.Tier1.heal", 5.0); // Curación configurada

        // Obtener o crear el equipo "GlowingRed"
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("GlowingRed");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("GlowingRed");
            team.setColor(ChatColor.RED);
        }

        // Referencia efectiva final del equipo
        final Team finalTeam = team;

        // Agregar al jugador al equipo
        finalTeam.addEntry(player.getName());
        player.setGlowing(true);

        // Curar al jugador al iniciar su glowing
        player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));

        // Programar tarea para eliminar el efecto glowing después del tiempo
        // especificado
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setGlowing(false);
            finalTeam.removeEntry(player.getName());
        }, glowingPlayerDuration);
    }

    private boolean isValidPergamino(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER)
            return false;

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData
                && itemName.equals(meta.getDisplayName());
    }

    private boolean isCarmesi(Player player) {
        File file = new File(plugin.getDataFolder(), "PlayerRazas.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("No se encontró el archivo PlayerRazas.yml.");
            return false;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String playerUUID = player.getUniqueId().toString();

        return config.contains("razas.Carmesi." + playerUUID);
    }

    private boolean isInCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId()))
            return false;
        return System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < cooldownTime;
    }

    private Entity getTargetEntity(Player player, int range) {
        return player.getNearbyEntities(range, range, range).stream()
                .filter(e -> e instanceof LivingEntity && player.hasLineOfSight(e))
                .min((e1, e2) -> Double.compare(player.getLocation().distanceSquared(e1.getLocation()),
                        player.getLocation().distanceSquared(e2.getLocation())))
                .orElse(null);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
