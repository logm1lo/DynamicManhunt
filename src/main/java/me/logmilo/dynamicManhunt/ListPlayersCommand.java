package me.logmilo.dynamicManhunt;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ListPlayersCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public ListPlayersCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            StringBuilder message = new StringBuilder(ChatColor.GREEN + "Current Players:\n");

            message.append(ChatColor.YELLOW).append("Runners: ");
            playerManager.getRunners().forEach(runner -> message.append(runner.getName()).append(" "));
            message.append("\n");

            message.append(ChatColor.RED).append("Hunters: ");
            playerManager.getHunters().forEach(hunter -> message.append(hunter.getName()).append(" "));

            player.sendMessage(message.toString());
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
        return false;
    }
}
