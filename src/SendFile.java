import java.io.*;
import java.net.DatagramPacket;


public class SendFile extends Thread {
    private static final byte DATA = 2;
    private byte[] b;
    private TftpServerWorker owner;
    private int block = 0;
    private static final String filenamee = "C:\\Users\\user\\Desktop\\204\\untitled\\sendBin\\";

    public SendFile(String filename, TftpServerWorker owner){
        this.owner = owner;
        System.out.println(filename);
        try {
            FileInputStream fos = new FileInputStream(filenamee+ filename.trim());
            File file = new File(filenamee+filename.trim());
            b = new byte[(int)file.length()];
            fos.read(b);
            fos.close();
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            owner.error();
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
    }

    public void run() {
        boolean toBreak = false;
        int giveUp = 0;
        while (true) {
            int curblock = block;
            byte[] toSend = new byte[514];
            if (toBreak) {
                DatagramPacket data = new DatagramPacket(toSend, 514);
                owner.send(data);
                break;
            }
            toSend[0] = DATA;
            toSend[1] = ((byte) curblock);
                        if (b.length >= 512) {
                for(int i = 0; i != 512; i++){
                    toSend[i + 2] = b[i];
                }
            } else {
                for (int i = 0; i != b.length; i++) {
                    toSend[i + 2] = b[i];
                    toBreak = true;
                }
            }
            DatagramPacket data = new DatagramPacket(toSend, 514);
            owner.send(data);
            if(curblock == 128){
                curblock = 0;
            }
            if(b.length-512 > 0) {
                if (waitForAck() && (int)(byte)block == (int)(byte)(curblock + 1)) {
                        byte[] c = new byte[b.length - 512];
                        System.arraycopy(b, 512, c, 0, b.length - 512);
                        b = c;
                    giveUp = 0;
                } else {
                    giveUp++;
                    if (giveUp == 5) {
                        System.out.println("Client not responding");
                        break;
                    }
                }
            }
            else{toBreak=true;}
        }
    }

    private boolean waitForAck(){
        try {
            sleep(1000);
        }
        catch (InterruptedException e){
            block = owner.getBlock();
            return true;
        }
        return false;
    }
}
