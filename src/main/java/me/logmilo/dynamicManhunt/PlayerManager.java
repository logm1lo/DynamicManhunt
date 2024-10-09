package me.logmilo.dynamicManhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerManager {
    private final List<UUID> runners; // Store runner players' UUIDs
    private final List<UUID> hunters; // Store hunter players' UUIDs

    public PlayerManager(DynamicManhunt plugin) {
        // Initialize the lists
        this.runners = Collections.synchronizedList(new ArrayList<>());
        this.hunters = Collections.synchronizedList(new ArrayList<>());
    }

    // Add a player to the list of runners
    public void addRunner(Player player) {
        if (!runners.contains(player.getUniqueId())) {
            runners.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "You have been added as a Runner!");
            broadcastRole(player, "Runner");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You are already a Runner.");
        }
    }

    // Add a player to the list of hunters
    public void addHunter(Player player) {
        if (!hunters.contains(player.getUniqueId())) {
            hunters.add(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You have been added as a Hunter!");
            broadcastRole(player, "Hunter");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You are already a Hunter.");
        }
    }

    public void removeRunner(Player player) {
        if (runners.remove(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You are no longer a Runner.");
        } else {
            player.sendMessage(ChatColor.RED + "You are not a Runner.");
        }
    }

    // Remove a player from the list of hunters
    public void removeHunter(Player player) {
        if (hunters.remove(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You are no longer a Hunter.");
        } else {
            player.sendMessage(ChatColor.RED + "You are not a Hunter.");
        }
    }

    // Get all runners as Player objects
    public List<Player> getRunners() {
        return getPlayersFromUUIDs(runners);
    }

    // Get all hunters as Player objects
    public List<Player> getHunters() {
        return getPlayersFromUUIDs(hunters);
    }

    // Helper method to get Player objects from a list of UUIDs
    private List<Player> getPlayersFromUUIDs(List<UUID> uuids) {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : uuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }
        return Collections.unmodifiableList(players);
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

    // Broadcast a player's role to all players
    private void broadcastRole(Player player, String role) {
        String message = ChatColor.GOLD + player.getName() + " has joined as a " + role + "!";
        Bukkit.broadcastMessage(message);
    }
}