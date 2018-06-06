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

import org.json.JSONArray;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class VacuumCleanupTest {
    private Instant start = Instant.ofEpochSecond(Instant.now().getEpochSecond());
    private Instant end = start.plusSeconds(300);
    private VacuumCleanup c0 = new VacuumCleanup(start, end, 300, 2000, true);
    private VacuumCleanup c1 = new VacuumCleanup(null, end, 300, 2000, true);
    private VacuumCleanup c2 = new VacuumCleanup(start, null, 300, 2000, true);
    private VacuumCleanup c3 = new VacuumCleanup(start, end, -1, -5, false);
    private VacuumCleanup c4 = new VacuumCleanup(new JSONArray("[1527546448,1527546629,181,1970000,0,1]"));
    private VacuumCleanup c5 = new VacuumCleanup(null);

    @Test
    public void getStartTest() {
        assertEquals(start, c0.getStart());
        assertEquals(Instant.ofEpochSecond(1527546448), c4.getStart());
        assertNotEquals(start, c2.getStart());
        assertNotEquals(start, c3.getStart());
        assertNotNull(c1.getStart());
        assertNotNull(c5.getStart());

    }

    @Test
    public void getEndTest() {
        assertEquals(end, c0.getEnd());
        assertEquals(Instant.ofEpochSecond(1527546629), c4.getEnd());
        assertNotEquals(end, c1.getEnd());
        assertNotEquals(end, c3.getEnd());
        assertNotNull(c2.getEnd());
        assertNotNull(c5.getEnd());
    }

    @Test
    public void getRuntimeTest() {
        assertEquals(300, c0.getRuntime());
        assertEquals(1, c1.getRuntime());
        assertEquals(1, c2.getRuntime());
        assertEquals(1, c3.getRuntime());
        assertEquals(181, c4.getRuntime());
        assertEquals(1, c5.getRuntime());
    }

    @Test
    public void getAreaTest() {
        assertEquals(2000, c0.getArea());
        assertEquals(2000, c1.getArea());
        assertEquals(2000, c2.getArea());
        assertEquals(0, c3.getArea());
        assertEquals(1970000, c4.getArea());
        assertEquals(0, c5.getArea());
    }

    @Test
    public void isCompletedTest() {
        assertTrue(c0.isCompleted());
        assertTrue(c1.isCompleted());
        assertTrue(c2.isCompleted());
        assertFalse(c3.isCompleted());
        assertTrue(c4.isCompleted());
        assertFalse(c5.isCompleted());
    }

    @Test
    public void constructTest() {
        VacuumCleanup c6 = new VacuumCleanup(c0.construct());
        VacuumCleanup c7 = new VacuumCleanup(c4.construct());
        assertEquals(c0, c6);
        assertEquals(c4, c7);
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "ObjectEqualsNull"})
    @Test
    public void equalsTest() {
        VacuumCleanup c6 = new VacuumCleanup(c0.construct());
        assertTrue(c0.equals(c6));

        assertFalse(c0.equals(c1));
        assertFalse(c0.equals(null));
        assertFalse(c0.equals(new Object()));
    }

    @Test
    public void hashCodeTest() {
        VacuumCleanup c6 = new VacuumCleanup(c0.construct());
        assertEquals(c0.hashCode(), c6.hashCode());
        assertNotEquals(c0.hashCode(), c1.hashCode());
    }

    @Test
    public void toStringTest() {
        assertEquals("VacuumCleanup{start=2018-05-28T22:27:28Z, end=2018-05-28T22:30:29Z, runtime=181, area=1970000, completed=true}", c4.toString());
    }
}