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

package de.sg_o.app.miio.vacuum;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class VacuumStatusTest {
    private VacuumStatus s0 = new VacuumStatus(true, true, 10000, 60, 8, false,VacuumStatus.Error.NONE,VacuumStatus.State.IDLE, 100, 23, 0);
    private JSONObject o0 = new JSONObject("{\"dnd_enabled\":1,\"map_present\":1,\"clean_area\":540000,\"fan_power\":60,\"msg_ver\":8,\"in_cleaning\":0,\"error_code\":0,\"state\":8,\"battery\":100,\"msg_seq\":23,\"clean_time\":0}");
    private VacuumStatus s1 = new VacuumStatus(o0);
    private VacuumStatus s2 = new VacuumStatus(null);
    @Test
    public void constructTest() {
        assertEquals(10000, s0.construct().optInt("clean_area"));
        assertEquals(540000, s1.construct().optInt("clean_area"));
        assertEquals(0, s2.construct().optInt("clean_area", -1));
    }

    @Test
    public void isDndEnabledTest() {
        assertTrue(s0.isDndEnabled());
        assertTrue(s1.isDndEnabled());
        assertFalse(s2.isDndEnabled());

        s0.setDndEnabled(false);

        assertFalse(s0.isDndEnabled());
    }

    @Test
    public void isMapPresentTest() {
        assertTrue(s0.isMapPresent());
        assertTrue(s1.isMapPresent());
        assertFalse(s2.isMapPresent());

        s0.setMapPresent(false);

        assertFalse(s0.isMapPresent());
    }

    @Test
    public void getCleanAreaTest() {
        assertEquals(10000, s0.getCleanArea());
        assertEquals(540000, s1.getCleanArea());
        assertEquals(0, s2.getCleanArea());

        s0.setCleanArea(1000);

        assertEquals(1000, s0.getCleanArea());
    }

    @Test
    public void getFanPowerTest() {
        assertEquals(60, s0.getFanPower());
        assertEquals(60, s1.getFanPower());
        assertEquals(60, s2.getFanPower());

        s0.setFanPower(100);

        assertEquals(100, s0.getFanPower());
    }

    @Test
    public void getMsgVersionTest() {
        assertEquals(8, s0.getMsgVersion());
        assertEquals(8, s1.getMsgVersion());
        assertEquals(8, s2.getMsgVersion());

        s0.setMsgVersion(5);

        assertEquals(5, s0.getMsgVersion());
    }

    @Test
    public void isInCleaningTest() {
        assertFalse(s0.isInCleaning());
        assertFalse(s1.isInCleaning());
        assertFalse(s2.isInCleaning());

        s0.setInCleaning(true);

        assertTrue(s0.isInCleaning());
    }

    @Test
    public void getErrorCodeTest() {
        assertEquals(VacuumStatus.Error.NONE, s0.getErrorCode());
        assertEquals(VacuumStatus.Error.NONE, s1.getErrorCode());
        assertEquals(VacuumStatus.Error.UNKNOWN, s2.getErrorCode());

        s0.setErrorCode(VacuumStatus.Error.CLEAN_FILTER);

        assertEquals(VacuumStatus.Error.CLEAN_FILTER, s0.getErrorCode());
    }

    @Test
    public void getStateTest() {
        assertEquals(VacuumStatus.State.IDLE, s0.getState());
        assertEquals(VacuumStatus.State.CHARGING, s1.getState());
        assertEquals(VacuumStatus.State.UNKNOWN, s2.getState());

        s0.setState(VacuumStatus.State.CLEANING);

        assertEquals(VacuumStatus.State.CLEANING, s0.getState());
    }

    @Test
    public void getBatteryTest() {
        assertEquals(100, s0.getBattery());
        assertEquals(100, s1.getBattery());
        assertEquals(100, s2.getBattery());

        s0.setBattery(20);

        assertEquals(20, s0.getBattery());
    }

    @Test
    public void getMsgSeqTest() {
        assertEquals(23, s0.getMsgSeq());
        assertEquals(23, s1.getMsgSeq());
        assertEquals(0, s2.getMsgSeq());

        s0.setMsgSeq(100);

        assertEquals(100, s0.getMsgSeq());
    }

    @Test
    public void getCleanTimeTest() {
        assertEquals(0, s0.getCleanTime());
        assertEquals(0, s1.getCleanTime());
        assertEquals(0, s2.getCleanTime());

        s0.setCleanTime(500);

        assertEquals(500, s0.getCleanTime());
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "ObjectEqualsNull"})
    @Test
    public void equalsTest() {
        VacuumStatus s3 = new VacuumStatus(s0.construct());
        assertEquals(s0, s3);
        assertNotEquals(s0, s1);
        assertNotEquals(s0, s2);

        assertFalse(s0.equals(null));
        assertFalse(s0.equals(new Object()));
    }

    @Test
    public void hashCodeTest() {
        VacuumStatus s3 = new VacuumStatus(s0.construct());
        assertEquals(s0.hashCode(), s3.hashCode());
        assertNotEquals(s0.hashCode(), s1.hashCode());
        assertNotEquals(s0.hashCode(), s2.hashCode());

        assertNotEquals(new Object().hashCode(), s0.hashCode());
    }

    @Test
    public void toStringTest() {
        assertEquals("VacuumStatus{dndEnabled=true, mapPresent=true, cleanArea=10000, fanPower=60, msgVersion=8, inCleaning=false, errorCode=NONE, state=IDLE, battery=100, msgSeq=23, cleanTime=0}", s0.toString());
        assertEquals("VacuumStatus{dndEnabled=true, mapPresent=true, cleanArea=540000, fanPower=60, msgVersion=8, inCleaning=false, errorCode=NONE, state=CHARGING, battery=100, msgSeq=23, cleanTime=0}", s1.toString());
        assertEquals("VacuumStatus{dndEnabled=false, mapPresent=false, cleanArea=0, fanPower=60, msgVersion=8, inCleaning=false, errorCode=UNKNOWN, state=UNKNOWN, battery=100, msgSeq=0, cleanTime=0}", s2.toString());
    }
}