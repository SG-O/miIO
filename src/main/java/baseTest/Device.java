package baseTest;

import org.json.JSONArray;
import org.json.JSONObject;
import util.ByteArray;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Device {

    private static final int PORT = 54321;
    private byte[] rcv = new byte[65507];

    private InetAddress ip;
    private Token token;

    private DatagramSocket socket;

    private int deviceID = -1;
    private int timeStamp = -1;

    private long methodID;

    public Device(InetAddress ip, Token token) {
        this.ip = ip;
        this.token = token;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
        } catch (SocketException ignored) {
        }

    }

    private boolean hello(InetAddress broadcast, String[] acceptableModels) {
        if (socket == null) return false;
        Message hello = new Message(null,0,0,0,null,null);
        byte[] helloMsg = hello.create();
        DatagramPacket packet;
        if (ip == null){
            if (acceptableModels == null) return false;
            packet = new DatagramPacket(helloMsg, helloMsg.length, broadcast, PORT);
        } else {
            packet = new DatagramPacket(helloMsg, helloMsg.length, ip, PORT);
        }
        try {
            socket.send(packet);
        } catch (IOException e) {
            return false;
        }
        packet = new DatagramPacket(rcv, rcv.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            return false;
        }
        if (ip == null){
            ip = packet.getAddress();
        }
        byte[] worker = new byte[2];
        System.arraycopy(rcv, 2, worker, 0, 2);
        int length = (int)ByteArray.fromBytes(worker);
        worker = new byte[length];
        System.arraycopy(rcv, 0, worker, 0, length);
        Message response = new Message(worker, null);
        if (!response.getToken().equals(new Token("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16))){
            token = response.getToken();
        } else if (token == null){
            return false;
        }
        if (!((response.getDeviceID() == -1) || (response.getTimeStamp() == -1))){
            deviceID = response.getDeviceID();
            timeStamp = response.getTimeStamp();
            if (acceptableModels != null){
                for (String s: acceptableModels) {
                    s.equals(model());
                }
            }
            return true;
        }
        return false;
    }

    public Message send(String method, JSONArray params, String[] acceptableModels){
        boolean helloResponse = false;
        if (deviceID == -1 || timeStamp == -1 || token == null || ip == null) {
            List<InetAddress> broadcast = listAllBroadcastAddresses();
            if (broadcast == null) return null;
            for (InetAddress i: broadcast) {
                if (hello(i, acceptableModels)){
                    helloResponse = true;
                    break;
                }
            }
            if (!helloResponse) return null;
            methodID = this.timeStamp & 0b1111111111111; // Possible collision about every 2 hours > acceptable
        }
        if (methodID >= 10000) methodID = 1;
        if (ip == null || token == null) return null;
        if (socket == null) return null;
        timeStamp++;
        Message msg = new Message(this.token,this.deviceID,timeStamp,this.methodID,method,params);
        methodID++;
        byte[] binMsg = msg.create();


        DatagramPacket packet = new DatagramPacket(binMsg, binMsg.length, ip, PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            return null;
        }
        packet = new DatagramPacket(rcv, rcv.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            return null;
        }

        byte[] worker = new byte[2];
        System.arraycopy(rcv, 2, worker, 0, 2);
        int length = (int)ByteArray.fromBytes(worker);
        worker = new byte[length];
        System.arraycopy(rcv, 0, worker, 0, length);
        Message response = new Message(worker, this.token);
        if (!((response.getDeviceID() == -1) || (response.getTimeStamp() == -1))){
            return response;
        }
        return null;
    }

    private List<InetAddress> listAllBroadcastAddresses() {
        List<InetAddress> broadcastList = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            try {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
            } catch (SocketException e) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(InterfaceAddress::getBroadcast)
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    public JSONObject info(){
        Message resp = send("miIO.info", null, null);
        if (resp == null) return new JSONObject();
        return resp.getParams().optJSONObject(0);
    }

    public boolean update(String url, String md5){
        if (url == null || md5 == null) return false;
        if (md5.length() != 32) return false;
        JSONObject params = new JSONObject();
        params.put("mode","normal");
        params.put("install", "1");
        params.put("app_url", url);
        params.put("file_md5", md5);
        params.put("proc", "dnld install");
        JSONArray payload = new JSONArray();
        payload.put(params);
        Message resp = send("miIO.ota", payload, null);
        if (resp == null) return false;
        return resp.getParams().optString(0).toLowerCase().equals("ok");
    }

    public int updateProgress(){
        Message resp = send("miIO.get_ota_progress", null, null);
        if (resp == null) return 0;
        return resp.getParams().optInt(0);
    }

    public String updateStatus(){
        Message resp = send("miIO.get_ota_state", null, null);
        if (resp == null) return "";
        return resp.getParams().optString(0);
    }

    public boolean configureRouter(String ssid, String password){
        return configureRouter(ssid, password, 0);
    }

    public boolean configureRouter(String ssid, String password, int uid){
        if (ssid == null || password == null) return false;
        JSONObject params = new JSONObject();
        params.put("ssid",ssid);
        params.put("passwd", password);
        params.put("uid", uid);
        JSONArray payload = new JSONArray();
        payload.put(params);
        Message resp = send("miIO.config_router", payload, null);
        if (resp == null) return false;
        return resp.getParams().optString(0).toLowerCase().equals("ok");
    }

    public String model(){
        JSONObject in = info();
        if (in == null) return "";
        return in.optString("model");
    }

    public String firmware(){
        JSONObject in = info();
        if (in == null) return "";
        return in.optString("fw_ver");
    }
}
