package base;

import base.messages.Command;
import base.messages.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import util.ByteArray;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class Device {

    private static final int PORT = 54321;
    private byte[] rcv = new byte[65507];

    private InetAddress ip;
    private Token token;
    private int retries;
    private String[] acceptableModels;

    private DatagramSocket socket;

    private int deviceID = -1;
    private int timeStamp = -1;

    private long methodID;

    /**
     * Baseclass for all miIO devices.
     * @param ip The IP address of the device to connect to. If the address is null the first device that is an acceptableModel will be chosen.
     * @param token The token for that device. If the token is null the token will be extracted from unprovisioned devices.
     * @param acceptableModels An array of acceptable devices to connect to.
     * @param timeout The timeout for the communication
     * @param retries The number of retries after a failed communication
     */
    public Device(InetAddress ip, Token token, String[] acceptableModels, int timeout, int retries) {
        this.ip = ip;
        this.token = token;
        this.acceptableModels = acceptableModels;
        if (timeout < 1) timeout = 1000;
        if (retries < 0) retries = 0;
        this.retries = retries;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
        } catch (SocketException ignored) {
        }

    }

    /**
     * Try to connect to a device or discover it.
     * @param broadcast The InetAddress to broadcast to if no ip was given
     * @return True if a device was found
     */
    private boolean hello(InetAddress broadcast) {
        if (socket == null) return false;
        Command hello = new Command();
        byte[] helloMsg = hello.create();
        DatagramPacket packet;
        if (ip == null){
            if (this.acceptableModels == null) return false;
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
        Response response = new Response(worker, null);
        if (!response.getToken().equals(new Token("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16))){
            token = response.getToken();
        } else if (token == null){
            return false;
        }
        if (!((response.getDeviceID() == -1) || (response.getTimeStamp() == -1))){
            deviceID = response.getDeviceID();
            timeStamp = response.getTimeStamp();
            if (this.acceptableModels != null){
                boolean modelOk = false;
                for (String s: this.acceptableModels) {
                    try {
                        if (s.equals(model())) modelOk = true;
                    } catch (CommandExecutionException ignored) {
                    }
                }
                return modelOk;
            }
            return true;
        }
        return false;
    }

    /**
     * Connect to a device and send a Hello message. If no IP has been specified, this will try do discover a device on the network.
     * @return True if the device has been successfully acquired.
     */
    public boolean discover(){
        boolean helloResponse = false;
        for (int helloRetries = this.retries; helloRetries >= 0; helloRetries--) {
            List<InetAddress> broadcast = listAllBroadcastAddresses();
            if (broadcast == null) return false;
            for (InetAddress i : broadcast) {
                if (hello(i)) {
                    helloResponse = true;
                    break;
                }
            }
            if (helloResponse) break;
        }
        if (helloResponse) {
            methodID = this.timeStamp & 0b1111111111111; // Possible collision about every 2 hours > acceptable
        }
        return helloResponse;
    }


    /**
     * Send a command to a device. If no IP has been specified, this will try do discover a device on the network.
     * @param method The method to execute on the device.
     * @param params The command to execute on the device. Must be a JSONArray or JSONObject.
     * @return The response from the device.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public Response send(String method, Object params) throws CommandExecutionException {
        return send(method, params, this.retries);
    }

    private Response send(String method, Object params, int sendRetries) throws CommandExecutionException {
        if (deviceID == -1 || timeStamp == -1 || token == null || ip == null) {
            if (!discover()) throw new CommandExecutionException(CommandExecutionException.Error.DEVICE_NOT_FOUND);
        }
        if (methodID >= 10000) methodID = 1;
        if (ip == null || token == null) throw new CommandExecutionException(CommandExecutionException.Error.IP_OR_TOKEN_UNKNOWN);
        if (socket == null) return null;
        timeStamp++;
        Command msg = new Command(this.token,this.deviceID,timeStamp,this.methodID,method,params);
        methodID++;
        byte[] binMsg = msg.create();

        DatagramPacket packet = new DatagramPacket(binMsg, binMsg.length, ip, PORT);
        try {
            socket.send(packet);
        } catch (SocketTimeoutException to){
            if (sendRetries > 0){
                sendRetries--;
                return send(method, params, sendRetries);
            }
            throw new CommandExecutionException(CommandExecutionException.Error.TIMEOUT);
        } catch (IOException e) {
            return null;
        }
        packet = new DatagramPacket(rcv, rcv.length);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException to){
            if (sendRetries > 0){
                sendRetries--;
                return send(method, params, sendRetries);
            }
            throw new CommandExecutionException(CommandExecutionException.Error.TIMEOUT);
        } catch (IOException e) {
            return null;
        }
        byte[] worker = new byte[2];
        System.arraycopy(rcv, 2, worker, 0, 2);
        int length = (int)ByteArray.fromBytes(worker);
        worker = new byte[length];
        System.arraycopy(rcv, 0, worker, 0, length);
        return parseResponse(worker, method, params, sendRetries);
    }

    private Response parseResponse(byte[] rawData, String method, Object params, int sendRetries) throws CommandExecutionException {
        Response response = new Response(rawData, this.token);
        if (!response.isValid()) {
            if (sendRetries > 0){
                sendRetries--;
                return send(method, params, sendRetries);
            }
            throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        }
        if (response.getPayloadID() != (methodID - 1)){
            if (sendRetries > 0){
                sendRetries--;
                return send(method, params, sendRetries);
            }
            throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        }
        if (!((response.getDeviceID() == -1) || (response.getTimeStamp() == -1))){
            if (response.getParams() == null) {
                if (sendRetries > 0){
                    sendRetries--;
                    return send(method, params, sendRetries);
                }
                throw new CommandExecutionException(CommandExecutionException.Error.EMPTY_RESPONSE);
            }
            if (response.getParams().getClass() == String.class){
                if (response.getParams().equals("unknown_method")) throw new CommandExecutionException((CommandExecutionException.Error.UNKNOWN_METHOD));
            }
            return response;
        }
        throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
    }

    private List<InetAddress> listAllBroadcastAddresses() {
        List<InetAddress> broadcastList = new ArrayList<>();
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

    /**
     * Send a command to a device. If no IP has been specified, this will try do discover a device on the network.
     * @param method The method to execute on the device.
     * @param params The command to execute on the device. Must be a JSONArray or JSONObject.
     * @return The response from the device as a JSONObject.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public JSONObject sendToObject(String method, Object params) throws CommandExecutionException {
        Response resp = send(method, params);
        if (resp == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        if (resp.getParams() == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        if (resp.getParams().getClass() != JSONObject.class) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return (JSONObject)resp.getParams();
    }

    /**
     * Send a command to a device without parameters. If no IP has been specified, this will try do discover a device on the network.
     * @param method The method to execute on the device.
     * @return The response from the device as a JSONObject.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public JSONObject sendToObject(String method) throws CommandExecutionException {
        return sendToObject(method, null);
    }

    /**
     * Send a command to a device. If no IP has been specified, this will try do discover a device on the network.
     * @param method The method to execute on the device.
     * @param params The command to execute on the device. Must be a JSONArray or JSONObject.
     * @return The response from the device as a JSONArray.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public JSONArray sendToArray(String method, Object params) throws CommandExecutionException {
        Response resp = send(method, params);
        if (resp == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        if (resp.getParams() == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        if (resp.getParams().getClass() != JSONArray.class) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return (JSONArray)resp.getParams();
    }

    /**
     * Send a command to a device without parameters. If no IP has been specified, this will try do discover a device on the network.
     * @param method The method to execute on the device.
     * @return The response from the device as a JSONArray.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public JSONArray sendToArray(String method) throws CommandExecutionException {
        return sendToArray(method, null);
    }

    /**
     * Send a command to a device. If no IP has been specified, this will try do discover a device on the network.
     * @param method The method to execute on the device.
     * @param params The command to execute on the device. Must be a JSONArray or JSONObject.
     * @return True if a ok was received from the device.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean sendOk(String method, Object params) throws CommandExecutionException {
        return sendToArray(method, params).optString(0).toLowerCase().equals("ok");
    }

    /**
     * Send a command to a device without parameters. If no IP has been specified, this will try do discover a device on the network.
     * @param method The method to execute on the device.
     * @return True if a ok was received from the device.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean sendOk(String method) throws CommandExecutionException {
        return sendOk(method, null);
    }

    /**
     * Get the device info from the device
     * @return The device info as a JSONObject
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public JSONObject info() throws CommandExecutionException {
        return sendToObject("miIO.info");
    }

    /**
     * Command the device to update
     * @param url The URL to update from
     * @param md5 The MD5 Checksum for the update
     * @return True if the command has been received. This does not mean that the update was successful.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean update(String url, String md5) throws CommandExecutionException {
        if (url == null || md5 == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        if (md5.length() != 32) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        JSONObject params = new JSONObject();
        params.put("mode","normal");
        params.put("install", "1");
        params.put("app_url", url);
        params.put("file_md5", md5);
        params.put("proc", "dnld install");
        return sendOk("miIO.ota", params);
    }

    /**
     * Request the update progress as a percentage value from 0 to 100
     * @return The current progress.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int updateProgress() throws CommandExecutionException {
        int resp = sendToArray("miIO.get_ota_progress").optInt(0, -1);
        if ((resp < 0) || (resp > 100)) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return resp;
    }

    /**
     * Request the update status.
     * @return The update status.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public String updateStatus() throws CommandExecutionException {
        String resp = sendToArray("miIO.get_ota_state").optString(0, null);
        if (resp == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return resp;
    }

    /**
     * Set the deviced network connection up.
     * @param ssid The SSID to device should connect to
     * @param password The password for that connection
     * @return True if the command was received successfully. This does not mean that the connection has been correctly established.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean configureRouter(String ssid, String password) throws CommandExecutionException {
        return configureRouter(ssid, password, 0);
    }

    public boolean configureRouter(String ssid, String password, int uid) throws CommandExecutionException {
        if (ssid == null || password == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        JSONObject params = new JSONObject();
        params.put("ssid",ssid);
        params.put("passwd", password);
        params.put("uid", uid);
        return sendOk("miIO.config_router", params);
    }


    /**
     * Get the devices model id.
     * @return The devices model id.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public String model() throws CommandExecutionException {
        JSONObject in = info();
        if (in == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return in.optString("model");
    }

    /**
     * Get the devices firmware version.
     * @return The devices current firmware version.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public String firmware() throws CommandExecutionException {
        JSONObject in = info();
        if (in == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return in.optString("fw_ver");
    }
}