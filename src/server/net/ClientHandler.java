package server.net;

import Common.FromClient;
import Common.FromServer;
import server.controller.Controller;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader fromClient;
    private PrintWriter toClient;
    private boolean connected;
    private Controller controller = new Controller();

    ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.connected = true;
    }

    public void run() {
        try {
            boolean autoFlush = true;
            this.fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.toClient = new PrintWriter(clientSocket.getOutputStream(), autoFlush);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (connected) {
            try {
                String cmd = fromClient.readLine();
                CommandParser command = new CommandParser(cmd);
                command.handleCommand();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendGameState() {
        sendMessage(controller.getWordState(), FromServer.WORD);
        sendMessage(controller.getAttemptsState(), FromServer.ATTEMPTS);
        sendMessage(controller.getScoreState(), FromServer.SCORE);
    }

    private void sendMessage(String message, FromServer type) {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append(":");
        builder.append(message);
        toClient.println(builder.toString());
    }

    private class CommandParser {
        private FromClient commandType;
        private String commandArgs;

        CommandParser(String command) {
            String[] cmd = command.split(" ");
            this.commandType = FromClient.valueOf(cmd[0]);
            if (cmd.length > 1)
                this.commandArgs = cmd[1];
        }

        private void handleCommand() {
            switch (this.commandType) {
                case START:
                    controller.startGame();
                    sendGameState();
                    break;
                case GUESS:
                    if (controller.gameIsOngoing()) {
                        controller.processGuess(this.commandArgs);
                        sendGameState();
                    } else {
                        toClient.println(FromServer.NOT_INITIALIZED);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
