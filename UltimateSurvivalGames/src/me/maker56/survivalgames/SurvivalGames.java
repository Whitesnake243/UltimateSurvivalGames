package me.maker56.survivalgames;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import me.maker56.survivalgames.arena.ArenaManager;
import me.maker56.survivalgames.arena.chest.ChestListener;
import me.maker56.survivalgames.arena.chest.ChestManager;
import me.maker56.survivalgames.commands.CommandSG;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.commands.permission.PermissionHandler;
import me.maker56.survivalgames.database.ConfigLoader;
import me.maker56.survivalgames.database.sql.DatabaseManager;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.game.GameManager;
import me.maker56.survivalgames.listener.*;
import me.maker56.survivalgames.metrics.Metrics;
import me.maker56.survivalgames.scoreboard.ScoreBoardManager;
import me.maker56.survivalgames.sign.SignManager;
import me.maker56.survivalgames.user.UserManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class SurvivalGames extends JavaPlugin {

	public static SurvivalGames instance;
	public static FileConfiguration messages, database, signs, reset, chestloot, scoreboard, kits, arenas;
	public static ArenaManager arenaManager;
	public static GameManager gameManager;
	public static ChestManager chestManager;
	public static UserManager userManger;
	public static SignManager signManager;
	public static ScoreBoardManager scoreBoardManager;
	public static Region s;
	public static int WorldeditVer;
	
	public static Economy econ;
	
	public static String version = Bukkit.getPluginManager().getPlugin("SurvivalGames").getDescription().getVersion();
	
	private static PluginManager pm = Bukkit.getPluginManager();
	
	public void onDisable() {
		if(gameManager != null) {
			for(Game game : gameManager.getGames()) {
				game.kickall();
			}
		}
		try {
			DatabaseManager.close();
		} catch (NoClassDefFoundError T) {
			Util.debug("Database Not in Use");
		}
	}
	
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit") && !Bukkit.getPluginManager().isPluginEnabled("AsyncWorldEdit")) {
			Util.Error("[SurvivalGames] ##########################################################");
			Util.Error("[SurvivalGames] ############  FastAsyncWorldEdit NOT FOUND! ##############");
			Util.Error("[SurvivalGames] ############    AsyncWorldEdit NOT FOUND!   ##############");
			Util.Error("[SurvivalGames] ############    It is highly recommended    ##############");
			Util.Error("[SurvivalGames] ############     To install one of them     ##############");
			Util.Error("[SurvivalGames] ##########################################################");
		}
		if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
			Util.Error("[SurvivalGames] WorldEdit Found!");
		}
		if (Bukkit.getPluginManager().isPluginEnabled("AsyncWorldEdit")) {
			Util.Error("[SurvivalGames] AsyncWorldEdit Found!");
		}
		instance = this;
		version += getDescription().getVersion();

		new ConfigLoader().load();
		DatabaseManager.open();
		DatabaseManager.load();

		// Hate update notifcations with a passion
		//startUpdateChecker();

		PermissionHandler.reinitializeDatabase();
		Game.reinitializeDatabase();
		MessageHandler.reload();

		if (setupEconomy())
			Util.debug("[SurvivalGames] Vault found!");

		chestManager = new ChestManager();
		scoreBoardManager = new ScoreBoardManager();
		arenaManager = new ArenaManager();
		gameManager = new GameManager();
		userManger = new UserManager();
		signManager = new SignManager();

		getCommand("sg").setExecutor(new CommandSG());

		//pm.registerEvents(new SelectionListener(), this);
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new ChestListener(), this);
		pm.registerEvents(new SignListener(), this);
		pm.registerEvents(new ResetListener(), this);
		pm.registerEvents(new UpdateListener(), this);
		pm.registerEvents(new SpectatorListener(), this);
		pm.registerEvents(new ChatListener(), this);

		try {
			new Metrics(this).start();
		} catch (IOException e) {
			Util.debug("[SurvivalGames] Cannot load metrics: " + e.getMessage());
		}

		if (getWorldEdit() != null) {
			Util.debug("[SurvivalGames] Plugin enabled. WorldEdit found!");
		} else {
			Util.debug("[SurvivalGames] Plugin disabled.");
			Bukkit.getPluginManager().disablePlugin(SurvivalGames.instance);
		}

		signManager.updateSigns();


		// UPDATE CHECKING
		new UpdateChecker(this, 81702).getVersion(version -> {
			if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
				Util.debug("There is not a new update available.");
			} else {
				Util.debug("A newer version of survivalgames is available. (" + version + ") You can download it here: https://www.spigotmc.org/resources/ultimatesurvivalgames-mc-1-16-1-17.81702 You're using " + SurvivalGames.version);

			}
		});
		// VAULT
	}
	private boolean setupEconomy() {
		if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
	        if (economyProvider != null) {
	            econ = economyProvider.getProvider();
	        }
		}

        return (econ != null);
	}
	
	// FILECONFIGURATION SAVE
	public static void saveall() {
	    saveDataBase();
	    saveArenas();
	    saveReset();
	    saveSigns();
	    saveChests();
	    saveKits();
	    saveMessages();
	    saveScoreboard();
    }
	public static void saveMessages() {
		try {
			messages.save("plugins/SurvivalGames/messages.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveArenas() {
		try {
			arenas.save("plugins/SurvivalGames/arenas.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDataBase() {
		try {
			database.save("plugins/SurvivalGames/database.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveSigns() {
		try {
			signs.save("plugins/SurvivalGames/signs.yml");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveReset() {
		try {
			reset.save("plugins/SurvivalGames/reset.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveChests() {
		try {
			chestloot.save("plugins/SurvivalGames/chestloot.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveScoreboard() {
		try {
			scoreboard.save("plugins/SurvivalGames/scoreboard.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveKits() {
		try {
			kits.save("plugins/SurvivalGames/kits.yml");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	// WORLDEDIT
	
	public static WorldEditPlugin getWorldEdit() {
		if(!pm.isPluginEnabled("WorldEdit")) {
			return null;
		} else {
			return (WorldEditPlugin) pm.getPlugin("WorldEdit");
		}
	}
	
	// API
	
	public static GameManager getGameManager() {
		return gameManager;
	}
	
	public static ArenaManager getArenaManager() {
		return arenaManager;
	}
	
	public static ChestManager getChestManager() {
		return chestManager;
	}
	
	public static UserManager getUserManager() {
		return userManger;
	}
	
	public static SignManager getSignManager() {
		return signManager;
	}
	
	public static ScoreBoardManager getScoreboardManager() {
		return scoreBoardManager;
	}
	
	public static SurvivalGames getInstance() {
		return instance;
	}

}
