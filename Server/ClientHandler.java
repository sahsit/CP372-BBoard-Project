import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Class to handle client connections
// one instance of this class is created for every client that connects to the server, and it runs in its own thread to handle communication with that client
public class ClientHandler implements Runnable {
    // a set to keep track of all connected clients, so that we can broadcast messages to all clients when the board state changes (e.g. when a new note is posted)
    private static final Set<ClientHandler> CLIENTS = ConcurrentHashMap.newKeySet();
    private final Socket socket; // client socket
    private final Config config; // server configuration
    private final Board board;
    private BufferedWriter out; // output stream to client
    private final Object outLock = new Object(); // if two threads try talking to the client at once, outLock ensures one finishes before the other starts

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
            CLIENTS.add(this); // register the user in the CLIENTS set 

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

                // switch statement to handle different commands, with a try-catch block to catch any exceptions thrown by the Board methods
                try {
                    switch (cmd) {
                        case "POST": {
                            // all commands are handled by a helper method
                            String event = handlePost(parts);
                            broadcastLine(event);
                            break;
                        }
                        case "GET":
                            handleGet(cmd, parts, out);
                            break;

                        case "PIN":
                            String event = handlePin(parts);
                            broadcastLine(event);
                            break;

                        case "UNPIN":
                            String event1 = handleUnpin(parts);
                            broadcastLine(event1);
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

                        case "SYNC":
                            sendSync(out);
                            break;

                        default:
                            sendLine("ERR UNKNOWN_COMMAND\n");
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

    private String handlePost(String[] parts) throws Exception {
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        String color = parts[3];
    
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 4; i < parts.length; i++) {
            if (i > 4) messageBuilder.append(" ");
            messageBuilder.append(parts[i]);
        }
    
        String message = messageBuilder.toString();
    
        board.post(x, y, color, message);
    
        sendLine("OK NOTE_POSTED");
    
        return ("EVENT POST " + x + " " + y + " " + color + " " + message);
    }

    private String handlePin(String[] parts) throws Exception {
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
    
        board.pin(x, y);
    
        sendLine("OK PIN_ADDED");
    
        return ("EVENT PIN " + x + " " + y);
    }

    private String handleUnpin(String[] parts) throws Exception {
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
    
        board.unpin(x, y);
    
        sendLine("OK UNPINNED");
    
        return ("EVENT UNPIN " + x + " " + y);
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

    private void sendSync(BufferedWriter out) throws IOException {
        // Get full state
        List<Note> notes = board.getNotes(null, null, null, null); // all notes
        List<Board.Point> pins = board.getPins();

        // Header tells client counts
        out.write("SNAPSHOT " + notes.size() + " " + pins.size() + "\n");

        // Notes
        for (Note n : notes) {
            // Keep it parseable: last field is message (could contain spaces)
            // Format: NOTE x y color pinned message...
            out.write("NOTE " + n.x + " " + n.y + " " + n.color + " " + n.isPinned + " " + n.message + "\n");
        }

        // Pins
        for (Board.Point p : pins) {
            out.write("PIN " + p.x + " " + p.y + "\n");
        }

        out.flush();
    }

    
}