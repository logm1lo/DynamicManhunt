package me.logmilo.dynamicManhunt;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;

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
                gameManager.startGame(new ArrayList<>(Bukkit.getOnlinePlayers()), 1); // Example: 1 hunter
                player.sendMessage("Manhunt started!");
            } else if (args.length > 1 && args[1].equalsIgnoreCase("stop") && gameManager.isGameActive()) {
                gameManager.stopGame();
                player.sendMessage("Manhunt stopped!");
            } else if (args.length > 1 && args[1].equalsIgnoreCase("help")) {
                showHelpCommands(player);
            } else if (args.length > 2 && args[1].equalsIgnoreCase("addHunter")) {
                addHunter(player, args[2]);
            } else if (args.length > 2 && args[1].equalsIgnoreCase("removeHunter")) {
                removeHunter(player, args[2]);
            } else if (args.length > 1 && args[1].equalsIgnoreCase("listHunters")) {
                listHunters(player);
            } else if (args.length > 2 && args[1].equalsIgnoreCase("addRunner")) {
                addRunner(player, args[2]);
            } else if (args.length > 2 && args[1].equalsIgnoreCase("removeRunner")) {
                removeRunner(player, args[2]);
            } else if (args.length > 1 && args[1].equalsIgnoreCase("listRunners")) {
                listRunners(player);
            } else {
                player.sendMessage("Invalid command or game already active.");
            }
            event.setCancelled(true);
        }
    }

    private void showHelpCommands(Player player) {
        player.sendMessage("=== Manhunt Commands ===");
        player.sendMessage("/manhunt start - Start the manhunt game.");
        player.sendMessage("/manhunt stop - Stop the active manhunt game.");
        player.sendMessage("/manhunt addHunter <player> - Add a hunter.");
        player.sendMessage("/manhunt removeHunter <player> - Remove a hunter.");
        player.sendMessage("/manhunt listHunters - List all hunters.");
        player.sendMessage("/manhunt addRunning <player> - Add a runner.");
        player.sendMessage("/manhunt removeRunner <player> - Remove a runner.");
        player.sendMessage("/manhunt listRunners - List all runners.");
        player.sendMessage("/manhunt help - Show this help message.");
        player.sendMessage("========================");
    }

    private void addHunter(Player player, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && !gameManager.isHunter(targetPlayer)) {
            gameManager.addHunter(targetPlayer);
            player.sendMessage("Added " + playerName + " as a hunter.");
        } else {
            player.sendMessage("Player not found or already a hunter.");
        }
    }

    private void removeHunter(Player player, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && gameManager.isHunter(targetPlayer)) {
            gameManager.removeHunter(targetPlayer);
            player.sendMessage("Removed " + playerName + " from hunters.");
        } else {
            player.sendMessage("Player not found or not a hunter.");
        }
    }

    private void listHunters(Player player) {
        List<Player> hunters = gameManager.getHunters();
        if (hunters.isEmpty()) {
            player.sendMessage("No hunters currently.");
        } else {
            player.sendMessage("=== Current Hunters ===");
            for (Player hunter : hunters) {
                player.sendMessage(hunter.getName());
            }
            player.sendMessage("========================");
        }
    }

    private void addRunner(Player player, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && !gameManager.isRunner(targetPlayer)) {
            gameManager.addRunner(targetPlayer);
            player.sendMessage("Added " + playerName + " as a runner.");
        } else {
            player.sendMessage("Player not found or already a runner.");
        }
    }

    private void removeRunner(Player player, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && gameManager.isRunner(targetPlayer)) {
            gameManager.removeRunner(targetPlayer);
            player.sendMessage("Removed " + playerName + " from runners.");
        } else {
            player.sendMessage("Player not found or not a runner.");
        }
    }

    private void listRunners(Player player) {
        List<Player> runners = gameManager.getRunners();
        if (runners.isEmpty()) {
            player.sendMessage("No runners currently.");
        } else {
            player.sendMessage("=== Current Runners ===");
            for (Player runner : runners) {
                player.sendMessage(runner.getName());
            }
            player.sendMessage("========================");
        }
    }
}