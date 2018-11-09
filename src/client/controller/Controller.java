package client.controller;

import client.net.OutputHandler;
import client.net.ServerConnection;

import java.util.concurrent.CompletableFuture;

public class Controller {
    private ServerConnection connection;
    private OutputHandler outputHandler;

    public Controller(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    public void connect(String host, int port, String manual) {
        CompletableFuture.runAsync(() -> {
            connection = new ServerConnection();
            connection.connect(host, port, this.outputHandler);
        }).thenRun(() -> outputHandler.handleMessage("Connected to host '" + host + "' \non port: " + port + "\n"
                                                        + manual));
    }

    public void startGame() {
        CompletableFuture.runAsync(() -> {
            connection.startGame();
        });
    }

    public void guess(String guess) {
        CompletableFuture.runAsync(() -> {
            connection.guess(guess);
        });
    }

    public void disconnect() {
        CompletableFuture.runAsync(() -> {
            connection.disconnect();
        }).thenRun(() -> outputHandler.handleMessage("disconnected"));
    }
}
