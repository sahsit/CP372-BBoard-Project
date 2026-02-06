import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;


// cd server
// java GUI localhost 12345

public class GUI implements ActionListener{

    private Client client;
    private JFrame frame;

    int count = 0;
    JLabel label;
    
    private BoardPanel boardPanel;

    JButton postButton;
    JButton getButton;
    JButton getPinsButton;
    JButton pinButton;
    JButton unpinButton;
    JButton shakeButton;
    JButton clearButton;
    JButton disconnectButton;

    JTextField postX = new JTextField(5);
    JTextField postY = new JTextField(5);
    JTextField postColours = new JTextField(10);
    JTextField postMessage = new JTextField(15);

    JTextField getColour = new JTextField(10);
    JTextField getContainsX = new JTextField(5);
    JTextField getContainsY = new JTextField(5);
    JTextField getRefersTo = new JTextField(10);

    JTextField pinX = new JTextField(5);
    JTextField pinY = new JTextField(5);

    JTextField unpinX = new JTextField(5);
    JTextField unpinY = new JTextField(5);

    //For sync
    private int snapshotNotesRemaining = 0;
    private int snapshotPinsRemaining = 0;
    private boolean inSnapshot = false;

    //For GET
    private JTextArea outputArea;

    // Parser state for "OK <n> + N lines" replies
    private enum PendingReply { NONE, GET_NOTES, GET_PINS }
    private PendingReply pending = PendingReply.NONE;
    private int remainingLines = 0;

    private final java.util.List<BoardPanel.NoteView> pendingNotes = new java.util.ArrayList<>();
    private final java.util.List<BoardPanel.pinsView> pendingPins = new java.util.ArrayList<>();





    public GUI(String host, int port) {

        

        //Connect to the server (localhost 12345)
        try {
            client = new Client(host, port);
            client.readHandshake();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Could not connect to server: " + e.getMessage());
            System.exit(1);
        }


        frame = new JFrame("Title");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        //Board
        this.boardPanel = new BoardPanel(client.boardW, client.boardH, client.noteW, client.noteH);
        frame.add(boardPanel, BorderLayout.CENTER);

        


        //Button Panel
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(0, 1, 5, 5));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Buttons
        postButton = new JButton("POST");
        postButton.addActionListener(this);

        // POST panel
        JPanel postPanel = new JPanel(new GridBagLayout());
        postPanel.setBorder(BorderFactory.createTitledBorder("POST"));

        GridBagConstraints postBag = new GridBagConstraints();
        postBag.insets = new Insets(2, 2, 2, 2);
        postBag.anchor = GridBagConstraints.WEST;

        postBag.gridx = 0; postBag.gridy = 0;
        postPanel.add(new JLabel("X:"), postBag);
        postBag.gridx = 1;
        postPanel.add(postX, postBag);

        postBag.gridx = 0; postBag.gridy = 1;
        postPanel.add(new JLabel("Y:"), postBag);
        postBag.gridx = 1;
        postPanel.add(postY, postBag);

        postBag.gridx = 0; postBag.gridy = 2;
        postPanel.add(new JLabel("Colour:"), postBag);
        postBag.gridx = 1;
        postPanel.add(postColours, postBag);

        postBag.gridx = 0; postBag.gridy = 3;
        postPanel.add(new JLabel("Message:"), postBag);
        postBag.gridx = 1;
        postPanel.add(postMessage, postBag);

        postBag.gridx = 0; postBag.gridy = 4;
        postBag.gridwidth = 2;
        postBag.anchor = GridBagConstraints.CENTER;
        postBag.fill = GridBagConstraints.HORIZONTAL;
        postPanel.add(postButton, postBag);

        getButton = new JButton("GET");
        getButton.addActionListener(this);



        // GET panel
        JPanel getPanel = new JPanel(new GridBagLayout());
        getPanel.setBorder(BorderFactory.createTitledBorder("GET"));

        GridBagConstraints getBag = new GridBagConstraints();
        getBag.insets = new Insets(2, 2, 2, 2);
        getBag.anchor = GridBagConstraints.WEST;

        getBag.gridx = 0; getBag.gridy = 0;
        getPanel.add(new JLabel("Colour:"), getBag);
        getBag.gridx = 1;
        getPanel.add(getColour, getBag);

        getBag.gridx = 0; getBag.gridy = 1;
        getPanel.add(new JLabel("X:"), getBag);
        getBag.gridx = 1;
        getPanel.add(getContainsX, getBag);

        getBag.gridx = 0; getBag.gridy = 2;
        getPanel.add(new JLabel("Y:"), getBag);
        getBag.gridx = 1;
        getPanel.add(getContainsY, getBag);

        getBag.gridx = 0; getBag.gridy = 3;
        getPanel.add(new JLabel("Contains:"), getBag);
        getBag.gridx = 1;
        getPanel.add(getRefersTo, getBag);

        getBag.gridx = 0; getBag.gridy = 4;
        getBag.gridwidth = 2;
        getBag.anchor = GridBagConstraints.CENTER;
        getBag.fill = GridBagConstraints.HORIZONTAL;
        getPanel.add(getButton, getBag);




        getPinsButton = new JButton("GET PINS");
        getPinsButton.addActionListener(this);


        //Pins Panel
        pinButton = new JButton("PIN");
        pinButton.addActionListener(this);

        JPanel pinPanel = new JPanel(new GridBagLayout());
        pinPanel.setBorder(BorderFactory.createTitledBorder("PIN"));

        GridBagConstraints pinBag = new GridBagConstraints();
        pinBag.insets = new Insets(2, 2, 2, 2);
        pinBag.anchor = GridBagConstraints.WEST;

        pinBag.gridx = 0; pinBag.gridy = 0;
        pinPanel.add(new JLabel("X:"), pinBag);

        pinBag.gridx = 1;
        pinPanel.add(pinX, pinBag);

        pinBag.gridx = 0; pinBag.gridy = 1;
        pinPanel.add(new JLabel("Y:"), pinBag);

        pinBag.gridx = 1;
        pinPanel.add(pinY, pinBag);

        pinBag.gridx = 0; pinBag.gridy = 2;
        pinBag.gridwidth = 2;
        pinBag.anchor = GridBagConstraints.CENTER;
        pinBag.fill = GridBagConstraints.HORIZONTAL;
        pinPanel.add(pinButton, pinBag);


        unpinButton = new JButton("UNPIN");
        unpinButton.addActionListener(this);

        JPanel unpinPanel = new JPanel(new GridBagLayout());
        unpinPanel.setBorder(BorderFactory.createTitledBorder("UNPIN"));

        GridBagConstraints unpinBag = new GridBagConstraints();
        unpinBag.insets = new Insets(2, 2, 2, 2);
        unpinBag.anchor = GridBagConstraints.WEST;

        unpinBag.gridx = 0; unpinBag.gridy = 0;
        unpinPanel.add(new JLabel("X:"), unpinBag);

        unpinBag.gridx = 1;
        unpinPanel.add(unpinX, unpinBag);

        unpinBag.gridx = 0; unpinBag.gridy = 1;
        unpinPanel.add(new JLabel("Y:"), unpinBag);

        unpinBag.gridx = 1;
        unpinPanel.add(unpinY, unpinBag);

        unpinBag.gridx = 0; unpinBag.gridy = 2;
        unpinBag.gridwidth = 2;
        unpinBag.anchor = GridBagConstraints.CENTER;
        unpinBag.fill = GridBagConstraints.HORIZONTAL;
        unpinPanel.add(unpinButton, unpinBag);


        shakeButton = new JButton("SHAKE");
        shakeButton.addActionListener(this);

        clearButton = new JButton("CLEAR");
        clearButton.addActionListener(this);

        disconnectButton = new JButton("DISCONNECT");
        disconnectButton.addActionListener(this);


        // Create label ONCE
        label = new JLabel("Colours: " + String.join(", ", client.colours));

        // Output area (GET output)
        outputArea = new JTextArea(8, 40);
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);

        // Bottom-right panel: label + output
        JPanel bottomRight = new JPanel(new BorderLayout(5, 5));
        bottomRight.add(label, BorderLayout.NORTH);
        bottomRight.add(scroll, BorderLayout.CENTER);

        // Buttons list (right side)
        buttons.add(getPinsButton);
        buttons.add(shakeButton);
        buttons.add(clearButton);
        buttons.add(disconnectButton);

        // Stack the “forms” (POST/GET/PIN/UNPIN)
        JPanel topPanels = new JPanel();
        topPanels.setLayout(new BoxLayout(topPanels, BoxLayout.Y_AXIS));
        topPanels.add(postPanel);
        topPanels.add(Box.createVerticalStrut(10));
        topPanels.add(getPanel);
        topPanels.add(Box.createVerticalStrut(10));
        topPanels.add(pinPanel);
        topPanels.add(Box.createVerticalStrut(10));
        topPanels.add(unpinPanel);

        // Right panel container (create ONCE)
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rightPanel.add(topPanels, BorderLayout.NORTH);
        rightPanel.add(buttons, BorderLayout.CENTER);
        rightPanel.add(bottomRight, BorderLayout.SOUTH);  // label + output here

        frame.add(rightPanel, BorderLayout.EAST);



        frame.add(rightPanel, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        client.startListener(this::handleServerLine, ex -> {
            appendOutput("Disconnected: " + (ex == null ? "closed" : ex.getMessage()));
        });


        //Get updates from sync
        sendToServer("SYNC");

    }

    public static void main(String[] args){
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        new GUI(host, port);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == postButton) {
            //sendToServer("POST");
            //System.out.println("POST");

            try {
                int x = Integer.parseInt(postX.getText().trim());
                int y = Integer.parseInt(postY.getText().trim());
                String colour = postColours.getText().trim();
                String message = postMessage.getText().trim();

                sendToServer("POST " + x + " " + y + " " + colour + " " + message);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "X and Y must be integers.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }


        } else if (e.getSource() == getButton) {
            // Build GET command exactly like your server expects:
            // GET colour=<color|null> contains=<x|null> <y> refersTo=<msg|null>

            String color = getColour.getText().trim();
            if (color.isEmpty()) color = "null";

            String xText = getContainsX.getText().trim();
            String yText = getContainsY.getText().trim();
            String containsPart;
            String yPart;

            if (xText.isEmpty() || yText.isEmpty()) {
                containsPart = "contains=null";
                yPart = "0"; // placeholder because server expects parts[3], but it won’t parse if contains ends with null
            } else {
                containsPart = "contains=" + xText;
                yPart = yText;
            }

            String refers = getRefersTo.getText().trim();
            if (refers.isEmpty()) refers = "null";

            String cmd = "GET " +
                    "colour=" + color + " " +
                    containsPart + " " + yPart + " " +
                    "refersTo=" + refers;

                // Arm parser BEFORE sending so reply is classified correctly
            pending = PendingReply.GET_NOTES;
            remainingLines = 0;
            pendingNotes.clear();
            pendingPins.clear();

            appendOutput(">> " + cmd);
            sendToServer(cmd);

        } else if (e.getSource() == getPinsButton){

            String cmd = "GET PINS";

            // Arm parser BEFORE sending
            pending = PendingReply.GET_PINS;
            remainingLines = 0;
            pendingNotes.clear();
            pendingPins.clear();

            appendOutput(">> " + cmd);
            sendToServer(cmd);

        } else if (e.getSource() == pinButton){
            try {
                int x = Integer.parseInt(pinX.getText().trim());
                int y = Integer.parseInt(pinY.getText().trim());

                sendToServer("PIN " + x + " " + y);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "X and Y must be integers.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == unpinButton){
            try {
                int x = Integer.parseInt(unpinX.getText().trim());
                int y = Integer.parseInt(unpinY.getText().trim());
                sendToServer("UNPIN " + x + " " + y);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "X and Y must be integers.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == shakeButton){
            sendToServer("SHAKE");
        } else if (e.getSource() == clearButton){
            sendToServer("CLEAR");
        } else if (e.getSource() == disconnectButton){
            sendToServer("DISCONNECT");
            if (frame != null) frame.dispose();
            System.exit(0);
        }
        
    }

    private void sendToServer(String cmd) {
        new Thread(() -> {
            try {
                client.sendCommand(cmd);
                //String result = client.sendCommand(cmd);
                //System.out.println(result);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    //Handling the broadcast message
    private void handleServerMessage(String msg) {
        if (msg == null) return;
        msg = msg.trim();
        if (msg.isEmpty()) return;

        // Start snapshot
        if (!inSnapshot && msg.startsWith("SNAPSHOT")) {
            String[] p = msg.split("\\s+");
            if (p.length != 3) {
                System.out.println("Bad SNAPSHOT header: " + msg);
                return;
            }

            snapshotNotesRemaining = Integer.parseInt(p[1]);
            snapshotPinsRemaining  = Integer.parseInt(p[2]);
            inSnapshot = true;

            boardPanel.clearAll(); // must clear notes AND pins
            return;
        }

        // Consume snapshot body
        if (inSnapshot) {

            // 1) Notes first
            if (snapshotNotesRemaining > 0) {
                if (!msg.startsWith("NOTE ")) {
                    System.out.println("Expected NOTE, got: " + msg);
                    return;
                }

                // NOTE x y colour pinned message...
                String[] p = msg.split("\\s+", 6);
                if (p.length < 5) {
                    System.out.println("Bad NOTE line: " + msg);
                    return;
                }

                int x = Integer.parseInt(p[1]);
                int y = Integer.parseInt(p[2]);
                String colour = p[3];
                boolean pinned = Boolean.parseBoolean(p[4]);
                String message = (p.length == 6) ? p[5] : "";

                boardPanel.postNote(new BoardPanel.NoteView(x, y, colour, message, pinned));
                snapshotNotesRemaining--;

            // 2) Then pins
            } else if (snapshotPinsRemaining > 0) {
                if (!msg.startsWith("PIN ")) {
                    System.out.println("Expected PIN, got: " + msg);
                    return;
                }

                String[] p = msg.split("\\s+");
                if (p.length != 3) {
                    System.out.println("Bad PIN line: " + msg);
                    return;
                }

                int x = Integer.parseInt(p[1]);
                int y = Integer.parseInt(p[2]);

                boardPanel.addPin(new BoardPanel.pinsView(x, y));
                snapshotPinsRemaining--;
            }

            // Finished
            if (snapshotNotesRemaining == 0 && snapshotPinsRemaining == 0) {
                inSnapshot = false;
                boardPanel.repaint();
            }

            return; // snapshot lines should not fall into EVENT parsing
        }



        // Only handle EVENT messages here; everything else is a reply/error/status line.
        if (!msg.startsWith("EVENT")) {
            System.out.println("FROM SERVER (non-event): " + msg);
            return;
        }

        // Expected:
        // EVENT POST 10 20 blue hello world
        // EVENT PIN  12 34
        // EVENT UNPIN 12 34
        // EVENT CLEAR
        String[] parts = msg.split("\\s+", 3); // EVENT + action + rest
        if (parts.length < 2) {
            System.out.println("Bad EVENT (missing action): " + msg);
            return;
        }

        String action = parts[1];
        String rest = (parts.length == 3) ? parts[2] : "";

        try {
            switch (action.toUpperCase()) {

                case "POST": {
                    // rest: "x y colour message..."
                    String[] p = rest.split("\\s+", 4);
                    if (p.length < 4) {
                        System.out.println("Bad EVENT POST: " + msg);
                        return;
                    }

                    int x = Integer.parseInt(p[0]);
                    int y = Integer.parseInt(p[1]);
                    String colour = p[2];
                    String message = p[3];

                    boardPanel.postNote(new BoardPanel.NoteView(x, y, colour, message, false));
                    boardPanel.repaint();
                    break;
                }

                case "PIN": {
                    // rest: "x y"
                    String[] p = rest.split("\\s+");
                    if (p.length != 2) {
                        System.out.println("Bad EVENT PIN: " + msg);
                        return;
                    }

                    int x = Integer.parseInt(p[0]);
                    int y = Integer.parseInt(p[1]);

                    // IMPORTANT: use the correct class name (PinView vs pinsView)
                    boardPanel.addPin(new BoardPanel.pinsView(x, y));
                    boardPanel.repaint();
                    break;
                }

                case "UNPIN": {
                    // rest: "x y"
                    String[] p = rest.split("\\s+");
                    if (p.length != 2) {
                        System.out.println("Bad EVENT UNPIN: " + msg);
                        return;
                    }

                    int x = Integer.parseInt(p[0]);
                    int y = Integer.parseInt(p[1]);

                    boardPanel.removePin(x, y);
                    boardPanel.repaint();
                    break;
                }

                case "CLEAR": {
                    boardPanel.clearAll();
                    // if you store pins, also clear pins here:
                    // boardPanel.clearPins();
                    boardPanel.repaint();
                    break;
                }

                default:
                    System.out.println("Unknown EVENT: " + msg);
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Bad number in EVENT: " + msg);
        }
    }

    //For GET
    private void appendOutput(String msg) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(msg);
            if (!msg.endsWith("\n")) outputArea.append("\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    private void handleServerLine(String line) {
        // Always run parsing on the Swing thread (safe UI updates)
        SwingUtilities.invokeLater(() -> {
            if (line == null) return;

            // 1) Broadcast events: all clients apply
            if (line.startsWith("EVENT ")) {
                handleEvent(line);
                return;
            }

            // 2) Errors: show on this client only
            if (line.startsWith("ERR")) {
                appendOutput(line);
                // Cancel any pending reply to avoid being stuck
                pending = PendingReply.NONE;
                remainingLines = 0;
                pendingNotes.clear();
                pendingPins.clear();
                return;
            }

            // 3) If we are in the middle of collecting GET/GETPINS detail lines
            if (pending != PendingReply.NONE && remainingLines > 0) {
                if (pending == PendingReply.GET_NOTES) {
                    parseNoteLine(line);   // expects "NOTE ..."
                } else if (pending == PendingReply.GET_PINS) {
                    parsePinLine(line);    // expects "PIN ..."
                }

                remainingLines--;

                if (remainingLines == 0) {
                    // Done → replace what THIS client is displaying
                    if (pending == PendingReply.GET_NOTES) {
                        boardPanel.setNotes(new java.util.ArrayList<>(pendingNotes));
                        appendOutput("GET returned " + pendingNotes.size() + " note(s).");
                    } else if (pending == PendingReply.GET_PINS) {
                        boardPanel.setPins(new java.util.ArrayList<>(pendingPins));
                        appendOutput("GET PINS returned " + pendingPins.size() + " pin(s).");
                    }

                    pending = PendingReply.NONE;
                    pendingNotes.clear();
                    pendingPins.clear();
                }
                return;
            }

            // 4) Fresh reply header: "OK <n>"
            if (line.startsWith("OK ")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2) {
                    appendOutput("Bad OK line: " + line);
                    return;
                }

                int n;
                try {
                    n = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    appendOutput("Bad OK count: " + line);
                    return;
                }

                // If we don't know what we're waiting for, just show it
                if (pending == PendingReply.NONE) {
                    appendOutput("OK " + n);
                    return;
                }

                remainingLines = n;
                appendOutput(line);
                if (n == 0) {
                    // immediate completion
                    if (pending == PendingReply.GET_NOTES) {
                        boardPanel.setNotes(java.util.Collections.<BoardPanel.NoteView>emptyList());
                        appendOutput("GET returned 0 note(s).");
                    } else if (pending == PendingReply.GET_PINS) {
                        boardPanel.setPins(java.util.Collections.<BoardPanel.pinsView>emptyList());
                        appendOutput("GET PINS returned 0 pin(s).");
                    }
                    pending = PendingReply.NONE;
                }
                return;
            }

            // 5) Any other non-event lines (e.g., "OK NOTE_POSTED", "OK PIN_ADDED", etc.)
            appendOutput(line);
        });
    }

    private void handleEvent(String line) {
        // EVENT POST x y color message...
        // EVENT PIN x y
        // EVENT UNPIN x y
        // EVENT BOARD_CLEARED
        // EVENT BOARD_SHAKEN

        String[] parts = line.split("\\s+");
        if (parts.length < 2) return;

        String type = parts[1].toUpperCase();

        switch (type) {
            case "POST": {
                // EVENT POST x y color message...
                if (parts.length < 6) return;
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                String color = parts[4];

                // message is remainder after "EVENT POST x y color "
                int prefixLen = ("EVENT POST " + parts[2] + " " + parts[3] + " " + parts[4] + " ").length();
                String msg = line.length() >= prefixLen ? line.substring(prefixLen) : "";

                boardPanel.postNote(new BoardPanel.NoteView(x, y, color, msg, false));
                boardPanel.repaint();
                break;
            }
            case "PIN": {
                if (parts.length < 4) return;
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                boardPanel.addPin(new BoardPanel.pinsView(x, y));
                boardPanel.repaint();
                break;
            }
            case "UNPIN": {
                if (parts.length < 4) return;
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                boardPanel.removePin(x, y);
                boardPanel.repaint();
                break;
            }
            case "BOARD_CLEARED": {
                boardPanel.clearAll();
                boardPanel.repaint();
                break;
            }
            case "BOARD_SHAKEN": {
                // Your server removes unpinned notes; easiest client-side is to request a full snapshot
                // If you want, you can call: client.sendCommand("SYNC");
                appendOutput("Board shaken (consider SYNC to reflect removed notes).");
                break;
            }
        }
    }

    private void parseNoteLine(String line) {
        if (!line.startsWith("NOTE ")) {
            appendOutput("Unexpected line (expected NOTE): " + line);
            return;
        }

        // NOTE x y color ...message... PINNED=true
        String[] parts = line.split("\\s+");
        if (parts.length < 6) {
            appendOutput("Bad NOTE line: " + line);
            return;
        }

        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        String color = parts[3];

        // last token must be PINNED=...
        String pinnedToken = parts[parts.length - 1];
        boolean pinned = pinnedToken.startsWith("PINNED=") && pinnedToken.substring(7).equalsIgnoreCase("true");

        // message is everything between color and PINNED=
        StringBuilder sb = new StringBuilder();
        for (int i = 4; i < parts.length - 1; i++) {
            if (i > 4) sb.append(' ');
            sb.append(parts[i]);
        }
        String msg = sb.toString();

        pendingNotes.add(new BoardPanel.NoteView(x, y, color, msg, pinned));
    }

    private void parsePinLine(String line) {
        if (!line.startsWith("PIN ")) {
            appendOutput("Unexpected line (expected PIN): " + line);
            return;
        }
        String[] parts = line.split("\\s+");
        if (parts.length != 3) {
            appendOutput("Bad PIN line: " + line);
            return;
        }
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        pendingPins.add(new BoardPanel.pinsView(x, y));
    }

    

}
