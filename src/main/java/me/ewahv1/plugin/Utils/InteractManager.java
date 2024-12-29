package me.ewahv1.plugin.Utils;

import me.ewahv1.plugin.Main;
import me.ewahv1.plugin.Listeners.Pergaminos.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InteractManager implements Listener {

    private final Main plugin;
    private final ValidationManager validationManager;
    private final Map<String, PergaminoHandler> pergaminoHandlers;

    public InteractManager(Main plugin) {
        this.plugin = plugin;
        this.validationManager = new ValidationManager(plugin);
        this.pergaminoHandlers = new HashMap<>();

        // Registrar handlers para cada raza
        pergaminoHandlers.put("Carmesi", new CarmesiT1(plugin));
        pergaminoHandlers.put("Draconico", new DraconicoT1(plugin));
        pergaminoHandlers.put("Elfo", new ElfoT1(plugin));
        pergaminoHandlers.put("Humano", new HumanoT1(plugin));
        // Añadir más razas según sea necesario
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        plugin.getLogger().info("[DEBUG] Interacción detectada por jugador: " + player.getName());

        if (!validationManager.isValidInteraction(event)) {
            plugin.getLogger().info("[DEBUG] Interacción inválida.");
            return;
        }

        if (!validationManager.isValidPergamino(item)) {
            plugin.getLogger().info("[DEBUG] Item no es un pergamino válido.");
            return;
        }

        String pergaminoRaza = validationManager.getPergaminoRaza(item);
        if (pergaminoRaza == null) {
            plugin.getLogger().info("[DEBUG] No se pudo determinar la raza para este pergamino.");
            return;
        }

        if (!validationManager.isPlayerOfRace(player, pergaminoRaza)) {
            validationManager.sendActionBar(player, "Este pergamino solo puede ser usado por la raza " + pergaminoRaza);
            plugin.getLogger().info("[DEBUG] El jugador no pertenece a la raza requerida: " + pergaminoRaza);
            return;
        }

        if (validationManager.isInCooldown(player, pergaminoRaza, item)) {
            validationManager.sendCooldownMessage(player, pergaminoRaza, item);
            return;
        }

        plugin.getLogger().info("[DEBUG] Pergamino raza detectada: " + pergaminoRaza);

        // Redirigir la lógica según la raza del pergamino
        PergaminoHandler handler = pergaminoHandlers.get(pergaminoRaza);
        if (handler != null) {
            handler.onUse(player, item);
        } else {
            plugin.getLogger().info("[DEBUG] No se encontró lógica para la raza: " + pergaminoRaza);
        }
    }
}
