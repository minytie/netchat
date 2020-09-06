import java.net.*;
import java.io.*;

public class Command {

    // The static field containing the current user input
    private static String[] param;
    private static int queryId = 0;

    /**
     * The static method that parses the current command input by the user
     * @param command the command in the form of a string
     */
    public static void parseCommand(String command) {
        try {
            parseArguments(command);
            switch (param[0]) {
                // returns back to the prompt
                case "":
                    return;

                // list the available sockets and connected sockets based on the config ports
                case "List":
                case "list":
                    TcpSocketController.printLocalSocket();
                    break;

                // connect to a peer with the provided IP address and port
                case "Connect":
                case "connect":
                    try {
                        ping(param[1], param[2]);
                    }
                    catch (IndexOutOfBoundsException e) {
                        help(false);
                    }
                    break;

                // send to all neighboring peers a query of the filename
                case "Get":
                case "get":
                    getFile(param[1]);
                    break;

                // disconnects all connection with peers
                case "Leave":
                case "leave":
                    TcpSocketController.disconnectAllSockets();
                    System.out.println("Closed all active TCP connection");
                    break;

                // disconnects all connection with peers and exit the program
                case "Exit":
                case "exit":
                    TcpSocketController.disconnectAllSockets();
                    System.out.println("Have a great day. Bye!");
                    System.exit(0);

                    // prints the help section
                case "Help":
                case "help":
                    help(false);
                    break;

                // when a command is not found
                default:
                    System.out.println("Error: " + command + ": command not found");
            }

        } catch (Exception ex) {

        }
    }

    /**
     * The static method that processes the arguments and stores them into an array of strings
     * @param args the user input to be split into an array
     */
    public static void parseArguments(String args) {
        param = args.split(" ");
    }

    /**
     * The static method that prints out the help section
     * @param versionHelp the boolean parameter that indicates whether to print the version information
     */
    public static void help(boolean versionHelp) {
        System.out.println();
        if (!versionHelp) {
            System.out.println("P2P Network - version 0.1");
            System.out.println("Case Western Reserve University - 2019\n");
        }
        System.out.println("List of commands: ");
        System.out.println("Connect [ip-address] [port] - connect to the peer with the designated IP address and port number");
        System.out.println("Get [filename.txt] - the peer runs the file query protocol to find and download this file.");
        System.out.println("Leave - close all TCP connections with neighboring peers");
        System.out.println("Help - print this help menu");
        System.out.println("Exit - close all TCP connections and terminates the program\n");
    }

    /**
     * The static method that ping the peer socket with the provided IP address
     * @param ipAddrStr the IP address in the format of a string
     * @param portStr the port number in the format of a string
     */
    public static void ping(String ipAddrStr, String portStr){
        PeerTopology instance = PeerTopology.PeerTopologyLoader.getInstance();
        instance.broadcast(ipAddrStr, "PI:" + ipAddrStr + ":" + portStr);
    }

    /**
     * The static method that connects an available socket to the peer socket with the provided IP address and port
     * @param ipAddrStr the IP address in the format of a string
     * @param portStr the port number in the format of a string
     */
    public static void connect(String ipAddrStr, String portStr) {
        //try {
            System.out.println("Connecting to peer...");
            TcpSocket peerSocket = TcpSocketController.getNextAvailableSocket();
            peerSocket.connectToPeer(ipAddrStr, Integer.parseInt(portStr));
//            System.out.println("Connected!!!");
        /*}
        catch (UnknownHostException e) {
            System.out.println("Error: unknown host " + ipAddrStr + " on port " + portStr);
        }
        catch (IOException e) {
            System.out.println("Error: Socket connection failed");
        }
        */
    }
    /**
     * The static method that handles with sending queries for file transfer
     * @param filename a string representing the filename of the requested file
     */
    public static void getFile(String filename) {
        try {
        for (TcpSocket activeSocket : TcpSocketController.connectedTcpSockets) {
            System.out.println("Sent query to " + activeSocket.getLocalPort());
            activeSocket.writeToPeer("Q:"+(++queryId)+";" + filename);
            // wait the response
            while (activeSocket.response == null ||
                   !activeSocket.response.contains("R:" + queryId)) {
                Thread.sleep(500);
            }
            if (activeSocket.response.endsWith("miss")) {
                System.out.println(activeSocket.response);
            } else {
                activeSocket.writeToPeer("T:" + filename);
                System.out.println("File downloading");
                int size = activeSocket.file.size();
                while (size == 0 ||
                       !activeSocket.file.get(size - 1).equals("###")) {
                    Thread.sleep(500);
                    size = activeSocket.file.size();
                }

                File file = new File("obtained/" + activeSocket.file.get(0));
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }
                activeSocket.file.remove(size - 1);
                activeSocket.file.remove(0);
                FileWriter fw = new FileWriter(file);
                BufferedWriter  bw=new BufferedWriter(fw);
                for(String arr:activeSocket.file){
                    bw.write(arr+"\n");
                }
                activeSocket.file.clear();
                bw.close();
                fw.close();
                break;
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

