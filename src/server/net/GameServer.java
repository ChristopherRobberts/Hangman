package server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private static final int TIME_UNTIL_TIMEOUT = 600000;
    private static final int LINGER_TIME = 100000;
    private static final int application_portNr = 8080;

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.start();
    }

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(application_portNr);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                serveClient(clientSocket);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void serveClient(Socket socket) {

        ClientHandler clientHandler = new ClientHandler(this, socket);
        Thread dedicatedThread = new Thread(clientHandler);
        dedicatedThread.start();
    }
}
