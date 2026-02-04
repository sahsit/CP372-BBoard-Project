import java.util.*;

// Class to hold server configuration parameters
public class Config {
    final int port;
    final int boardW, boardH;
    final int noteW, noteH;
    final List<String> colors;

    public Config(int port, int boardW, int boardH, int noteW, int noteH, List<String> colors) {
        this.port = port;
        this.boardW = boardW;
        this.boardH = boardH;
        this.noteW = noteW;
        this.noteH = noteH;
        this.colors = List.copyOf(colors);
        // List.copyOf makes sure that the list cannot be changed after it's initialized
        // You can't add more colors once Config is created 
    }
}
