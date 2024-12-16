import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class clinet {
    public static void main(String[] args) {
        final ArrayList<String>[] stringsList = new ArrayList[]{new ArrayList<>()};
        final File[] Filetosend = {null};
        JFrame jframe  = new JFrame("client");
        jframe.setSize(500,500);
        jframe.setLayout(new BoxLayout(jframe.getContentPane(),BoxLayout.Y_AXIS));
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea jt =new JTextArea(10,10);

jt.setBackground(Color.white);
jt.setSize(10,10);
        JLabel tittel = new JLabel(" Welcom To Client ");
        tittel.setBackground(Color.red);
        tittel.setFont(new Font("Arial",Font.BOLD,20));
        tittel.setBorder(new EmptyBorder(20,0,10,0));
        tittel.setAlignmentX(Component.CENTER_ALIGNMENT);


        ////////////
        JLabel packet = new JLabel("The number of packet send:");
        packet.setFont(new Font("Arial",Font.BOLD,20));
        packet.setBorder(new EmptyBorder(20,0,10,0));
        packet.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel p = new JPanel();
        p.add(packet);




        JLabel size = new JLabel("The size of file send");
       size.setFont(new Font("Arial",Font.BOLD,20));
        size.setBorder(new EmptyBorder(20,0,10,0));
      size.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel ss = new JPanel();
        p.add(ss);
        ///////
        JLabel jfile = new JLabel("Choose a file to send.");
        jfile.setFont(new Font("Arial",Font.BOLD,20));
        jfile.setBorder(new EmptyBorder(50,0,0,0));
        jfile.setAlignmentX(Component.CENTER_ALIGNMENT);
        //////
        JPanel buuton=new JPanel();
        buuton.setBorder(new EmptyBorder(75,0,10,0));

        JButton sendFile= new JButton("Send File");
        sendFile.setPreferredSize(new Dimension(150,75));
        sendFile.setFont(new Font("Arial",Font.BOLD,20));

        JButton choosefile = new JButton("Choose File");
        choosefile.setPreferredSize(new Dimension(150,75));
        choosefile.setFont(new Font("Arial",Font.BOLD,20));

        buuton.add(sendFile);
        buuton.add(choosefile);

        choosefile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Choose a file to send");
                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    Filetosend[0] = jFileChooser.getSelectedFile();

                    jfile.setText("The file you want to send:" + Filetosend[0].getName());
                    ///divided file
size.setText("The size of file sent "+Filetosend[0].length());

                    ArrayList <String> stringsList = new ArrayList<String >();
                    try (BufferedReader br = new BufferedReader(new FileReader(Filetosend[0]))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            stringsList.add(line);
                        }


                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    packet.setText("The number of packet is "+stringsList.size());

                }
            }

        });


        sendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Filetosend[0] == null) {
                    jfile.setText("Please choose a file first");
                } else {
                    try {
                        Socket socket = new Socket("localhost", 1200);
                     //   Socket socket = new Socket("localhost", 1200);
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                        ArrayList<String> stringsList = new ArrayList<>();
                        try (BufferedReader br = new BufferedReader(new FileReader(Filetosend[0]))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                stringsList.add(line);
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                        // Send the total number of lines to the server
                        dataOutputStream.writeInt(stringsList.size());
                        dataOutputStream.flush();

                        for (String line : stringsList) {
                            String file = Filetosend[0].getName();
                            byte[] filebyte = file.getBytes();
                            byte[] filecontent = line.getBytes();
                            dataOutputStream.writeInt(filebyte.length);
                            dataOutputStream.write(filebyte);
                            dataOutputStream.writeInt(filecontent.length);
                            dataOutputStream.write(filecontent);
                            dataOutputStream.flush();

                            // Start the timer for this line
                            long startTime = System.currentTimeMillis();

                            // Wait for acknowledgment from the server within 5 seconds
                            socket.setSoTimeout(9000);


                            try {
                                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                                String ack = dataInputStream.readUTF();


                                if (ack.equals("ACK")) {
                                    System.out.println("Packet sent and acknowledged: " + line);
                                    jt.append("Packet sent and acknowledged: " + line + "\n");
                                } else {
                                    System.out.println("Packet not acknowledged: " + line);
                                    jt.append("Packet not acknowledged: " + line + "\n");
                                }

                            } catch (SocketTimeoutException ex) {
                                System.out.println("Packet  not acknowledged within 5 seconds: " +line);
                                System.out.println("Packet sent and acknowledged : " + line);

                                jt.append("Packet not acknowledged within 5 seconds: " + line + "\n");
                                jt.append("Packet sent and acknowledged : " + line+ "\n");

                            }

                            // Reset the timeout for the socket back to default
                            socket.setSoTimeout(0);


                        }

                    } catch (SocketException ex) {

                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });




        jframe.add(tittel);
        jframe.add(jfile);

        jframe.add(buuton);
        jframe.add(p);
        jframe.add(size);
        jframe.add(jt);
        jframe.setVisible(true);





    }
}
