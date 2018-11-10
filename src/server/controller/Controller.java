package server.controller;

import server.model.Game;

public class Controller {
    private Game game;

    public Controller() {
        this.game = new Game();
    }

    public void startGame() {
        this.game.startGame();
    }

    public void processGuess(String guess) {
        System.out.println(guess);
        this.game.processGuess(guess.toLowerCase());
    }

    public boolean gameIsOngoing() {
        return this.game.isOngoing();
    }

    public boolean gameIsWon() { return this.game.isWon(); }

    public String getScoreState() {
        return Integer.toString(this.game.getScoreState());
    }

    public String getAttemptsState() {
        return Integer.toString(this.game.getRemainingAttemptsState());
    }

    public String getWordState() {
        return this.game.getWordProgress();
    }
}
