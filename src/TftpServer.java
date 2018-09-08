import java.net.DatagramPacket;
import java.net.DatagramSocket;

class TftpServer
{
    public void start_server()
    {
        try {
            DatagramSocket ds = new DatagramSocket(0);
            System.out.println("TftpServer on port " + ds.getLocalPort());
            while(true) {
                byte[] buf = new byte[1472];
                DatagramPacket p = new DatagramPacket(buf, 1472);
                ds.receive(p);
                TftpServerWorker worker = new TftpServerWorker(p);
                worker.start();
            }
        }
        catch(Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    public static void main(String args[])
    {
        TftpServer d = new TftpServer();
        d.start_server();
    }
}