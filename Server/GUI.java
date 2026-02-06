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
    

    JButton postButton;
    JButton getButton;
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
        JPanel board = new JPanel();
        board.setPreferredSize(new Dimension(client.boardW, client.boardH));
        board.setBackground(Color.LIGHT_GRAY); // visual aid
        frame.add(board, BorderLayout.CENTER);

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



        pinButton = new JButton("PIN");
        pinButton.addActionListener(this);

        unpinButton = new JButton("UNPIN");
        unpinButton.addActionListener(this);

        shakeButton = new JButton("SHAKE");
        shakeButton.addActionListener(this);

        clearButton = new JButton("CLEAR");
        clearButton.addActionListener(this);

        disconnectButton = new JButton("DISCONNECT");
        disconnectButton.addActionListener(this);


        label = new JLabel("Colours: " + String.join(", ", client.colours));

        
        buttons.add(pinButton);
        buttons.add(unpinButton);
        buttons.add(shakeButton);
        buttons.add(clearButton);
        buttons.add(disconnectButton);
        buttons.add(label);

        
        JPanel topPanels = new JPanel();
        topPanels.setLayout(new BoxLayout(topPanels, BoxLayout.Y_AXIS));
        topPanels.add(postPanel);
        topPanels.add(Box.createVerticalStrut(10));
        topPanels.add(getPanel);

        
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        rightPanel.add(topPanels, BorderLayout.NORTH);
        rightPanel.add(buttons, BorderLayout.CENTER);
        rightPanel.add(label, BorderLayout.SOUTH);

        frame.add(rightPanel, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

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
                int x = Integer.parseInt(getContainsX.getText().trim());
                int y = Integer.parseInt(getContainsY.getText().trim());
                String colour = getColour.getText().trim();
                String contains = getRefersTo.getText().trim();

                sendToServer("GET color=" + colour + " contains=" + x + " " + y + " refersTo=" + contains);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "X and Y must be integers.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == pinButton){
            sendToServer("PIN");
        } else if (e.getSource() == unpinButton){
            sendToServer("UNPIN");
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
