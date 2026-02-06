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



    public GUI(String host, int port) {

        

        //Connect to the server (localhost 12345)
        try {
            client = new Client(host, port);
            client.readHandshake();
            client.sendCommand("SYNC");
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


        label = new JLabel("Colours: " + String.join(", ", client.colours));

        buttons.add(getPinsButton);
        buttons.add(shakeButton);
        buttons.add(clearButton);
        buttons.add(disconnectButton);

        
        JPanel topPanels = new JPanel();
        topPanels.setLayout(new BoxLayout(topPanels, BoxLayout.Y_AXIS));
        topPanels.add(postPanel);
        topPanels.add(Box.createVerticalStrut(10));
        topPanels.add(getPanel);
        topPanels.add(Box.createVerticalStrut(10));
        topPanels.add(pinPanel);
        topPanels.add(Box.createVerticalStrut(10));
        topPanels.add(unpinPanel);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rightPanel.add(topPanels, BorderLayout.NORTH);
        rightPanel.add(buttons, BorderLayout.CENTER);
        rightPanel.add(label, BorderLayout.SOUTH);

        frame.add(rightPanel, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        client.startListener(
            msg -> SwingUtilities.invokeLater(() -> handleServerMessage(msg)),
            ex  -> SwingUtilities.invokeLater(() -> {JOptionPane.showMessageDialog(frame, "Disconnected from server.");
            })
        );

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
            //sendToServer("GET");
            //System.out.println("GET");

            try {
                String colour = getColour.getText().trim();
                String xText = getContainsX.getText().trim();
                String yText = getContainsY.getText().trim();
                String refersTo = getRefersTo.getText().trim();



                StringBuilder cmd = new StringBuilder("GET");

                if (colour.isEmpty()) {
                    cmd.append(" color=null");
                } else {
                    cmd.append(" color=").append(colour);
                }

                //Needs both x and y
                if (!xText.isEmpty() && !yText.isEmpty()) { 
                    int x = Integer.parseInt(xText);
                    int y = Integer.parseInt(yText);
                    cmd.append(" contains=").append(x).append(" ").append(y);
                } else if(xText.isEmpty() && yText.isEmpty()) {
                    cmd.append(" contains=null null");
                } else {
                    int x = Integer.parseInt(xText);
                    int y = Integer.parseInt(yText);
                }

                if (!refersTo.isEmpty()) {
                    cmd.append(" refersTo=").append(refersTo);
                } else if(refersTo.isEmpty()){
                    cmd.append(" refersTo=null");
                }

                sendToServer(cmd.toString());


            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "X and Y must be integers.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == getPinsButton){

            sendToServer("GET PINS");

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



}
