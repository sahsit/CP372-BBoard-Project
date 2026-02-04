
import java.net.*;
import java.util.*;

public class Server {
    
    // main method to start the server
    public static void main(String[] args) {
        try {
            // use the parseArgs function to parse the arguments on startup
            // this variable is of type "Config", defined in Config.java and is made to hold all the configuration parameters
            Config configuration = parseArgs(args);

            // create the server socket to listen for incoming connections using the port given
            try (ServerSocket serverSocket = new ServerSocket(configuration.port)) {
                System.out.println("Bulletin board server listening on port" + configuration.port);
                
                // infinite loop to accept incoming client connections
                while (true) {
                    // when a new client comes, accept them... 
                    Socket client = serverSocket.accept();
                    System.out.println("New client connected from " + client.getRemoteSocketAddress());
                    // ... and create a new thread to handle the client
                    new Thread(new ClientHandler(client, configuration)).start();
                }

            }

        // catch any exceptions during the startup phase and print the error message
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // method to configure the board using the arguments given in the terminal
    private static Config parseArgs(String[] args) {
        // checks if the number of arguments are valid
        if (args.length < 6) {
            System.err.println("Usage: java Server <port> <boardW> <boardH> <noteW> <noteH> <color1> [<color2> ... colorN]");
            System.exit(1);
        }

        // parsing the integer arguments
        int port = parseInt(args[0], "port"); // using a custom method to check if the argument is a valid positive integer
        int boardW = parseInt(args[1], "board width");
        int boardH = parseInt(args[2], "board height");
        int noteW = parseInt(args[3], "note width");
        int noteH = parseInt(args[4], "note height");
        
        // parsing the color (string) arguments into a list
        List<String> colors = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            String c = args[i].trim();
            if (c.isEmpty() != true) {
                colors.add(c);
            }
        }
        
        // if no colors were given, print error
        if (colors.isEmpty() == true) {
            System.err.println("Error: At least one color must be specified.");
            System.exit(1);
        }

    
    // return object with all the configuration parameters
    return new Config(port, boardW, boardH, noteW, noteH, colors);
}

    // Helper method to parse and validate positive integers
    private static int parseInt(String s, String name) {
        try {
            // converts string to integer
            int v = Integer.parseInt(s);
            // checks if the integer is positive
            if (v <= 0) {
                throw new NumberFormatException("Error" + name + " must be a positive integer.");
            }
            return v;
        } catch (Exception e) {
            System.err.println("Error" + name + " must be a positive integer. Got: " + s);
            System.exit(1);
            return -1; // Unreachable but required by compiler
        }

    }






}


