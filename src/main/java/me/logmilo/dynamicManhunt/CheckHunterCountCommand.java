package me.logmilo.dynamicManhunt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckHunterCountCommand implements CommandExecutor {
    private final PlayerManager playerManager;

    public CheckHunterCountCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            int hunterCount = playerManager.getNumberOfHunters();
            player.sendMessage("Current number of hunters: " + hunterCount);
            return true;
        }
        return false; // Command can only be executed by players
    }
}
