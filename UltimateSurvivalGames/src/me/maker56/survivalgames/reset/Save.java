package me.maker56.survivalgames.reset;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.EditSession;
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
    private long sizeB;
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


        Util.debug("init arena save - min:" + Util.serializeLocation(sel.getMinimumLocation(), false) + " max:" + Util.serializeLocation(sel.getMaximumLocation(), false));
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
        try {
            start = System.currentTimeMillis();
            File file = null;
            BlockVector3 Max;
            BlockVector3 Min;
            String k;
            int Xmin,Zmin,Xmax,Zmax;

            for (int i = 0; i <= l.size()-1; i++) {
                String o = l.get(i);
                if (i >= l.size()-1) {
                    k = o.substring(1,l.get(i).length()-1);
                } else {
                    k= o.substring(1,l.get(i).length());
                }
                Util.Error(k);
                String[] split = k.split(", ");
                Xmin = Integer.parseInt(split[0])*16;
                Zmin = Integer.parseInt(split[1])*16;
                if(Xmin < 0) {
                    Xmax = Xmin-16;
                } else {
                    Xmax = Xmin+16;
                }
                if(Zmin < 0) {
                    Zmax = Zmin-16;
                } else {
                    Zmax = Zmin+16;
                }
                Max = BlockVector3.at(Xmax,255,Zmax);
                Min = BlockVector3.at(Xmin,0, Zmin);
                file = new File("plugins/SurvivalGames/reset/" + lobby + arena + "/" + Xmax +","+ Zmax + ".schematic");
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

            }

            if(pname != null) {
                startPercentInfoScheduler();
            }

            // Copy start


            sizeB = file.length();
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
        Util.debug("end save");
        if(task != null)
            task.cancel();
    }

    public void runa() {
        Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    start = System.currentTimeMillis();
                    File file = null;
                    Region k = s;
                    for (int i = 0; i >= k.getChunkCubes().size() || k.getChunkCubes().isEmpty(); --i) {
                        BlockVector3 a = k.getChunkCubes().iterator().next();
                        file = new File("plugins/SurvivalGames/reset/" + lobby + arena + "/" + a.getBlockX() + a.getBlockZ() + ".schematic");
                        file.mkdirs();
                        if(file.exists()) {
                            file.delete();
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                        BlockArrayClipboard clipboard = new BlockArrayClipboard(k);
                        EditSession es = SurvivalGames.getWorldEdit().getWorldEdit().getEditSessionFactory().getEditSession(k.getWorld(), -1);
                        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(es, k, clipboard, k.getMinimumPoint());
                        forwardExtentCopy.setCopyingEntities(true);
                        Operations.complete(forwardExtentCopy);
                        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                            writer.write(clipboard);
                        }

                    }

                    if(pname != null) {
                        startPercentInfoScheduler();
                    }

                    // Copy start


                    sizeB = file.length();
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
                return null;
            }
        });
    }

    private boolean save = false;
    private EditSession es;
    private BlockVector3 origin;
    private List<BlockVector3> cSave = new ArrayList<>();

    byte[] blocks;
    byte[] addBlocks;
    byte[] blockData;
    ArrayList<Tag> tileEntities;
    int width;
    int height;
    int length;

    public void nextSave() {
        save = true;
        Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, new Callable<Void>() {
            @Override
            public Void call() {
                try {

                    Util.debug("Start save: " + cSave.size() + " blocks");
                    for(BlockVector3 v : cSave) {
                        int x = v.getBlockX();
                        int y = v.getBlockY();
                        int z = v.getBlockZ();

                        int index = y * width * length + z * width + x;
                        BlockState block = es.getBlock(BlockVector3.at(x, y, z).add(origin));

                        // Save 4096 IDs in an AddBlocks section
                        if (block.getBlockType().getLegacyId() > 255) {
                            if (addBlocks == null) { // Lazily create section
                                addBlocks = new byte[(blocks.length >> 1) + 1];
                            }
                            addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                            addBlocks[index >> 1] & 0xF0 | (block.getBlockType().getLegacyId() >> 8) & 0xF
                                    : addBlocks[index >> 1] & 0xF | ((Integer.valueOf(block.getBlockType().getId()) >> 8) & 0xF) << 4);
                        }

                        blocks[index] = (byte) block.getBlockType().getLegacyId();
                        blockData[index] = Byte.valueOf(block.toBaseBlock().getNbtData().toString());

                        // Get the list of key/values from the block
                        BaseBlock blockss = es.getFullBlock(BlockVector3.at(x, y, z).add(origin));
                        CompoundTag rawTag = blockss.getNbtData();
                        if (rawTag != null) {
                            Map<String, Tag> values = new HashMap<>();
                            for (Map.Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                values.put(entry.getKey(), entry.getValue());
                            }

                            values.put("id", new StringTag(blockss.getNbtId()));
                            values.put("x", new IntTag(x));
                            values.put("y", new IntTag(y));
                            values.put("z", new IntTag(z));

                            CompoundTag tileEntityTag = new CompoundTag(values);
                            tileEntities.add(tileEntityTag);
                        }
                    }

                    cSave.clear();


                } catch(Exception e) {
                    Util.Error(e.toString());
                }

                //
                save = false;
                return null;
            }
        });
    }


}