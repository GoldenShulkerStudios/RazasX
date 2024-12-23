package me.ewahv1.plugin.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.List;

public class IniciarJugadorRaza implements Listener {

    public static void entregarPergamino(Player player, String razaSeleccionada) {
        File pergaminosFile = new File(AsignarRazaPlayer.getPlugin().getDataFolder(), "Pergaminos.yml");
        File razasFile = new File(AsignarRazaPlayer.getPlugin().getDataFolder(), "razas.yml");

        if (!pergaminosFile.exists() || !razasFile.exists()) {
            player.sendMessage(
                    ChatColor.RED + "No se encontraron los archivos necesarios (Pergaminos.yml o razas.yml).");
            return;
        }

        YamlConfiguration pergaminosConfig = YamlConfiguration.loadConfiguration(pergaminosFile);
        YamlConfiguration razasConfig = YamlConfiguration.loadConfiguration(razasFile);

        // Busca la configuración del pergamino para la raza seleccionada
        String tierPath = "Pergaminos." + razaSeleccionada + ".Tier1";
        if (!pergaminosConfig.contains(tierPath)) {
            player.sendMessage(ChatColor.RED + "No se encontró un pergamino para la raza " + razaSeleccionada + ".");
            return;
        }

        String name = ChatColor.translateAlternateColorCodes('&',
                pergaminosConfig.getString(tierPath + ".name", "Pergamino Desconocido"));
        List<String> lore = pergaminosConfig.getStringList(tierPath + ".lore");
        int customModelData = pergaminosConfig.getInt(tierPath + ".customModelData", 0);
        boolean unbreaking = pergaminosConfig.getBoolean(tierPath + ".unbreaking", false);
        boolean hideEnchants = pergaminosConfig.getBoolean(tierPath + ".hide_enchants", false);

        // Crea el pergamino como un ItemStack
        ItemStack pergamino = new ItemStack(Material.PAPER);
        ItemMeta meta = pergamino.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).toList());
            meta.setCustomModelData(customModelData);

            if (unbreaking) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            }
            if (hideEnchants) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            pergamino.setItemMeta(meta);
        }

        // Añade el pergamino al inventario del jugador o lo lanza al suelo si no hay
        // espacio
        player.getInventory().addItem(pergamino).forEach((index, leftover) -> {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        });

        player.sendMessage(ChatColor.GOLD + "Has recibido el pergamino: " + name);

        // Asignar atributos base y efectos
        aplicarAtributosYefectos(player, razaSeleccionada, razasConfig);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        File razasFile = new File(AsignarRazaPlayer.getPlugin().getDataFolder(), "razas.yml");

        if (!razasFile.exists())
            return;

        YamlConfiguration razasConfig = YamlConfiguration.loadConfiguration(razasFile);
        String razaSeleccionada = ""; // Reemplaza con el método para obtener la raza del jugador

        // Reaplicar atributos y efectos al respawnear
        if (razasConfig.contains(razaSeleccionada)) {
            aplicarAtributosYefectos(player, razaSeleccionada, razasConfig);
        }
    }

    private static void aplicarAtributosYefectos(Player player, String raza, YamlConfiguration razasConfig) {
        AsignarRazaPlayer.getPlugin().getLogger()
                .info("[DEBUG] Aplicando atributos y efectos para la raza " + raza + " al jugador " + player.getName());

        // Asignar base_health
        double baseHealth = razasConfig.getDouble(raza + ".base_health", 20);
        player.setMaxHealth(baseHealth);
        player.setHealth(baseHealth);
        AsignarRazaPlayer.getPlugin().getLogger()
                .info("[DEBUG] Vida base asignada a " + baseHealth + " para el jugador " + player.getName());

        // Programar la aplicación de efectos con retraso
        Bukkit.getScheduler().runTaskLater(AsignarRazaPlayer.getPlugin(), () -> {
            AsignarRazaPlayer.getPlugin().getLogger()
                    .info("[DEBUG] Comenzando la aplicación de efectos para la raza: " + raza);

            // Verificar si existen efectos en la configuración
            if (!razasConfig.contains(raza + ".effects")) {
                AsignarRazaPlayer.getPlugin().getLogger()
                        .warning("[DEBUG] No se encontraron efectos para la raza " + raza + ".");
                return;
            }

            // Obtener la lista de efectos
            List<String> effects = razasConfig.getStringList(raza + ".effects");
            if (effects == null || effects.isEmpty()) {
                AsignarRazaPlayer.getPlugin().getLogger()
                        .warning("[DEBUG] La lista de efectos está vacía o es nula para la raza " + raza + ".");
                return;
            }
            AsignarRazaPlayer.getPlugin().getLogger()
                    .info("[DEBUG] Efectos encontrados para la raza " + raza + ": " + effects);

            // Aplicar efectos
            for (String effectName : effects) {
                AsignarRazaPlayer.getPlugin().getLogger()
                        .info("[DEBUG] Procesando efecto: " + effectName);

                PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
                if (effectType != null) {
                    AsignarRazaPlayer.getPlugin().getLogger()
                            .info("[DEBUG] Tipo de efecto encontrado: " + effectType.getName());

                    // Eliminar efecto previo si existe
                    player.removePotionEffect(effectType);
                    AsignarRazaPlayer.getPlugin().getLogger()
                            .info("[DEBUG] Efecto previo eliminado: " + effectType.getName());

                    // Aplicar el nuevo efecto
                    player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, 0, true, false));
                    AsignarRazaPlayer.getPlugin().getLogger()
                            .info("[DEBUG] Efecto " + effectName + " aplicado al jugador " + player.getName());
                } else {
                    AsignarRazaPlayer.getPlugin().getLogger()
                            .warning("[DEBUG] Efecto inválido o desconocido: " + effectName + " para la raza " + raza);
                }
            }

            player.sendMessage("§aSe han reaplicado los atributos y efectos de tu raza: " + raza + ".");
        }, 10L); // Retraso de 10 ticks (0.5 segundos)
    }
}