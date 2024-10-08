package me.logmilo.dynamicManhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerManager {

    private final DynamicManhunt plugin; // Reference to the main plugin
    private final List<UUID> runners; // Store runner players' UUIDs
    private final List<UUID> hunters; // Store hunter players' UUIDs

    public PlayerManager(DynamicManhunt plugin) {
        this.plugin = plugin;
        this.runners = new ArrayList<>();
        this.hunters = new ArrayList<>();
    }

    // Add a player to the list of runners
    public void addRunner(Player player) {
        runners.add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You have been added as a Runner!");
    }

    // Add a player to the list of hunters
    public void addHunter(Player player) {
        hunters.add(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "You have been added as a Hunter!");
    }

    // Remove a player from the list of runners
    public void removeRunner(Player player) {
        runners.remove(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "You are no longer a Runner.");
    }

    // Remove a player from the list of hunters
    public void removeHunter(Player player) {
        hunters.remove(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "You are no longer a Hunter.");
    }

    // Get all runners as Player objects
    public List<Player> getRunners() {
        List<Player> runnerPlayers = new ArrayList<>();
        for (UUID uuid : runners) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                runnerPlayers.add(player);
            }
        }
        return Collections.unmodifiableList(runnerPlayers); // Return an unmodifiable list
    }

    // Get all hunters as Player objects
    public List<Player> getHunters() {
        List<Player> hunterPlayers = new ArrayList<>();
        for (UUID uuid : hunters) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                hunterPlayers.add(player);
            }
        }
        return Collections.unmodifiableList(hunterPlayers); // Return an unmodifiable list
    }

    // Clear all players from the game
    public void clearPlayers() {
        runners.clear();
        hunters.clear();
    }

    // Check if a player is a runner
    public boolean isRunner(Player player) {
        return runners.contains(player.getUniqueId());
    }

    // Check if a player is a hunter
    public boolean isHunter(Player player) {
        return hunters.contains(player.getUniqueId());
    }

    // Get the number of runners
    public int getNumberOfRunners() {
        return runners.size();
    }

    // Get the number of hunters
    public int getNumberOfHunters() {
        return hunters.size();
    }

    // Broadcast the role of every player (Runners and Hunters)
    public void broadcastRoles() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isRunner(player)) {
                player.sendMessage(ChatColor.GREEN + "You are a Runner!");
            } else if (isHunter(player)) {
                player.sendMessage(ChatColor.RED + "You are a Hunter!");
            } else {
                player.sendMessage(ChatColor.GRAY + "You are not participating.");
            }
        }
    }
}
