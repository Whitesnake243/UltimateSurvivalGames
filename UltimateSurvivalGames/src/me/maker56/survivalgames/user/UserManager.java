package me.maker56.survivalgames.user;

import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.commands.permission.Permission;
import me.maker56.survivalgames.commands.permission.PermissionHandler;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.game.GameManager;
import me.maker56.survivalgames.game.GameState;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class UserManager {
    public ArrayList<ItemStack> contents = new ArrayList<ItemStack>();
	private GameManager gm = SurvivalGames.gameManager;

	// START SPECTATOR

	public void leaveGame(SpectatorUser su) {
		Game g = su.getGame();
		for(User u : g.getUsers()) {
			u.getPlayer().showPlayer(su.getPlayer());
		}
		g.leaveSpectator(su);
		if(g.getScoreboardPhase() != null) {
			su.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		setState(su.getPlayer(), su);
	}

	public void joinGameAsSpectator(Player p, String gamename) {
		if(!SurvivalGames.instance.getConfig().getBoolean("Spectating.Enabled")) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("spectator-disabled")));
			return;
		}

		if(!PermissionHandler.hasPermission(p, Permission.SPECTATE)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("no-permission")));
			return;
		}

		if(isSpectator(p.getName()) || isPlaying(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-already-playing")));
			return;
		}

		if(p.getVehicle() != null) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-vehicle")));
			return;
		}

		Game g = gm.getGame(gamename);

		if(g == null) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-unknown-game").replace("%0%", gamename)));
			return;
		}

		int max = SurvivalGames.instance.getConfig().getInt("Spectating.Max-Spectators-Per-Arena", 8);
		if(g.getSpecators().size() >= max) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("spectator-full").replace("%0%", Integer.valueOf(max).toString())));
			return;
		}

		GameState state = g.getState();

		if(state == GameState.VOTING || state == GameState.WAITING || state == GameState.COOLDOWN || state == GameState.RESET) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("spectator-game-running")));
			return;
		}

		g.joinSpectator(new SpectatorUser(p, g));
		return;
	}

	// END SPECTATOR
	public void joinGame(Player p, String gamename) {
		if(!PermissionHandler.hasPermission(p, Permission.JOIN)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("no-permission")));
			return;
		}
		if (p.getInventory().getContents() != null) {
            int es = new Integer(p.getInventory().getContents().length);
            for (int i = 1; i < es + 1; i++) {
                if (i > es) {
                    break;
                }
                ItemStack item = null;
                try {
                    item = new ItemStack(p.getInventory().getItem(i));
                } catch (IllegalArgumentException e) {
                }
                if (item != null) {
                    for (int ii = 1 ; ii < es + 1; ii++) {
                        if (p.getEnderChest().getContents().length < 28) {
                            p.getEnderChest().addItem(new ItemStack(item));
                            p.getInventory().setItem(i, new ItemStack(Material.AIR));
                            break;
                        }
                        if (p.getEnderChest().getContents().length > 26){
                            contents.add(item);
                            p.getInventory().setItem(i, new ItemStack(Material.AIR));
                            break;
                        }
                    }
                    //int e = new Integer(p.getEnderChest().firstEmpty());
                    //if (e < 0 && e > 28) {
                    //    p.getEnderChest().addItem(new ItemStack(item));
                    //}
                    //if (e > 27 && e < 38) {
                    //    contents.add(item);
                    // }
                }
            }
            int Ar = new Integer(contents.size());
            //Testing to see if its set or not
            //p.sendMessage(contents.toString()+" | "+ p.getEnderChest().getContents().toString()+" | "+ p.getInventory().getContents().toString());
            if (Ar > 26) {
                SurvivalGames.Pinvo.set("Inventories."+p.getName(), contents);
                SurvivalGames.savePinvo();
                return;
            }

        }
		if(isPlaying(p.getName())) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-already-playing")));
			return;
		}
		
		if(p.getVehicle() != null) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-vehicle")));
			return;
		}
		
		Game g = gm.getGame(gamename);
		
		if(g == null) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-unknown-game").replace("%0%", gamename)));
			return;
		}
		
		GameState state = g.getState();
		
		if(state != GameState.VOTING && state != GameState.WAITING && state != GameState.COOLDOWN) {
			if(SurvivalGames.instance.getConfig().getBoolean("Spectating.Enabled")) {
				joinGameAsSpectator(p, gamename);
			} else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-game-running")));
			}
			return;
		}
		
		if(g.getUsers().size() >= g.getMaximumPlayers()) {
			User kick = PermissionHandler.canJoin(p, g);
			if(kick != null) {
				kick.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("fulljoin-kick")));
				leaveGame(kick.getPlayer());
			} else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("join-game-full")));
				return;
			}
		}
		
		User user = new User(p, g);
		g.join(user);
		return;
	}
	

	public void leaveGame(final Player p) {
		if(!isPlaying(p.getName())) {
			SpectatorUser su = getSpectator(p.getName());
			if(su != null) {
				leaveGame(su);
				return;
			}
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("leave-not-playing")));
			return;
		}
		
		if(p.getVehicle() != null)
			p.getVehicle().setPassenger(null);
		if(p.getPassenger() != null) {
			p.setPassenger(null);
		}
		
		final User user = getUser(p.getName());
		user.getStatistics().updateStatistics();
		user.clear();
		Game game = user.getGame();
		
		for(SpectatorUser su : game.getSpecators()) {
			p.showPlayer(su.getPlayer());
		}
		
		
		if(game.getState() == GameState.WAITING || game.getState() == GameState.VOTING || game.getState() == GameState.COOLDOWN)
			game.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-leave").replace("%0%", p.getName()).replace("%1%", Integer.valueOf(game.getPlayingUsers() - 1).toString()).replace("%2%", Integer.valueOf(game.getMaximumPlayers()).toString())));
		game.leave(user);
		
		if(game.getScoreboardPhase() != null) {
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}
		
		
		if(p.isDead()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SurvivalGames.instance, new Runnable() {
				public void run() {
					setState(p, user);
				}
			}, 1);
		} else {
			setState(p, user);
		}
		ArrayList<ItemStack> con = new ArrayList(SurvivalGames.Pinvo.getList("Inventories." + p.getName()));
        if (p.getInventory().getContents() == null) {
            int es = new Integer(p.getEnderChest().getContents().length);
            for (int i = 1; i < es + 1; i++) {
                if (i > es) {
                    break;
                }
                ItemStack item = null;
                try {
                    item = new ItemStack(p.getEnderChest().getItem(i));
                } catch (IllegalArgumentException e) {
                }
                if (item != null) {
                    for (int ii = 1; ii < es + 1; ii++) {
                        if (p.getInventory().getContents().length <= 27) {
                            p.getInventory().addItem(new ItemStack(item));
                            p.getEnderChest().setItem(i, new ItemStack(Material.AIR));
                            break;
                        }
                        if (p.getInventory().getContents().length >=27 && p.getInventory().getContents().length <= 37) {
                            p.getInventory().setItem(i,item);
                            con.remove(item);
                            break;
                        }
                    }
                }
            }
        }
	}
	
	public void setState(Player p, UserState state) {
		p.teleport(state.getLocation());
		p.setFoodLevel(state.getFoodLevel());
		p.setGameMode(state.getGameMode());
		p.setAllowFlight(state.getAllowFlight());
		p.setFlying(state.isFlying());
		p.setLevel(state.getLevel());
		p.setExp(state.getExp());
		p.setFallDistance(state.getFallDistance());
		p.setFireTicks(state.getFireTicks());
		}
	
	public boolean isSpectator(String name) {
		return getSpectator(name) != null;
	}
	
	public SpectatorUser getSpectator(Player p) {
		return getSpectator(p.getName());
	}
	
	public SpectatorUser getSpectator(String name) {
		for(Game game : gm.getGames()) {
			for(SpectatorUser su : game.getSpecators()) {
				if(su.getName().equals(name))
					return su;
			}
		}
		return null;
	}
	
	public boolean isPlaying(String name) {
		return getUser(name) != null;
	}
	
	public User getUser(Player player) {
		return getUser(player.getName());
	}
	
	public User getUser(String name) {
		for(Game game : gm.getGames()) {
			User u = game.getUser(name);
			if(u != null)
				return u;
		}
		return null;
	}

}
