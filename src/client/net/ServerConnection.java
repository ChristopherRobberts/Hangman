package client.net;

import common.FromClient;
import common.FromServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class ServerConnection {
    private static final char DELIMITER = ':';
    private BufferedReader fromServer;
    private PrintWriter toServer;
    private boolean listening;

    public void connect(String host, int port, OutputHandler outputHandler) {
        try {
            boolean autoFlush = true;
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));
            toServer = new PrintWriter(socket.getOutputStream(), autoFlush);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            listening = true;
            Listener listener = new Listener(outputHandler);
            Thread listeningThread = new Thread(listener);
            listeningThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        String msg = String.valueOf(FromClient.START);
        int header = messageByteSize(msg);
        toServer.println(header + String.valueOf(DELIMITER) + msg);
    }

    public void guess(String guess) {
        String msg = String.valueOf(FromClient.GUESS) + DELIMITER + guess;
        int header = messageByteSize(msg);
        toServer.println(header + String.valueOf(DELIMITER) + msg);
    }

    public void disconnect() {
        this.listening = false;
        String msg = String.valueOf(FromClient.DISCONNECT);
        int header = messageByteSize(msg);
        toServer.println(header + String.valueOf(DELIMITER) + msg);
    }

    private int messageByteSize(String msg) {
        byte[] bytes = msg.getBytes(Charset.forName("UTF-8"));
        return bytes.length;
    }

    private class Listener implements Runnable {
        private OutputHandler outputHandler;
        private ServerMessageParser serverMessageParser;

        private Listener(OutputHandler outputHandler) {
            this.outputHandler = outputHandler;
        }

        public void run() {

            while (listening) {
                this.serverMessageParser = new ServerMessageParser();
                serverMessageParser.handleMessage();
            }
        }

        private class ServerMessageParser {
            private FromServer label;
            private String content;
            private int msgByteSize;

            private ServerMessageParser() {
                StringBuilder messageSize = new StringBuilder();
                char tmp;
                try {
                    while ((tmp = (char) fromServer.read()) != DELIMITER) {
                        messageSize.append(tmp);
                    }

                    String msgSize = messageSize.toString().replaceAll("\n", "");
                    this.msgByteSize = Integer.parseInt(msgSize);
                    char[] msg = new char[this.msgByteSize];

                    for (int i = 0; i < this.msgByteSize; i++) {
                        msg[i] = (char) fromServer.read();
                    }

                    String entireMessage = new String(msg);
                    String[] parts = entireMessage.split(Character.toString(DELIMITER));
                    this.label = FromServer.valueOf(parts[0]);
                    this.content = (parts.length > 1) ? parts[1] : "";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void handleMessage() {
                switch (this.label) {
                    case WORD:
                        outputHandler.handleMessage("Word: " + this.content);
                        break;
                    case ATTEMPTS:
                        outputHandler.handleMessage("Remaining failed attempts: " + this.content);
                        break;
                    case SCORE:
                        outputHandler.handleMessage("Your current score: " + this.content);
                        break;
                    case NO_VALUE:
                        outputHandler.handleMessage("Remaining failed attempts: no value");
                        break;
                    case NOT_INITIALIZED:
                        outputHandler.handleMessage("you must start the game before guessing!");
                        break;
                    default:
                        outputHandler.handleMessage("server says what?");
                        break;
                }
            }
        }
    }
}
