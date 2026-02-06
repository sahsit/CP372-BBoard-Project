import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Class to handle client connections
public class ClientHandler implements Runnable {
    private static final Set<ClientHandler> CLIENTS = ConcurrentHashMap.newKeySet();
    private final Socket socket; // client socket
    private final Config config; // server configuration
    private final Board board;
    private BufferedWriter out; // output stream to client
    private final Object outLock = new Object(); // lock for synchronizing writes to client

    ClientHandler(Socket socket, Config config, Board board) {
        this.socket = socket;
        this.config = config;
        this.board = board;
    }

    @Override
    // Method to run the client handler thread
    public void run() {
        try (
            // create input and output streams for the client socket
            Socket s = socket;
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ) {
            
            this.out = out;
            CLIENTS.add(this);

            // start handshake process with client
            sendHandshake(out, config);

            String line;
            // while loop to read client commands from input stream
            while ((line = in.readLine()) != null) {
                // after receiving a message, print the socket address and and message to the server console
                System.out.println("Received from " + s.getRemoteSocketAddress() + ": " + line);
                
                
                // split the line by whitespace (which is what \\s+ means)
                String[] parts = line.trim().split("\\s+");

                // the "command" part of the instruction is always the first part (index 0)
                String cmd = parts[0].toUpperCase();

                try {
                    switch (cmd) {
                        case "POST":
                            handlePost(parts, out);
                            broadcastLine("EVENT POST 10 20 blue hello world"); // hardcoded OK
                            break;

                        case "GET":
                            handleGet(cmd, parts, out);
                            break;

                        case "PIN":
                            handlePin(parts, out);
                            broadcastLine("EVENT PIN 70 70"); // hardcoded OK
                            break;

                        case "UNPIN":
                            handleUnpin(parts, out);
                            broadcastLine("EVENT UNPIN 70 70"); // hardcoded OK
                            break;

                        case "CLEAR":
                            board.clear();
                            out.write("OK CLEARED\n");
                            broadcastLine("EVENT BOARD_CLEARED"); // hardcoded OK
                            break;

                        case "SHAKE":
                            board.shake();
                            out.write("OK SHAKE_COMPLETE\n");
                            broadcastLine("EVENT BOARD_SHAKEN"); // hardcoded OK
                            break;

                        case "DISCONNECT":
                            return;

                        default:
                            out.write("ERR UNKNOWN_COMMAND\n");
                            break;
                    }
                    out.flush();
                } catch (Exception e) {
                    out.write("ERR " + e.getMessage() + "\n");
                    out.flush();
                }
            }

        // catch any IO exceptions during client handling
        } catch (IOException e) {
            System.err.println("Client handler error: " + socket.getRemoteSocketAddress());
        // finally block to indicate client disconnection
        } finally {
            CLIENTS.remove(this);
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        }
    }

    // Method to send handshake (startup server) information to the client
    private void sendHandshake(BufferedWriter out, Config config) throws IOException {
        // first line is OK status response followed by 3 indicating 3 lines of info to follow
        out.write("OK 3\n");
        // then send board dimensions, note dimensions, and color list
        out.write("BOARD " + config.boardW + " " + config.boardH + "\n");
        out.write("NOTE " + config.noteW + " " + config.noteH + "\n");
        out.write("COLORS " + config.colors.size() + " " + String.join(" ", config.colors) + "\n");
        out.flush();
    }

    // if the command is "GET"...
    private void handleGet(String cmd, String[] parts, BufferedWriter out) throws IOException {
        
        if (cmd.equals("GET") == true) {
            // ... check if it's a GET PINS command
            if (parts.length > 1 && parts[1].equalsIgnoreCase("PINS")) {
                handleGetPins(cmd, parts, out);
            } else {
                // ... or just a filtered GET command
                handleFilteredGet(parts, out);
            }


        }
        //return null;
        // add real return
    }

    // if the command is the filtered "GET"...
    private void handleFilteredGet(String[] parts, BufferedWriter out) throws IOException {

        System.out.println("handleFilteredGet");

        //assuming the GUI sends: GET colour=<color> contains=<x> <y> refersTo=<message>
        // parts[0] = GET
        // parts[1] = colour=blue
        // parts[2] = contains=50
        // parts[3] = 50
        // parts[4] = refersTo=refridgerator

        // if the user leaves any of the GET fields blank (i.e. they dont want to filter by color), then the GUI should send "null" for that field 

        // color filter
        String rawColor = parts[1].split("=")[1];
        String filterColor;
        if (rawColor.equalsIgnoreCase("null")) {
            filterColor = null;
        } else {
            filterColor = rawColor;
        }

        //coordinates filter
        Integer filterX = null;
        Integer filterY = null;
        if (parts[2].endsWith("null") != true) {
            // Only parse if it's a real number
            filterX = Integer.parseInt(parts[2].split("=")[1]); 
            filterY = Integer.parseInt(parts[3]);
        }

        // message filter
        String rawSearch = parts[4].split("=")[1];
        String filterSearch;
        if (rawSearch.equalsIgnoreCase("null")) {
            filterSearch = null;
        } else {
            filterSearch = rawSearch;
        }

        List<Note> results = board.getNotes(filterColor, filterX, filterY, filterSearch);

        out.write("OK " + results.size() + "\n");
        for (Note n : results) {
            out.write("NOTE " + n.x + " " + n.y + " " + n.color + " " + n.message + " PINNED=" + n.isPinned + "\n");
        }

        //need to add return statement
        //return null;
    }
    

    // if the command is "GET"...
    private void handleGetPins(String cmd, String[] parts, BufferedWriter out) throws IOException {
        List<Board.Point> pinList = board.getPins();
        out.write("OK " + pinList.size() + "\n");
        for (Board.Point p : pinList) {
            out.write("PIN " + p.x + " " + p.y + "\n");
        }

        out.flush();
    
    }

    private void handlePost(String[] parts, BufferedWriter out) throws IOException {
        try {
        // parts[0] is "POST"
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        String color = parts[3];

        // Everything from parts[4] to the end is the message
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 4; i < parts.length; i++) {
            messageBuilder.append(parts[i]);
            // Add a space back between words, but not after the last word
            if (i < parts.length - 1) {
                messageBuilder.append(" ");
            }
        }

        String message = messageBuilder.toString();
        
        board.post(x, y, color, message);
        out.write("OK NOTE_POSTED\n");
        } catch (NumberFormatException e) {
            // If x or y weren't numbers
            out.write("ERROR Invalid coordinates. Usage: POST x y color message\n");
        } catch (ArrayIndexOutOfBoundsException e) {
            // If the user forgot a parameter (e.g., just sent "POST 10 10")
            out.write("ERROR Missing parameters. Usage: POST x y color message\n");
        } catch (Exception e) {
            // Any other unexpected error
            out.write("ERROR " + e.getMessage() + "\n");
        }
        out.flush();
    }

    private void handlePin(String[] parts, BufferedWriter out) throws IOException {
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);

            board.pin(x, y);
            out.write("OK PIN_ADDED\n");
        } catch (NumberFormatException e) {
            out.write("ERROR Invalid coordinates. Usage: PIN x y\n");
        } catch (ArrayIndexOutOfBoundsException e) {
            out.write("ERROR Missing parameters. Usage: PIN x y\n");
        } catch (Exception e) {
            out.write("ERROR " + e.getMessage() + "\n");
        }
        out.flush();
    }

    private void handleUnpin(String[] parts, BufferedWriter out) throws IOException {
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);

            board.unpin(x, y);
            out.write("OK\n");
        } catch (NumberFormatException e) {
            out.write("ERROR Invalid coordinates. Usage: UNPIN x y\n");
        } catch (ArrayIndexOutOfBoundsException e) {
            out.write("ERROR Missing parameters. Usage: UNPIN x y\n");
        } catch (Exception e) {
            out.write("ERROR " + e.getMessage() + "\n");
        }
        out.flush();
    }


    private void sendLine(String line) throws IOException {
        synchronized (outLock) {
            out.write(line);
            out.write("\n");
            out.flush();
        }
    }

    private static void broadcastLine(String line) {
        for (ClientHandler ch : CLIENTS) {
            try {
                if (ch.out != null) {
                    ch.sendLine(line);
                }
            } catch (IOException e) {
                CLIENTS.remove(ch);
            }
        }
    }
    
}