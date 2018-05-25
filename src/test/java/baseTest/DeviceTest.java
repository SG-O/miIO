package baseTest;

import base.CommandExecutionException;
import base.Device;
import base.Token;
import device.vacuum.Vacuum;
import org.json.JSONObject;
import org.junit.Test;
import server.Server;
import serverTest.ServerGenericEvents;
import serverTest.ServerVacuumEvents;

import java.net.InetAddress;

import static base.CommandExecutionException.Error.DEVICE_NOT_FOUND;
import static base.CommandExecutionException.Error.UNKNOWN_METHOD;
import static org.junit.Assert.*;

public class DeviceTest {

    @Test //
    public void sendTest() throws Exception {

        Server ts0 = new Server(null,12345678,null, null, null,null, null, 10000, null);
        ServerGenericEvents ev = new ServerGenericEvents();
        ts0.registerOnServerEventListener(ev);
        ts0.start();

        Device d0 = new Device(InetAddress.getByName("127.0.0.1"), ts0.getTk(), null, 0, 2);
        assertEquals(ts0.getMacAddress(), d0.info().optString("mac"));
        assertEquals(ts0.getModel(), d0.model());
        assertEquals(ts0.getFirmware(), d0.firmware());
        assertTrue(d0.update("127.0.0.1", "6cd9eb1aee36e091974f259ea81621fa"));
        assertEquals(0, d0.updateProgress());
        assertEquals("downloading", d0.updateStatus());
        ev.setUpdateProgress(50);
        assertEquals(ev.getUpdateProgress(), d0.updateProgress());
        ev.setUpdateProgress(100);
        assertEquals(ev.getUpdateProgress(), d0.updateProgress());
        ev.setUpdateStatus(ServerGenericEvents.UpdateStatus.INSTALLING);
        assertEquals(ev.getUpdateStatus().status, d0.updateStatus());
        ev.setUpdateStatus(ServerGenericEvents.UpdateStatus.FAILED);
        assertEquals(ev.getUpdateStatus().status, d0.updateStatus());
        assertTrue(d0.configureRouter("ABC", "123"));
        ts0.terminate();

        ServerVacuumEvents evCleaner = new ServerVacuumEvents();
        Token tk = new Token("00112233445566778899AABBCCDDEEFF", 16);
        Server ts1 = new Server(tk,ts0.getDeviceId() * 2,"rockrobo.vacuum.v1", "3.3.9_003194", ts0.getHardware(),ts0.getNetwork(), ts0.getMacAddress(), ts0.getLifeTime() * 2, ts0.getAccessPoint());
        ts1.start();
        Vacuum d1 = new Vacuum(InetAddress.getByName("127.0.0.1"), tk, 0, 2);
        assertEquals(ts1.getMacAddress(), d1.info().optString("mac"));
        assertEquals(ts1.getModel(), d1.model());
        assertEquals(ts1.getFirmware(), d1.firmware());
        try {
            d1.update("127.0.0.1", "6cd9eb1aee36e091974f259ea81621fa");
        } catch (CommandExecutionException e){
            assertEquals(UNKNOWN_METHOD.cause, e.getError().cause);
        }
        ts1.registerOnServerEventListener(ev);
        ts1.registerOnServerEventListener(evCleaner);
        assertTrue(d1.update("127.0.0.1", "6cd9eb1aee36e091974f259ea81621fa"));
        ev.setUpdateProgress(0);
        assertEquals(0, d1.updateProgress());
        assertEquals("downloading", d1.updateStatus());
        ev.setUpdateProgress(50);
        assertEquals(ev.getUpdateProgress(), d1.updateProgress());
        ev.setUpdateProgress(100);
        assertEquals(ev.getUpdateProgress(), d1.updateProgress());
        ev.setUpdateStatus(ServerGenericEvents.UpdateStatus.INSTALLING);
        assertEquals(ev.getUpdateStatus().status, d1.updateStatus());
        ev.setUpdateStatus(ServerGenericEvents.UpdateStatus.FAILED);
        assertEquals(ev.getUpdateStatus().status, d1.updateStatus());
        assertTrue(d1.configureRouter("ABC", "123"));

        JSONObject network = ts1.getNetwork();
        JSONObject accessPoint = ts1.getAccessPoint();

        assertEquals("127.0.0.1", d1.info().optJSONObject("netif").optString("gw"));
        network.put("gw", "127.0.0.5");
        ts1.setNetwork(network);
        assertEquals("127.0.0.5", d1.info().optJSONObject("netif").optString("gw"));

        assertEquals("WLAN Router", d1.info().optJSONObject("ap").optString("ssid"));
        accessPoint.put("ssid", "New Router");
        ts1.setAccessPoint(accessPoint);
        assertEquals("New Router", d1.info().optJSONObject("ap").optString("ssid"));

        long lifetime = ts1.getLifeTime();
        assertEquals(lifetime, d1.info().optLong("life"));
        ts1.setLifeTime(ts1.getLifeTime() + 100);
        assertEquals(lifetime + 100, d1.info().optLong("life"));

        assertEquals(3, d1.status().optInt("state"));
        assertTrue(d1.start());
        assertEquals(5, d1.status().optInt("state"));
        assertTrue(d1.pause());
        assertEquals(10, d1.status().optInt("state"));
        assertTrue(d1.home());
        assertEquals(8, d1.status().optInt("state"));
        assertTrue(d1.spotCleaning());
        assertEquals(17, d1.status().optInt("state"));
        assertTrue(d1.stop());
        assertEquals(3, d1.status().optInt("state"));

        assertTrue(d1.findMe());

        assertEquals(60, d1.getFanSpeed());
        assertTrue(d1.setFanSpeed(100));
        assertEquals(100, d1.getFanSpeed());
        assertTrue(d1.setFanSpeed(60));
        assertEquals(60, d1.getFanSpeed());

        assertEquals(0, d1.consumableStatus().optInt("main_brush_work_time"));

        ts1.terminate();
    }

    @Test
    public void failTest() throws Exception {
        Token tk = new Token("00112233445566778899AABBCCDDEEFF", 16);
        Device d0 = new Device(InetAddress.getByName("127.0.0.1"), tk, null, 100, 1);
        try {
            d0.update("127.0.0.5", "6cd9eb1aee36e091974f259ea81621fa");
        } catch (CommandExecutionException e){
            assertEquals(DEVICE_NOT_FOUND.cause, e.getError().cause);
            assertEquals("DEVICE_NOT_FOUND", e.toString());
        }
        assertFalse(d0.discover());
    }
}