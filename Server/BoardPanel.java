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

        public NoteView(int x, int y, String colour, String message, boolean pinned) {
            this.x = x; this.y = y;
            this.colour = colour;
            this.message = message;
            this.pinned = pinned;
        }
    }

    private final int noteW, noteH;
    private final List<NoteView> notes = new ArrayList<>();

    public BoardPanel(int boardW, int boardH, int noteW, int noteH) {
        this.noteW = noteW;
        this.noteH = noteH;
        setPreferredSize(new Dimension(boardW, boardH));
        setBackground(Color.LIGHT_GRAY);
    }

    // Called on EDT
    public void upsertNote(NoteView nv) {
        // If your rules guarantee complete overlap = same x,y, we can key by x,y
        for (int i = 0; i < notes.size(); i++) {
            NoteView existing = notes.get(i);
            if (existing.x == nv.x && existing.y == nv.y) {
                notes.set(i, nv);
                return;
            }
        }
        notes.add(nv);
    }

    public void clearAll() {
        notes.clear();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw each note
        for (NoteView n : notes) {
            Color fill = parseColour(n.colour);
            g.setColor(fill);
            g.fillRect(n.x, n.y, noteW, noteH);

            g.setColor(Color.BLACK);
            g.drawRect(n.x, n.y, noteW, noteH);

            // message (simple)
            String text = n.message == null ? "" : n.message;
            g.drawString(text.length() > 18 ? text.substring(0, 18) + "…" : text,
                    n.x + 4, n.y + 16);

            // pinned indicator
            if (n.pinned) {
                g.drawString("📌", n.x + noteW - 18, n.y + 16);
            }
        }
    }

    private Color parseColour(String c) {
        if (c == null) return Color.WHITE;
        switch (c.toLowerCase()) {
            case "red": return Color.RED;
            case "blue": return Color.BLUE;
            case "green": return Color.GREEN;
            case "yellow": return Color.YELLOW;
            case "black": return Color.DARK_GRAY;
            case "white": return Color.WHITE;
            default: return Color.PINK; // unknown colour fallback
        }
    }
}
