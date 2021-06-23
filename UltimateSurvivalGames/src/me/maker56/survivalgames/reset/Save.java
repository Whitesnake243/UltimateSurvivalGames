package me.maker56.survivalgames.reset;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.chunk.Chunk;
import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.events.SaveDoneEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

import static me.maker56.survivalgames.SurvivalGames.s;

@SuppressWarnings("deprecation")
public class Save extends Thread {
    private long sizeB = 0;
    private String format;
    private String lobby, arena;
    private long Time;
    private long seconds;;
    private Region s;
    private List<String> l;

    private Selection sel;
    private long start;


    public Save(String lobby, String arena, Selection sel, Player pname, Region s, List<String> l) {
        this.lobby = lobby;
        this.sel = sel;
        this.arena = arena;
        this.pname = pname;
        this.s = s;
        this.l = l;
    }

    // PERCENT CALCULATION

    private double maxSteps, stepsDone = 0;
    private BukkitTask task;
    private Player pname;

    public void startPercentInfoScheduler() {
        task = Bukkit.getScheduler().runTaskTimer(SurvivalGames.instance, new Runnable() {
            public void run() {
                Player p = pname;
                if(p != null) {
                    Time = System.currentTimeMillis() - start;
                    seconds = (int) (Time / 1000);
                    Time -= seconds * 1000;
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&eSaving Arena Please Standby, Time elapsed: "+ seconds));

                }
            }
        }, 70, 60);
    }


    // temp till new saving is finished and working
    public void run() {
        if(pname != null) {
            startPercentInfoScheduler();
        }
        try {
            start = System.currentTimeMillis();
            File file;
            BlockVector3 Max;
            BlockVector3 Min;
            String k;
            int minX = 0,minZ = 0,maxX = 0,maxZ = 0,chunkX,chunkZ;
            for (int i = 0; i <= l.size()-1; i++) {
                String o = l.get(i);
                if (i >= l.size()-1) {
                    k = o.substring(1,l.get(i).length()-1);
                } else {
                    k = o.substring(1,l.get(i).length());
                }
                //String k = "-3, 4";
                String[] split = k.split(", ");
                chunkX = Integer.parseInt(split[0]);
                chunkZ = Integer.parseInt(split[1]);
                if(chunkX >= 0){

                    minX = chunkX*16;
                    maxX = minX+15;

                }
                if(chunkZ >= 0){

                    minZ = chunkZ*16;
                    maxZ = minZ+15;

                }
                if(chunkX < 0){

                    minX = chunkX*16;
                    maxX = minX+15;

                }
                if(chunkZ < 0){

                    minZ = chunkZ*16;
                    maxZ = minZ+15;

                }
                Max = BlockVector3.at(maxX,255, maxZ);
                Min = BlockVector3.at(minX,0, minZ);
                file = new File("plugins/SurvivalGames/reset/" + lobby + arena + "/" + split[0] +","+ split[1] + ".schematic");
                file.mkdirs();
                if(file.exists()) {
                    file.delete();
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                Region t = new CuboidRegion(Min,Max);
                BlockArrayClipboard clipboard = new BlockArrayClipboard(t);

                try (EditSession editSession = SurvivalGames.getWorldEdit().getWorldEdit().newEditSession(s.getWorld())) {
                    ForwardExtentCopy fo = new ForwardExtentCopy(
                            editSession, t, clipboard, Min
                    );
                    // configure here
                    fo.setCopyingEntities(false);

                    Operations.complete(fo);
                }
                try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                    writer.write(clipboard);
                }
                sizeB = sizeB+ file.length();
            }


            // Copy start


            format = "Bytes";
            if(sizeB >= 1000) {
                sizeB = sizeB / 1000;
                format = "KiloBytes";
                if(sizeB >= 1000) {
                    sizeB = sizeB / 1000;
                    format = "MegaBytes";
                }
            }

            //End saving of arena
            Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Bukkit.getPluginManager().callEvent(new SaveDoneEvent(lobby, arena,  (System.currentTimeMillis() - start), sizeB, format));
                    return null;
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
        stepsDone++;
        if(task != null)
            task.cancel();
    }
}