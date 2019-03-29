package me.maker56.survivalgames.reset;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.events.ResetDoneEvent;

import org.apache.commons.lang.UnhandledException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.plugin.Plugin;

import static me.maker56.survivalgames.SurvivalGames.database;
import static me.maker56.survivalgames.SurvivalGames.s;

@SuppressWarnings("deprecation")
public class Reset extends Thread {

    private static List<String> resets = new ArrayList<>();
    private Integer px;
    private Integer py;
    private Integer pz;

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

    private String lobby, arena;
    private World world;
    private long start;
    private List<String> chunks = new ArrayList<>();

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
        if(isResetting(lobby, arena))
            return;
        System.out.println("[SurvivalGames] Start arena reset... (arena " + arena + ", lobby " + lobby + ")");
        resets.add(lobby + arena);
        start = System.currentTimeMillis();

        File file = new File("plugins/SurvivalGames/reset/" + lobby + arena + ".schematic");
        Clipboard clipboard;
        String pos = database.getString("Games." + lobby + ".Arenas." + arena + ".Min");

        pos.replaceFirst("\\(","0,");
        pos.replaceFirst("\\)",",0");
        String[] rawpos = pos.split(", ");
        px = Integer.parseInt(rawpos[1]);
        py = Integer.parseInt(rawpos[2]);
        pz = Integer.parseInt(rawpos[3]);


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
                        .to(BlockVector3.at(px,py,pz))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            Util.Error(String.valueOf(e));
        }


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
    EditSession es = WorldEdit.getInstance().getEditSessionFactory().getEditSession(s.getWorld(), -1);
    public void resetNext() {
        build = true;
        Util.debug("reset next!");
        Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
            @Override
            public Void call() {
                for(Entry<Vector3, BaseBlock> map : cReset.entrySet()) {
                    try {
                        BlockVector3 ma = BlockVector3.at(map.getKey().getX(),map.getKey().getY(),map.getKey().getZ());
                        es.setBlock(ma, map.getValue());
                    } catch (MaxChangedBlocksException e) {
                        e.printStackTrace();
                    }
                }
                cReset.clear();
                build = false;
                return null;
            }
        });
    }

    public void resetEntities(final String chunk) {
        Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
            @Override
            public Void call() {
                String[] split = chunk.split(",");
                Chunk c = world.getChunkAt(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
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

    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws DataException {
        return expected.cast(items.get(key));
    }


}