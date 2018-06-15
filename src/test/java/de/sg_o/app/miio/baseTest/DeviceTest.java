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

import de.sg_o.app.miio.base.CommandExecutionException;
import de.sg_o.app.miio.base.Device;
import de.sg_o.app.miio.base.Token;
import de.sg_o.app.miio.server.Server;
import de.sg_o.app.miio.serverTest.ServerGenericEvents;
import de.sg_o.app.miio.serverTest.ServerVacuumEvents;
import de.sg_o.app.miio.vacuum.*;
import org.json.JSONObject;
import org.junit.Test;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.ZoneId;

import static de.sg_o.app.miio.base.CommandExecutionException.Error.*;
import static org.junit.Assert.*;

public class DeviceTest {

    @Test //
    public void sendTest() throws Exception {

        Server ts0 = new Server(null,12345678,null, null, null,null, null, 10000, null);
        ServerGenericEvents ev = new ServerGenericEvents();
        ts0.registerOnServerEventListener(ev);
        ts0.start();

        Device d0 = new Device(InetAddress.getByName("127.0.0.1"), ts0.getTk(), null, 0, 2);
        assertEquals("127.0.0.1", d0.getIp().getHostAddress());
        assertEquals(ts0.getTk(), d0.getToken());
        assertNull(d0.getAcceptableModels());
        assertEquals(2, d0.getRetries());
        assertEquals(1000, d0.getTimeout());
        assertEquals(ts0.getMacAddress(), d0.info().optString("mac"));
        assertEquals("{\"result\":\"unknown_method\",\"id\":0}", d0.send("{\"method\":\"get_status\",\"id\":0}"));
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

        assertEquals(VacuumStatus.State.UNKNOWN, d1.status().getState());
        assertTrue(d1.start());
        assertEquals(VacuumStatus.State.CLEANING, d1.status().getState());
        assertTrue(d1.pause());
        assertEquals(VacuumStatus.State.PAUSED, d1.status().getState());
        assertTrue(d1.home());
        assertEquals(VacuumStatus.State.CHARGING, d1.status().getState());
        assertTrue(d1.spotCleaning());
        assertEquals(VacuumStatus.State.SPOT_CLEANUP, d1.status().getState());
        assertTrue(d1.stop());
        assertEquals(VacuumStatus.State.IDLE, d1.status().getState());

        assertTrue(d1.findMe());

        assertEquals(60, d1.getFanSpeed());
        assertTrue(d1.setFanSpeed(100));
        assertEquals(100, d1.getFanSpeed());
        assertTrue(d1.setFanSpeed(60));
        assertEquals(60, d1.getFanSpeed());

        assertEquals(0, d1.consumableStatus().getMainBrushWorkTime());
        evCleaner.getConsumables().setMainBrushWorkTime(10000);
        assertEquals(10000, d1.consumableStatus().getMainBrushWorkTime());
        assertTrue(d1.resetConsumable(VacuumConsumableStatus.Names.MAIN_BRUSH));
        assertEquals(0, d1.consumableStatus().getMainBrushWorkTime());

        assertEquals(ZoneId.systemDefault(), d1.getTimezone());
        assertTrue(d1.setTimezone(ZoneId.of("America/Los_Angeles")));
        assertEquals(ZoneId.of("America/Los_Angeles"), d1.getTimezone());
        assertTrue(d1.setTimezone(ZoneId.systemDefault()));
        assertEquals(ZoneId.systemDefault(), d1.getTimezone());

        VacuumTimer t0 = new VacuumTimer(null, true, 14, 0, null);
        assertEquals(0, d1.getTimers().length);
        assertTrue(d1.addTimer(t0));
        assertEquals(t0, d1.getTimers()[0]);
        t0.setEnabled(false);
        assertTrue(d1.setTimerEnabled(t0));
        assertFalse(d1.getTimers()[0].isEnabled());
        assertTrue(d1.removeTimer(t0));
        assertEquals(0, d1.getTimers().length);

        assertEquals(new VacuumDoNotDisturb(null, null), d1.getDoNotDisturb());
        LocalTime start = LocalTime.of(12, 30);
        LocalTime end = LocalTime.of(15, 30);
        assertTrue(d1.setDoNotDisturb(new VacuumDoNotDisturb(start, end)));
        assertEquals(new VacuumDoNotDisturb(start, end), d1.getDoNotDisturb());
        assertTrue(d1.getDoNotDisturb().isEnabled());
        assertTrue(d1.disableDoNotDisturb());
        assertFalse(d1.getDoNotDisturb().isEnabled());
        assertTrue(d1.setDoNotDisturb(new VacuumDoNotDisturb(null, null)));
        assertEquals(new VacuumDoNotDisturb(null, null), d1.getDoNotDisturb());

        assertTrue(d1.goTo(new Point()));

        assertTrue(d1.goTo(0.0f, 0.0f));
        assertTrue(d1.goToMapPosition(0, 0));

        assertTrue(d1.cleanArea(new Point(), new Point(), 1));
        assertTrue(d1.cleanArea(0.0f, 0.0f, 1.0f, 1.0f, 1));
        assertTrue(d1.cleanAreaFromMap(0,0,1,1,1));
        assertTrue(d1.cleanAreaFromMap(1,1,0,0,1));

        assertEquals(120000, d1.getTotalCleanedArea());
        assertEquals(900, d1.getTotalCleaningTime());
        assertEquals(6, d1.getTotalCleans());
        assertEquals(6, d1.getAllCleanups().length);
        assertTrue(d1.getAllCleanups()[0].isCompleted());

        assertEquals(90, d1.getSoundVolume());
        assertTrue(d1.setSoundVolume(50));
        assertEquals(50, d1.getSoundVolume());
        assertTrue(d1.setSoundVolume(-5));
        assertEquals(0, d1.getSoundVolume());
        assertTrue(d1.setSoundVolume(200));
        assertEquals(100, d1.getSoundVolume());
        assertTrue(d1.setSoundVolume(90));
        assertEquals(90, d1.getSoundVolume());
        assertTrue(d1.testSoundVolume());

        assertTrue(d1.manualControlStart());
        assertEquals(VacuumStatus.State.REMOTE_CONTROL, d1.status().getState());
        assertTrue(d1.manualControlMove(0.0f, 0.2f, -1));
        assertTrue(d1.manualControlStop());
        assertEquals(VacuumStatus.State.IDLE, d1.status().getState());
        assertTrue(d1.manualControlMove(300.0f, 0.4f, 500));
        assertEquals(VacuumStatus.State.REMOTE_CONTROL, d1.status().getState());
        assertTrue(d1.manualControlStop());
        assertEquals(VacuumStatus.State.IDLE, d1.status().getState());
        assertTrue(d1.manualControlMove(-300.0f, -0.4f, 500));
        assertEquals(VacuumStatus.State.REMOTE_CONTROL, d1.status().getState());
        assertTrue(d1.manualControlStop());
        assertEquals(VacuumStatus.State.IDLE, d1.status().getState());

        assertEquals(0, d1.soundpackInstallStatus().getProgress());
        assertEquals(0, d1.soundpackInstallStatus().getSid());
        assertEquals(-1, d1.installSoundpack("127.0.0.1", "6cd9eb1aee36e091974f259ea81621fa", 5).getSid());
        assertEquals(100, d1.soundpackInstallStatus().getProgress());
        assertEquals(5, d1.soundpackInstallStatus().getSid());

        assertEquals("{\"current_high\":500,\"stall_time\":10,\"current_low\":400,\"enable\":0,\"current_integral\":450}", d1.getCarpetModeState().toString());
        assertTrue(d1.setCarpetMode(true, 500, 400, 450, 10));
        assertEquals("{\"current_high\":500,\"stall_time\":10,\"current_low\":400,\"enable\":1,\"current_integral\":450}", d1.getCarpetModeState().toString());

        assertEquals("0000000000001", d1.getSerialnumber());

        ts1.terminate();
    }

    @Test
    public void failTest() throws Exception {
        Token tk = new Token("00112233445566778899AABBCCDDEEFF", 16);
        Vacuum d0 = new Vacuum(InetAddress.getByName("127.0.0.1"), tk, 100, 1);
        try {
            d0.update("127.0.0.5", "6cd9eb1aee36e091974f259ea81621fa");
            fail();
        } catch (CommandExecutionException e){
            assertEquals(DEVICE_NOT_FOUND.cause, e.getError().cause);
            assertEquals("DEVICE_NOT_FOUND: DEVICE_NOT_FOUND", e.toString());
        }
        try {
            d0.installSoundpack(null, "6cd9eb1aee36e091974f259ea81621fa", 5);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.installSoundpack("127.0.0.5", null, 5);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.goTo(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.cleanArea(null, new Point(), 1);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.cleanArea(new Point(), null, 1);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.cleanArea(new Point(), new Point(), 0);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.setTimezone(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.resetConsumable(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.setFanSpeed(500);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.setFanSpeed(-1);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.addTimer(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.setTimerEnabled(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.removeTimer(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.setDoNotDisturb(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        try {
            d0.send(null);
            fail();
        } catch (CommandExecutionException e){
            assertEquals(INVALID_PARAMETERS.cause, e.getError().cause);
        }
        assertFalse(d0.discover());
    }

    @Test
    public void serialisationTest() throws Exception {
        Server ts0 = new Server(null,12345678,null, null, null,null, null, 10000, null);
        ServerGenericEvents ev = new ServerGenericEvents();
        ts0.registerOnServerEventListener(ev);
        ts0.start();

        Device d0 = new Device(InetAddress.getByName("127.0.0.1"), ts0.getTk(), null, 0, 2);
        assertEquals(ts0.getMacAddress(), d0.info().optString("mac"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(d0);
        oos.flush();
        out.flush();
        byte[] serialized = out.toByteArray();
        oos.close();
        out.close();
        ByteArrayInputStream in = new ByteArrayInputStream(serialized);
        ObjectInputStream ois = new ObjectInputStream(in);
        Device serial0 = (Device) ois.readObject();
        ois.close();
        in.close();

        assertEquals(ts0.getMacAddress(), serial0.info().optString("mac"));

        ts0.terminate();

        ServerVacuumEvents evCleaner = new ServerVacuumEvents();
        Token tk = new Token("00112233445566778899AABBCCDDEEFF", 16);
        Server ts1 = new Server(tk,ts0.getDeviceId() * 2,"rockrobo.vacuum.v1", "3.3.9_003194", ts0.getHardware(),ts0.getNetwork(), ts0.getMacAddress(), ts0.getLifeTime() * 2, ts0.getAccessPoint());
        ts1.registerOnServerEventListener(ev);
        ts1.registerOnServerEventListener(evCleaner);
        ts1.start();
        Vacuum d1 = new Vacuum(InetAddress.getByName("127.0.0.1"), tk, 0, 2);
        assertEquals(60, d1.getFanSpeed());
        assertTrue(d1.setFanSpeed(100));
        assertEquals(100, d1.getFanSpeed());

        out = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(out);
        oos.writeObject(d1);
        oos.flush();
        out.flush();
        serialized = out.toByteArray();
        oos.close();
        out.close();
        in = new ByteArrayInputStream(serialized);
        ois = new ObjectInputStream(in);
        Vacuum serial1 = (Vacuum) ois.readObject();
        ois.close();
        in.close();

        assertEquals(100, serial1.getFanSpeed());
        assertTrue(d1.setFanSpeed(60));
        assertEquals(60, d1.getFanSpeed());

        ts1.terminate();
    }
}