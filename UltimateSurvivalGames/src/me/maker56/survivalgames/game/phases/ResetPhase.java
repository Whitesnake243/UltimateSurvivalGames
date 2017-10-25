package me.maker56.survivalgames.game.phases;

import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.game.GameState;

public class ResetPhase {
	
	private Game game;
	
	public ResetPhase(Game game) {
		this.game = game;
		start();
	}
	
	private void start() {
		game.kickall();
		game.setState(GameState.RESET);
		String name = game.getName();
			SurvivalGames.gameManager.unload(game);
			SurvivalGames.gameManager.load(name);
			SurvivalGames.signManager.updateSigns();
		}
}