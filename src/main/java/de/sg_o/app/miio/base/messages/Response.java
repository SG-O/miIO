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

public class Response extends Message{
    private Object params;

    public Response(Token token, int deviceID, int timeStamp, long payloadID, Object params) {
        super(token, Message.NORMAL_UNKNOWN, deviceID, timeStamp, payloadID);
        this.params = params;
    }

    public Response(byte[] message, Token token) {
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
                    this.params = ob.optJSONObject("result");
                    if (this.params == null) this.params = ob.optJSONArray("result");
                    if (this.params == null) this.params = ob.optString("result", null);
                }
            }
        }
    }

    public Response(Token token, int deviceID, int timeStamp){
        super(token, Message.NORMAL_UNKNOWN,deviceID, timeStamp, 0);
        params = null;
    }

    public Object getParams() {
        return params;
    }

    private String constructPayload(){
        JSONObject out = new JSONObject();
        out.put("id", super.getPayloadID());
        if (params != null) {
            if (params.getClass() == JSONObject.class) {
                JSONObject paramsObj = (JSONObject) params;
                out.put("result", paramsObj);
            }
            if (params.getClass() == JSONArray.class) {
                JSONArray paramsObj = (JSONArray) params;
                    out.put("result", paramsObj);
            }
            if (params.getClass() == String.class) {
                String paramsObj = (String) params;
                out.put("result", paramsObj);
            }
        }
        return out.toString();
    }

    @Override
    public boolean isHello() {
        return super.isHello() || params == null;
    }

    public byte[] create(){
        if (isHello()){
            return super.create(null);
        }
        return super.create(constructPayload());
    }
}
