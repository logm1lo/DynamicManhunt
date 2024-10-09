package me.logmilo.dynamicManhunt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ManhuntListener implements Listener {
    private final DynamicManhunt plugin;

    public ManhuntListener(DynamicManhunt dynamicManhunt) {
        this.plugin = dynamicManhunt;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args[0].equalsIgnoreCase("/manhunt")) {
            if (args.length > 1) {
                switch (args[1].toLowerCase()) {
                    case "start":
                        handleStartCommand(player);
                        break;
                    case "stop":
                        handleStopCommand(player);
                        break;
                    case "help":
                        showHelpCommands(player);
                        break;
                    case "addhunter":
                        if (args.length > 2) {
                            addHunter(player, args[2]);
                        } else {
                            player.sendMessage("Please specify a player to add as a hunter.");
                        }
                        break;
                    case "removehunter":
                        if (args.length > 2) {
                            removeHunter(player, args[2]);
                        } else {
                            player.sendMessage("Please specify a player to remove from hunters.");
                        }
                        break;
                    case "listhunters":
                        listHunters(player);
                        break;
                    case "addrunner":
                        if (args.length > 2) {
                            addRunner(player, args[2]);
                        } else {
                            player.sendMessage("Please specify a player to add as a runner.");
                        }
                        break;
                    case "removerunner":
                        if (args.length > 2) {
                            removeRunner(player, args[2]);
                        } else {
                            player.sendMessage("Please specify a player to remove from runners.");
                        }
                        break;
                    case "listrunners":
                        listRunners(player);
                        break;
                    default:
                        player.sendMessage("Invalid command. Use /manhunt help for a list of commands.");
                        break;
                }
            } else {
                player.sendMessage("Invalid command. Use /manhunt help for a list of commands.");
            }
            event.setCancelled(true); // Prevent the command from being processed further
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GameManager gameManager = plugin.getGameManager();

        // Check if the player is holding a compass and is a hunter
        if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS && gameManager.isHunter(player)) {
            gameManager.useCompass(player); // Use the compass
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            GameManager gameManager = plugin.getGameManager();

            // If the player is a runner and would be killed by damage, down them instead
            if (gameManager.isRunner(player) && player.getHealth() - event.getFinalDamage() <= 0) {
                event.setCancelled(true); // Cancel the death
                gameManager.downPlayer(player); // Down the player
            }
        }
    }

    private void handleStartCommand(Player player) {
        GameManager gameManager = plugin.getGameManager(); // Access GameManager via DynamicManhunt
        if (!gameManager.isGameActive()) {
            gameManager.startGame(new ArrayList<>(Bukkit.getOnlinePlayers()), 1); // Example: 1 hunter
            player.sendMessage("Manhunt started!");

            // Use the getConfigSetting to fetch hunter cooldown from the config
            Long hunterCooldown = plugin.getConfigSetting("hunterAbilityCooldown", Long.class);
            if (hunterCooldown != null) {
                player.sendMessage("Hunter cooldown is set to " + hunterCooldown + " seconds.");
            } else {
                player.sendMessage("Default hunter cooldown is being used.");
            }

            giveCompassToHunters(); // Give compass to hunters
        } else {
            player.sendMessage("Manhunt is already active.");
        }
    }

    private void handleStopCommand(Player player) {
        GameManager gameManager = plugin.getGameManager();
        if (gameManager.isGameActive()) {
            gameManager.stopGame();
            player.sendMessage("Manhunt stopped!");
        } else {
            player.sendMessage("No active Manhunt game to stop.");
        }
    }

    private void showHelpCommands(Player player) {
        player.sendMessage("=== Manhunt Commands ===");
        player.sendMessage("/manhunt start - Start the manhunt game.");
        player.sendMessage("/manhunt stop - Stop the active manhunt game.");
        player.sendMessage("/manhunt addhunter <player> - Add a hunter.");
        player.sendMessage("/manhunt removehunter <player> - Remove a hunter.");
        player.sendMessage("/manhunt listhunters - List all hunters.");
        player.sendMessage("/manhunt addrunner <player> - Add a runner.");
        player.sendMessage("/manhunt removerunner <player> - Remove a runner.");
        player.sendMessage("/manhunt listrunners - List all runners.");
        player.sendMessage("/manhunt help - Show this help message.");
        player.sendMessage("========================");
    }

    private void addHunter(Player player, String playerName) {
        GameManager gameManager = plugin.getGameManager();
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && !gameManager.isHunter(targetPlayer)) {
            gameManager.addHunter(targetPlayer);
            player.sendMessage("Added " + playerName + " as a hunter.");
            giveCompassToPlayer(targetPlayer); // Give compass to the new hunter
        } else if (targetPlayer == null) {
            player.sendMessage("Player not found.");
        } else {
            player.sendMessage(playerName + " is already a hunter.");
        }
    }

    private void removeHunter(Player player, String playerName) {
        GameManager gameManager = plugin.getGameManager();
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && gameManager.isHunter(targetPlayer)) {
            gameManager.removeHunter(targetPlayer);
            player.sendMessage("Removed " + playerName + " from hunters.");
        } else if (targetPlayer == null) {
            player.sendMessage("Player not found.");
        } else {
            player.sendMessage(playerName + " is not a hunter.");
        }
    }

    private void listHunters(Player player) {
        GameManager gameManager = plugin.getGameManager();
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
        GameManager gameManager = plugin.getGameManager();
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && !gameManager.isRunner(targetPlayer)) {
            gameManager.addRunner(targetPlayer);
            player.sendMessage("Added " + playerName + " as a runner.");
        } else if (targetPlayer == null) {
            player.sendMessage("Player not found.");
        } else {
            player.sendMessage(playerName + " is already a runner.");
        }
    }

    private void removeRunner(Player player, String playerName) {
        GameManager gameManager = plugin.getGameManager();
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer != null && gameManager.isRunner(targetPlayer)) {
            gameManager.removeRunner(targetPlayer);
            player.sendMessage("Removed " + playerName + " from runners.");
        } else if (targetPlayer == null) {
            player.sendMessage("Player not found.");
        } else {
            player.sendMessage(playerName + " is not a runner.");
        }
    }

    private void listRunners(Player player) {
        GameManager gameManager = plugin.getGameManager();
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

    // Method to give a compass to all hunters
    private void giveCompassToHunters() {
        GameManager gameManager = plugin.getGameManager();
        for (Player hunter : gameManager.getHunters()) {
            giveCompassToPlayer(hunter);
        }
    }

    // Method to give a compass to a single player
    private void giveCompassToPlayer(Player player) {
        player.getInventory().addItem(new ItemStack(Material.COMPASS));
        player.sendMessage("You have been given a compass for tracking runners.");
    }
}
