package me.maker56.survivalgames.listener;

import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.UpdateChecker;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.commands.permission.Permission;
import me.maker56.survivalgames.commands.permission.PermissionHandler;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {
	private static String updateInfo = null, versiona = SurvivalGames.version;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(PermissionHandler.hasPermission(p, Permission.LOBBY) || PermissionHandler.hasPermission(p, Permission.ARENA)) {
			new UpdateChecker(Bukkit.getPluginManager().getPlugin("SurvivalGames"), 81702).getVersion(version -> {
				if (!SurvivalGames.version.equalsIgnoreCase(version)) {
					Util.debug("A newer version of survivalgames is available. (" + version + ") You can download it here: https://www.spigotmc.org/resources/ultimatesurvivalgames-mc-1-16-1-17.81702 You're using " + SurvivalGames.version);
					updateInfo = MessageHandler.getMessage("prefix") + "§eA newer version of SurvivalGames is available. §7(§b" + version + "§7) §eYou can download it here: §bhttps://www.spigotmc.org/resources/ultimatesurvivalgames-mc-1-16-1-17.81702 §7You're using §o" + SurvivalGames.version;
				}
			});
		}
	}
}
