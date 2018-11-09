package client.view;

import Common.FromClient;
import client.controller.Controller;

import java.util.Scanner;

public class CommandLineHandler implements Runnable {
    private Controller controller;
    private CommandLine outPutHandler = new CommandLine();
    private Scanner scanner = new Scanner(System.in);
    private boolean waitingForInput = false;
    private static int port = 8080;
    private static String host = "localhost";

    public void start() {
        if (waitingForInput) {
            return;
        }

        waitingForInput = true;
        controller = new Controller(outPutHandler);
        Thread cmdHandler = new Thread(this);
        cmdHandler.start();
    }

    public void run() {

        while (waitingForInput) {
            String command = scanner.nextLine();
            CommandParser commandParser = new CommandParser(command);
            switch (commandParser.commandType) {
                case START:
                    controller.startGame();
                    break;
                case GUESS:
                    controller.guess(commandParser.commandArgs);
                    break;
                case CONNECT:
                    controller.connect(host, port, manual());
                    break;
                case DISCONNECT:
                    controller.disconnect();
                    waitingForInput = false;
                    break;
                case UNKNOWN:
                    outPutHandler.handleMessage("wrong or too many commands\n" + manual());
                    break;
                default:
                    break;
            }
        }
    }

    private String manual() {
        return  "Valid commands: \n"
                + FromClient.START + " - starts a game\n"
                + FromClient.GUESS + " *LETTER*/*WORD* to guess a letter or a word\n"
                + FromClient.DISCONNECT + " - disconnects from the game server";
    }

    private class CommandParser{
        private FromClient commandType;
        private String commandArgs;

        private CommandParser(String command) {
            String[] entireCommand = command.split(" ");
            if (entireCommand.length > 2) {
                this.commandType = FromClient.UNKNOWN;
                return;
            }

            try {
                this.commandType = FromClient.valueOf(entireCommand[0].toUpperCase());
                if (entireCommand.length > 1) {
                    this.commandArgs = entireCommand[1];
                }
            } catch (IllegalArgumentException e) {
                this.commandType = FromClient.UNKNOWN;
            }
        }
    }
}
