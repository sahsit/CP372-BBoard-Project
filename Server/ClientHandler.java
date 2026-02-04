import java.io.*;
import java.net.*;

// Class to handle client connections
public class ClientHandler implements Runnable {
    private final Socket socket; // client socket
    private final Config config; // server configuration

    ClientHandler(Socket socket, Config config) {
        this.socket = socket;
        this.config = config;
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

            // start handshake process with client
            sendHandshake(out, config);

            String line;
            // while loop to read client commands from input stream
            while ((line = in.readLine()) != null) {
                // after receiving a message, print the socket address and and message to the server console
                System.out.println("Received from " + s.getRemoteSocketAddress() + ": " + line);
                
                // PROCESS CLIENT COMMANDS BELOW - UNFINISHED, ONLY KNOWS DISCONNECT 

                // if client sends DISCONNECT command, respond and break the loop to end connection
                if (line.trim().equalsIgnoreCase("DISCONNECT")) {
                    out.write("OK 0\n");
                    out.flush();
                    break;
                } else {
                    // temporary generic fallback response - more robust error catching later
                    out.write("OK 0\n");
                    out.flush();
                }

            }

        // catch any IO exceptions during client handling
        } catch (IOException e) {
            System.err.println("Client handler error: " + socket.getRemoteSocketAddress());
        // finally block to indicate client disconnection
        } finally {
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


}