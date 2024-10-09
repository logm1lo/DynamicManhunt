package me.logmilo.dynamicManhunt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LeaveRunnerCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public LeaveRunnerCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            playerManager.removeRunner(player); // Call removeRunner when the command is used
            return true;
        }
        return false; // Command can only be executed by players
    }
}
