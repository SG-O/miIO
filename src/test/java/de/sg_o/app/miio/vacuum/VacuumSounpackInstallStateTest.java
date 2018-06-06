package de.sg_o.app.miio.vacuum;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class VacuumSounpackInstallStateTest {
    private VacuumSounpackInstallState s0 = new VacuumSounpackInstallState(30, VacuumSounpackInstallState.State.UNKNOWN, VacuumSounpackInstallState.Error.NONE, 2);
    private VacuumSounpackInstallState s1 = new VacuumSounpackInstallState(-1, VacuumSounpackInstallState.State.ERROR, VacuumSounpackInstallState.Error.CHECKSUM, 20);
    private VacuumSounpackInstallState s2 = new VacuumSounpackInstallState(200, null, null, -5);

    @Test
    public void getProgressTest() {
        assertEquals(30, s0.getProgress());
        assertEquals(0, s1.getProgress());
        assertEquals(100, s2.getProgress());

        s0.setProgress(-1);
        assertEquals(0, s0.getProgress());
        s0.setProgress(200);
        assertEquals(100, s0.getProgress());
        s0.setProgress(50);
        assertEquals(50, s0.getProgress());
    }

    @Test
    public void getStateTest() {
        assertEquals(VacuumSounpackInstallState.State.UNKNOWN, s0.getState());
        assertEquals(VacuumSounpackInstallState.State.ERROR, s1.getState());
        assertEquals(VacuumSounpackInstallState.State.UNKNOWN, s2.getState());

        s0.setState(VacuumSounpackInstallState.State.INSTALLING);
        assertEquals(VacuumSounpackInstallState.State.INSTALLING, s0.getState());
        s0.setState(null);
        assertEquals(VacuumSounpackInstallState.State.UNKNOWN, s0.getState());

    }

    @Test
    public void getErrorTest() {
        assertEquals(VacuumSounpackInstallState.Error.NONE, s0.getError());
        assertEquals(VacuumSounpackInstallState.Error.CHECKSUM, s1.getError());
        assertEquals(VacuumSounpackInstallState.Error.UNKNOWN, s2.getError());

        s0.setError(VacuumSounpackInstallState.Error.DOWNLOAD);
        assertEquals(VacuumSounpackInstallState.Error.DOWNLOAD, s0.getError());
        s0.setError(null);
        assertEquals(VacuumSounpackInstallState.Error.UNKNOWN, s0.getError());
    }

    @Test
    public void getSidTest() {
        assertEquals(2, s0.getSid());
        assertEquals(20, s1.getSid());
        assertEquals(-5, s2.getSid());

        s0.setSid(-5);
        assertEquals(-5, s0.getSid());
        s0.setSid(300);
        assertEquals(300, s0.getSid());
    }

    @Test
    public void constructTest() {
        VacuumSounpackInstallState s3 = new VacuumSounpackInstallState(s0.construct(true));
        VacuumSounpackInstallState s4 = new VacuumSounpackInstallState(s1.construct(true));
        VacuumSounpackInstallState s5 = new VacuumSounpackInstallState(s2.construct(false));
        VacuumSounpackInstallState s6 = new VacuumSounpackInstallState(new JSONObject());
        VacuumSounpackInstallState s7 = new VacuumSounpackInstallState(null);

        assertEquals(s0, s3);
        assertEquals(s1, s4);
        assertNotEquals(s2, s5);
        assertEquals(-1, s5.getSid());

        assertEquals(0, s6.getProgress());
        assertEquals(VacuumSounpackInstallState.State.UNKNOWN, s6.getState());
        assertEquals(VacuumSounpackInstallState.Error.UNKNOWN, s6.getError());
        assertEquals(-1, s6.getSid());

        assertEquals(0, s7.getProgress());
        assertEquals(VacuumSounpackInstallState.State.UNKNOWN, s7.getState());
        assertEquals(VacuumSounpackInstallState.Error.UNKNOWN, s7.getError());
        assertEquals(-1, s7.getSid());
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "ObjectEqualsNull"})
    @Test
    public void equalsTest() {
        VacuumSounpackInstallState s3 = new VacuumSounpackInstallState(30, VacuumSounpackInstallState.State.UNKNOWN, VacuumSounpackInstallState.Error.NONE, 2);
        VacuumSounpackInstallState s4 = new VacuumSounpackInstallState(-1, VacuumSounpackInstallState.State.ERROR, VacuumSounpackInstallState.Error.CHECKSUM, 20);
        VacuumSounpackInstallState s5 = new VacuumSounpackInstallState(200, null, null, -5);

        assertTrue(s0.equals(s3));
        assertTrue(s1.equals(s4));
        assertTrue(s2.equals(s5));

        assertFalse(s0.equals(s1));
        assertFalse(s0.equals(s2));
        assertFalse(s1.equals(s2));

        assertFalse(s0.equals(null));
        assertFalse(s0.equals(new Object()));
    }

    @Test
    public void hashCodeTest() {
        VacuumSounpackInstallState s3 = new VacuumSounpackInstallState(30, VacuumSounpackInstallState.State.UNKNOWN, VacuumSounpackInstallState.Error.NONE, 2);
        VacuumSounpackInstallState s4 = new VacuumSounpackInstallState(-1, VacuumSounpackInstallState.State.ERROR, VacuumSounpackInstallState.Error.CHECKSUM, 20);
        VacuumSounpackInstallState s5 = new VacuumSounpackInstallState(200, null, null, -5);

        assertEquals(s0.hashCode(), s3.hashCode());
        assertEquals(s1.hashCode(), s4.hashCode());
        assertEquals(s2.hashCode(), s5.hashCode());

        assertNotEquals(s0.hashCode(), s1.hashCode());
        assertNotEquals(s0.hashCode(), s2.hashCode());
        assertNotEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void toStringTest() {
        assertEquals("VacuumSounpackInstallState{progress=30, state=UNKNOWN, error=NONE, sid=2}", s0.toString());
        assertEquals("VacuumSounpackInstallState{progress=30, state=UNKNOWN, error=NONE, sid=2}", s0.toString());
        assertEquals("VacuumSounpackInstallState{progress=30, state=UNKNOWN, error=NONE, sid=2}", s0.toString());
    }
}