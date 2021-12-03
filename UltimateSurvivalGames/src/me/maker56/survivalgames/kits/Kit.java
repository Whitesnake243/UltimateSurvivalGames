package me.maker56.survivalgames.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Kit {
	
	private String name, description, permission;
	private double costMoney;
	private int costPoints;
	private PayType type;
	private Inventory inventory;
	
	private int slot = 0;
	
	public Kit(String name, String description, Inventory Inventory, String permission, double money, int points, PayType type) {
		this.name = name;
		this.description = description;
		this.permission = permission;
		this.costMoney = money;
		this.costPoints = points;
		this.type = type;
		this.inventory = inventory;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public Inventory getItemStack() {
		return inventory;
	}

	public void setKits(int kits) {
		this.slot = kits;
	}
	public PayType getPayType() {
		return type;
	}
	
	public double getCostMoney() {
		return costMoney;
	}
	
	public int getCostPoints() {
		return costPoints;
	}
	
	public enum PayType { MONEY, POINTS }
	
	// ABILITIES / ITEMS
	private List<ItemStack> items = new ArrayList<>();
	private List<Abilities> abilities = new ArrayList<>();
	
	public List<ItemStack> getItems() {
		return items;
	}
	
	public List<Abilities> getAbilities() {
		return abilities;
	}
}
