import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;


// cd server
// java GUI localhost 12345

public class GUI implements ActionListener{

    private Client client;
    
    int count = 0;
    JLabel label;
    JFrame frame;

    JButton postButton;
    JButton getButton;
    JButton pinButton;
    JButton unpinButton;
    JButton shakeButton;
    JButton clearButton;
    JButton disconnectButton;


    public GUI(String host, int port){

        //Connect to the server (localhost 12345)
        try {
            client = new Client(host, port);
            client.readHandshake();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Could not connect to server: " + e.getMessage());
            System.exit(1);
        }

        frame = new JFrame();

        postButton = new JButton("POST");
        postButton.addActionListener(this);
        getButton = new JButton("GET");
        getButton.addActionListener(this);
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


        label = new JLabel("Label");

        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createEmptyBorder(300, 300, 200, 300));
        panel.setLayout(new GridLayout(0, 1));
       
        panel.add(postButton);
        panel.add(getButton);
        panel.add(pinButton);
        panel.add(unpinButton);
        panel.add(shakeButton);
        panel.add(clearButton);
        panel.add(disconnectButton);
        

        panel.add(label);

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Title");
        frame.pack();
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
            sendToServer("POST");
            //System.out.println("POST");
        } else if (e.getSource() == getButton) {
            sendToServer("GET");
            //System.out.println("GET");
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
