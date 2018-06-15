package de.sg_o.app.miio.yeelightTest;

import de.sg_o.app.miio.base.Token;
import de.sg_o.app.miio.server.Server;
import de.sg_o.app.miio.serverTest.ServerYeelightColorEvents;
import de.sg_o.app.miio.yeelight.ColorLight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.awt.*;
import java.net.InetAddress;

public class ColorLightTest {
    private Server ts1;
    private ColorLight d0;

    @Before
    public void setUp() throws Exception {
        ServerYeelightColorEvents evLight = new ServerYeelightColorEvents();
        Token tk = new Token("00112233445566778899AABBCCDDEEFF", 16);
        ts1 = new Server(tk,1234,"yeelink.light.color1", "3.3.9_003194", null, null, null, 1000, null);
        ts1.registerOnServerEventListener(evLight);
        ts1.start();
        d0 = new ColorLight(InetAddress.getByName("127.0.0.1"), tk, 0, 2);
    }

    @After
    public void tearDown() {
        ts1.terminate();
    }

    @Test
    public void powerTest() throws Exception {
        assertFalse(d0.isOn());
        assertTrue(d0.setPower(true, false, 0));
        assertTrue(d0.isOn());
        assertTrue(d0.togglePower());
        assertFalse(d0.isOn());
    }

    @Test
    public void brightnessTest() throws Exception {
        assertEquals(100, d0.getBrightness());
        assertTrue(d0.setBrightness(50, false, 0));
        assertEquals(50, d0.getBrightness());
    }

    @Test
    public void modeTest() throws Exception {
        assertEquals(2, d0.getDeviceMode());
        assertTrue(d0.setRGB(Color.ORANGE, false, 0));
        assertEquals(1, d0.getDeviceMode());
        assertTrue(d0.setHSV(0, 100, false, 0));
        assertEquals(3, d0.getDeviceMode());
    }

    @Test
    public void colorTempTest() throws Exception {
        assertEquals(4000, d0.getColorTemperature());
        assertTrue(d0.setColorTemperature(6000, false, 0));
        assertEquals(6000, d0.getColorTemperature());
    }

    @Test
    public void rgbTest() throws Exception {
        assertEquals(Color.red, d0.getRGB());
        assertTrue(d0.setRGB(Color.ORANGE, false, 0));
        assertEquals(Color.orange, d0.getRGB());
    }

    @Test
    public void hsvTest() throws Exception {
        assertEquals(0, d0.getHue());
        assertEquals(100, d0.getSaturation());
        assertTrue(d0.setHSV(200, 50, false, 0));
        assertEquals(200, d0.getHue());
        assertEquals(50, d0.getSaturation());
    }

    @Test
    public void defaultTest() throws Exception {
        assertTrue(d0.setAsDefault());
    }

    @Test
    public void powerOffTimerTest() throws Exception {
        assertEquals(0, d0.getTimeUntilPowerOff());
        assertTrue(d0.powerOffAfterTime(10));
        assertEquals(10, d0.getTimeUntilPowerOff());
        assertTrue(d0.stopPowerOffAfterTime());
        assertEquals(0, d0.getTimeUntilPowerOff());
    }

    @Test
    public void nameTest() throws Exception {
        assertEquals("", d0.getName());
        assertTrue(d0.setName("Room"));
        assertEquals("Room", d0.getName());
    }
}
