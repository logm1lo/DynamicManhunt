package me.logmilo.dynamicManhunt;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class DynamicManhunt extends JavaPlugin {
    private GameManager gameManager;
    private Map<String, Object> configSettings; // A map to hold configurable settings

    @Override
    public void onEnable() {
        // Initialize the GameManager instance
        showStartupMessages();
        this.gameManager = new GameManager(this); // Initialize GameManager

        // Example: Register commands or events
        getServer().getPluginManager().registerEvents(new ManhuntListener(gameManager, this), this);
        // Initialize configuration settings
        configSettings = new HashMap<>();
        loadDefaultConfigSettings(); // Load default settings

        // Register event listeners
        getServer().getPluginManager().registerEvents(new GameListener(this, gameManager), this);

        // Log the plugin enabling
        getLogger().info("Dynamic Manhunt Plugin has been enabled!");
    }

    private void showStartupMessages() {
        getLogger().info("================================");
        getLogger().info("        Dynamic Manhunt        ");
        getLogger().info("          Version 1.0          ");
        getLogger().info("   Developed by LogMilo        ");
        getLogger().info("  Enjoy your Manhunt Experience!");
        getLogger().info("================================");
    }

    @Override
    public void onDisable() {
        // Ensure the game is stopped on disable to avoid issues
        if (gameManager.isGameActive()) {
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

    // Method to load default configuration settings
    private void loadDefaultConfigSettings() {
        configSettings.put("hunterAbilityCooldown", 60L); // Cooldown in seconds
        configSettings.put("runnerSpeedBoostInterval", 30L); // Interval in seconds
        configSettings.put("randomEventInterval", 10L); // Random events every 10 seconds
        configSettings.put("supplyDropInterval", 60L); // Supply drops every 60 seconds
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
        Object value = configSettings.get(key);
        if (value == null) {
            return null; // Return null if the key does not exist
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Value for key '" + key + "' is not of type " + type.getName());
        }
        return type.cast(value); // Safe cast
    }
}