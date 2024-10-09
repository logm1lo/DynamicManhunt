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

    // Constants for messages
    private static final String NOT_ENOUGH_PLAYERS_MSG = ChatColor.RED + "Not enough players to start the game! At least 2 players required.";
    private static final String GAME_STARTED_MSG = ChatColor.GREEN + "Manhunt has started! Hunters: %s, Runners: %s";
    private static final String GAME_STOPPED_MSG = ChatColor.RED + "Manhunt has been stopped.";
    private static final String ALREADY_ACTIVE_MSG = ChatColor.RED + "Manhunt is already active or invalid command.";

    public GameListener(DynamicManhunt plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args[0].equalsIgnoreCase("/manhunt")) {
            event.setCancelled(true); // Cancel the event to prevent command conflicts

            // Check the subcommand
            if (args.length > 1) {
                switch (args[1].toLowerCase()) {
                    case "start":
                        handleStartCommand(player);
                        break;

                    case "stop":
                        handleStopCommand(player);
                        break;

                    default:
                        player.sendMessage(ALREADY_ACTIVE_MSG);
                        break;
                }
            } else {
                player.sendMessage(ALREADY_ACTIVE_MSG);
            }
        }
    }

    private void handleStartCommand(Player player) {
        if (gameManager.isGameActive()) {
            player.sendMessage(ALREADY_ACTIVE_MSG);
            return;
        }

        // Get all online players
        List<Player> allPlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        int totalPlayers = allPlayers.size();

        // Ensure there are enough players
        if (totalPlayers < 2) {
            player.sendMessage(NOT_ENOUGH_PLAYERS_MSG);
            return;
        }

        // Set the number of hunters to be one less than the number of runners
        int numberOfHunters = totalPlayers - 1; // At least one runner required

        // Start the game with all players
        gameManager.startGame(allPlayers, numberOfHunters); // Pass the number of hunters
        String message = String.format(GAME_STARTED_MSG, gameManager.getHunters(), gameManager.getRunners());
        Bukkit.broadcastMessage(message); // Broadcast to all players

        // Log the game start event
        plugin.getLogger().info("Manhunt started with " + numberOfHunters + " hunters and " + (totalPlayers - numberOfHunters) + " runners.");
    }

    private void handleStopCommand(Player player) {
        if (!gameManager.isGameActive()) {
            player.sendMessage(ALREADY_ACTIVE_MSG);
            return;
        }

        gameManager.stopGame();
        player.sendMessage(GAME_STOPPED_MSG);

        // Log the game stop event
        plugin.getLogger().info("Manhunt stopped by " + player.getName() + ".");
    }
}
