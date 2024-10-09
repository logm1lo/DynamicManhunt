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
    private final Map<Player, Long> compassCooldowns = new HashMap<>();

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
            giveCompassToHunter(hunter);
        }

        startGameMechanics();
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

        if (runners.isEmpty()) {
            hunter.sendMessage("§cThere are no runners to track!");
            return;
        }

        Player nearestRunner = getNearestRunner(hunter);
        if (nearestRunner != null) {
            hunter.setCompassTarget(nearestRunner.getLocation());
            hunter.sendMessage("§aTracking " + nearestRunner.getName() + "!");
            startCompassCooldown(hunter);
        }
    }

    private boolean isOnCooldown(Player player) {
        return compassCooldowns.containsKey(player) && System.currentTimeMillis() - compassCooldowns.get(player) < 5000;
    }

    private void startCompassCooldown(Player player) {
        compassCooldowns.put(player, System.currentTimeMillis());
    }

    private Player getNearestRunner(Player hunter) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player runner : runners) {
            double distance = hunter.getLocation().distance(runner.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = runner;
            }
        }

        return nearest;
    }

    public void stopGame() {
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
                for (Player runner : downedPlayers.keySet()) {
                    if (System.currentTimeMillis() - downedPlayers.get(runner) >= 30000) {
                        toRevive.add(runner);
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
        scores.put(player, scores.getOrDefault(player, 0) + 1);
    }

    private Location getRandomSpawnLocation() {
        // Fixing the method to access the first world correctly
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
            if (i < numberOfHunters) {
                playerManager.addHunter(allPlayers.get(i));
            } else {
                playerManager.addRunner(allPlayers.get(i));
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
                for (Player hunter : hunters) {
                    if (isHunterAbilityReady(hunter)) {
                        applyRandomAbility(hunter);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    private boolean isHunterAbilityReady(Player hunter) {
        return !hunterCooldowns.containsKey(hunter) || System.currentTimeMillis() - hunterCooldowns.get(hunter) >= 60000;
    }

    private void applyRandomAbility(Player hunter) {
        int randomAbility = new Random().nextInt(2);
        if (randomAbility == 0) {
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
            for (Chunk chunk : world.getLoadedChunks()) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        Location loc = chunk.getBlock(x, world.getHighestBlockYAt(chunk.getX(), chunk.getZ()), z).getLocation();
                        loc.getBlock().setType(Material.WATER);
                    }
                }
            }
        }
    }

    private void earthquakeEffect() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getWorld().createExplosion(player.getLocation(), 0F, false, false);
        }
    }

    private void strikeLightning() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getWorld().strikeLightning(player.getLocation());
        }
    }

    private void startSupplyDrops() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameActive) return;

                Location dropLocation = getRandomSpawnLocation();
                ItemStack supplies = new ItemStack(Material.GOLDEN_APPLE);
                Item item = dropLocation.getWorld().dropItemNaturally(dropLocation, supplies);
                item.setVelocity(new Vector(0, 0.5, 0));
                Bukkit.broadcastMessage("§eA supply drop has occurred!");
            }
        }.runTaskTimer(plugin, 0L, 300L);
    }
}
