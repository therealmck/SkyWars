package me.therealmck.skywars;

import me.therealmck.skywars.commands.*;
import me.therealmck.skywars.data.Game;
import me.therealmck.skywars.data.Queue;
import me.therealmck.skywars.data.SkyWarsMap;
import me.therealmck.skywars.guis.listeners.*;
import me.therealmck.skywars.placeholderapi.SkyWarsPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin {
    public static Queue queue = new Queue();
    public static List<SkyWarsMap> maps = new ArrayList<>();
    public static HashMap<Player, Game> activeCustomGames = new HashMap<>();
    public static List<Game> waitingGames;
    public static List<Game> runningGames;

    @Override
    public void onEnable() {
        // Get all maps from config
        new BukkitRunnable() {
            @Override
            public void run() {
                // Config
                createMapConfig();
                createPlayerDataConfig();
                createSkyWarsConfig();
                saveResource("configtemplate.yml", true);


                for (String key : mapConfig.getKeys(false)) {
                    try {
                        ConfigurationSection section = mapConfig.getConfigurationSection(key);
                        assert section != null;
                        System.out.println(Bukkit.getWorld(key));
                        maps.add(new SkyWarsMap(Bukkit.getWorld(key), section.getList("Spawns"), section.getList("IslandChests"), section.getList("MidChests")));
                    } catch (Exception e) {
                        System.out.println("Map "+key+" couldn't be loaded. Does it exist?");
                    }
                }
            }
        }.runTaskLater(this, 1);

        //PlaceholderAPI
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new SkyWarsPlaceholderExpansion(this).register();
        }


        // Commands
        getCommand("skywars").setExecutor(new SkyWarsCommand());
        getCommand("addislandchest").setExecutor(new AddIslandChest());
        getCommand("addmidchest").setExecutor(new AddMidChest());
        getCommand("addspawnpoint").setExecutor(new AddSpawnPoint());
        getCommand("addworld").setExecutor(new AddWorld());

        // Event listeners
        getServer().getPluginManager().registerEvents(new EventChooserGuiListener(), this);
        getServer().getPluginManager().registerEvents(new IslandLootGuiListener(), this);
        getServer().getPluginManager().registerEvents(new MainCustomGameGuiListener(), this);
        getServer().getPluginManager().registerEvents(new MidLootGuiListener(), this);
        getServer().getPluginManager().registerEvents(new ModifierGuiListener(), this);


        // Begin task to update games
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Game> toRemove = new ArrayList<>();
                for (Game game : waitingGames) {
                    if (game.getPlayers().size() >= skyWarsConfig.getInt("MinimumPlayers")) {
                        toRemove.add(game);
                        runningGames.add(game);
                        game.fillChests();
                        game.beginGame();
                    }
                }

                waitingGames.removeAll(toRemove);
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {

    }


    // Configuration files
    public static File skyWarsFile;
    public static FileConfiguration skyWarsConfig;

    private void createSkyWarsConfig() {
        skyWarsFile = new File(getDataFolder(), "config.yml");
        if (!skyWarsFile.exists()) {
            skyWarsFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        skyWarsConfig = new YamlConfiguration();
        try {
            skyWarsConfig.load(skyWarsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }


    public static void saveSkyWarConfig() {
        try {
            skyWarsConfig.save(skyWarsFile);
        } catch (Exception e) {e.printStackTrace();}
    }


    public static File mapFile;
    public static FileConfiguration mapConfig;

    private void createMapConfig() {
        mapFile = new File(getDataFolder(), "maps.yml");
        if (!mapFile.exists()) {
            mapFile.getParentFile().mkdirs();
            saveResource("maps.yml", false);
        }

        mapConfig = new YamlConfiguration();
        try {
            mapConfig.load(mapFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void saveMapConfig() {
        try {
            mapConfig.save(mapFile);
        } catch (Exception e) {e.printStackTrace();}
    }


    public static File playerDataFile;
    public static FileConfiguration playerDataConfig;

    private void createPlayerDataConfig() {
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            saveResource("playerdata.yml", false);
        }

        playerDataConfig = new YamlConfiguration();
        try {
            playerDataConfig.load(playerDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerDataConfig() {
        try {
            playerDataConfig.save(playerDataFile);
        } catch (Exception e) {e.printStackTrace();}
    }
}
