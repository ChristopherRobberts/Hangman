package server.net;

import Common.FromClient;
import Common.FromServer;
import server.controller.Controller;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class ClientHandler implements Runnable {
    private static final String MESSAGE_DELIMITER = ":";
    private GameServer gameServer;
    private Socket clientSocket;
    private BufferedReader fromClient;
    private PrintWriter toClient;
    private boolean connected = false;
    private Controller controller = new Controller();

    ClientHandler(GameServer gameServer, Socket clientSocket) {
        this.gameServer = gameServer;
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
                connected = false;
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
        builder.append(MESSAGE_DELIMITER);
        builder.append(message);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(calculateByteSize(builder.toString()));
        stringBuilder.append(MESSAGE_DELIMITER);
        stringBuilder.append(builder);
        toClient.println(stringBuilder.toString());
    }

    private int calculateByteSize(String s) {
        byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
        return bytes.length;
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
                        int messageSize = calculateByteSize(FromServer.NOT_INITIALIZED.toString());
                        toClient.println(messageSize + MESSAGE_DELIMITER + FromServer.NOT_INITIALIZED);
                    }
                    break;
                case DISCONNECT:
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connected = false;
                    break;
                default:
                    break;
            }
        }
    }
}
