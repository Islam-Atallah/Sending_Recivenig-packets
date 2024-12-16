import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server {
    public static  void main(String[] args) throws IOException {

        // GUI setup
        JFrame jFrame = new JFrame("server");
        // ... GUI components initialization ...


        jFrame.setSize(500,500);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(),BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea jt =new JTextArea(10,10);
        jFrame.setVisible(true);

        JLabel tittel = new JLabel(" Welcom To Server ");
        tittel.setBackground(Color.red);
        tittel.setFont(new Font("Arial",Font.BOLD,20));
        tittel.setBorder(new EmptyBorder(20,0,10,0));
        tittel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel size  = new JLabel("Size of file receive ");
        size.setBackground(Color.red);
       size.setFont(new Font("Arial",Font.BOLD,20));
       size.setBorder(new EmptyBorder(20,0,10,0));
        size.setAlignmentX(Component.CENTER_ALIGNMENT);
        jFrame.add(tittel);
        jFrame.add(size);
        jFrame.add(jt);
        // Server setup/
      ServerSocket serverSocket = new ServerSocket(1200);
     //   String ipAddress = InetAddress.getLocalHost().getHostAddress();
      //  System.out.println("Server running on IP address: " + ipAddress);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
           //     socket.setReceiveBufferSize(5);

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                int totalLines = dataInputStream.readInt();

                // Store the lines that need acknowledgment
                ArrayList<String> linesToAck = new ArrayList<>();

                // Simulate not sending acknowledgment for Line 2 (index 1)
                boolean simulateUnacknowledgedLine = true;
                int lineToNotAcknowledge = 1;

                for (int i = 0; i < totalLines; i++) {
                    int filenamelen = dataInputStream.readInt();
                    if (filenamelen > 0) {
                        byte[] filebyte = new byte[filenamelen];
                        dataInputStream.readFully(filebyte, 0, filebyte.length);
                        String filename = new String(filebyte);

                        int filecontentlen = dataInputStream.readInt();
                        if (filecontentlen > 0) {
                            byte[] filecontent = new byte[filecontentlen];
                            dataInputStream.readFully(filecontent, 0, filecontent.length);

                            // Update GUI with the size of the received file content
                            // ... (You can update the GUI here if needed) ...

                            // Write the file content to a new file
                            writeToFile("server_received.txt", new String(filecontent));

                            // Store the line for acknowledgment
                            linesToAck.add(String.valueOf(i));

                            // Send acknowledgment to the client
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            if (simulateUnacknowledgedLine && i == lineToNotAcknowledge) {
                                simulateUnacknowledgedLine = false;
                                System.out.println("Sequence " + i + " not sent an acknowledgment."+"\n");
                                jt.append("Sequence " + i + " not sent an acknowledgment."+"\n");
                                System.out.println("Sequence " + i + " sent an acknowledgment."+"\n");
                                jt.append("Sequence " + i + "  sent an acknowledgment."+"\n");

                            } else {
                                dataOutputStream.writeUTF("ACK");
                                System.out.println("Sequence " + i + " sent an acknowledgment.");
                                jt.append("Sequence " + i + "  sent an acknowledgment."+"\n");

                            }
                            dataOutputStream.flush();
                        }
                    }
                }

                // Resend lines that need to be resent and not acknowledged
                ArrayList<String> linesToResend = new ArrayList<>(linesToAck);
                for (String line : linesToAck) {
                    int lineNumber = Integer.parseInt(line);
                    String file = "server_received.txt";
                    String lineContent = readLineFromFile(file, lineNumber);

                    byte[] filebyte = file.getBytes();
                    byte[] filecontent = lineContent.getBytes();
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    dataOutputStream.writeInt(filebyte.length);
                    dataOutputStream.write(filebyte);
                    dataOutputStream.writeInt(filecontent.length);
                    dataOutputStream.write(filecontent);
                    dataOutputStream.flush();

                    // Wait for acknowledgment from the client within 5 seconds
                    socket.setSoTimeout(5000);

                    try {
                        DataInputStream ackStream = new DataInputStream(socket.getInputStream());
                        String ack = ackStream.readUTF();

                        if (ack.equals("ACK")) {
                            System.out.println("Line " + lineNumber + " acknowledged.");
                            linesToResend.remove(String.valueOf(lineNumber)); // Remove the line from linesToResend
                        } else {
                            System.out.println("Line " + lineNumber + " not acknowledged. Resending...");
                        }
                    } catch (SocketTimeoutException ex) {
                        System.out.println("Line " + lineNumber + " not acknowledged. Resending...");
                    }

                    // Reset the timeout for the socket back to default
                    socket.setSoTimeout(0);
                }

                // Resend lines that were not acknowledged
                for (String line : linesToResend) {
                    int lineNumber = Integer.parseInt(line);
                    String file = "server_received.txt";
                    String lineContent = readLineFromFile(file, lineNumber);

                    byte[] filebyte = file.getBytes();
                    byte[] filecontent = lineContent.getBytes();
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    dataOutputStream.writeInt(filebyte.length);
                    dataOutputStream.write(filebyte);
                    dataOutputStream.writeInt(filecontent.length);
                    dataOutputStream.write(filecontent);
                    dataOutputStream.flush();

                    socket.setSoTimeout(5000);

                    try {
                        DataInputStream ackStream = new DataInputStream(socket.getInputStream());
                        String ack = ackStream.readUTF();

                        if (ack.equals("ACK")) {
                            System.out.println("Line " + lineNumber + " re-sent and acknowledged.");
                            linesToAck.remove(String.valueOf(lineNumber)); // Remove the line from linesToAck
                        } else {
                            System.out.println("Line " + lineNumber + " not acknowledged after resend.");
                        }
                    } catch (SocketTimeoutException ex) {
                        System.out.println("Line " + lineNumber + " not acknowledged after resend.");
                    }

                    // Reset the timeout for the socket back to default
                    socket.setSoTimeout(0);
                }

                // Send all acknowledgments to the client
                sendAcknowledgments(socket, linesToAck);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private static void writeToFile(String filename, String content) {
        try {
            FileOutputStream out = new FileOutputStream(filename, true);
            out.write(content.getBytes());
            out.write("\n".getBytes());
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static String readLineFromFile(String filename, int lineNumber) {
        // ... Read a specific line from the file ...
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int currentLineNumber = 0;
            while ((line = br.readLine()) != null) {
                currentLineNumber++;
                if (currentLineNumber == lineNumber) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void sendAcknowledgments(Socket socket, ArrayList<String> linesToAck) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeInt(linesToAck.size());
        for (String line : linesToAck) {
            dataOutputStream.writeUTF(line);
        }
        dataOutputStream.flush();
    }

}