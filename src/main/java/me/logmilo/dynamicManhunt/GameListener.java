package me.logmilo.dynamicManhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class GameListener implements Listener {
    private final DynamicManhunt plugin;
    private final GameManager gameManager;

    public GameListener(DynamicManhunt plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args[0].equalsIgnoreCase("/manhunt")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("start") && !gameManager.isGameActive()) {
                // Get all online players
                List<Player> allPlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
                int totalPlayers = allPlayers.size();

                // Set the number of hunters to be one less than the number of runners
                int numberOfHunters = totalPlayers > 1 ? totalPlayers - 1 : 0; // At least one runner required

                // Ensure there are enough players
                if (totalPlayers < 2) {
                    player.sendMessage(ChatColor.RED + "Not enough players to start the game! At least 2 players required.");
                    return;
                }

                // Start the game with all players
                gameManager.startGame(allPlayers, numberOfHunters); // Pass the number of hunters
                String message = String.format("%sManhunt has started! Hunters: %s, Runners: %s",
                        ChatColor.GREEN, gameManager.getHunters(), gameManager.getRunners());
                Bukkit.broadcastMessage(message); // Broadcast to all players
            } else if (args.length > 1 && args[1].equalsIgnoreCase("stop") && gameManager.isGameActive()) {
                gameManager.stopGame();
                player.sendMessage(ChatColor.RED + "Manhunt has been stopped.");
            } else {
                player.sendMessage(ChatColor.RED + "Manhunt is already active or invalid command.");
            }
            event.setCancelled(true);
        }
    }
}
