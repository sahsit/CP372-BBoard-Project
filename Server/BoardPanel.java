import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {
    public static class NoteView {
        public final int x, y;
        public final String colour;
        public final String message;
        public boolean pinned;

        //Client side of server's note (just a view)
        public NoteView(int x, int y, String colour, String message, boolean pinned) {
            this.x = x; this.y = y;
            this.colour = colour;
            this.message = message;
            this.pinned = pinned;
        }
    }

    public static class pinsView {
        public final int x, y;

        public pinsView(int x, int y){
            this.x = x;
            this.y = y;
        }    
        
    }

    private final int noteW, noteH;
    private final List<NoteView> notes = new ArrayList<>();
    private final List<pinsView> pins = new ArrayList<>();

    public BoardPanel(int boardW, int boardH, int noteW, int noteH) {
        this.noteW = noteW;
        this.noteH = noteH;
        setPreferredSize(new Dimension(boardW, boardH));
        setBackground(Color.LIGHT_GRAY);
    }

    
    public void postNote(NoteView j) {
        notes.add(j);
    }

    public void postPin(pinsView j){
        pins.add(j);
    }

    public void clearAll() {
        notes.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        //Clears old and resets background
        super.paintComponent(g);

        //Loop through each note and draw each
        for (NoteView n : notes) {

            Color fill = parseColour(n.colour);
            g.setColor(fill);
            g.fillRect(n.x, n.y, noteW, noteH);

            g.setColor(Color.BLACK);
            g.drawRect(n.x, n.y, noteW, noteH);

            //Write message
            String text = n.message == null ? "" : n.message;
            g.drawString(text.length() > 18 ? text.substring(0, 18) + "…" : text, n.x + 4, n.y + 16);
        }

        g.setColor(Color.RED);
        int r = 4; // radius

        for (pinsView p : pins) {
            g.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
        }
    }

    //Helper to get colour, used for note posting
    private Color parseColour(String c) {
    
        switch (c.toLowerCase()) {

            case "red": return Color.RED;
            case "blue": return Color.BLUE;
            case "green": return Color.GREEN;
            case "yellow": return Color.YELLOW;
            case "black": return Color.DARK_GRAY;
            case "white": return Color.WHITE;
            //Default is black, this list needs to include possible colours from server, if not true then black will be added
            default: return Color.BLACK;
        }
    }

    //Helpers for adding and removing pins
    public void addPin(pinsView p) {
        
        for (pinsView existing : pins) {
            if (existing.x == p.x && existing.y == p.y) {
                return;
            }
        }
        pins.add(p);
    }

    public void removePin(int x, int y) {
        pins.removeIf(p -> p.x == x && p.y == y);
    }

}
