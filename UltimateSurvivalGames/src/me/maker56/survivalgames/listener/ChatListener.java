package me.maker56.survivalgames.listener;

import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.user.UserManager;
import me.maker56.survivalgames.user.UserState;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Iterator;

public class ChatListener implements Listener {

	private UserManager um = SurvivalGames.getUserManager();
	private static boolean chat, pex;
	private static String design, specPrefix;
	
	public ChatListener() {
		reinitializeConfig();
	}
	
	public static void reinitializeConfig() {
		FileConfiguration config = SurvivalGames.instance.getConfig();
		chat = config.getBoolean("Chat.Enabled");
		design = ChatColor.translateAlternateColorCodes('&', config.getString("Chat.Design"));
		specPrefix = ChatColor.translateAlternateColorCodes('&', config.getString("Chat.Spectator-State"));
		pex = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");
	}

	
	// SEPARATED CHAT
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(chat && !event.isCancelled()) {
			UserState u = um.getUser(event.getPlayer());
			
			if(u == null)
				u = um.getSpectator(event.getPlayer());
			
			if(u != null) {
				String format = design;
				String[] formats = getFormats(u.getPlayer());
				format = format.replace("{STATE}", ChatColor.translateAlternateColorCodes('&', u.isSpectator() ? specPrefix : ""));
				format = format.replace("{PREFIX}", ChatColor.translateAlternateColorCodes('&', formats[0]));
				format = format.replace(ChatColor.translateAlternateColorCodes('&', "{PLAYERNAME}"), ChatColor.translateAlternateColorCodes('&', u.getName()));
				format = format.replace(ChatColor.translateAlternateColorCodes('&', "{SUFFIX}"), ChatColor.translateAlternateColorCodes('&', formats[1]));
				format = format.replace("{MESSAGE}", ChatColor.translateAlternateColorCodes('&', event.getMessage()));
				
				System.out.println(ChatColor.stripColor(format));
				
				TextComponent bc = new TextComponent(format);
				bc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to show " + u.getName() + (u.getName().toLowerCase().endsWith("s") ? "" : "'s") + " statistics")));
				bc.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/sg stats " + u.getName()));
	
				event.setCancelled(true);
				Game g = u.getGame();
				
				if(u.isSpectator()) {
					g.sendSpectators(bc);
				} else {
					g.sendMessage(bc);
				}
			} else {
				event.getRecipients().removeIf(p -> um.isPlaying(p.getName()) || um.isSpectator(p.getName()));
			}
			
		}

	}
	
	public String[] getFormats(Player p) {
		if(p.isOp()) {
			return new String[] {"&7[&3SG&7] &r[&4", "&r]&7: &a" };
		}
		return new String[] { "&7[&3SG&7] &r", "&7: &r" };
	}

}
