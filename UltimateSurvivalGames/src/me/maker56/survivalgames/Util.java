package me.maker56.survivalgames;

import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.listener.UpdateListener;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Util {
	
	// ITEMSTACK
	public static boolean debug = false;
	private static Random random = new Random();
	
	@SuppressWarnings("deprecation")
	public static ItemStack parseItemStack(String s) {
		try {
			String[] gSplit = s.split(" ");
			ItemStack is;
			
			// ITEM ID / MATERIAL / SUBID
			String[] idsSplit = gSplit[0].split(":");
			try {
				is = new ItemStack(Material.getMaterial(idsSplit[0]));
			} catch(NumberFormatException e) {
				e.printStackTrace();
				is = new ItemStack(Material.valueOf(idsSplit[0].toLowerCase()));
			}

			if(idsSplit.length > 1)
				is.setDurability(Short.parseShort(idsSplit[1]));
			
			if(gSplit.length > 1) {
				int metaStart = 2;
				
				try {
					is.setAmount(Integer.parseInt(gSplit[1]));
				} catch(NumberFormatException e) {
					metaStart = 1;
				}
				ItemMeta im = is.getItemMeta();
				for(int meta = metaStart; meta < gSplit.length; meta++) {
					String rawKey = gSplit[meta];
					String[] split = rawKey.split(":");
					String key = split[0];
					
					if(key.equalsIgnoreCase("name")) {
						im.setDisplayName(ChatColor.translateAlternateColorCodes('&', split[1]).replace("_", " "));
					} else if(key.equalsIgnoreCase("lore")) {
						List<String> lore = new ArrayList<>();
						for(String line : split[1].split("//")) {
							lore.add(ChatColor.translateAlternateColorCodes('&', line).replace("_", " "));
						}
						im.setLore(lore);
					} else if(key.equalsIgnoreCase("color") && im instanceof LeatherArmorMeta) {
						LeatherArmorMeta lam = (LeatherArmorMeta) im;
						String[] csplit = split[1].split(",");
						Color color = Color.fromBGR(Integer.parseInt(csplit[0]), Integer.parseInt(csplit[1]), Integer.parseInt(csplit[2]));
						lam.setColor(color);
					} else if(key.equalsIgnoreCase("effect") && im instanceof PotionMeta) {
						PotionMeta pm = (PotionMeta) im;
						String[] psplit = split[1].split(",");
						pm.addCustomEffect(new PotionEffect(PotionEffectType.getByName(psplit[0]), Integer.parseInt(psplit[1]) * 20, Integer.parseInt(psplit[2])), true);
					} else if(key.equalsIgnoreCase("player") && im instanceof SkullMeta) {
						((SkullMeta)im).setOwner(split[1]);
					} else if(key.equalsIgnoreCase("enchant")) {
						String[] esplit = split[1].split(",");
						im.addEnchant(getEnchantment(esplit[0]), Integer.parseInt(esplit[1]), true);
					}
					
				}
				is.setItemMeta(im);
			}
			
			return is;
		} catch(Exception e) {
			System.err.println("[SurvivalGames] Cannot parse ItemStack: " + s + " - Mabye this is the reason: " + e.toString());
			return null;
		}
	}
	
	public static Vector calculateVector(Location from, Location to) {

		double dX = from.getX() - to.getX();
        double dY = from.getY() - to.getY();
        double dZ = from.getZ() - to.getZ();
        double yaw = Math.atan2(dZ, dX);
        double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch) * Math.sin(yaw);
        double z = Math.cos(pitch);

		return new Vector(x, z, y);
    }
	
	public static void shootRandomFirework(Location loc, int height) {
		Firework f = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fm = f.getFireworkMeta();
		fm.setPower(height);
		int effectAmount = random.nextInt(3) + 1;
		for(int i = 0; i < effectAmount; i++) {
			Builder b = FireworkEffect.builder();
			int colorAmount = random.nextInt(3) + 1;
			for(int ii = 0; ii < colorAmount; ii++) {
				b.withColor(Color.fromBGR(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
			}
			b.with(Type.values()[random.nextInt(Type.values().length)]);
			b.flicker(random.nextInt(2) != 0);
			b.trail(random.nextInt(2) != 0);
			fm.addEffect(b.build());
		}
		f.setFireworkMeta(fm);
	}
	
	// EXP PERCENT
	
	public static float getExpPercent(float value, float max) {
		if(value == 0)
			return 0;
		return value / max;
	}
	
	// ENCHANTMENT
	public static Enchantment getEnchantment(String enc) {
		enc = enc.toUpperCase();
		Enchantment en = Enchantment.getByName(enc);
		
		if(en == null) {
			switch (enc) {
				case "PROTECTION" -> en = Enchantment.PROTECTION_ENVIRONMENTAL;
				case "FIRE_PROTECTION" -> en = Enchantment.PROTECTION_FIRE;
				case "FEATHER_FALLING" -> en = Enchantment.PROTECTION_FALL;
				case "BLAST_PROTECTION" -> en = Enchantment.PROTECTION_EXPLOSIONS;
				case "PROJECTILE_PROTCETION" -> en = Enchantment.PROTECTION_PROJECTILE;
				case "RESPIRATION" -> en = Enchantment.OXYGEN;
				case "AQUA_AFFINITY" -> en = Enchantment.WATER_WORKER;
				case "SHARPNESS" -> en = Enchantment.DAMAGE_ALL;
				case "SMITE" -> en = Enchantment.DAMAGE_UNDEAD;
				case "BANE_OF_ARTHROPODS" -> en = Enchantment.DAMAGE_ARTHROPODS;
				case "LOOTING" -> en = Enchantment.LOOT_BONUS_MOBS;
				case "EFFICIENCY" -> en = Enchantment.DIG_SPEED;
				case "UNBREAKING" -> en = Enchantment.DURABILITY;
				case "FORTUNE" -> en = Enchantment.LOOT_BONUS_BLOCKS;
				case "POWER" -> en = Enchantment.ARROW_DAMAGE;
				case "PUNCH" -> en = Enchantment.ARROW_KNOCKBACK;
				case "FLAME" -> en = Enchantment.ARROW_FIRE;
				case "INFINITY" -> en = Enchantment.ARROW_INFINITE;
				case "LUCK_OF_THE_SEA" -> en = Enchantment.LUCK;
			}
		}
		return en;
	}
	// TIME
	
	public static String getFormatedTime(int seconds) {
		int minutes = seconds / 60;
		int hours = minutes / 60;
		int days = hours / 24;
		
		seconds -= minutes * 60;
		minutes -= hours * 60;
		hours -= days * 24;
		
		String s = "";
		if(days > 0)
			s += days + "d";
		if(hours > 0)
			s += hours + "h";
		if(minutes > 0)
			s += minutes + "m";
		if(seconds > 0) 
			s += seconds + "s";
	
		return s;
	}
	
	// LOCATION
	
	public static Location parseLocation(String s) {
		String[] split = s.split(",");
		Location loc = null;
		try {
			World world = Bukkit.getWorld(split[0]);
			if(split.length == 6) {
				double x = Double.parseDouble(split[1]);
				double y = Double.parseDouble(split[2]);
				double z = Double.parseDouble(split[3]);

				float yaw = Float.parseFloat(split[4]);
				float pitch = Float.parseFloat(split[5]);
				loc = new Location(world, x, y, z, yaw, pitch);
			} else if(split.length == 4) {
				int x = Integer.parseInt(split[1]);
				int y = Integer.parseInt(split[2]);
				int z = Integer.parseInt(split[3]);

				loc = new Location(world, x, y, z);
			}
		} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.err.println("[SurvivalGames] Cannot parse location from string: " + s);
		}
		
		return loc;
	}

	public static  BlockVector2 StrToBv3(String bv3) {
		Double xMin;
		Double zMin;
		bv3 = bv3.substring(1, bv3.length() - 1);
		String[] a = bv3.split(", ");
		if(a.length >= 3) {
			return null;
		} else {
			xMin = Double.parseDouble(a[0]);
			zMin = Double.parseDouble(a[1]);
			Util.Error("X:"+a[0]+", Y:"+a[1]+", Z:"+a[2]);
			return BlockVector2.at(xMin,zMin);
		}

	}

	public static  String ChunkCalcBv2(BlockVector2 bv3) {
		int xMin = bv3.getBlockX()*16;
		int yMin = 0;
		int zMin = bv3.getBlockZ()*16;
		int xMax;
		int yMax = 255;
		int zMax;
		if(xMin < 0) {
			xMax = xMin-16;
		} else {
			xMax = xMin+16;
		}
		if(zMin < 0) {
			zMax = zMin-16;
		} else {
			zMax = zMin+16;
		}
		return BlockVector3.at(xMin,yMin,zMin)+"/"+BlockVector3.at(zMax,yMax,zMax);
	}
	public static  String ChunkCalc(BlockVector3 bv3) {
		int xMin = bv3.getBlockX()*16;
		int yMin = 0;
		int zMin = bv3.getBlockZ()*16;
		int xMax;
		int yMax =255;
		int zMax;

		if(xMin < 0) {
			xMax = xMin-16;
		} else {
			xMax = xMin+16;
		}
		if(zMin < 0) {
			zMax = zMin-16;
		} else {
			zMax = zMin+16;
		}
		return BlockVector3.at(xMin,yMin,zMin)+"/"+BlockVector3.at(zMax,yMax,zMax);
	}
    public static BlockVector3 parseLocToBv3(String s) {
        String[] split = s.split(",");
        BlockVector3 loc = null;
        try {
            if(split.length == 6) {
                double x = Double.parseDouble(split[1]);
                double y = Double.parseDouble(split[2]);
                double z = Double.parseDouble(split[3]);
                loc = BlockVector3.at(x, y, z);
            } else if(split.length == 4) {
                int x = Integer.parseInt(split[1]);
                int y = Integer.parseInt(split[2]);
                int z = Integer.parseInt(split[3]);

                loc = BlockVector3.at(x, y, z);
            }
        } catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("[SurvivalGames] Cannot parse location from string: " + s);
        }

        return loc;
    }
	public static BlockVector3 parseLocToBv3(Double x, Double y, Double z) {
		return BlockVector3.at(x, y, z);
	}
	public static BlockVector3 parseLocToBv3(Location loc) {
		return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
	}
	public static Location parseBv3ToLoc(BlockVector3 bv3, String w){
		return new Location(Bukkit.getWorld(w),bv3.getX(),bv3.getY(),bv3.getZ());
	}
	public static String serializeLocation(Location l, boolean exact) {
		if(l != null) {
			String key = l.getWorld().getName() + ",";
			if(exact) {
				key += l.getX() + "," + l.getY() + "," + l.getZ() + "," + l.getYaw() + "," + l.getPitch();
			} else {
				key += l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
			}
			
			return key;
		}
		return null;
	}

	public static void debug(Object object) {
		if(debug) {
			System.out.println("[SurvivalGames] [Debug] " + object.toString());
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.isOp()) 
					p.sendMessage("&7[Debug] " + object.toString());
			}
		}
	}
	public static void Error(String e) {
		SurvivalGames.instance.getLogger().severe("[SurvivalGames] " + e);
	}
}
