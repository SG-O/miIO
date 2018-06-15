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

package de.sg_o.app.miio.base.messages;

import de.sg_o.app.miio.base.Token;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;

public class Command extends Message {
    private String method;
    private Object params;

    public Command(Token token, int deviceID, int timeStamp, long payloadID, String method, Object params) {
        super(token, Message.NORMAL_UNKNOWN, deviceID, timeStamp, payloadID);
        if (method == null) method = "";
        this.method = method;
        this.params = params;
    }

    public Command(byte[] message, Token token) {
        super(message, token);
        if (super.isValid()){
            if (message.length > 0x20){
                byte[] payload = new byte[message.length - 0x20];
                System.arraycopy(message, 0x20, payload,0, payload.length);
                payload = super.getToken().decrypt(payload);
                if (payload != null){
                    Charset charset = Charset.forName("ISO-8859-1");
                    int i;
                    //noinspection StatementWithEmptyBody
                    for (i = 0; i < payload.length && payload[i] != 0; i++) { }
                    String pl = new String(payload, 0, i, charset);
                    JSONObject ob = new JSONObject(pl);
                    this.method = ob.optString("method", null);
                    this.params = ob.optJSONObject("params");
                    if (this.params == null) this.params = ob.optJSONArray("params");
                }
            }
        }
    }

    public Command(){
        super(null, Message.HELLO_UNKNOWN, Message.HELLO_DEVICE_ID, Message.HELLO_TIME_STAMP, 0);
        method = null;
        params = null;
    }

    public String getMethod() {
        return method;
    }

    public Object getParams() {
        return params;
    }

    private String constructPayload(){
        JSONObject out = new JSONObject();
        out.put("id", super.getPayloadID());
        if (method == null) return null;
        out.put("method", method);
        if (params != null) {
            if (params.getClass() == JSONObject.class) {
                JSONObject paramsObj = (JSONObject) params;
                if (paramsObj.length() > 0) {
                    out.put("params", paramsObj);
                }
            }
            if (params.getClass() == JSONArray.class) {
                JSONArray paramsObj = (JSONArray) params;
                out.put("params", paramsObj);
            }
        } else {
            out.put("params", new JSONArray());
        }
        return out.toString();
    }

    public byte[] create(){
        if (super.isHello()){
            return super.create(null);
        }
        return super.create(constructPayload());
    }
}
