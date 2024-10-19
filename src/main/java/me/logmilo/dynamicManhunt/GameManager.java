package me.logmilo.dynamicManhunt;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class GameManager {
    private final DynamicManhunt plugin;
    private final PlayerManager playerManager;
    private boolean gameActive;
    private final List<Player> runners = new ArrayList<>();
    private final List<Player> hunters = new ArrayList<>();
    private final Map<Player, Long> hunterCooldowns = new HashMap<>();
    private final Map<Player, Long> downedPlayers = new HashMap<>();
    private final Map<Player, Integer> scores = new HashMap<>();
    private final Map<Player, Long> compassCooldowns = new HashMap<>();
    private final Map<Location, Material> originalBlockStates = new HashMap<>(); // To store original block states

    public GameManager(DynamicManhunt plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(plugin);
    }

    public void startGame(List<Player> allPlayers, int numberOfHunters) {
        saveOriginalWorldState(); // Save the original world state at the start of the game
        selectPlayers(allPlayers, numberOfHunters);
        gameActive = true;

        Bukkit.broadcastMessage("§aDynamic Manhunt has started!");
        playerManager.broadcastRoles();

        hunters.forEach(this::initializeHunter);
        startGameMechanics();
    }

    private void initializeHunter(Player hunter) {
        hunter.setWalkSpeed(0.3f);
        giveCompassToHunter(hunter);
    }

    private void startGameMechanics() {
        startHunterAbilities();
        startRunnerBoosts();
        startRandomEvents();
        startSupplyDrops();
        startReviveChecks();
    }

    private void giveCompassToHunter(Player hunter) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        hunter.getInventory().addItem(compass);
        hunter.sendMessage("§aYou have received a compass to track runners!");
    }

    public void useCompass(Player hunter) {
        if (isOnCooldown(hunter)) {
            hunter.sendMessage("§cYou must wait before using the compass again!");
            return;
        }

        Player nearestRunner = getNearestRunner(hunter);
        if (nearestRunner == null) {
            hunter.sendMessage("§cThere are no runners to track!");
            return;
        }

        hunter.setCompassTarget(nearestRunner.getLocation());
        hunter.sendMessage("§aTracking " + nearestRunner.getName() + "!");
        startCompassCooldown(hunter);
    }

    private boolean isOnCooldown(Player player) {
        return compassCooldowns.getOrDefault(player, 0L) > System.currentTimeMillis() - 5000;
    }

    private void startCompassCooldown(Player player) {
        compassCooldowns.put(player, System.currentTimeMillis());
    }

    private Player getNearestRunner(Player hunter) {
        return runners.stream()
                .min(Comparator.comparingDouble(runner -> hunter.getLocation().distance(runner.getLocation())))
                .orElse(null);
    }

    public void stopGame() {
        resetWorld(); // Reset the world when the game stops
        gameActive = false;
        playerManager.clearPlayers();
        Bukkit.broadcastMessage("§cDynamic Manhunt has ended!");
        displayScores();
    }

    private void displayScores() {
        scores.forEach((player, score) -> player.sendMessage("§e" + player.getName() + " - Score: " + score));
    }

    public void downPlayer(Player player) {
        if (runners.remove(player)) {
            downedPlayers.put(player, System.currentTimeMillis());
            player.sendMessage("§cYou have been downed! You will be revived in 30 seconds.");
            Bukkit.broadcastMessage(player.getName() + " has been downed!");
        }
    }

    private void startReviveChecks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive) return;

                List<Player> toRevive = new ArrayList<>();
                for (Map.Entry<Player, Long> entry : downedPlayers.entrySet()) {
                    if (System.currentTimeMillis() - entry.getValue() >= 30000) {
                        toRevive.add(entry.getKey());
                    }
                }

                for (Player runner : toRevive) {
                    Bukkit.broadcastMessage(runner.getName() + " has been revived!");
                    runners.add(runner);
                    downedPlayers.remove(runner);
                    runner.teleport(getRandomSpawnLocation());
                    incrementScore(runner);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void incrementScore(Player player) {
        scores.merge(player, 1, Integer::sum);
    }

    private Location getRandomSpawnLocation() {
        World world = Bukkit.getWorlds().get(0); // Get the first world
        double x = (Math.random() * 200) - 100;
        double z = (Math.random() * 200) - 100;
        int y = world.getHighestBlockYAt((int) x, (int) z) + 1;
        return new Location(world, x, y, z);
    }

    public void removeHunter(Player player) {
        hunters.remove(player);
    }

    public boolean isHunter(Player player) {
        return hunters.contains(player);
    }

    public void addRunner(Player player) {
        if (!runners.contains(player)) {
            runners.add(player);
        }
    }

    public void removeRunner(Player player) {
        runners.remove(player);
    }

    public boolean isRunner(Player player) {
        return runners.contains(player);
    }

    public void addHunter(Player player) {
        if (!hunters.contains(player)) {
            hunters.add(player);
            player.sendMessage("You are now a Hunter!");
        }
    }

    private void selectPlayers(List<Player> allPlayers, int numberOfHunters) {
        Collections.shuffle(allPlayers);
        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
            if (i < numberOfHunters) {
                playerManager.addHunter(player);
            } else {
                playerManager.addRunner(player);
            }
        }

        playersNotifyRoles();
    }

    private void playersNotifyRoles() {
        hunters.forEach(player -> player.sendMessage("§aYou are a Hunter!"));
        runners.forEach(player -> player.sendMessage("§aYou are a Runner!"));
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public List<Player> getRunners() {
        return Collections.unmodifiableList(runners);
    }

    public List<Player> getHunters() {
        return Collections.unmodifiableList(hunters);
    }

    private void startHunterAbilities() {
        new BukkitRunnable() {
            @Override
            public void run() {
                hunters.forEach(hunter -> {
                    if (isHunterAbilityReady(hunter)) {
                        applyRandomAbility(hunter);
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    private boolean isHunterAbilityReady(Player hunter) {
        return hunterCooldowns.getOrDefault(hunter, 0L) <= System.currentTimeMillis() - 60000;
    }

    private void applyRandomAbility(Player hunter) {
        if (new Random().nextBoolean()) {
            hunter.setWalkSpeed(0.4f); // Speed boost
        } else {
            hunter.setVelocity(hunter.getVelocity().setY(1)); // Jump boost
        }
        hunterCooldowns.put(hunter, System.currentTimeMillis());
        Bukkit.getScheduler().runTaskLater(plugin, () -> hunter.setWalkSpeed(0.3f), 200L);
    }

    private void startRunnerBoosts() {
        new BukkitRunnable() {
            @Override
            public void run() {
                runners.forEach(runner -> {
                    runner.setWalkSpeed(0.3f); // Speed boost for 5 seconds
                    Bukkit.getScheduler().runTaskLater(plugin, () -> runner.setWalkSpeed(0.2f), 100L);
                });
            }
        }.runTaskTimer(plugin, 0L, 600L);
    }

    private void startRandomEvents() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive) return;

                int randomEvent = new Random().nextInt(3);
                switch (randomEvent) {
                    case 0 -> {
                        Player randomRunner = getRandomRunner();
                        if (randomRunner != null) {
                            Bukkit.broadcastMessage("§cA flood has occurred near " + randomRunner.getName() + "!");
                            floodWorld(randomRunner.getLocation());
                        }
                    }
                    case 1 -> {
                        Bukkit.broadcastMessage("§cAn earthquake has shaken the world!");
                        earthquakeEffect(getRandomEventLocation());
                    }
                    case 2 -> {
                        Player randomPlayer = getRandomPlayer();
                        if (randomPlayer != null) {
                            Bukkit.broadcastMessage("§cLightning strikes the ground!");
                            strikeLightning(randomPlayer);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 400L); // Adjust the frequency as desired
    }
    private Player getRandomRunner() {
        if (runners.isEmpty()) {
            return null; // No runners to choose from
        }
        return runners.get(new Random().nextInt(runners.size()));
    }
    private Player getRandomPlayer() {
        List<Player> allPlayers = new ArrayList<>(hunters);
        allPlayers.addAll(runners);
        if (allPlayers.isEmpty()) {
            return null; // No players to choose from
        }
        return allPlayers.get(new Random().nextInt(allPlayers.size()));
    }

    private Location getRandomEventLocation() {
        List<Player> allPlayers = new ArrayList<>(hunters);
        allPlayers.addAll(runners);
        if (allPlayers.isEmpty()) {
            return getRandomSpawnLocation();
        }

        Player randomPlayer = allPlayers.get(new Random().nextInt(allPlayers.size()));
        Location playerLocation = randomPlayer.getLocation();
        return new Location(playerLocation.getWorld(), playerLocation.getX() + (Math.random() * 100 - 50), playerLocation.getY(), playerLocation.getZ() + (Math.random() * 100 - 50));
    }

    private void floodWorld(Location center) {
        for (World world : Bukkit.getWorlds()) {
            int radius = 10; // Define how far to flood around the center
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, 0, z).getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.WATER);
                    }
                }
            }
        }
    }

    private void earthquakeEffect(Location location) {
        // Example earthquake logic
        World world = location.getWorld();
        if (world == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§eThe ground shakes beneath you!");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        }
    }

    private void strikeLightning(Player player) {
        if (player != null) {
            Location playerLocation = player.getLocation();
            player.getWorld().strikeLightning(playerLocation);
            Bukkit.broadcastMessage("§cLightning has struck at " + player.getName() + "'s location!");
        }
    }

    private void startSupplyDrops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive) return;

                if (new Random().nextBoolean()) {
                    dropSupplyPackage();
                }
            }
        }.runTaskTimer(plugin, 0L, 200L); // Adjust frequency as needed
    }

    private void dropSupplyPackage() {
        Location dropLocation = getRandomSpawnLocation();
        World world = dropLocation.getWorld();
        if (world != null) {
            world.dropItem(dropLocation, new ItemStack(Material.CHEST)); // Example item
            world.dropItem(dropLocation, new ItemStack(Material.GOLDEN_APPLE)); // Example item
            Bukkit.broadcastMessage("§aA supply drop has occurred at " + dropLocation.getBlockX() + ", " + dropLocation.getBlockZ() + "!");
        }
    }

    private void resetWorld() {
        originalBlockStates.forEach((location, material) -> location.getBlock().setType(material));
        originalBlockStates.clear();
        Bukkit.getWorlds().get(0).getPlayers().forEach(player -> player.sendMessage("The world has been reset!"));
    }

    public void saveOriginalWorldState() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // This is where you perform the world state saving.
                try {
                    // Create a HashMap to store original block states
                    HashMap<Location, Material> originalState = new HashMap<>();

                    // Example logic to save block states in a defined area
                    int startX = -100; // Replace with your start coordinates
                    int endX = 100; // Replace with your end coordinates
                    int startZ = -100; // Replace with your start coordinates
                    int endZ = 100; // Replace with your end coordinates

                    for (int x = startX; x <= endX; x++) {
                        for (int z = startZ; z <= endZ; z++) {
                            // Assuming you're saving the world at the height of 64
                            Location loc = new Location(Bukkit.getWorld("world"), x, 64, z); // Adjust world name and height as needed
                            Material blockType = loc.getBlock().getType();

                            // Store the block state in the HashMap
                            originalState.put(loc, blockType);
                        }
                    }

                    // Optionally, log the number of blocks saved
                    Bukkit.getLogger().info("Saved original world state: " + originalState.size() + " blocks.");

                    // Additional logic to handle the originalState if needed
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Failed to save original world state: " + e.getMessage());
                    e.printStackTrace(); // Print stack trace for debugging
                }
            }
        }.runTaskAsynchronously(plugin); // Run the task asynchronously
    }
}