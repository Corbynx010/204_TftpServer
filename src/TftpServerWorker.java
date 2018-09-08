import java.net.*;
import java.io.*;
import java.util.*;

class TftpServerWorker extends Thread {
    private static final byte RRQ = 1;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;
    private SendFile sendfile;
    private DatagramSocket ds;
    private int block = 0;
    private int port;
    private InetAddress ia;

    public void run() {
        byte[] buffer = new byte[514];
        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
        while (true) {
            try {
                incoming.setLength(buffer.length);
                ds.receive(incoming);
                byte[] bytereq = incoming.getData();
                if (bytereq[0] == ACK) {
                    System.out.println("ACK Received, Block: " + (int)bytereq[1]);
                    block = (int)bytereq[1]+1;
                    sendfile.interrupt();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void send(DatagramPacket dp){
        try {
            dp.setPort(port);
            dp.setAddress(ia);
            ds.send(dp);
        }catch(IOException e){e.printStackTrace();}
    }

    void error(){
        byte[] b = new byte[25];
        b[0] = ERROR;
        String x = "Missing file";
        byte[] c = x.getBytes();
        int i = 1;
        for(byte a : c){
            b[i] = a;
            i++;
        }
        DatagramPacket dp = new DatagramPacket(b, b.length);
        send(dp);
    }

    int getBlock() { return block; }

    TftpServerWorker(DatagramPacket req)
    {
        try {
            port = req.getPort();
            ia = req.getAddress();
            ds = new DatagramSocket();
            byte[] reqi = req.getData();
            if(reqi[0] == RRQ) {
                byte[] sendreq = Arrays.copyOfRange(reqi, 1, reqi.length);
                String reqString = new String(sendreq, "UTF-8");
                sendfile = new SendFile(reqString, this);
                sendfile.start();
            }
        }catch (Exception e){e.printStackTrace();}
    }
}
