package me.maker56.survivalgames.user;

import java.util.Iterator;

import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.statistics.StatisticData;
import me.maker56.survivalgames.statistics.StatisticLoader;
import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public abstract class UserState {
	
	protected Player player;
	private int food, level, fireticks;
	private float exp;
	private Location loc;
	private GameMode gamemode;
	private boolean allowFlying, flying;
	private float fall;
	
	private long joinTime = System.currentTimeMillis();
	private Game game;
	
	public UserState(Player p, Game game) {
		this.game = game;
		this.player = p;
		this.food = p.getFoodLevel();
		this.exp = p.getExp();
		this.level = p.getLevel();
		this.loc = p.getLocation();
		this.fall = p.getFallDistance();
		this.gamemode = p.getGameMode();
		this.allowFlying = p.getAllowFlight();
		this.flying = p.isFlying();
		this.fireticks = p.getFireTicks();
		
		StatisticLoader.load(this);
	}
	
	
	public Game getGame() {
		return game;
	}
	
	public long getJoinTime() {
		return joinTime;
	}
	
	public float getFallDistance() {
		return fall;
	}

	public GameMode getGameMode() {
		return gamemode;
	}

	public boolean getAllowFlight() {
		return allowFlying;
	}

	public boolean isFlying() {
		return flying;
	}
	
	public int getFireTicks() {
		return this.fireticks;
	}

	public Location getLocation() {
		return this.loc;
	}
	
	public int getFoodLevel() {
		return this.food;
	}
	

	public float getExp() {
		return this.exp;
	}

	public int getLevel() {
		return this.level;
	}

	public void clear() {
		for (Iterator<PotionEffect> i = player.getActivePotionEffects().iterator(); i.hasNext();) {
			player.removePotionEffect(i.next().getType());
		}
		player.setWalkSpeed(0.2F);
		player.setFlySpeed(0.1F);
		player.setMaxHealth(20D);
		player.setHealth(20D);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0);
		player.setFireTicks(0);
		player.setGameMode(GameMode.SURVIVAL);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.getInventory().setHeldItemSlot(0);

		clearInventory();
	}

	public void clearInventory() {
		ItemStack[] inv = player.getInventory().getContents();
		for (int i = 0; i < inv.length; i++) {
			inv[i] = null;
		}
		player.getInventory().setContents(inv);
		inv = player.getInventory().getArmorContents();
		for (int i = 0; i < inv.length; i++) {
			inv[i] = null;
		}
		player.getInventory().setArmorContents(inv);
		player.updateInventory();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getName() {
		return player.getName();
	}
	
	public void sendMessage(BaseComponent[] message) {
		player.spigot().sendMessage(message);
	}
	
	public void sendMessage(String message) {
		player.sendMessage(message);
	}
	
	public String getUUID() {
		return player.getUniqueId().toString();
	}
	
	public abstract boolean isSpectator();
	
	// SQL
	private StatisticData sd;
	
	public StatisticData getStatistics() {
		return sd;
	}
	
	public void setStatistics(StatisticData sd) {
		this.sd = sd;
	}
	
	public boolean areStatisticsLoaded() {
		return sd != null;
	}

}