package me.maker56.survivalgames.game;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.arena.Arena;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.game.phases.CooldownPhase;
import me.maker56.survivalgames.game.phases.DeathmatchPhase;
import me.maker56.survivalgames.game.phases.IngamePhase;
import me.maker56.survivalgames.game.phases.VotingPhase;

public class GameManager {
	
	private List<Game> games = new ArrayList<>();
	private static FileConfiguration cfg;
	
	public GameManager() {
		reinitializeDatabase();
		loadAll();
	}
	
	public static void reinitializeDatabase() {
		cfg = SurvivalGames.database;
	}
	
	public void createGame(Player p, String lobbyname) {
		String path = "Games." + lobbyname;
		
		if(cfg.contains(path)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-already-exists").replace("%0%", lobbyname)));
			return;
		}
		
		path += ".";
		FileConfiguration config = SurvivalGames.instance.getConfig();
		
		boolean enableVoting = config.getBoolean("Default.Enable-Voting");
		int lobbytime = config.getInt("Default.Lobby-Time");
		int maxVotingArenas = config.getInt("Default.Max-Voting-Arenas");
		int reqPlayers = config.getInt("Default.Required-Players-to-start");
		
		cfg.set(path + "Enable-Voting", enableVoting);
		cfg.set(path + "Lobby-Time", lobbytime);
		cfg.set(path + "Max-Voting-Arenas", maxVotingArenas);
		cfg.set(path + "Required-Players-to-start", reqPlayers);
		cfg.set(path + "Lobby", Util.serializeLocation(p.getLocation(), true));
		SurvivalGames.saveDataBase();
		
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-created").replace("%0%", lobbyname)));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-set-spawn").replace("%0%", lobbyname)));
		return;
	}
	
	public void setSpawn(Player p, String lobbyname) {
		if(!cfg.contains("Games." + lobbyname)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-not-found").replace("%0%", lobbyname)));
			return;
		}
		
		Location loc = p.getLocation();
		
		String s = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
		cfg.set("Games." + lobbyname + ".Lobby", s);
		SurvivalGames.saveDataBase();
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-spawn-set").replace("%0%", lobbyname)));
		return;
	}
	
	public void loadAll() { 
		int loaded = 0;
		
		if(cfg.contains("Games")) {
			for(String key : cfg.getConfigurationSection("Games.").getKeys(false)) {
				if(load(key))
					loaded++;
			}
		}
		
		System.out.println("[SurvivalGames] " + loaded + " lobbys loaded!");
	}
	
	public void unload(Game game) {
		if(game != null) {
			if(game.getPlayingUsers() > 0)
				game.kickall();
			VotingPhase vp = game.getVotingPhrase();
			if(vp != null) {
				vp.cancelTask();
			}
			CooldownPhase cp = game.getCooldownPhrase();
			if(cp != null)
				cp.cancelTask();
			IngamePhase ip = game.getIngamePhrase();
			if(ip != null) {
				ip.cancelDeathmatchTask();
				ip.cancelLightningTask();
				ip.cancelTask();
			}
			DeathmatchPhase dp = game.getDeathmatch();
			if(dp != null) {
				dp.cancelTask();
			}
			games.remove(game);

		}
	}
	
	public boolean load(String name) {
		Bukkit.broadcastMessage("1");
		if(getGame(name) != null) {
			System.out.println("[SurvivalGames] Lobby " + name + " is already loaded!");
			return false;
		}
		
		String path = "Games." + name;
		
		if(!cfg.contains(path)) {
			System.out.println("[SurvivalGames] Lobby " + name + " does not exist!");
			return false;
		}
		
		path += ".";
		
		if(!cfg.contains(path + "Arenas")) {
			System.out.println("[SurvivalGames] Lobby " + name + " has no arenas!");
			return false;
		}
		
		List<Arena> arenas = new ArrayList<>();
		Bukkit.broadcastMessage(path);
        for(String key : cfg.getConfigurationSection(path + "Arenas.").getKeys(false)) {
            String[] keysplit = key.split(",");
            if(!cfg.getBoolean(path + "Arenas." + keysplit[0]+ ".Enabled")) {
                continue;
            }

            Arena arena = SurvivalGames.arenaManager.getArena(name, key);

            if(arena != null) {
                arenas.add(arena);
            }
        }
		Bukkit.broadcastMessage("2.1-GM");
		if(arenas.size() == 0) {
			System.out.println("[SurvivalGames] No arena in lobby " + name + " loaded!");
			return false;
		}
		Bukkit.broadcastMessage("2.2");
		if(!cfg.contains(path + "Lobby") && arenas.size() != 1) {
			System.out.println("[SurvivalGames] The spawn point in lobby " + name + " isn't defined!");
			return false;
		}
		Bukkit.broadcastMessage("2.3");
		Location lobby = Util.parseLocation(cfg.getString(path + "Lobby"));
		Bukkit.broadcastMessage("2.4");
		boolean voting = cfg.getBoolean(path + "Enable-Voting");
		Bukkit.broadcastMessage("2.5");
		int lobbytime = cfg.getInt(path + "Lobby-Time");
		Bukkit.broadcastMessage("2.6");
		int maxVotingArenas = cfg.getInt(path + "Max-Voting-Arenas");
		Bukkit.broadcastMessage("2.7");
		int reqplayers = cfg.getInt(path + "Required-Players-to-start");
		Bukkit.broadcastMessage("2.8");
		boolean resetEnabled = SurvivalGames.instance.getConfig().getBoolean("Enable-Arena-Reset");
		Bukkit.broadcastMessage("3");
		
		games.add(new Game(name, lobby, voting, lobbytime, maxVotingArenas, reqplayers, arenas, resetEnabled));
		return true;
	}
	
	public List<Game> getGames() {
		return games;
	}
	
	public Game getGame(String name) {
		for(Game game : games) {
			if(game.getName().equalsIgnoreCase(name))
				return game;
		}
		return null;
	}

}
