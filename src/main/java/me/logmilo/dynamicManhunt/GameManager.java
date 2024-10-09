package me.logmilo.dynamicManhunt;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private final DynamicManhunt plugin;
    private final PlayerManager playerManager;
    private boolean gameActive;
    private final List<Player> runners = new ArrayList<>();
    private final List<Player> hunters = new ArrayList<>();
    private final Map<Player, Long> hunterCooldowns = new HashMap<>();
    private final Map<Player, Long> downedPlayers = new HashMap<>();
    private final Map<Player, Integer> scores = new HashMap<>();

    public GameManager(DynamicManhunt plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager(plugin);
    }

    public void startGame(List<Player> allPlayers, int numberOfHunters) {
        selectPlayers(allPlayers, numberOfHunters);
        gameActive = true;

        Bukkit.broadcastMessage("§aDynamic Manhunt has started!");
        playerManager.broadcastRoles();

        for (Player hunter : hunters) {
            hunter.setWalkSpeed(0.3f);
        }

        startHunterAbilities();
        startRunnerBoosts();
        startRandomEvents();
        startSupplyDrops();
        startReviveChecks(); // Start revival checks
    }

    // Add a method to stop the game
    public void stopGame() {
        gameActive = false;
        playerManager.clearPlayers();
        Bukkit.broadcastMessage("§cDynamic Manhunt has ended!");

        // Display scores
        displayScores();
    }

    // Show final scores
    private void displayScores() {
        for (Player player : scores.keySet()) {
            player.sendMessage("§e" + player.getName() + " - Score: " + scores.get(player));
        }
    }

    public void downPlayer(Player player) {
        if (runners.contains(player)) {
            runners.remove(player);
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

                for (Player runner : new ArrayList<>(downedPlayers.keySet())) {
                    long downedTime = System.currentTimeMillis() - downedPlayers.get(runner);
                    if (downedTime >= 30000) { // If downed for more than 30 seconds
                        Bukkit.broadcastMessage(runner.getName() + " has been revived!");
                        runners.add(runner);
                        downedPlayers.remove(runner);
                        runner.teleport(getRandomSpawnLocation());
                        incrementScore(runner); // Increment score for revival
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every second
    }

    private void incrementScore(Player player) {
        scores.put(player, scores.getOrDefault(player, 0) + 1);
    }

    private Location getRandomSpawnLocation() {
        World world = Bukkit.getWorlds().getFirst();
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
        // Shuffle the list and assign roles
        Collections.shuffle(allPlayers);
        for (int i = 0; i < allPlayers.size(); i++) {
            if (i < numberOfHunters) {
                playerManager.addHunter(allPlayers.get(i));
            } else {
                playerManager.addRunner(allPlayers.get(i));
            }
        }

        for (Player player : hunters) {
            player.sendMessage("§aYou are a Hunter!");
        }
        for (Player player : runners) {
            player.sendMessage("§aYou are a Runner!");
        }
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
                for (Player hunter : hunters) {
                    if (!hunterCooldowns.containsKey(hunter) || System.currentTimeMillis() - hunterCooldowns.get(hunter) >= 60000) {
                        int randomAbility = new Random().nextInt(2);
                        if (randomAbility == 0) {
                            hunter.setWalkSpeed(0.4f); // Speed boost
                        } else {
                            hunter.setVelocity(hunter.getVelocity().setY(1)); // Jump boost
                        }
                        hunterCooldowns.put(hunter, System.currentTimeMillis());
                        Bukkit.getScheduler().runTaskLater(plugin, () -> hunter.setWalkSpeed(0.3f), 200L); // Reset speed
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    private void startRunnerBoosts() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player runner : runners) {
                    runner.setWalkSpeed(0.3f); // Speed boost for 5 seconds
                    Bukkit.getScheduler().runTaskLater(plugin, () -> runner.setWalkSpeed(0.2f), 100L);
                }
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
                    case 0:
                        Bukkit.broadcastMessage("§cA flood has occurred!");
                        floodWorld();
                        break;
                    case 1:
                        Bukkit.broadcastMessage("§cAn earthquake has shaken the world!");
                        earthquakeEffect();
                        break;
                    case 2:
                        Bukkit.broadcastMessage("§cLightning strikes the ground!");
                        strikeLightning();
                        break;
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    private void floodWorld() {
        for (World world : Bukkit.getWorlds()) {
            for (int x = -50; x <= 50; x++) {
                for (int z = -50; z <= 50; z++) {
                    Location loc = new Location(world, x, world.getHighestBlockYAt(x, z) + 1, z);
                    loc.getBlock().setType(Material.WATER);
                }
            }
        }
    }

    private void earthquakeEffect() {
        for (World world : Bukkit.getWorlds()) {
            for (int x = -30; x <= 30; x++) {
                for (int z = -30; z <= 30; z++) {
                    Location loc = new Location(world, x, world.getHighestBlockYAt(x, z), z);
                    loc.getBlock().setType(Material.AIR);
                    loc.add(0, -1, 0).getBlock().setType(Material.AIR);
                }
            }
        }
    }

    private void strikeLightning() {
        for (World world : Bukkit.getWorlds()) {
            Location loc = new Location(world, randomX(world), world.getHighestBlockYAt(randomX(world), randomZ(world)) + 1, randomZ(world));
            world.strikeLightning(loc);
        }
    }

    private int randomX(World world) {
        return (int) (world.getWorldBorder().getSize() / 2) - new Random().nextInt((int) world.getWorldBorder().getSize());
    }

    private int randomZ(World world) {
        return (int) (world.getWorldBorder().getSize() / 2) - new Random().nextInt((int) world.getWorldBorder().getSize());
    }

    private void startSupplyDrops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive || runners.isEmpty()) return;

                Player targetedRunner = runners.get(new Random().nextInt(runners.size()));
                Bukkit.broadcastMessage(ChatColor.YELLOW + "A supply drop is coming for " + targetedRunner.getName() + "!");

                ItemStack itemStack = getRandomOverpoweredItem();
                Location dropLocation = getRandomDropLocationNearPlayer(targetedRunner);
                removeExpiredItems(dropLocation); // Remove nearby items before dropping new ones
                Item droppedItem = targetedRunner.getWorld().dropItem(dropLocation, itemStack);
                droppedItem.setVelocity(new Vector(0, 0, 0)); // Stop item from falling
            }
        }.runTaskTimer(plugin, 0L, 1200L); // Drop every minute
    }

    private ItemStack getRandomOverpoweredItem() {
        Random random = new Random();
        int randomIndex = random.nextInt(15); // Adjusting to 15 for the additional items

        return switch (randomIndex) {
            case 0 -> new ItemStack(Material.DIAMOND_SWORD);
            case 1 -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
            case 2 -> new ItemStack(Material.NETHERITE_HELMET);
            case 3 -> new ItemStack(Material.NETHERITE_CHESTPLATE);
            case 4 -> new ItemStack(Material.NETHERITE_LEGGINGS);
            case 5 -> new ItemStack(Material.NETHERITE_BOOTS);
            case 6 -> new ItemStack(Material.BOW);
            case 7 -> new ItemStack(Material.ARROW, 64);
            case 8 -> new ItemStack(Material.POTION);
            case 9 -> new ItemStack(Material.ENDER_PEARL, 5);
            case 10 -> new ItemStack(Material.GOLDEN_APPLE, 5);
            case 11 -> new ItemStack(Material.TNT, 5);
            case 12 -> new ItemStack(Material.FIREWORK_ROCKET, 3);
            case 13 -> new ItemStack(Material.GOLD_BLOCK, 5);
            case 14 -> new ItemStack(Material.DIAMOND_BLOCK, 3);
            default -> new ItemStack(Material.STICK);
        };
    }

    private Location getRandomDropLocationNearPlayer(Player player) {
        Random random = new Random();
        Location playerLocation = player.getLocation();
        double offsetX = (random.nextDouble() * 20) - 10;
        double offsetZ = (random.nextDouble() * 20) - 10;
        Location dropLocation = playerLocation.clone().add(offsetX, 0, offsetZ);
        int highestY = dropLocation.getWorld().getHighestBlockYAt(dropLocation);
        dropLocation.setY(highestY + 1);
        return dropLocation;
    }

    private int getRandomTime(int minSeconds, int maxSeconds) {
        Random random = new Random();
        return (random.nextInt(maxSeconds - minSeconds) + minSeconds) * 20;
    }

    private void removeExpiredItems(Location dropLocation) {
        // Search for Item entities within a 5-block radius around the drop location
        for (Item item : dropLocation.getWorld().getNearbyEntitiesByType(Item.class, dropLocation, 5)) {
            item.remove(); // Remove the item entity
        }
    }
}
