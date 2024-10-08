package me.logmilo.dynamicManhunt;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;
import java.util.*;

public class GameManager {
    private final DynamicManhunt plugin;
    private final List<Player> runners = new ArrayList<>();
    private final List<Player> hunters = new ArrayList<>();
    private final Map<Player, Long> hunterCooldowns = new HashMap<>();
    private boolean gameActive = false;

    public GameManager(DynamicManhunt plugin) {
        this.plugin = plugin;
    }

    public void startGame(List<Player> allPlayers, int numberOfHunters) {
        selectPlayers(allPlayers, numberOfHunters);
        gameActive = true;

        Bukkit.broadcastMessage("§aDynamic Manhunt has started!");

        for (Player hunter : hunters) {
            hunter.setWalkSpeed(0.3f); // Normal speed is 0.2
        }

        // Start periodic events
        startHunterAbilities();
        startRunnerBoosts();
        startRandomEvents();
        startSupplyDrops();
    }

    private void selectPlayers(List<Player> allPlayers, int numberOfHunters) {
        Collections.shuffle(allPlayers); // Shuffle the player list to randomize order

        hunters.clear();
        runners.clear();

        for (int i = 0; i < allPlayers.size(); i++) {
            if (i < numberOfHunters) {
                hunters.add(allPlayers.get(i)); // First 'numberOfHunters' players are hunters
            } else {
                runners.add(allPlayers.get(i)); // The rest are runners
            }
        }

        for (Player player : hunters) {
            player.sendMessage("§aYou are a Hunter!");
        }
        for (Player player : runners) {
            player.sendMessage("§aYou are a Runner!");
        }
    }

    public void stopGame() {
        gameActive = false;
        runners.clear();
        hunters.clear();
        hunterCooldowns.clear();
        Bukkit.broadcastMessage("§cDynamic Manhunt has ended!");
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
                Bukkit.broadcastMessage(ChatColor.YELLOW + "A supply drop has occurred near " + targetedRunner.getName() + "!");

                Location dropLocation = getRandomDropLocationNearPlayer(targetedRunner);
                ItemStack randomItem = getRandomOverpoweredItem();
                targetedRunner.getWorld().dropItemNaturally(dropLocation, randomItem);

                Bukkit.getScheduler().runTaskLater(plugin, () -> removeExpiredItems(dropLocation), getRandomTime(600, 900));
            }
        }.runTaskTimer(plugin, 0L, 1200L);
    }

    private ItemStack getRandomOverpoweredItem() {
        Random random = new Random();
        int randomIndex = random.nextInt(14);

        switch (randomIndex) {
            case 0:
                return new ItemStack(Material.NETHERITE_SWORD);
            case 1:
                return new ItemStack(Material.NETHERITE_AXE);
            case 2:
                return new ItemStack(Material.ENDER_PEARL, 5);
            case 3:
                return new ItemStack(Material.ENDER_EYE, 64);
            case 4:
                return new ItemStack(Material.NETHERITE_HELMET);
            case 5:
                return new ItemStack(Material.NETHERITE_CHESTPLATE);
            case 6:
                return new ItemStack(Material.NETHERITE_LEGGINGS);
            case 7:
                return new ItemStack(Material.NETHERITE_BOOTS);
            case 8:
                return new ItemStack(Material.TRIDENT);
            case 9:
                return new ItemStack(Material.WATER_BUCKET);
            case 10:
                return new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 8);
            case 11:
                ItemStack elytra = new ItemStack(Material.ELYTRA);
                ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET, 64);
                return combineItems(elytra, fireworks);
            case 12:
                return new ItemStack(Material.SPLASH_POTION, 4);
            case 13:
                ItemStack endCrystal = new ItemStack(Material.END_CRYSTAL, 2);
                ItemStack obsidian = new ItemStack(Material.OBSIDIAN, 5);
                return combineItems(endCrystal, obsidian);
            case 14:
                return new ItemStack(Material.POTION, 3);
            default:
                return new ItemStack(Material.STICK);
        }
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

    private ItemStack combineItems(ItemStack... items) {
        return items[0];
    }
}
