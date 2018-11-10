package server.net;

import Common.FromClient;
import Common.FromServer;
import server.controller.Controller;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class ClientHandler implements Runnable {
    private static final char DELIMITER = ':';
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
            ClientMessageParser command = new ClientMessageParser();
            command.handleCommand();
        }
    }

    private void sendGameState() {
        sendMessage(controller.getWordState(), FromServer.WORD);
        if (controller.gameIsWon()) {
            sendMessage("", FromServer.NO_VALUE);
        } else {
            sendMessage(controller.getAttemptsState(), FromServer.ATTEMPTS);
        }
        sendMessage(controller.getScoreState(), FromServer.SCORE);
    }

    private void sendMessage(String message, FromServer type) {
        String msg = type + String.valueOf(DELIMITER) + message;
        int header = calculateByteSize(msg);
        String toSend = header + String.valueOf(DELIMITER) + msg;
        toClient.println(toSend);
    }

    private int calculateByteSize(String s) {
        byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
        return bytes.length;
    }

    private class ClientMessageParser {
        private FromClient messageType;
        private String messageContent;
        private int msgByteSize;

        private ClientMessageParser() {
            StringBuilder messageSize = new StringBuilder();
            char tmp;
            try {
                while ((tmp = (char) fromClient.read()) != ':') {
                    messageSize.append(tmp);
                }

                String msgSize = messageSize.toString().replaceAll("\n", "");
                this.msgByteSize = Integer.parseInt(msgSize);
                char[] msg = new char[this.msgByteSize];

                for (int i = 0; i < this.msgByteSize; i++) {
                    msg[i] = (char) fromClient.read();
                }

                String entireMessage = new String(msg);
                String[] parts = entireMessage.split(String.valueOf(DELIMITER));
                this.messageType = FromClient.valueOf(parts[0]);
                this.messageContent = (parts.length > 1) ? parts[1] : "";

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleCommand() {
            switch (this.messageType) {
                case START:
                    controller.startGame();
                    sendGameState();
                    break;
                case GUESS:
                    if (controller.gameIsOngoing()) {
                        controller.processGuess(this.messageContent);
                        sendGameState();
                    } else {
                        int messageSize = calculateByteSize(FromServer.NOT_INITIALIZED.toString());
                        toClient.println(messageSize + String.valueOf(DELIMITER)
                                + String.valueOf(FromServer.NOT_INITIALIZED));
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
