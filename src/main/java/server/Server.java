package server;

import base.Token;
import base.messages.Command;
import base.messages.Response;
import org.json.JSONObject;
import util.ByteArray;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Server extends Thread{
    private List<OnServerEventListener> listener = new ArrayList<>();
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[65507];

    private Token tk;
    private int deviceId;
    private String model;
    private String firmware;
    private String hardware;
    private JSONObject network;
    private String macAddress;
    private long lifeTime;
    private JSONObject accessPoint;

    public Server(Token tk, int deviceId, String model, String firmware, String hardware, JSONObject network, String macAddress, long lifeTime, JSONObject accessPoint) throws SocketException {
        if (tk == null) { //if no token was provided generate a new random one
            Random rd = new Random();
            byte[] invalidTokenBytes = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
            byte[] tokenBytes = new byte[16];
            System.arraycopy(invalidTokenBytes,0,tokenBytes,0,16);
            while (Arrays.equals(tokenBytes, invalidTokenBytes)) {
                rd.nextBytes(tokenBytes);
            }
            tk = new Token(tokenBytes);
        }
        this.tk = tk;
        this.deviceId = deviceId;
        if (model == null) model = "miio.generic";
        this.model = model;
        if (firmware == null) firmware = "0.0.1_000001";
        this.firmware = firmware;
        if (hardware == null) hardware = "Linux";
        this.hardware = hardware;
        if (network == null) {
            network = new JSONObject();
            network.put("gw", "127.0.0.1");
            network.put("localIp", "127.0.0.1");
            network.put("mask", "255.0.0.0");

        }
        this.network = network;
        if (macAddress == null) macAddress = "02:00:00:00:00:01";
        this.macAddress = macAddress;
        this.lifeTime = lifeTime;
        if (accessPoint == null) {
            accessPoint = new JSONObject();
            accessPoint.put("rssi", -10);
            accessPoint.put("bssid", "02:00:00:00:00:00");
            accessPoint.put("ssid", "WLAN Router");
        }
        this.accessPoint = accessPoint;
        this.socket = new DatagramSocket(54321);
        this.socket.setSoTimeout(1000);
    }

    public void registerOnServerEventListener(OnServerEventListener listener){
        this.listener.add(listener);
    }

    public void terminate() {
        this.running = false;
        while (!socket.isClosed()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
    }


    public void run() {
        this.running = true;

        while (this.running) {
            DatagramPacket packet = new DatagramPacket(this.buf, this.buf.length);
            try {
                this.socket.receive(packet);
            } catch (IOException ignored) {
                continue;
            }

            byte[] worker = new byte[2];
            System.arraycopy(this.buf, 2, worker, 0, 2);
            int length = (int)ByteArray.fromBytes(worker);
            worker = new byte[length];
            System.arraycopy(this.buf, 0, worker, 0, length);
            Command msg = new Command(worker, tk);
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            int timeStamp = (int)(System.currentTimeMillis() / 1000L);
            Response resp;
            if (msg.isHello()){
                resp = new Response(this.tk, this.deviceId, timeStamp);
            } else {
                if (msg.getDeviceID() != this.deviceId) continue;
                Object data = executeCommand(msg.getMethod(),msg.getParams());
                if (data == null){
                    data = "unknown_method";
                }
                resp = new Response(this.tk, this.deviceId, timeStamp, msg.getPayloadID(), data);
            }
            byte[] respMsg = resp.create();
            System.arraycopy(respMsg,0,this.buf,0,respMsg.length);
            packet = new DatagramPacket(buf, respMsg.length, address, port);

            try {
                this.socket.send(packet);
            } catch (IOException ignored) {
            }
        }
        this.socket.close();
    }

    public int getDeviceId() {
        return deviceId;
    }

    public Token getTk() {
        return tk;
    }

    public String getModel() {
        return model;
    }

    public String getFirmware() {
        return firmware;
    }

    public String getHardware() {
        return hardware;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public JSONObject getNetwork() {
        return network;
    }

    public void setNetwork(JSONObject network) {
        if (network == null) return;
        this.network = network;
    }

    public long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(long lifeTime) {
        this.lifeTime = lifeTime;
    }

    public JSONObject getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(JSONObject accessPoint) {
        if (accessPoint == null) return;
        this.accessPoint = accessPoint;
    }

    private Object executeCommand(String method, Object params){
        if (method == null) return null;
        switch (method) {
            case "miIO.info":
                return generateInfo();
            default:
                if (!listener.isEmpty()){
                    Object ret = null;
                    for (OnServerEventListener lis : listener) {
                        ret = lis.onCommandListener(method, params);
                        if (ret != null) break;
                    }
                    return ret;

                } else {
                    return null;
                }
        }
    }

    private JSONObject generateInfo(){
        JSONObject obj = new JSONObject();
        obj.put("hw_ver", hardware);
        obj.put("netif", network);
        obj.put("model", model);
        obj.put("fw_ver", firmware);
        obj.put("mac", macAddress);
        obj.put("life", lifeTime);
        obj.put("ap", accessPoint);
        obj.put("token", tk.toString());
        return obj;
    }
}
