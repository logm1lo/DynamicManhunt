package me.logmilo.dynamicManhunt;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final PlayerManager playerManager;

    public PlayerListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerManager.removeRunner(event.getPlayer()); // Remove the player from runners on quit
        playerManager.removeHunter(event.getPlayer()); // Optionally, remove from hunters as well
    }
}

