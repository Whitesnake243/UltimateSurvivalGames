package me.maker56.survivalgames.listener;

import java.util.List;
import java.util.concurrent.Callable;

import com.sk89q.worldedit.math.BlockVector3;
import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.arena.Arena;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.commands.permission.Permission;
import me.maker56.survivalgames.commands.permission.PermissionHandler;
import me.maker56.survivalgames.events.ResetDoneEvent;
import me.maker56.survivalgames.events.SaveDoneEvent;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.game.GameManager;
import me.maker56.survivalgames.game.GameState;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ResetListener implements Listener {
	
	private GameManager gm = SurvivalGames.gameManager;
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemDrop(PlayerDropItemEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getItemDrop().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		logChunk(event.getEntity().getLocation());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onFromToEvent(BlockFromToEvent event)  {
	    if(!event.isCancelled()) {
	    	logChunk(event.getToBlock().getLocation());
		    logChunk(event.getBlock().getLocation());
	    }
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockGrow(BlockGrowEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeafDecay(LeavesDecayEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBurn(BlockBurnEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getBlock().getLocation());
		}
	}
	
	@EventHandler
	public void onBlockFade(BlockFadeEvent event) {
		if(!event.isCancelled()) {
			logChunk(event.getBlock().getLocation());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockExplode(EntityExplodeEvent event) {
		if(!event.isCancelled()) {
			List<Block> blocks = event.blockList();
			if(blocks.size() > 0) {
				Location loc = blocks.get(0).getLocation();
				for(Game game : gm.getGames()) {
					for(Arena a : game.getArenas()) {
						if(a.containsBlock(loc)) {
							Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
								@Override
								public Void call() {
									event.setCancelled(true);
									return null;
								}
							});
							return;
						}
					}
				}
			}	
		}
	}
	 
	private void logChunk(Location loc) {
		for(Game game : gm.getGames()) {
			if(game.getState() == GameState.INGAME || game.getState() == GameState.DEATHMATCH) {
				Arena a = game.getCurrentArena();
				if(a.containsBlock(loc)) {
					BlockVector3 chunkKey = Util.parseLocToBv3(loc.getX(),loc.getY(), loc.getZ());
					if(!game.getChunksToReset().contains(chunkKey)) {
						game.getChunksToReset().add(chunkKey);
						List<String> reset = SurvivalGames.reset.getStringList("Startup-Reset." + game.getName() + "." + a.getName());
						reset.add(chunkKey.toString());
						SurvivalGames.reset.set("Startup-Reset." + game.getName() + "." + a.getName(), reset);
						SurvivalGames.saveReset();
					}
					return;
				}
			}
		}
	}
	
	
	// AFTER SAVE / RESET
	
	@EventHandler
	public void onSaveComplete(SaveDoneEvent event) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(PermissionHandler.hasPermission(p, Permission.ARENA)) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "Done saving arena " + event.getArena() + " in lobby " + event.getLobby() + "! It tooks " + event.getTime() + "! The file is " + event.getFileSize() + " " + event.getFileSizeFormat() + " big."));
			}
		}
	}
	
	@EventHandler
	public void onResetComplete(ResetDoneEvent event) {
		Game game = gm.getGame(event.getLobby());
		if(game != null) {
			gm.unload(game);
		}
		gm.load(event.getLobby());
		SurvivalGames.signManager.updateSigns();
	}

}
