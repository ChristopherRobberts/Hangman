package client.net;

import Common.FromClient;
import Common.FromServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerConnection {
    private BufferedReader fromServer;
    private PrintWriter toServer;

    public void connect(String host, int port, OutputHandler outputHandler) {
        try {
            boolean autoFlush = true;
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));
            toServer = new PrintWriter(socket.getOutputStream(), autoFlush);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Listener listener = new Listener(outputHandler);
            Thread listeningThread = new Thread(listener);
            listeningThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        String msg = String.valueOf(FromClient.START);
        toServer.println(msg);
    }

    public void guess(String guess) {
        String msg = String.valueOf(FromClient.GUESS) + " " + guess;
        toServer.println(msg);
    }

    private class Listener implements Runnable {
        private OutputHandler outputHandler;

        private Listener(OutputHandler outputHandler) {
            this.outputHandler = outputHandler;
        }

        public void run() {
            try {
                while (true) {
                    String message = fromServer.readLine();
                    ServerMessageParser serverMessageParser = new ServerMessageParser(message);
                    serverMessageParser.handleMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private class ServerMessageParser {
            private FromServer label;
            private String content;

            private ServerMessageParser(String entireCommand) {
                String[] messageParts = entireCommand.split(":");

                this.content = (messageParts.length < 2) ? "" : messageParts[1];
                this.label = FromServer.valueOf(messageParts[0]);
            }

            private void handleMessage() {
                switch(this.label) {
                    case WORD:
                        outputHandler.handleMessage("Word: " + this.content);
                        break;
                    case ATTEMPTS:
                        outputHandler.handleMessage("Remaining failed attempts: " + this.content);
                        break;
                    case SCORE:
                        outputHandler.handleMessage("Your current score: " + this.content);
                        break;
                    case NOT_INITIALIZED:
                        outputHandler.handleMessage("you must start the game before guessing dummy");
                        break;
                    default:
                        outputHandler.handleMessage("server says what?");
                        break;
                }
            }
        }
    }
}
