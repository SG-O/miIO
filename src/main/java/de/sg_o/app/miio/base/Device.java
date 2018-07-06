/*
 * Copyright (c) 2018 Joerg Bayer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg_o.app.miio.base;

import de.sg_o.app.miio.base.messages.Command;
import de.sg_o.app.miio.base.messages.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import de.sg_o.app.miio.util.ByteArray;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class Device implements Serializable {
    private static final int PORT = 54321;
    private static final long serialVersionUID = -924264471464948810L;
    private static byte[] rcv = new byte[65507];

    private InetAddress ip;
    private Token token;
    private int retries;
    private String[] acceptableModels;

    private transient DatagramSocket socket;

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
     * @return The IP address of the device.
     */
    public InetAddress getIp() {
        return ip;
    }

    /**
     * @return The token of the device.
     */
    public Token getToken() {
        return token;
    }

    /**
     * @return The number of retries when discovering a device or sending a command.
     */
    public int getRetries() {
        return retries;
    }

    /**
     * @return The model strings this device must have.
     */
    public String[] getAcceptableModels() {
        return acceptableModels;
    }

    /**
     * @return The timeout for the communication to fail.
     */
    public int getTimeout() {
        try {
            return socket.getSoTimeout();
        } catch (Exception e) {
            return 0;
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
        Response response;
        try {
            response = new Response(worker, null);
        } catch (CommandExecutionException e) {
            return false;
        }
        if (token == null){
            if (!(response.getToken().equals(new Token("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16)) || response.getToken().equals(new Token("00000000000000000000000000000000",16)))) {
                token = response.getToken();
            } else {
                return false;
            }
        }
        if (!((response.getDeviceID() == -1) || (response.getTimeStamp() == -1))){
            deviceID = response.getDeviceID();
            timeStamp = response.getTimeStamp();
            methodID = timeStamp & 0b1111111111111; // Possible collision about every 2 hours > acceptable
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
        if (deviceID == -1 || timeStamp == -1 || token == null || ip == null) {
            if (!discover()) throw new CommandExecutionException(CommandExecutionException.Error.DEVICE_NOT_FOUND);
        }
        if (methodID >= 10000) methodID = 1;
        if (ip == null || token == null) throw new CommandExecutionException(CommandExecutionException.Error.IP_OR_TOKEN_UNKNOWN);
        if (socket == null) return null;
        timeStamp++;
        Command msg = new Command(this.token,this.deviceID,timeStamp,this.methodID,method,params);
        methodID++;
        int retriesLeft = this.retries;
        while (true) {
            try {
                return parseResponse(send(msg.create()));
            } catch (CommandExecutionException e) {
                if (retriesLeft > 0){
                    retriesLeft--;
                    continue;
                }
                throw e;
            }
        }
    }

    /**
     * Send an arbitrary string as payload to the device.
     * @param payload The string to send.
     * @return The response of the device as an unparsed string.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public String send(String payload) throws CommandExecutionException {
        if (payload == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
        if (deviceID == -1 || timeStamp == -1 || token == null || ip == null) {
            if (!discover()) throw new CommandExecutionException(CommandExecutionException.Error.DEVICE_NOT_FOUND);
        }
        if (methodID >= 10000) methodID = 1;
        if (ip == null || token == null) throw new CommandExecutionException(CommandExecutionException.Error.IP_OR_TOKEN_UNKNOWN);
        if (socket == null) return null;
        timeStamp++;
        Command msg = new Command(this.token,this.deviceID,timeStamp,this.methodID,"", null);
        methodID++;
        int retriesLeft = this.retries;
        while (true) {
            try {
                byte[] resp = send(msg.create(payload));
                if (!Response.testMessage(resp, this.token)) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
                if (resp.length > 0x20) {
                    byte[] pl = new byte[resp.length - 0x20];
                    System.arraycopy(resp, 0x20, pl, 0, pl.length);
                    String payloadString = Response.decryptPayload(pl, this.token);
                    if (payloadString == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
                    return payloadString;
                }
            } catch (CommandExecutionException e) {
                if (retriesLeft > 0){
                    retriesLeft--;
                    continue;
                }
                throw e;
            }
        }
    }

    private byte[] send(byte[] binMsg) throws CommandExecutionException {
        DatagramPacket packet = new DatagramPacket(binMsg, binMsg.length, ip, PORT);
        try {
            socket.send(packet);
        } catch (SocketTimeoutException to){
            throw new CommandExecutionException(CommandExecutionException.Error.TIMEOUT);
        } catch (IOException e) {
            return null;
        }
        packet = new DatagramPacket(rcv, rcv.length);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException to){
            throw new CommandExecutionException(CommandExecutionException.Error.TIMEOUT);
        } catch (IOException e) {
            return null;
        }
        byte[] worker = new byte[2];
        System.arraycopy(rcv, 2, worker, 0, 2);
        int length = (int)ByteArray.fromBytes(worker);
        worker = new byte[length];
        System.arraycopy(rcv, 0, worker, 0, length);
        return worker;
    }

    private Response parseResponse(byte[] rawData) throws CommandExecutionException {
        Response response = new Response(rawData, this.token);
        if (!response.isValid()) {
            throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        }
        if (response.getPayloadID() != (methodID - 1)){
            throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        }
        if (!((response.getDeviceID() == -1) || (response.getTimeStamp() == -1))){
            if (response.getParams() == null) {
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

            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if (address == null) continue;
                InetAddress broadcast = address.getBroadcast();
                if (broadcast != null) {
                    broadcastList.add(broadcast);
                }
            }
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
        if (url == null || md5 == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
        if (md5.length() != 32) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
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
        if (ssid == null || password == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(socket.getSoTimeout());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        socket = new DatagramSocket();
        socket.setSoTimeout(in.readInt());
    }
}