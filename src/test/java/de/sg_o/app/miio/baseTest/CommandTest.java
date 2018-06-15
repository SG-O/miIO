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

package de.sg_o.app.miio.baseTest;

import de.sg_o.app.miio.base.Token;
import de.sg_o.app.miio.base.messages.Command;
import org.json.JSONArray;
import org.junit.Test;
import de.sg_o.app.miio.util.ByteArray;

import static org.junit.Assert.*;

public class CommandTest {
    private int[] arV = {1234567890};
    private JSONArray ar = new JSONArray(arV);

    private Command msg0 = new Command();
    private Command msgHello = new Command(msg0.create(), null);
    private Command msg1 = new Command(new Token("000102030405060708090A0B0C0D0E0F", 0),0x01234567,0x5b00bfac,23,"get_status",null);
    private Command msg2 = new Command(msg1.create(), msg1.getToken());
    private Command msg3 = new Command(new Token("100102030405060708090A0B0C0D0E0F", 0),0x01234568,0x5b00bfad,24,"get_clean_record",ar);
    private Command msg4 = new Command(msg3.create(), msg1.getToken());

    @Test
    public void getTokenTest() {
        assertEquals(new Token("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16), msg0.getToken());
        assertEquals(new Token("000102030405060708090A0B0C0D0E0F", 0), msg1.getToken());
        assertEquals(msg1.getToken(), msg2.getToken());
        assertEquals(new Token("100102030405060708090A0B0C0D0E0F", 0), msg3.getToken());
        assertEquals(msg1.getToken(), msg4.getToken());
        assertNotEquals(msg3.getToken(), msg4.getToken());
    }

    @Test
    public void isHelloTest() {
        assertTrue(msg0.isHello());
        assertTrue(msgHello.isHello());
        assertEquals("21310020FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", ByteArray.bytesToHex(msg0.create()));
        assertFalse(msg1.isHello());
        assertFalse(msg2.isHello());
        assertFalse(msg3.isHello());
        assertFalse(msg4.isHello());
    }

    @Test
    public void isValidTest() {
        assertTrue(msg0.isValid());
        assertTrue(msgHello.isValid());
        assertTrue(msg1.isValid());
        assertTrue(msg2.isValid());
        assertTrue(msg3.isValid());
        assertFalse(msg4.isValid());
    }

    @Test
    public void getDeviceIDTest() {
        assertEquals(-1,msg0.getDeviceID());
        assertEquals(0x01234567,msg1.getDeviceID());
        assertEquals(msg1.getDeviceID(),msg2.getDeviceID());
        assertEquals(0x01234568,msg3.getDeviceID());
        assertEquals(0,msg4.getDeviceID());
    }

    @Test
    public void getTimeStampTest() {
        assertEquals(-1,msg0.getTimeStamp());
        assertEquals(0x5b00bfac,msg1.getTimeStamp());
        assertEquals(msg1.getTimeStamp(),msg2.getTimeStamp());
        assertEquals(0x5b00bfad,msg3.getTimeStamp());
        assertEquals(0,msg4.getTimeStamp());
    }

    @Test
    public void getMethodIDTest() {
        assertEquals(0,msg0.getPayloadID());
        assertEquals(23,msg1.getPayloadID());
        assertEquals(msg1.getPayloadID(),msg2.getPayloadID());
        assertEquals(24,msg3.getPayloadID());
        assertEquals(0,msg4.getPayloadID());
    }

    @Test
    public void getMethodTest() {
        assertNull(msg0.getMethod());
        assertEquals("get_status",msg1.getMethod());
        assertEquals(msg1.getMethod(),msg2.getMethod());
        assertEquals("get_clean_record",msg3.getMethod());
        assertNull(msg4.getMethod());
    }

    @Test
    public void getParamsTest() {
        Command msg5 = new Command(msg3.create(), msg3.getToken());

        assertNull(msg0.getParams());
        assertNull(msg1.getParams());
        assertEquals("[]", msg2.getParams().toString());

        assertEquals(ar.length(),((JSONArray)msg3.getParams()).length());
        assertEquals(ar.get(0),((JSONArray)msg3.getParams()).get(0));

        assertNull(msg4.getParams());

        assertEquals(((JSONArray)msg3.getParams()).length(),((JSONArray)msg5.getParams()).length());
        assertEquals(((JSONArray)msg3.getParams()).get(0),((JSONArray)msg5.getParams()).get(0));
    }

    @Test
    public void invalidMessageTest() {
        Command msg5 = new Command(null, new Token("000102030405060708090A0B0C0D0E0F", 16));
        byte[] iM0 = {0,0,0,0};
        Command msg6 = new Command(iM0, new Token("000102030405060708090A0B0C0D0E0F", 16));
        byte[] iM1 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Command msg7 = new Command(iM1, new Token("000102030405060708090A0B0C0D0E0F", 16));
        byte[] iM2 = {0x21,0x31,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Command msg8 = new Command(iM2, new Token("000102030405060708090A0B0C0D0E0F", 16));

        assertFalse(msg5.isValid());
        assertFalse(msg6.isValid());
        assertFalse(msg7.isValid());
        assertFalse(msg8.isValid());
    }

    @Test
    public void messageDecryptTest() {
        assertNull(Command.decryptPayload(new byte[]{0, 1}, null));
        assertNull(Command.decryptPayload(null, new Token("000102030405060708090A0B0C0D0E0F", 16)));
    }
}