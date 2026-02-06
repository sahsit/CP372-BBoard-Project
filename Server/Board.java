import java.util.*;

// Class to represent the bulletin board
public class Board {
    // Board needs configuration parameters
    private final Config cfg;
    // List to hold notes
    private final List<Note> notes = new ArrayList<>();
    // Hashmap to hold pin coordinates and counts 
    // "Point" will be a custom class that holds a pin's x, y coordinates 
    private final Set<Point> pins = new HashSet<>();

    public Board(Config cfg) {
        this.cfg = cfg;
    }

    // Class to represent a pin on the board
    public static final class Point {
        public final int x, y;
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        // All Java classes inherit from Object class, which has a default equals method, so we need to override it
        // equals method checks if two Point objects' coordinates (pin coordinates) are equal
        // (we use this when we get an UNPIN command: the user's inputted coordinates are turned into a Point object, then we compare the user's coordinates with the real stored pin to check if a pin exists at that location)
        @Override public boolean equals(Object o) {
            // is the given Object a Point instance? If not, return false
            if ((o instanceof Point) != true) 
                return false;
            // cast the Object to a Point to compare coordinates
            Point p = (Point) o;
            // Check if the coordinates of the new user-inputted pin and the coordinates of the current object
            return x == p.x && y == p.y;
        }

        // again, all Java classes inherit from Object class, which has a default hashCode method, so we need to override it
        // this method gets the hash value for a specific coordinate, so it can be found in O(1) time
        @Override public int hashCode() {
            return Objects.hash(x, y);
        }
    }

        // custom exception class for board-related errors
        public static final class BoardException extends Exception {
            public final String code;
            public BoardException(String code, String message) {
                super(message);
                this.code = code;
            }
        }

        //--------- BOARD COMMANDS ---------//

        // CLEAR command clears all notes and pins from the board
        public synchronized void clear() {
            notes.clear();
            pins.clear();
        }

        public synchronized void shake() {
            notes.removeIf(n -> !isNotePinned(n));
        }

        public synchronized void post(int x, int y, String color, String message) throws BoardException {
            if (cfg.colors.contains(color) != true) {
                throw new BoardException("COLOUR_NOT_SUPPORTED", color + " is not a supported color");

            }

            if (noteFits(x, y) != true) {
                throw new BoardException("OUT_OF_BOUNDS ", "Coordinate (" + x + ", " + y + ") is out of bounds");
            }
  
            for (Note n : notes) {
                if (n.x == x && n.y == y) {
                    throw new BoardException("COMPLETE_OVERLAP ", "Coordinate (" + x + ", " + y + ") overlaps with existing note");
                }
            }

            notes.add(new Note(x, y, color, message));
            
        }

        public synchronized void pin(int x, int y) throws BoardException {
            if (inBoard(x, y) != true) {
                throw new BoardException("OUT_OF_BOUNDS ", "Coordinate (" + x + ", " + y + ") is out of bounds");
            }

            boolean hit = false;
            for (Note n : notes) {
                if (doesNoteContainPin(n, x, y) == true) {
                    hit = true;
                }
            }
            if (hit != true) {
                throw new BoardException("NO_NOTE_AT_COORDINATE ", "Coordinate (" + x + ", " + y + ") does not contain a note");
            }

            if (pins.contains(new Point(x, y)) == true) {
                throw new BoardException("PIN_ALREADY_EXISTS ", "Coordinate (" + x + ", " + y + ") already contains a pin");
            }

            pins.add(new Point(x, y));
        }

        public synchronized void unpin(int x, int y) throws BoardException {
            if (inBoard(x, y) != true) {
                throw new BoardException("OUT_OF_BOUNDS ", "Coordinate (" + x + ", " + y + ") is out of bounds");
            }

            Point p = new Point(x, y);
            if (pins.contains(p) != true) {
                throw new BoardException("PIN_NOT_FOUND ", "Coordinate (" + x + ", " + y + ") does not contain a pin");
            }
            
            pins.remove(p);
           
        }

        public synchronized List<Note> getNotes(String color, Integer x, Integer y, String message) {
            List<Note> results = new ArrayList<>();

            for (Note n : notes) {
                // check and compare the color
                if (color != null && n.color.equals(color) != true) {
                    continue;
                }

                // check if the user point is inside of a note
                if (x != null && y != null) {
                    if (doesNoteContainPin(n, x, y) != true) {
                        continue;
                    }
                }

                // search messages of remaining notes
                if (message != null && n.message.toLowerCase().contains(message.toLowerCase()) != true) {
                    continue;
                }

                Note resultNote = new Note(n.x, n.y, n.color, n.message);
                resultNote.isPinned = isNotePinned(n);

                results.add(resultNote);
                
            }
            return results;
        }

        public synchronized List<Point> getPins() {
            return new ArrayList<>(pins);
        }

        //--------- HELPER METHODS ---------//
        
        private boolean inBoard(int x, int y) {
            return x >= 0 && x < cfg.boardW && y >= 0 && y < cfg.boardH;
        }

        private boolean noteFits(int x, int y) {
            return x >= 0 && (x + cfg.noteW) <= cfg.boardW && y >= 0 && (y + cfg.noteH) <= cfg.boardH;
        }

        private boolean doesNoteContainPin(Note n, int x, int y) {
            return x >= n.x && x < (n.x + cfg.noteW) && y >= n.y && y < (n.y + cfg.noteH);
        }

        private boolean isNotePinned(Note n) {
            for (Point p : pins) {
                if (doesNoteContainPin(n, p.x, p.y) == true) {
                    return true;
                }
            }
            return false;
        }



}
