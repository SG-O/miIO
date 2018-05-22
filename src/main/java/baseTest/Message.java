package baseTest;

import org.json.JSONArray;
import org.json.JSONObject;
import util.ByteArray;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
public class Message {
    private static final short HEADER = 0x2131;
    private static final int HELLO_UNKNOWN = ByteArray.UNSIGNED_FFFFFFFF;
    private static final int NORMAL_UNKNOWN = 0;

    private Token token;
    private int unknownHeader;
    private int deviceID;
    private int timeStamp;

    private long methodID;
    private String method;
    private JSONArray params;

    public Message(Token token, int deviceID, int timeStamp, long methodID, String method, JSONArray params) {
        if (token == null) {
            this.token = new Token("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16);
            this.unknownHeader = HELLO_UNKNOWN;
            if (deviceID <= 0 || timeStamp <= 0) {
                deviceID = ByteArray.UNSIGNED_FFFFFFFF;
                timeStamp = ByteArray.UNSIGNED_FFFFFFFF;
            }
        } else {
            this.unknownHeader = NORMAL_UNKNOWN;
            this.token = token;
        }
        this.deviceID = deviceID;
        this.timeStamp = timeStamp;
        this.methodID = methodID;
        if (method == null) method = "";
        this.method = method;
        if (params == null) params = new JSONArray();
        this.params = params;
    }

    public Message(byte[] message, Token token) {
        if (token == null) {
            this.token = new Token("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16);
        } else {
            this.token = token;
        }
        if (!testMessage(message)){
            initialEmpty();
        } else {
            byte[] worker = new byte[4];
            System.arraycopy(message, 4, worker, 0, 4);
            this.unknownHeader = (int) ByteArray.fromBytes(worker);

            System.arraycopy(message, 8, worker, 0, 4);
            this.deviceID = (int) ByteArray.fromBytes(worker);

            System.arraycopy(message, 12, worker, 0, 4);
            this.timeStamp = (int) ByteArray.fromBytes(worker);

            if (this.unknownHeader == HELLO_UNKNOWN){
                worker = new byte[16];
                System.arraycopy(message, 16, worker,0, 16);
                this.token = new Token(worker);
            }

            if (message.length > 0x20){
                byte[] payload = new byte[message.length - 0x20];
                System.arraycopy(message, 0x20, payload,0, payload.length);
                payload = this.token.decrypt(payload);
                if (payload != null){
                    Charset charset = Charset.forName("ISO-8859-1");
                    int i;
                    //noinspection StatementWithEmptyBody
                    for (i = 0; i < payload.length && payload[i] != 0; i++) { }
                    String pl = new String(payload, 0, i, charset);
                    JSONObject ob = new JSONObject(pl);
                    this.methodID = ob.optLong("id");
                    this.method = ob.optString("method");
                    this.params = ob.optJSONArray("params");
                    if (this.params == null) this.params = ob.optJSONArray("result");
                    if (this.params == null) {
                        this.params = new JSONArray();
                        JSONObject result = ob.optJSONObject("result");
                        if (result != null) this.params.put(result);
                    }
                }
            }
        }
    }

    private boolean testMessage(byte[] message) {
        if (message == null) {
            return false;
        } else {
            if (message.length < 0x20){
                return false;
            } else {
                byte[] worker = new byte[2];
                System.arraycopy(message, 0, worker, 0, 2);
                if (((short)ByteArray.fromBytes(worker)) != HEADER){
                    return false;
                } else {
                    System.arraycopy(message, 2, worker, 0, 2);
                    if (((int)ByteArray.fromBytes(worker)) != message.length){
                        return false;
                    } else {
                        worker = new byte[4];
                        System.arraycopy(message, 4, worker, 0, 4);
                        int ukHeader = (int) ByteArray.fromBytes(worker);

                        if (ukHeader != HELLO_UNKNOWN && message.length != 0x20) {
                            byte[] md5 = new byte[16];
                            System.arraycopy(message, 16, md5, 0, 16);
                            System.arraycopy(token.getToken(), 0, message, 16, 16);
                            MessageDigest md;
                            try {
                                md = MessageDigest.getInstance("MD5");
                            } catch (NoSuchAlgorithmException e) {
                                return false;
                            }
                            md.update(message);
                            return Arrays.equals(md.digest(),md5);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
    }

    private void initialEmpty(){
        this.unknownHeader = NORMAL_UNKNOWN;
        deviceID = ByteArray.UNSIGNED_FFFFFFFF;
        timeStamp = ByteArray.UNSIGNED_FFFFFFFF;
        method = "";
        params = new JSONArray();
    }

    public Token getToken() {
        return token;
    }

    public boolean isHello(){
        return this.unknownHeader == HELLO_UNKNOWN;
    }

    public int getDeviceID() {
        return deviceID;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public long getMethodID() {
        return methodID;
    }

    public String getMethod() {
        return method;
    }

    public JSONArray getParams() {
        return params;
    }

    public String constructPayload(){
        StringBuilder builder = new StringBuilder();
        builder.append("{\"id\": ");
        builder.append(methodID);
        builder.append(", \"method\": \"");
        builder.append(method);
        builder.append("\"");
        if (params.length() > 0) {
            builder.append(", \"params\": ");
            if (params.length() == 1){
                JSONObject obj = params.optJSONObject(0);
                if (obj != null) {
                    builder.append(obj.toString());
                } else {
                    builder.append(params.toString());
                }
            } else {
                builder.append(params.toString());
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private byte[] getBytePayload(String payload){
        if (payload == null) return new byte[0];
        CharsetEncoder enc = Charset.forName("ISO-8859-1").newEncoder();
        int len = payload.length();
        byte b[] = new byte[len + 1];
        ByteBuffer buf = ByteBuffer.wrap(b);
        enc.encode(CharBuffer.wrap(payload), buf, true);
        b[len] = 0;
        return b;
    }

    private byte[] getPayload(String payload){
        return this.token.encrypt(getBytePayload(payload));
    }

    public byte[] create() {
        String payload;
        if (isHello()) {
            payload = null;
        } else {
            payload = constructPayload();
        }
        return create(payload);
    }

    public byte[] create(String pl) {
        byte[] payload = new byte[0];
        if (pl != null) {
            payload = getPayload(pl);
        }
        byte[] msg = new byte[32 + payload.length];
        short length = (short)msg.length;
        System.arraycopy(ByteArray.toBytes(HEADER,2),0,msg,0,2);
        System.arraycopy(ByteArray.toBytes(length,2),0,msg,2,2);
        System.arraycopy(ByteArray.toBytes(unknownHeader,4),0,msg,4,4);
        System.arraycopy(ByteArray.toBytes(deviceID,4),0,msg,8,4);
        System.arraycopy(ByteArray.toBytes(timeStamp,4),0,msg,12,4);
        System.arraycopy(token.getToken(),0,msg,16,16);
        System.arraycopy(payload,0,msg,0x20,payload.length);
        if(!isHello()) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
            md.update(msg);
            System.arraycopy(md.digest(), 0, msg, 16, 16);
        }
        return msg;
    }
}
