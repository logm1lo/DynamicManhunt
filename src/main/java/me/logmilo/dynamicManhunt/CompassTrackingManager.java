package me.logmilo.dynamicManhunt;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompassTrackingManager implements Listener {
    private final GameManager gameManager;
    private final Map<Player, Long> cooldowns = new HashMap<>(); // Track cooldowns

    public CompassTrackingManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerUseCompass(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player is holding a compass
        if (item != null && item.getType() == Material.COMPASS) {
            if (isOnCooldown(player)) {
                player.sendMessage("§cYou need to wait before using the compass again.");
                return; // If cooldown is active
            }

            // Find the nearest runner
            Player nearestRunner = findNearestRunner(player);
            if (nearestRunner != null) {
                Location runnerLocation = nearestRunner.getLocation();
                player.setCompassTarget(runnerLocation);
                player.sendMessage("§aCompass is now pointing to " + nearestRunner.getName() + "!");
            } else {
                player.sendMessage("§cNo runners are currently active.");
            }

            // Set cooldown for 5 seconds
            setCooldown(player);
        }
    }

    private boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player) && System.currentTimeMillis() < cooldowns.get(player);
    }

    private void setCooldown(Player player) {
        cooldowns.put(player, System.currentTimeMillis() + (long) 5000);
    }

    private Player findNearestRunner(Player hunter) {
        List<Player> runners = gameManager.getRunners();
        Player nearestRunner = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player runner : runners) {
            double distance = hunter.getLocation().distance(runner.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestRunner = runner;
            }
        }
        return nearestRunner;
    }
}
