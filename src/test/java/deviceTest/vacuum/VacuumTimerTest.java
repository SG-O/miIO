package deviceTest.vacuum;

import base.CommandExecutionException;
import device.vacuum.VacuumTimer;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class VacuumTimerTest {
    private LocalTime tm = LocalTime.of(10,0);
    private Set<DayOfWeek> d =  new HashSet<>();
    private JSONArray empty = new JSONArray();
    private VacuumTimer t0;
    private VacuumTimer t1;
    private VacuumTimer t2;
    private VacuumTimer t3;
    private VacuumTimer t4;

    @Before
    public void setUp() {
        d.add(DayOfWeek.MONDAY);
        d.add(DayOfWeek.THURSDAY);
        empty.put("");
        empty.put("");
        t0 = new VacuumTimer("1234", true, tm, d, empty);
        t1 = new VacuumTimer(null, false, null, null, null);
        t2 = new VacuumTimer("2345", true, 12, 30, d);
        t3 = new VacuumTimer(null, false, 100, 100, null);
        t4 = new VacuumTimer(null, false, -2, -10, null);
    }

    @Test
    public void constructTest() throws Exception {
        VacuumTimer t5 = new VacuumTimer(t0.construct(true));
        VacuumTimer t6 = new VacuumTimer(t2.construct());
        assertNull(t1.construct());
        assertNull(t2.construct(true));

        assertEquals(t0, t5);
        assertEquals(t2, t6);
        assertEquals(t0.getRunDays(), t5.getRunDays());
        assertEquals(t0.getTime(), t5.getTime());
        assertEquals(t0.getJob(), t5.getJob());
        assertEquals(t2.getRunDays(), t6.getRunDays());
        assertEquals(t2.getTime(), t6.getTime());
        assertNull(t2.getJob());

        Set<DayOfWeek> dSingle =  new HashSet<>();
        dSingle.add(DayOfWeek.SUNDAY);
        VacuumTimer t7 = new VacuumTimer("1234", true, tm, dSingle, empty);
        VacuumTimer t8 = new VacuumTimer(t7.construct(true));
        assertEquals(t7, t8);
        assertEquals(t7.getRunDays(), t8.getRunDays());
        VacuumTimer t9 = new VacuumTimer("1234", true, tm, null, empty);
        assertEquals("[\"1234\",\"on\",[\"0 10 * * *\",[\"\",\"\"]]]", t9.construct(true).toString());
    }

    @Test
    public void constructorFailTest() {
        try {
            new VacuumTimer(t1.construct());
            fail();
        } catch (CommandExecutionException e){
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
        try {
            new VacuumTimer(new JSONArray("[\"1234\",\"on\"]"));
            fail();
        } catch (CommandExecutionException e){
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
        try {
            new VacuumTimer(new JSONArray("[\"1234\",\"on\",[[\"\",\"\"]]]"));
            fail();
        } catch (CommandExecutionException e){
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
        try {
            new VacuumTimer(new JSONArray("[\"1234\",\"on\",[]]"));
            fail();
        } catch (CommandExecutionException e){
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
        try {
            new VacuumTimer(new JSONArray("[\"1234\",\"on\",[\"0 * * * 1,4\",[\"\",\"\"]]]"));
            fail();
        } catch (CommandExecutionException e){
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
        try {
            new VacuumTimer(new JSONArray("[\"1234\",\"on\",[\"0 12 * * F\",[\"\",\"\"]]]"));
            fail();
        } catch (CommandExecutionException e){
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
    }

    @Test
    public void getIDTest() {
        assertEquals("1234", t0.getID());
        assertNotEquals(null, t1.getID());
        assertEquals("2345", t2.getID());
        assertNotEquals(null, t3.getID());
        assertNotEquals(null, t4.getID());
    }

    @Test
    public void isEnabledTest() {
        assertTrue(t0.isEnabled());
        assertFalse(t1.isEnabled());
        assertTrue(t2.isEnabled());
        assertFalse(t3.isEnabled());
        assertFalse(t4.isEnabled());

        t0.setEnabled(false);
        t1.setEnabled(true);

        assertTrue(t1.isEnabled());
        assertFalse(t0.isEnabled());
    }

    @Test
    public void getTimeTest() {
        assertEquals(tm, t0.getTime());
        assertNull(t1.getTime());
        assertEquals(LocalTime.of(12,30), t2.getTime());
        assertEquals(LocalTime.of(23,59), t3.getTime());
        assertEquals(LocalTime.of(0,0), t4.getTime());
    }

    @Test
    public void getRunDaysTest() {
        assertEquals(d, t0.getRunDays());
        assertEquals(0, t1.getRunDays().size());
        assertEquals(d, t2.getRunDays());
        assertEquals(0, t3.getRunDays().size());
        assertEquals(0, t4.getRunDays().size());
    }

    @Test
    public void getJobTest() {
        assertEquals(empty, t0.getJob());
        assertNull(t1.getJob());
        assertNull(t2.getJob());
        assertNull(t3.getJob());
        assertNull(t4.getJob());

        JSONArray job = new JSONArray();
        job.put("start_clean");
        job.put(-1);

        t0.setJob(job);

        assertEquals(job, t0.getJob());
    }

    @SuppressWarnings({"SimplifiableJUnitAssertion", "ConstantConditions", "ObjectEqualsNull"})
    @Test
    public void equalsTest() throws Exception {
        VacuumTimer t5 = new VacuumTimer(t0.construct(true));
        VacuumTimer t6 = new VacuumTimer(t2.construct());
        assertTrue(t0.equals(t5));
        assertTrue(t2.equals(t6));
        assertFalse(t0.equals(null));
        assertFalse(t0.equals(new Object()));
    }

    @Test
    public void hashCodeTest() throws Exception {
        VacuumTimer t5 = new VacuumTimer(t0.construct(true));
        VacuumTimer t6 = new VacuumTimer(t2.construct());
        assertEquals(t0.hashCode(), t5.hashCode());
        assertEquals(t2.hashCode(), t6.hashCode());
    }

    @Test
    public void toStringTest() {
        Set<DayOfWeek> dSingle =  new HashSet<>();
        dSingle.add(DayOfWeek.SUNDAY);
        VacuumTimer t5 = new VacuumTimer("1234", true, tm, dSingle, empty);
        VacuumTimer t6 = new VacuumTimer("1234", true, tm, null, empty);
        assertEquals("VacuumTimer{ID='1234', enabled=true, time=10:00, runDays=[SUNDAY], job=[\"\",\"\"]}", t5.toString());
        assertEquals("VacuumTimer{ID='1234', enabled=true, time=10:00, runDays=[], job=[\"\",\"\"]}", t6.toString());
    }
}