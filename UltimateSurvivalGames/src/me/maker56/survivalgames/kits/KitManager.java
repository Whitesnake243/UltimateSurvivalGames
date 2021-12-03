package me.maker56.survivalgames.kits;

import me.maker56.survivalgames.SurvivalGames;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class KitManager {


    private static FileConfiguration cfg;
    private ArrayList<Kit> Kits;
    private ArrayList<String> Skits;
    //private ;


    public static void reinitializeDatabase() {
        cfg = SurvivalGames.database;
    }

    public void load() {
        String path = "Kit.";
        FileConfiguration config = SurvivalGames.instance.getConfig();


        cfg.set("kits.Enabled.Names", config.getList("Kits.Enabled.Names"));
        cfg.set("Kit.Item", config.get("Kit.Item"));



    }

}
