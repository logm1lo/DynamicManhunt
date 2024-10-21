package me.logmilo.dynamicManhunt;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class DynamicManhunt extends JavaPlugin {
    private GameManager gameManager;
    private FileConfiguration config; // To hold the plugin configuration

    @Override
    public void onEnable() {
        showStartupMessages();
        this.gameManager = new GameManager(this); // Initialize GameManager
        PlayerManager playerManager = new PlayerManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ManhuntListener(this), this); // Pass only 'this'
        getServer().getPluginManager().registerEvents(new CompassTrackingManager(gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(this, gameManager), this);

        if (getCommand("checkhunters") != null) {
            Objects.requireNonNull(getCommand("checkhunters")).setExecutor(new CheckHunterCountCommand(playerManager));
        } else {
            getLogger().warning("Command 'checkhunters' not found in plugin.yml");
        }

        // Register commands
        Objects.requireNonNull(getCommand("checkhunters")).setExecutor(new CheckHunterCountCommand(playerManager));
        Objects.requireNonNull(getCommand("checkrunners")).setExecutor(new CheckRunnerCountCommand(playerManager));
        Objects.requireNonNull(getCommand("leaverunner")).setExecutor(new LeaveRunnerCommand(playerManager));
        Objects.requireNonNull(getCommand("listplayers")).setExecutor(new ListPlayersCommand(playerManager));

        // Load configuration settings
        loadConfigSettings();

        // Log the plugin enabling
        getLogger().info("Dynamic Manhunt Plugin has been enabled!");
    }

    private void showStartupMessages() {
        getLogger().info("===============================");
        getLogger().info("        Dynamic Manhunt");
        getLogger().info("           Version 1.2.0");
        getLogger().info("       Developed by logm1lo");
        getLogger().info("   Enjoy your Manhunt Experience!");
        getLogger().info("===============================");

        // Ensure the default config is saved
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Ensure the game is stopped on disable to avoid issues
        if (gameManager != null && gameManager.isGameActive()) {
            gameManager.stopGame();
            getLogger().info("Stopped active Manhunt game.");
        }

        // Log the plugin disabling
        getLogger().info("Dynamic Manhunt Plugin has been disabled.");
    }

    // Getter for GameManager
    public GameManager getGameManager() {
        return gameManager;
    }

    // Method to load configuration settings
    private void loadConfigSettings() {
        // Load the config file
        saveDefaultConfig(); // Create a default config if it does not exist
        config = getConfig(); // Get the configuration file

        // Example of loading config settings with default values
        config.addDefault("hunterAbilityCooldown", 60L); // Cooldown in seconds
        config.addDefault("runnerSpeedBoostInterval", 30L); // Interval in seconds
        config.addDefault("randomEventInterval", 10L); // Random events every 10 seconds
        config.addDefault("supplyDropInterval", 60L); // Supply drops every 60 seconds
        config.options().copyDefaults(true); // Copy default values if they are not already set
        saveConfig(); // Save the config
    }

    /**
     * Retrieves a configurable setting by its key.
     *
     * @param key The key of the configuration setting.
     * @param type The class of the expected return type.
     * @param <T> The type of the configuration setting.
     * @return The value associated with the key, or null if the key does not exist.
     * @throws ClassCastException If the value cannot be cast to the expected type.
     */
    public <T> T getConfigSetting(String key, Class<T> type) {
        if (!config.contains(key)) {
            getLogger().log(Level.WARNING, "Configuration key '" + key + "' does not exist.");
            return null; // Return null if the key does not exist
        }

        Object value = config.get(key);
        if (!type.isInstance(value)) {
            throw new ClassCastException("Value for key '" + key + "' is not of type " + type.getName());
        }
        return type.cast(value); // Safe cast
    }
}
