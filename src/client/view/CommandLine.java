package client.view;

import client.net.OutputHandler;

public class CommandLine implements OutputHandler {

    synchronized void println(String command) {
        System.out.println(command);
    }

    public void handleMessage(String message) {
        println(message);
    }
}
