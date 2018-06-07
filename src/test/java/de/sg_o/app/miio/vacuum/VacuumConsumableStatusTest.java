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

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class VacuumConsumableStatusTest {
    private VacuumConsumableStatus s0 = new VacuumConsumableStatus(540000, 72000, 180000, 180000);
    private JSONObject o0 = new JSONObject("{\"main_brush_work_time\":13980,\"sensor_dirty_time\":13980,\"side_brush_work_time\":13980,\"filter_work_time\":13980}");
    private VacuumConsumableStatus s1 = new VacuumConsumableStatus(o0);
    private VacuumConsumableStatus s2 = new VacuumConsumableStatus(null);

    @Test
    public void constructTest() {
        assertEquals(540000, s0.construct().optInt("main_brush_work_time"));
        assertEquals(13980, s1.construct().optInt("main_brush_work_time"));
        assertEquals(0, s2.construct().optInt("main_brush_work_time", -1));
    }

    @Test
    public void getMainBrushWorkTimeTest() {
        assertEquals(540000, s0.getMainBrushWorkTime());
        assertEquals(13980, s1.getMainBrushWorkTime());
        assertEquals(0, s2.getMainBrushWorkTime());

        assertEquals(540000, s0.getMainBrushWorkTimeLeft());
        assertEquals(1066020, s1.getMainBrushWorkTimeLeft());
        assertEquals(1080000, s2.getMainBrushWorkTimeLeft());

        assertEquals(50.0f, s0.getMainBrushWorkPercent(), 0.1f);
        assertEquals(1.3f, s1.getMainBrushWorkPercent(), 0.1f);
        assertEquals(0.0f, s2.getMainBrushWorkPercent(), 0.1f);

        s0.reset(VacuumConsumableStatus.Names.MAIN_BRUSH.toString());

        assertEquals(0, s0.getMainBrushWorkTime());

        s0.setMainBrushWorkTime(1000);

        assertEquals(1000, s0.getMainBrushWorkTime());
    }

    @Test
    public void getSensorTimeSinceCleaningTest() {
        assertEquals(72000, s0.getSensorTimeSinceCleaning());
        assertEquals(13980, s1.getSensorTimeSinceCleaning());
        assertEquals(0, s2.getSensorTimeSinceCleaning());

        assertEquals(36000, s0.getSensorTimeSinceCleaningLeft());
        assertEquals(94020, s1.getSensorTimeSinceCleaningLeft());
        assertEquals(108000, s2.getSensorTimeSinceCleaningLeft());

        assertEquals(66.6f, s0.getSensorPercentSinceCleaning(), 0.1f);
        assertEquals(13.0f, s1.getSensorPercentSinceCleaning(), 0.1f);
        assertEquals(0.0f, s2.getSensorPercentSinceCleaning(), 0.1f);

        s0.reset(VacuumConsumableStatus.Names.SENSOR.toString());

        assertEquals(0, s0.getSensorTimeSinceCleaning());

        s0.setSensorTimeSinceCleaning(2000);

        assertEquals(2000, s0.getSensorTimeSinceCleaning());
    }

    @Test
    public void getSideBrushWorkTimeTest() {
        assertEquals(180000, s0.getSideBrushWorkTime());
        assertEquals(13980, s1.getSideBrushWorkTime());
        assertEquals(0, s2.getSideBrushWorkTime());

        assertEquals(540000, s0.getSideBrushWorkTimeLeft());
        assertEquals(706020, s1.getSideBrushWorkTimeLeft());
        assertEquals(720000, s2.getSideBrushWorkTimeLeft());

        assertEquals(25.0f, s0.getSideBrushWorkPercent(), 0.1f);
        assertEquals(2.0f, s1.getSideBrushWorkPercent(), 0.1f);
        assertEquals(0.0f, s2.getSideBrushWorkPercent(), 0.1f);

        s0.reset(VacuumConsumableStatus.Names.SIDE_BRUSH.toString());

        assertEquals(0, s0.getSideBrushWorkTime());

        s0.setSideBrushWorkTime(3000);

        assertEquals(3000, s0.getSideBrushWorkTime());
    }

    @Test
    public void getFilterWorkTimeTest() {
        assertEquals(180000, s0.getFilterWorkTime());
        assertEquals(13980, s1.getFilterWorkTime());
        assertEquals(0, s2.getFilterWorkTime());

        assertEquals(360000, s0.getFilterWorkTimeLeft());
        assertEquals(526020, s1.getFilterWorkTimeLeft());
        assertEquals(540000, s2.getFilterWorkTimeLeft());

        assertEquals(33.3f, s0.getFilterWorkPercent(), 0.1f);
        assertEquals(2.6f, s1.getFilterWorkPercent(), 0.1f);
        assertEquals(0.0f, s2.getFilterWorkPercent(), 0.1f);

        s0.reset(VacuumConsumableStatus.Names.FILTER.toString());

        assertEquals(0, s0.getFilterWorkTime());

        s0.setFilterWorkTime(4000);

        assertEquals(4000, s0.getFilterWorkTime());
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "ObjectEqualsNull"})
    @Test
    public void equalsTest() {
        VacuumConsumableStatus s3 = new VacuumConsumableStatus(s0.construct());
        assertEquals(s0, s3);
        assertNotEquals(s0, s1);
        assertNotEquals(s0, s2);

        assertFalse(s0.equals(null));
        assertFalse(s0.equals(new Object()));
    }

    @Test
    public void hashCodeTest() {
        VacuumConsumableStatus s3 = new VacuumConsumableStatus(s0.construct());
        assertEquals(s0.hashCode(), s3.hashCode());
        assertNotEquals(s0.hashCode(), s1.hashCode());
        assertNotEquals(s0.hashCode(), s2.hashCode());

        assertNotEquals(new Object().hashCode(), s0.hashCode());
    }

    @Test
    public void toStringTest() {
        assertEquals("VacuumConsumableStatus{mainBrushWorkTime=540000, sensorTimeSinceCleaning=72000, sideBrushWorkTime=180000, filterWorkTime=180000}", s0.toString());
        assertEquals("VacuumConsumableStatus{mainBrushWorkTime=13980, sensorTimeSinceCleaning=13980, sideBrushWorkTime=13980, filterWorkTime=13980}", s1.toString());
        assertEquals("VacuumConsumableStatus{mainBrushWorkTime=0, sensorTimeSinceCleaning=0, sideBrushWorkTime=0, filterWorkTime=0}", s2.toString());
    }

    @Test
    public void serialisationTest() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(s0);
        oos.flush();
        out.flush();
        byte[] serialized = out.toByteArray();
        oos.close();
        out.close();
        ByteArrayInputStream in = new ByteArrayInputStream(serialized);
        ObjectInputStream ois = new ObjectInputStream(in);
        VacuumConsumableStatus serial = (VacuumConsumableStatus) ois.readObject();
        ois.close();
        in.close();
        assertEquals(s0, serial);
    }
}