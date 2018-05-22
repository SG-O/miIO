package baseTest;

import org.json.JSONArray;
import util.ByteArray;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class TestServer extends Thread{
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[1024];
    private Token tk;

    public TestServer(Token tk) throws SocketException {
        this.tk = tk;
        socket = new DatagramSocket(54321);
    }

    public void terminate() {
        running = false;
    }


    public void run() {
        running = true;

        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException ignored) {
                continue;
            }

            byte[] worker = new byte[2];
            System.arraycopy(buf, 2, worker, 0, 2);
            int length = (int)ByteArray.fromBytes(worker);
            worker = new byte[length];
            System.arraycopy(buf, 0, worker, 0, length);
            Message msg = new Message(worker, null);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            Message resp;
            if (msg.isHello()){
                resp = new Message(null, 12345, 12345678, 0, null, null);
            } else {
                JSONArray data = new JSONArray();
                data.put("ok");
                resp = new Message(tk, 12345, 12345678, 0, "response", data);
            }
            byte[] respMsg = resp.create();
            System.arraycopy(respMsg,0,buf,0,respMsg.length);
            packet = new DatagramPacket(buf, respMsg.length, address, port);

            try {
                socket.send(packet);
            } catch (IOException ignored) {
            }
        }
        socket.close();
    }
}
