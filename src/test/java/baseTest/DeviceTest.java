package baseTest;

import org.json.JSONObject;
import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.*;

public class DeviceTest {

    @Test //
    public void sendTest() throws Exception {
        Token tk = new Token("00112233445566778899AABBCCDDEEFF", 16);
        TestServer ts = new TestServer(tk);
        ts.start();

        Device d0 = new Device(InetAddress.getByName("127.0.0.1"), tk);
        assertNull(d0.info());
        assertEquals("", d0.model());
        assertEquals("", d0.firmware());
        assertTrue(d0.update("127.0.0.1", "6cd9eb1aee36e091974f259ea81621fa"));
        assertEquals(0, d0.updateProgress());
        assertEquals("ok", d0.updateStatus());
        assertTrue(d0.configureRouter("ABC", "123"));
        ts.terminate();
    }
}