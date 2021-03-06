import java.io.*;
import java.util.*;

public class ParseFile {

    // Initializes the array list of ports by reading from the file config_peer.txt
    public static void loadConfigPeerFile() {
        try {
            File configPeerFile = new File("config_peer.txt");
            BufferedReader br = new BufferedReader(new FileReader(configPeerFile));

            String portStr;
            int id = 1000;
            while ((portStr = br.readLine()) != null)
                TcpSocketController.availableTcpSockets.add(new TcpSocket(Integer.parseInt(portStr), id++));
        }
        catch (IOException e) {
            System.out.println("Error reading from config_peer.txt");
        }
    }

    public static boolean loadConfigSharingFile(String filename) {
        try {
            File configPeerFile = new File("config_sharing.txt");
            BufferedReader br = new BufferedReader(new FileReader(configPeerFile));

            String name;
            while ((name = br.readLine()) != null) {
                if (name.equals(filename)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            System.out.println("Error reading from config_sharing.txt");
            return false;
        }
    }

    /**
     * The static method that updates the config_peer.txt file with the available sockets
     * This will be called every time there is a modification to the list of available sockets
     */
    public static void writeConfigPeerFile() {
        try {
            FileWriter configPeerWriter = new FileWriter("config_peer.txt", false);
            for (TcpSocket socket : TcpSocketController.availableTcpSockets) {
                configPeerWriter.write(socket.getLocalPort() + "\n");
            }

            configPeerWriter.flush();
            configPeerWriter.close();
        }
        catch (IOException e) {
            System.out.println("Error writing to config_peer.txt");
        }
    }

    /**
     * The static method that updates the config_neighbors.txt file with the connected sockets
     * This will be called every time there is a modification to the list of connected sockets
     */
    public static void writeConfigNeighborsFile() {
        try {
            FileWriter configNeighborsWriter = new FileWriter("config_neighbors.txt", false);
            for (TcpSocket socket : TcpSocketController.connectedTcpSockets) {
                configNeighborsWriter.write(socket.getConnectionSocket().getRemoteSocketAddress().toString().substring(1) +  "\n");
            }

            configNeighborsWriter.flush();
            configNeighborsWriter.close();
        }
        catch (IOException e) {
            System.out.println("Error writing to config_neighbors.txt");
        }
    }
}
