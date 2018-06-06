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

import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.*;

public class VacuumDoNotDisturbTest {
    private LocalTime start = LocalTime.of(15, 30);
    private LocalTime startFb = LocalTime.of(22, 0);
    private LocalTime end = LocalTime.of(21, 30);
    private LocalTime endFb = LocalTime.of(8, 0);

    private VacuumDoNotDisturb d0 = new VacuumDoNotDisturb(start, end);
    private VacuumDoNotDisturb d1 = new VacuumDoNotDisturb(null, null);
    private VacuumDoNotDisturb d2 = new VacuumDoNotDisturb(start, end, true);
    private VacuumDoNotDisturb d3 = new VacuumDoNotDisturb(null, null, false);

    @Test
    public void getStartTest() {
        assertEquals(start, d0.getStart());
        assertEquals(startFb, d1.getStart());
        assertEquals(start, d2.getStart());
        assertEquals(startFb, d3.getStart());

    }

    @Test
    public void getEndTest() {
        assertEquals(end, d0.getEnd());
        assertEquals(endFb, d1.getEnd());
        assertEquals(end, d2.getEnd());
        assertEquals(endFb, d3.getEnd());
    }

    @Test
    public void isEnabledTest() {
        assertTrue(d0.isEnabled());
        assertTrue(d1.isEnabled());
        assertTrue(d2.isEnabled());
        assertFalse(d3.isEnabled());

        d0.setEnabled(false);

        assertFalse(d0.isEnabled());
    }

    @Test
    public void constructTest() {
        VacuumDoNotDisturb d4 = new VacuumDoNotDisturb(d0.construct());
        VacuumDoNotDisturb d5 = new VacuumDoNotDisturb(d0.construct(true));
        assertEquals(d0, d4);
        assertEquals(d0, d5);
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "ObjectEqualsNull"})
    @Test
    public void equalsTest() {
        assertTrue(d0.equals(d2));
        assertFalse(d0.equals(d1));
        assertFalse(d0.equals(d3));

        assertFalse(d0.equals(null));
        assertFalse(d0.equals(new Object()));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(d0.hashCode(), d2.hashCode());
        assertNotEquals(d0.hashCode(), d1.hashCode());
        assertNotEquals(d0.hashCode(), d3.hashCode());
    }

    @Test
    public void toStringTest() {
        assertEquals("VacuumDoNotDisturb{start=15:30, end=21:30, enabled=true}", d0.toString());
        assertEquals("VacuumDoNotDisturb{start=22:00, end=08:00, enabled=true}", d1.toString());
        assertEquals("VacuumDoNotDisturb{start=22:00, end=08:00, enabled=false}", d3.toString());
    }
}