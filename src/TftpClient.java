import java.net.*;
import java.io.*;

public class TftpClient {
    private static final byte FINAL = 0;
    private static final byte RRQ = 1;
    private static final byte DATA = 2;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;

    public static void main(String[] args) {
        int nextBlock = 0;
        int port;
        if (args.length != 3) {
            System.out.println("Usage: java QuoteClient <hostname> <requested file> <port>");
            return;
        }
        try {
            port = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println("Usage: java QuoteClient <hostname> <requested file> <port>");
            return;
        }
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] nextDP = new byte[252];
            InetAddress address = InetAddress.getByName(args[0]);
            nextDP[0] = RRQ;
            byte[] c = args[1].getBytes("UTF-8");
            int i = 1;
            for (byte b : c) {
                nextDP[i] = b;
                i++;
            }
            DatagramPacket packet = new DatagramPacket(nextDP, nextDP.length,
                    address, port);
            socket.send(packet);
            nextDP = new byte[2];
            File file = new File("receiveBin/" + args[1].trim());
            FileOutputStream fos = new FileOutputStream(file);
            while (true) {
                byte[] x = new byte[514];
                DatagramPacket data = new DatagramPacket(x, x.length);
                socket.receive(data);
                String received = new String(data.getData(), 0, data.getLength());
                String written = received.substring(2);
                if (x[0] == DATA && x[1] == nextBlock) {
                    if(nextBlock == 127){
                        nextBlock = 0;
                    }
                    fos.write(written.getBytes("UTF-8"));
                    nextDP[0] = ACK;
                    nextDP[1] = (byte) nextBlock;
                    packet = new DatagramPacket(nextDP, nextDP.length, address, data.getPort());
                    socket.send(packet);
                    nextBlock++;
                    System.out.println("Next Block:" + nextBlock);
                } else if (x[0] == ERROR) {
                    String s = new String(x).substring(1);
                    fos.close();
                    System.out.println(s);
                    break;
                } else if (x[0] == FINAL) {
                    fos.close();
                    break;
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket Exception " + e);
        } catch (IOException e) {
            System.out.println("IO Exception " + e);
        }
    }
}
