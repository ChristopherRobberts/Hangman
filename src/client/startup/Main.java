package client.startup;


import client.view.CommandLineHandler;

public class Main {

    public static void main(String[] args) {
        CommandLineHandler commandLineHandler = new CommandLineHandler();
        commandLineHandler.start();
    }
}
