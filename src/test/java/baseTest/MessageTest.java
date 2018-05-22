package baseTest;

import org.json.JSONArray;
import org.junit.Test;
import util.ByteArray;

import static org.junit.Assert.*;

public class MessageTest {
    private int[] arV = {1234567890};
    private JSONArray ar = new JSONArray(arV);

    private Message msg0 = new Message(null,0,0,0,null, null);
    private Message msgHello = new Message(msg0.create(), null);
    private Message msg1 = new Message(new Token("000102030405060708090A0B0C0D0E0F", 0),0x01234567,0x5b00bfac,23,"get_status",null);
    private Message msg2 = new Message(msg1.create(), msg1.getToken());
    private Message msg3 = new Message(new Token("100102030405060708090A0B0C0D0E0F", 0),0x01234568,0x5b00bfad,24,"get_clean_record",ar);
    private Message msg4 = new Message(msg3.create(), msg1.getToken());

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
    public void getDeviceIDTest() {
        assertEquals(-1,msg0.getDeviceID());
        assertEquals(0x01234567,msg1.getDeviceID());
        assertEquals(msg1.getDeviceID(),msg2.getDeviceID());
        assertEquals(0x01234568,msg3.getDeviceID());
        assertEquals(-1,msg4.getDeviceID());
    }

    @Test
    public void getTimeStampTest() {
        assertEquals(-1,msg0.getTimeStamp());
        assertEquals(0x5b00bfac,msg1.getTimeStamp());
        assertEquals(msg1.getTimeStamp(),msg2.getTimeStamp());
        assertEquals(0x5b00bfad,msg3.getTimeStamp());
        assertEquals(-1,msg4.getTimeStamp());
    }

    @Test
    public void getMethodIDTest() {
        assertEquals(0,msg0.getMethodID());
        assertEquals(23,msg1.getMethodID());
        assertEquals(msg1.getMethodID(),msg2.getMethodID());
        assertEquals(24,msg3.getMethodID());
        assertEquals(0,msg4.getMethodID());
    }

    @Test
    public void getMethodTest() {
        assertEquals("",msg0.getMethod());
        assertEquals("get_status",msg1.getMethod());
        assertEquals(msg1.getMethod(),msg2.getMethod());
        assertEquals("get_clean_record",msg3.getMethod());
        assertEquals("",msg4.getMethod());
    }

    @Test
    public void getParamsTest() {
        Message msg5 = new Message(msg3.create(), msg3.getToken());

        assertEquals(0,msg0.getParams().length());
        assertEquals(0,msg1.getParams().length());
        assertEquals(msg1.getParams().length(),msg2.getParams().length());

        assertEquals(ar.length(),msg3.getParams().length());
        assertEquals(ar.get(0),msg3.getParams().get(0));

        assertEquals(0,msg4.getParams().length());

        assertEquals(msg3.getParams().length(),msg5.getParams().length());
        assertEquals(msg3.getParams().get(0),msg5.getParams().get(0));
    }

    @Test
    public void invalidMessageTest() {
        Message msg5 = new Message(null, new Token("000102030405060708090A0B0C0D0E0F", 16));
        byte[] iM0 = {0,0,0,0};
        Message msg6 = new Message(iM0, new Token("000102030405060708090A0B0C0D0E0F", 16));
        byte[] iM1 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Message msg7 = new Message(iM1, new Token("000102030405060708090A0B0C0D0E0F", 16));
        byte[] iM2 = {0x21,0x31,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Message msg8 = new Message(iM2, new Token("000102030405060708090A0B0C0D0E0F", 16));

        assertEquals(-1,msg5.getDeviceID());
        assertEquals(-1,msg6.getDeviceID());
        assertEquals(-1,msg7.getDeviceID());
        assertEquals(-1,msg8.getDeviceID());
    }
}