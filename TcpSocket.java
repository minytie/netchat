import java.net.*;
import java.io.*;
import java.util.*;

public class TcpSocket extends ServerSocket {
    // Port number of the socket
    private int localPort;

    // A unique ID to identify the individual sockets from one another
    private int id;

    // The field for storing the accepted socket
    private Socket connectionSocket;

    // Handles inbound data stream
    private BufferedReader incomingFromPeer;

    // Handles outbound data stream
    private DataOutputStream outboundToPeer;

    public String response;
    public ArrayList<String> file;

    public TcpSocket(int localPort, int id) throws IOException {
        super(localPort);
        this.localPort = localPort;
        this.id = id;
        this.file = new ArrayList<>();
        // Make the socket readily available in the background
//        new Thread(new Runnable() {
//            public void run() {
//                connectedToPeer();
//            }
//        }).start();
    }

    /**
     * The method that actively initializes the socket connection with a peer
     * @param ipAddrStr hostname of the peer in the format of string
     * @param port port number of the peer in the format of int
     */
    public void connectToPeer(String ipAddrStr, int port) {
        try {
            connectionSocket = new Socket(ipAddrStr, port);
            TcpSocketController.socketConnected(this.getId());
            System.out.println("Connected to peer at " + ipAddrStr + " on port " + port + "!");

            new Thread(new Runnable() {
                public void run() {
                    setupDataTransfer();
                }
            }).start();
        }
        catch (IOException e) {
            System.out.println("TCP connection failed");
        }
    }

    /**
     * The method that handles the incoming established connection with a peer
     */
    public void connectedToPeer() {
        try {
            // Mark the socket as connected with the socket controller
            // If the TCP socket is connected by the user, the socket cannot be used by other peers
            // If the TCP socket is not connected, the socket can accept from other peers
            if (connectionSocket == null) {
                connectionSocket = this.accept();
                System.out.println("Peer connected successfully");
            }
            TcpSocketController.socketConnected(this.getId());

            new Thread(new Runnable() {
                public void run() {
                    setupDataTransfer();
                }
            }).start();
        }
        catch (IOException e) {
            System.out.println("TCP connection failed. Please try another peer or port");
        }
    }

    public void setupDataTransfer() {
        try {
            // Set up the incoming and outbound stream of data
            incomingFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outboundToPeer = new DataOutputStream(connectionSocket.getOutputStream());

            // Wait for incoming stream from the peer
            while (connectionSocket != null && connectionSocket.isConnected()){
                String streamStr = readFromPeer();
                if (streamStr == null){
                    Thread.sleep(500);
                    continue;
                }
                if (streamStr.startsWith("Q:")) {
                    System.out.println(streamStr);
                    boolean exist = ParseFile.loadConfigSharingFile(streamStr.split(";")[1]);
                    String queryId = streamStr.split(";")[0].split(":")[1];
                    String filename = streamStr.split(";")[1];
                    if (exist) {
                        writeToPeer("R:" + queryId + ":" + NetworkUtil.getOwnExternalIp() + ":" + getLocalPort() + ";" + filename);
                    } else {
                        writeToPeer("R:"+queryId+";"+filename+" miss");
                    }
                }else if (streamStr.startsWith("R:")) {
                    System.out.println(streamStr);
                    response = streamStr;
                }else if (streamStr.startsWith("T:")) {
                    System.out.println(streamStr);
                    String filename = streamStr.split(":")[1];
                    File file = new File("shared/"+filename);
                    writeToPeer("TB:"+filename);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String text;
                    while ((text = br.readLine()) != null) {
                        writeToPeer("TB:"+text);
                    }
                    writeToPeer("TB:###");
                } else if (streamStr.startsWith("TB:")) {
                    file.add(streamStr.replaceFirst("TB:",""));
                }
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method that handles the disconnection from the peer
     * @return true if the socket is disconnected without error
     */
    public boolean disconnectedFromPeer() {
        try {
            // If connectionSocket is linked with other peers
            if (connectionSocket != null) {
                // Close the connection
                // Has to be done before closing the data stream so the socket could send a null packet to terminate the readline method
                connectionSocket.close();
                connectionSocket = null;

                // Close the data stream
                incomingFromPeer.close();
                outboundToPeer.close();
                // Remove the socket from the list of connected sockets
                TcpSocketController.socketDisconnected(this.getId());

                // Return true if the above process is handled successfully
                return true;
            }
        }
        catch (IOException e) {
            System.out.println("Error closing socket");
        }
        return false;
    }

    /**
     * The method that reads messages from peer
     * @return a string read from the incoming string reader, or null if the socket is closed
     */
    public String readFromPeer() {
        try {
            // Return the message sent from peer
            return incomingFromPeer.readLine();
        }
        catch (IOException e) {
            System.out.println("Peer socket closed");
        }

        // Return null if the socket to peer is closed
        return null;
    }

    /**
     * The method that writes the message to peer
     * @param message the message in the format of string to be delivered to the peer
     */
    public void writeToPeer(String message) {
        try {
            outboundToPeer.writeBytes(message+"\n");
            outboundToPeer.flush();
        }
        catch (IOException e) {
            System.out.println("Error writing to socket (ID " + getId() + ")");
        }
    }

    /**
     * The method that processes the string received from the peer
     * @param streamStr a string received from the peer
     */
    public String parseStream(String streamStr) {
        if (streamStr != null)
            System.out.println(streamStr);
        return streamStr;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getId() {
        return id;
    }

    public Socket getConnectionSocket() {
        return connectionSocket;
    }
}
