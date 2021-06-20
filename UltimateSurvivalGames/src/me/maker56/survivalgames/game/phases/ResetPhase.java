package me.maker56.survivalgames.game.phases;

import com.sk89q.worldedit.math.BlockVector3;
import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.Util;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.game.GameState;
import me.maker56.survivalgames.reset.Reset;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class ResetPhase {
	
	private Game game;
	
	public ResetPhase(Game game) {
		this.game = game;
		start();
	}
	
	private void start() {

	    game.kickall();
        game.setState(GameState.RESET);
        World w;
        try {
            w = game.getCurrentArena().getMinimumLocation().getWorld();
        } catch (NullPointerException e){
            w = Bukkit.getWorld(SurvivalGames.instance.getConfig().getString("GameWorldName"));
        }




        if(game.isResetEnabled()) {
            new Reset(w, game.getName(), game.getCurrentArena().getName(), game.getChunksToReset()).start();
        } else {
            String name = game.getName();
            SurvivalGames.gameManager.unload(game);
             List<String> chunks = game.getChunksToReset();
            if (chunks != null) {
                int s = chunks.size();
                if (s > 0) {
                    for (int i = 0; i < s - 1; i++) {
                        String f = null;
                        if (chunks.isEmpty()) {
                            break;
                        }
                        try {
                            f = chunks.get(i);
                        } catch (IndexOutOfBoundsException e) {
                            Util.Error(e.toString());
                            }
                        if (f != null) {
                            //resetEntities(f);
                            chunks.remove(0);
                        }
                    }
                }
            }
            SurvivalGames.gameManager.load(name);
            SurvivalGames.signManager.updateSigns();
        }
    }
//    public void resetEntities(final String chunk) {
//        Bukkit.getScheduler().callSyncMethod(SurvivalGames.instance, (Callable<Void>) () -> {
//            World world = game.getCurrentArena().getDomeMiddle().getWorld();
//            Chunk c = world.getChunkAt(chunk.getBlockX(), chunk.getBlockZ());
//            boolean l = c.isLoaded();
//            if(!l)
//                c.load();
//
//            for(Entity e : c.getEntities()) {
//                if(e instanceof Item || e instanceof LivingEntity || e instanceof Arrow) {
//                    e.remove();
//                }
//            }
//
//            if(!l)
//                c.unload();
//
//            return null;
//        });
//    }

}
