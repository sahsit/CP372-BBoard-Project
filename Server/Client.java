
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class Client {
    
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private int boardW, boardH;
    private int noteW, noteH;
    private List<String> colours = new ArrayList<>();

    public Client(String host, int port) throws IOException {
    this.socket = new Socket(host, port);
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
}


    public void readHandshake() throws IOException {
        // first line is OK status response followed by 3 indicating 3 lines of info to follow
        // then send board dimensions, note dimensions, and color list
        // OK 3
        // BOARD width height
        // NOTE width height
        // COLOURS number c1 c2 c3

        String status = in.readLine();

        if (status == null) throw new IOException("Server closed connection during handshake.");

        String[] parts = status.trim().split("\\s+"); //Trims and removes whitespaces so parts is just [OK], [3]
        if (!parts[0].equalsIgnoreCase("OK")) { //Checks for first element to be "OK"
            throw new IOException("Invalid handshake status: " + status);
        }

        int linesToRead;
        try {
            linesToRead = Integer.parseInt(parts[1]); //Sets 2nd element to number of lines to read
        } catch (NumberFormatException e) {
            throw new IOException("Invalid handshake line count: " + status);
        }

        for (int i = 0; i < linesToRead; i++) { //Loop through number of lines given
            String line = in.readLine();
            if (line == null) throw new IOException("Server closed connection during handshake.");
            parseHandshakeLine(line); //Helper to parse lines
        }

        // Output for testing
        System.out.println("Handshake complete:");
        System.out.println("  BOARD " + boardW + " " + boardH);
        System.out.println("  NOTE  " + noteW + " " + noteH);
        System.out.println("  COLOURS " + colours.size() + " " + String.join(" ", colours));

    }
    

    private void parseHandshakeLine(String line) throws IOException {
        String[] input = line.trim().split("\\s+"); //Trim line into elements, remove whitespaces, etc.
        if (input.length == 0) return;

        switch (input[0]) {

            case "BOARD":
                if (input.length != 3) throw new IOException("Bad BOARD line: " + line);
                boardW = Integer.parseInt(input[1]);
                boardH = Integer.parseInt(input[2]);
                break;

            case "NOTE":
                if (input.length != 3) throw new IOException("Bad NOTE line: " + line);
                noteW = Integer.parseInt(input[1]);
                noteH = Integer.parseInt(input[2]);
                break;

            case "COLORS":
                if (input.length < 2) throw new IOException("Bad COLORS line: " + line); //Change to min colour inputs
                int num = Integer.parseInt(input[1]);

                colours.clear(); //For old colours list
                
                for (int i = 0; i < num; i++) {
                    colours.add(input[2 + i]); //Skip to 3rd element in input
                }

                break;

        }
    }

    
    public String sendCommand(String command) throws IOException {
        out.write(command);
        out.write("\n");
        out.flush();

        String response = in.readLine();
        if (response == null) throw new IOException("Server closed connection.");
        return response;
    }

    //Close helper
    public void close() {
        try { socket.close(); } catch (IOException ignored) {}
    }

    
    public static void main(String[] args) {

        //java Client localhost 12345
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Client client = new Client(host, port);
            client.readHandshake();

            System.out.println("\nType commands (DISCONNECT to quit):");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("\n");
                String line = in.readLine();

                String response = client.sendCommand(line);
                System.out.println(response);

                if (line.trim().equalsIgnoreCase("DISCONNECT")) {
                    break;
                }
            }

            client.close();

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
