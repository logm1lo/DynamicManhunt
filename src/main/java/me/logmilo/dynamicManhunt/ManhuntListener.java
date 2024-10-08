package me.logmilo.dynamicManhunt;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class ManhuntListener implements Listener {

    private final GameManager gameManager;

    public ManhuntListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args[0].equalsIgnoreCase("/manhunt")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("start") && !gameManager.isGameActive()) {
                // Start the game using GameManager
                List<Player> allPlayers = new ArrayList<>(gameManager.getPlugin().getServer().getOnlinePlayers());
                gameManager.startGame(allPlayers, 1); // Example: 1 hunter
                player.sendMessage("Manhunt started!");
            } else if (args.length > 1 && args[1].equalsIgnoreCase("stop") && gameManager.isGameActive()) {
                gameManager.stopGame();
                player.sendMessage("Manhunt stopped!");
            } else {
                player.sendMessage("Invalid command or game already active.");
            }
            event.setCancelled(true);
        }
    }
}
