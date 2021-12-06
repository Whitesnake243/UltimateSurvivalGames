package me.maker56.survivalgames.arena;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.listener.DomeListener;
import me.maker56.survivalgames.reset.Save;
import me.maker56.survivalgames.reset.Selection;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

import static me.maker56.survivalgames.SurvivalGames.s;

public class ArenaManager {
	
	private static FileConfiguration cfg = SurvivalGames.database;
	public HashMap<String, String[]> selectedarena = new HashMap<>();
	
	public static void reinitializeDatabase() {
		cfg = SurvivalGames.database;
	}
	
	public Arena getArena(Location loc) {
		for(Game game : SurvivalGames.gameManager.getGames()) {
			for(Arena arena : game.getArenas()) {
				if(arena.containsBlock(loc))
					return arena;
			}
		}
		return null;
	}
	
	
	// CONFIGURATION ==================================================
		
	// ARENA SAVEN
	
	public void save(Player p) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}

		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		
		Game game = SurvivalGames.gameManager.getGame(gamename);
		if(game != null) {
			Arena arena = game.getArena(arenaname);
			if(arena != null) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cYou can only save arenas of an unloaded lobby."));
				return;
			}
		}
		//Temp till worldedit finishes updating
		com.sk89q.worldedit.entity.Player Pl = BukkitAdapter.adapt(p);
		WorldEdit we = SurvivalGames.getWorldEdit().getWorldEdit();
		try {
			LocalSession ls = we.getInstance().getSessionManager().get(Pl);
			s = ls.getSelection(Pl.getWorld());
		} catch (IncompleteRegionException e) {
			Util.Error("World edit isnt completely updated for 1.17 I Know about this error Select the other WE point");
			Util.Error(e.toString()+"  "+"PlayerName: "+ Pl.getName()+ "Locations(Min,Max): "+ s.getMinimumPoint().toString()+","+s.getMaximumPoint().toString());
		}
		if (s.getMinimumPoint() != null && s.getMaximumPoint() != null) {
			Selection sel = new Selection(s.getMinimumPoint(), s.getMaximumPoint());
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "Saving arena... This may take a while. Laggs can occure. You'll get a message, when the save is completed."));
			BlockVector3 g;
			Set<BlockVector3> h = s.getChunkCubes();
			String j = h.toString();
			//Remove first []'s
			j = j.substring(1, j.length() - 1);
			//Remove first ()'s
			//j = j.substring(1, j.length() - 1);
			//Split  Coords apart
			String[] k = j.split("\\), ");
			List<String> l = new ArrayList<>();
			l.addAll(Arrays.asList(k).subList(0, k.length));
			//Convert to real world points
			(new Save(gamename, arenaname, sel, p, s, l)).start();
		} else {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "The arena isn't defined yet."));
		}
	}
	
	// ARENA LÖSCHEN
	
	public void delete(Player p) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		
		if(!cfg.contains("Games." + gamename)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-not-found").replace("%0%", gamename)));
			return;
		}
		
		if(!cfg.contains("Games." + gamename + ".Arenas." + arenaname)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cArena " + arenaname + " in lobby " + gamename + " not found!"));
			return;
		}
		
		Game game = SurvivalGames.gameManager.getGame(gamename);
		if(game != null) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cYou can only delete arenas of an unloaded lobby."));
			return;
		}
		
		cfg.set("Games." + gamename + ".Arenas." + arenaname, null);
		SurvivalGames.saveDataBase();
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "Arena " + arenaname + " was deleted in lobby " + gamename + " successfull!"));
	}
	
	// ARENA CHECKEN
	
	public void check(Player p) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "Arena-Check: arena &e" + arenaname + "&6, lobby &e" + gamename));
		String path = "Games." + gamename + ".Arenas." + arenaname + ".";
		
		boolean enabled = cfg.getBoolean(path + "Enabled");
		
		if(enabled) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThis arena is ready to play!"));
		}
		
		int spawns = cfg.getStringList(path + "Spawns").size();

		if(spawns < 2) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8&l➥ &bSpawns &7(&c" + spawns + "&7) &eAt least 2 Spawns required"));
		} else {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8&l➥ &bSpawns &7(&a" + spawns + "&7) &eAt least 2 Spawns required"));
		}
		
		boolean deathmatch = cfg.getBoolean(path + "Enable-Deathmatch");
		int dspawns = cfg.getStringList(path + "Deathmatch-Spawns").size();
		
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8&l➥ &bDeathmatch &7(&a" + deathmatch + "&7) &e(optional)"));
		
		if(deathmatch) {
			if(dspawns < 1) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8&l➥ &bDeathmatch-Spawns &7(&c" + dspawns + "&7) &eAt least 1 Deathmatch Spawn required"));
			} else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', " &8&l➥ &bDeathmatch-Spawns &7(&a" + dspawns + "&7) &eAt least 1 Deathmatch Spawn required"));
			}
		}
		
		p.sendMessage("   ");
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lNext step:"));
		
		if(spawns < 2) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aAt least are 2 Spawns required. Type &b/sg arena addspawn &ato add more spawns!"));
		} else if(deathmatch && dspawns < 1){
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aAt least are 1 Deathmatch-Spawn required. Type &b/sg arena deathmatch add &ato add more Deathmatch-Spawns!"));
		} else {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThis arena is ready to play. Just type &b/sg arena finish &ato finish the setup!"));
		}
	}
	
	// SETUP FINISHEN
	
	public void finishSetup(Player p) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		String path = "Games." + gamename + ".Arenas." + arenaname + ".";
		
		if(SurvivalGames.gameManager.getGame(gamename) != null) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cYou can't add an arena to a loaded lobby. Unload the lobby first with /sg lobby unload " + gamename));
		} else {
			cfg.set(path + "Enabled", true);
			SurvivalGames.saveDataBase();
			
			Game game = SurvivalGames.gameManager.getGame(gamename);
			if(game == null) {
				SurvivalGames.gameManager.unload(game);
			}
			SurvivalGames.gameManager.load(gamename);
				
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&aYou've finished the setup and activated the arena successfully!"));
		}
	}
	
	// DEATHMATCH DOME SETMIDDLE
	public void setDeathmatchDomeMiddle(Player p, boolean teleport) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		String path = "Games." + gamename + ".Arenas." + arenaname + ".Dome-Middle";

		if(teleport) {
			if(cfg.contains(path)) {
				Location loc = Util.parseLocation(Objects.requireNonNull(cfg.getString(path)));
				if(loc == null) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-deathmatch-domemiddle-notset")));
				} else {
					p.teleport(loc);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-deathmatch-domemiddle-teleport")));
				}
			} else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-deathmatch-domemiddle-notset")));
			}
		} else {
			cfg.set(path, Util.serializeLocation(p.getLocation(), false));
			SurvivalGames.saveDataBase();
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-deathmatch-domemiddle-set")));
		}
		
	}
	
	// DEATHMATCH DOME RADIUS
	public void setDeathmatchDomeRadius(Player p, int radius, boolean show) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		String path = "Games." + gamename + ".Arenas." + arenaname + ".Dome-Radius";
		// TODO (DEATHMATCH END REACHED MESSAGE)
		if(show) {
			int cradius = cfg.getInt(path);
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', cradius == 0 ? MessageHandler.getMessage("arena-deathmatch-domeradius-disabled") : MessageHandler.getMessage("arena-deathmatch-domeradius-show").replace("%0%", Integer.valueOf(cradius).toString())));
		} else {
			radius = Math.abs(radius);
			cfg.set(path, radius);
			SurvivalGames.saveDataBase();
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-deathmatch-domeradius-changed").replace("%0%", Integer.valueOf(radius).toString())));
		}
		
	}
	
	// DEATHMATCH CHANGEN
	
	public void changeDeathmatch(Player p) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		String path = "Games." + gamename + ".Arenas." + arenaname + ".";
		
		boolean deathmatch = cfg.getBoolean(path + "Enable-Deathmatch");
		
		if(deathmatch) {
			cfg.set(path + "Enable-Deathmatch", false);
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-deathmatch-changed").replace("%0%", "&cFALSE")));
		} else {
			cfg.set(path + "Enable-Deathmatch", true);
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-deathmatch-changed").replace("%0%", "&aTRUE")));
		}
		SurvivalGames.saveDataBase();
	}
	
	// SPAWN ADDEN
	
	public void addSpawn(Player p, String type) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		String path = "Games." + gamename + ".Arenas." + arenaname + ".";
		
		List<String> l = cfg.getStringList(path + type);
		Location loc = p.getLocation();
		l.add(Objects.requireNonNull(loc.getWorld()).getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
		cfg.set(path + type, l);
		SurvivalGames.saveDataBase();
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-spawn-added").replace("%0%", Integer.valueOf(l.size()).toString())));
	}
	
	// SPAWN REMVOVEN
	
	public void removeSpawn(Player p, int id, String type) {
		if(!selectedarena.containsKey(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-must-select").replace("%0%", "/sg arena select <LOBBYNAME> <ARENA NAME>")));
			return;
		}
		
		String gamename = selectedarena.get(p.getName())[0];
		String arenaname = selectedarena.get(p.getName())[1];
		String path = "Games." + gamename + ".Arenas." + arenaname + ".";
		

		id--;
		List<String> l = cfg.getStringList(path + type);
		
		try  {
			l.get(id);
		} catch(IndexOutOfBoundsException e) {
			id++;
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-spawn-notfound").replace("%0%", Integer.valueOf(id).toString())));
			return;
		}
		
		l.remove(id);
		cfg.set(path + type, l);
		SurvivalGames.saveDataBase();
		
		id++;
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-spawn-removed").replace("%0%", Integer.valueOf(id).toString())));
	}
	
	// ARENA ERSTELLEN
	
	public void createArena(Player p, String arenaname, String gamename) {
		if(!cfg.contains("Games." + gamename)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-not-found").replace("%0%", gamename)));
			return;
		}
		
		if(cfg.contains("Games." + gamename + ".Arenas." + arenaname)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-already-exists").replace("%0%", arenaname).replace("%1%", gamename)));
			return;
		}
		com.sk89q.worldedit.entity.Player Pl = BukkitAdapter.adapt(p);
        WorldEdit we = SurvivalGames.getWorldEdit().getWorldEdit();
		try {
            LocalSession ls = we.getInstance().getSessionManager().get(Pl);
            s = ls.getSelection(Pl.getWorld());
		} catch (IncompleteRegionException e) {
			Util.Error("World edit isnt completely updated for 1.17 I Know about this error Select the other WE point");
			Util.Error(e.toString()+"  "+"PlayerName: "+ Pl.getName()+ "Locations(Min,Max): "+ s.getMinimumPoint().toString()+","+s.getMaximumPoint().toString());
		}
		if(we != null) {
		    if(s.getMinimumPoint() == null || s.getMaximumPoint() == null) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-no-selection").replace("%0%", "/sg arena tools")));
				return;
			}

		}

		String chesttype = SurvivalGames.instance.getConfig().getString("Default.Arena.Chests.TypeID");
		String chestdata = SurvivalGames.instance.getConfig().getString("Default.Arena.Chests.Data");
		
		String path = "Games." + gamename + ".Arenas." + arenaname + ".";
		
		cfg.set(path + "Enabled", false);
		
		cfg.set(path + "Grace-Period", SurvivalGames.instance.getConfig().getInt("Default.Arena.Grace-Period"));
        cfg.set(path + "BMin", String.valueOf(s.getMinimumPoint()));

        Location Min = new Location(p.getWorld(), (double) s.getMinimumPoint().getBlockX(), (double) s.getMinimumPoint().getBlockY(), (double) s.getMinimumPoint().getBlockZ());
        cfg.set(path + "Min", Util.serializeLocation(Min, false));
        cfg.set(path + "BMax", String.valueOf(s.getMaximumPoint()));
        Location Max = new Location(p.getWorld(), (double) s.getMaximumPoint().getBlockX(), (double) s.getMaximumPoint().getBlockY(), (double) s.getMaximumPoint().getBlockZ());
        cfg.set(path + "Max", Util.serializeLocation(Max, false));
        cfg.set(path + "World", p.getWorld().getName());

		cfg.set(path + "Allowed-Blocks", SurvivalGames.instance.getConfig().getIntegerList("Default.Arena.Allowed-Blocks"));
		
		cfg.set(path + "Chest.TypeID", chesttype);
		cfg.set(path + "Chest.Data", chestdata);
		
		cfg.set(path + "Spawns", new ArrayList<String>());
		
		cfg.set(path + "Enable-Deathmatch", false);
		
		cfg.set(path + "Player-Deathmatch", SurvivalGames.instance.getConfig().getInt("Default.Arena.Player-Deathmatch-Start"));
		cfg.set(path + "Auto-Deathmatch", SurvivalGames.instance.getConfig().getInt("Default.Arena.Automaticly-Deathmatch-Time"));
		
		cfg.set(path + "Deathmatch-Spawns", new ArrayList<String>());
		
		cfg.set(path + "Money-on-Kill", SurvivalGames.instance.getConfig().getDouble("Default.Money-on-Kill"));
		cfg.set(path + "Money-on-Win", SurvivalGames.instance.getConfig().getDouble("Default.Money-on-Win"));
		
		cfg.set(path + "Midnight-chest-refill", SurvivalGames.instance.getConfig().getBoolean("Default.Midnight-chest-refill"));


		SurvivalGames.saveDataBase();
		selectArena(p, arenaname, gamename);
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-created").replace("%0%", arenaname).replace("%1%", gamename)));
		if(SurvivalGames.instance.getConfig().getBoolean("Enable-Arena-Reset"))
			save(p);
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-check").replace("%0%", "/sg arena check")));
	}
	
	
	// ARENA AUSW&?„HLEN
	
	public void selectArena(Player p, String arenaname, String gamename) {
		if(!cfg.contains("Games." + gamename)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-not-found").replace("%0%", gamename)));
			return;
		}
		
		if(!cfg.contains("Games." + gamename + ".Arenas." + arenaname)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-not-found").replace("%0%", arenaname).replace("%1%", gamename)));
			return;
		}
		
		selectedarena.put(p.getName(), new String[] { gamename, arenaname });
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("arena-selected").replace("%0%", arenaname).replace("%1%", gamename)));
	}
	
	// ARENA GETTEN

	public Arena getArena(String game, String arenaname) {

		if(!new File("plugins/SurvivalGames/reset/" + game + arenaname + "/").isDirectory() && SurvivalGames.instance.getConfig().getBoolean("Enable-Arena-Reset")) {
			System.out.println("[SurvivalGames] Cannot load arena " + arenaname + " in lobby " + game + ": Arena map file is missing! To create a map file, select the arena first with /sg arena select " + game + " " + arenaname + " and type /sg arena save!");
			return null;
		}
		
		String path = "Games." + game + ".Arenas." + arenaname + ".";

        Location min = Util.parseLocation(cfg.getString(path + "Min"));
        Location max = Util.parseLocation(cfg.getString(path + "Max"));
		
		if(min == null || max == null) {
			System.out.println("[SurvivalGames] Cannot load arena " + arenaname + " in lobby " + game + ": Minimum and maximum location of the arena aren't correct defined. Try to redefine it!");
			return null;
		}
		
		int graceperiod = cfg.getInt(path + "Grace-Period");
		
		Material chesttype = Material.getMaterial(cfg.getString(path + "Chest.TypeID"));
		int chestdata = cfg.getInt(path + "Chest.Data");
		
		List<Location> spawns = new ArrayList<>();
		
		for(String key : cfg.getStringList(path + "Spawns")) {
			spawns.add(Util.parseLocation(key));
		}
		
		boolean deathmatch = cfg.getBoolean(path + "Enable-Deathmatch");
		List<Location> deathmatchspawns = new ArrayList<>();
		
		if(deathmatch) {
			for(String key : cfg.getStringList(path + "Deathmatch-Spawns")) {
				deathmatchspawns.add(Util.parseLocation(key));
			}
		}
		
		List<String> allowedBlocks = cfg.getStringList(path + "Allowed-Blocks");
		
		int autodeathmatch = cfg.getInt(path + "Auto-Deathmatch");
		int playerdeathmatch = cfg.getInt(path + "Player-Deathmatch");
		
		if(!cfg.contains(path + "Money-on-Kill")) {
			cfg.set(path + "Money-on-Kill", SurvivalGames.instance.getConfig().getDouble("Default.Money-on-Kill"));
			cfg.set(path + "Money-on-Win", SurvivalGames.instance.getConfig().getDouble("Default.Money-on-Win"));
			
			cfg.set(path + "Midnight-chest-refill", SurvivalGames.instance.getConfig().getBoolean("Default.Midnight-chest-refill"));
			SurvivalGames.saveDataBase();
		}
		double kill = cfg.getDouble(path + "Money-on-Kill");
		double win = cfg.getDouble(path + "Money-on-Win");
		
		boolean refill = cfg.getBoolean(path + "Midnight-chest-refill");
		
		Location domeMiddle = Util.parseLocation(cfg.getString(path + "Dome-Middle", ""));
		int domeRadius = cfg.getInt(path + "Dome-Radius", 0);
		
		if(domeMiddle != null && domeRadius > 0) {
			if(!DomeListener.isRegistered())
				new DomeListener();
		}
		
		return new Arena(min, max, spawns, chesttype, chestdata, graceperiod, arenaname, game, deathmatch, deathmatchspawns, allowedBlocks, autodeathmatch, playerdeathmatch, kill, win, refill, domeMiddle, domeRadius);
	}

}
