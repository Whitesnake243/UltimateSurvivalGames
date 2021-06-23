package me.maker56.survivalgames.reset;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.events.ResetDoneEvent;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.game.GameManager;
import org.bukkit.Bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;



@SuppressWarnings("deprecation")
public class Reset extends Thread {

    private static List<String> resets = new ArrayList<>();

    public static boolean isResetting(String lobby, String arena) {
        return resets.contains(lobby + arena);
    }

    public static boolean isResseting(String lobby) {
        for(String key : resets) {
            if(key.startsWith(lobby))
                return true;
        }
        return false;
    }
    private GameManager gm = SurvivalGames.gameManager;
    private String lobby, arena;
    private World world;
    private long start;
    private List<String> chunks;

    private HashMap<Vector3, BaseBlock> cReset = new HashMap<>();
    private boolean build = false;

    public Reset(World w, String lobby, String arena, List<String> chunks) {
        this.world = w;
        this.lobby = lobby;
        this.arena = arena;
        this.chunks = chunks;
    }

    @Override
    public void run() {
        //Fawe  Worldedit
            //System.out.println("Fawe Not detected Using Regular Mode");
            if (isResetting(lobby, arena))
                return;
            System.out.println("[SurvivalGames] Starting arena reset... (arena: " + arena + ", lobby: " + lobby + ")");
            resets.add(lobby + arena);
            start = System.currentTimeMillis();
            Clipboard clipboard;
            String path = "Games." + lobby + ".Arenas." + arena;
            String M = SurvivalGames.database.getString("Games." + lobby + ".Arenas." + arena + ".Chest.TypeID");
            if (M == "54" || M.equals("chest")) {
                SurvivalGames.database.set(path + "Chest.TypeID", "CHEST");
                SurvivalGames.saveDataBase();
            }
            BlockVector3 Min;
            int minX = 0,minZ = 0,maxX = 0,maxZ = 0,chunkX,chunkZ;
        for (int i = 0; i <= chunks.size()-1; i++) {
            if (!chunks.isEmpty()) {
                String k = chunks.get(i).substring(1, chunks.get(i).length() - 1);
                String[] split = k.split(", ");
                chunkX = Integer.parseInt(split[0]);
                chunkZ = Integer.parseInt(split[1]);
                if(chunkX >= 0){

                    minX = chunkX*16;
                    //maxX = minX+15;

                }
                if(chunkZ >= 0){

                    minZ = chunkZ*16;
                    //maxZ = minZ+15;

                }
                if(chunkX < 0){

                    minX = chunkX*16;
                    //maxX = minX+15;

                }
                if(chunkZ < 0){

                    minZ = chunkZ*16;
                    //maxZ = minZ+15;

                }
                Min = BlockVector3.at(minX,0, minZ);
                File file = new File("plugins/SurvivalGames/reset/" + lobby + arena + "/" + split[0] +","+ split[1] + ".schematic");
                if (!file.exists()) {
                    Util.Error("Cant find chunk a schem for Arena:"+ arena +" In Lobby:"+ lobby +" Missing File:"+split[0]+","+split[1]+".schematic");
                    return;
                }
                try {
                    // find and load Schem
                    ClipboardFormat format = ClipboardFormats.findByFile(file);
                    try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                        clipboard = reader.read();
                    }

                    // paste Schem
                    try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), -1)) {
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(Min)
                                .ignoreAirBlocks(false)
                                .build();
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        Util.Error(""+e);
                    }
                } catch (IOException e) {
                    Util.Error(""+e);
                }
                resetEntities(BlockVector2.at(Double.parseDouble(split[0]),Double.parseDouble(split[1])));
                chunks.remove(i);

            }
        }
//            try {
//            //    find and load Schem
//                ClipboardFormat format = ClipboardFormats.findByFile(file);
//                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
//                clipboard = reader.read();
//                }
//
//
                // paste Schem
//                try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), -1)) {
//                    Operation operation = new ClipboardHolder(clipboard)
//                            .createPaste(editSession)
//                            .to(bv3)
//                            .ignoreAirBlocks(false)
//                            .build();
//                             Operations.complete(operation);
//                } catch (WorldEditException e) {
//                    e.printStackTrace();
//                }
//            } catch (IOException e) {
//                Util.Error(String.valueOf(e));
//            }




//        if (chunks != null) {
//            int s = chunks.size();
//            if (s > 0) {
//                for (int i = 0; i < s + 1; i++) {
//                    BlockVector2 f = null;
//                    try {
//                        //f = chunks.get(0);
//                    } catch (IndexOutOfBoundsException e) {
//                        Util.Error(e.toString());
//                        try {
//                            //f = chunks.get(1);
//                        }catch (IndexOutOfBoundsException t) {
//                            Util.Error(e.toString());
//                            break;
//                        }
//                    }
//                    if (f != null) {
//                        resetEntities(f);
//                        chunks.remove(0);
//                    }
//                }
//            }

        Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
            @Override
            public Void call() {
                resets.remove(lobby + arena);

                SurvivalGames.reset.set("Startup-Reset." + lobby + "." + arena, null);
                SurvivalGames.saveReset();

                int time = (int) ((System.currentTimeMillis() - start) / 1000);
                System.out.println("[SurvivalGames] Finished arena reset! (arena " + arena + ", lobby " + lobby + ") Time: " + time + " seconds!");
                Bukkit.getPluginManager().callEvent(new ResetDoneEvent(lobby, arena, time));
                return null;
            }
        });
    }

    public void resetEntities(BlockVector2 chunk) {
        Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
            @Override
            public Void call() {
                Chunk c = world.getChunkAt(chunk.getBlockX(), chunk.getBlockZ());
                boolean l = c.isLoaded();
                if(!l)
                    c.load();

                for(Entity e : c.getEntities()) {
                    if(e instanceof Item || e instanceof LivingEntity || e instanceof Arrow) {
                        e.remove();
                    }
                }

                if(!l)
                    c.unload();

                return null;
            }
        });
    }


}