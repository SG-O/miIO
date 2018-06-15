package de.sg_o.app.miio.yeelightTest;

import de.sg_o.app.miio.base.CommandExecutionException;
import de.sg_o.app.miio.yeelight.Prop;
import org.json.JSONArray;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class PropTest {
    private Prop.Names[] n0 = {Prop.Names.POWER, Prop.Names.BRIGHTNESS, Prop.Names.COLOR_TEMPERATURE, Prop.Names.RGB_COLOR,
            Prop.Names.HUE, Prop.Names.SATURATION, Prop.Names.COLOR_MODE, Prop.Names.COLOR_FLOW_ENABLED,
            Prop.Names.SLEEP_TIME_LEFT, Prop.Names.FLOW_PARAMETERS, Prop.Names.MUSIC_MODE_ENABLED, Prop.Names.DEVICE_NAME,
            Prop.Names.BACKGROUND_LIGHT_POWER, Prop.Names.BACKGROUND_LIGHT_FLOWING, Prop.Names.BACKGROUND_LIGHT_FLOW_PARAMETERS,
            Prop.Names.BACKGROUND_LIGHT_COLOR_TEMPERATURE, Prop.Names.BACKGROUND_LIGHT_COLOR_MODE,
            Prop.Names.BACKGROUND_LIGHT_BRIGHTNESS, Prop.Names.BACKGROUND_LIGHT_RGB_COLOR, Prop.Names.BACKGROUND_LIGHT_HUE,
            Prop.Names.BACKGROUND_LIGHT_SATURATION, Prop.Names.NIGHT_MODE_BRIGHTNESS};
    private Prop.Names[] n1 = {Prop.Names.POWER, Prop.Names.BRIGHTNESS, Prop.Names.RGB_COLOR};
    private Prop.Names[] n2 = {};
    private String[] s0 = {"on", "100", "4000", "1", "1", "100", "1", "0", "10", "", "0", "Room 1", "off", "0", "", "5000", "0", "2", "1", "0", "0", "0"};
    private String[] s1 = {"off", "50", "20"};

    private Prop p0 = new Prop(n0);
    private Prop p1 = new Prop(n1);
    private Prop p2 = new Prop(n2);

    @Test
    public void getPropsTest() {
        assertArrayEquals(n0, p0.getProps());
        assertArrayEquals(n1, p1.getProps());
        assertEquals(0, p2.getProps().length);
    }

    @Test
    public void getRequestArrayTest() throws CommandExecutionException {
        JSONArray ar0 = p0.getRequestArray();
        JSONArray ar1 = p1.getRequestArray();
        for(int i = 0; i < ar0.length(); i++){
            assertEquals(n0[i].toString(), ar0.optString(i, "invalid"));
        }
        for(int i = 0; i < ar1.length(); i++){
            assertEquals(n1[i].toString(), ar1.optString(i, "invalid"));
        }
        try {
            p2.getRequestArray();
            fail();
        } catch (CommandExecutionException e) {
            assertEquals(CommandExecutionException.Error.INVALID_PARAMETERS, e.getError());
        }
    }

    @Test
    public void parseResponseTest() throws CommandExecutionException {
        JSONArray resp0 = new JSONArray(s0);
        JSONArray resp1 = new JSONArray(s1);
        Map<Prop.Names, String> parse0 = p0.parseResponse(resp0);
        Map<Prop.Names, String> parse1 = p1.parseResponse(resp1);
        for(int i = 0; i < n0.length; i++){
            assertEquals(s0[i], parse0.get(n0[i]));
        }
        for(int i = 0; i < n1.length; i++){
            assertEquals(s1[i], parse1.get(n1[i]));
        }
        try {
            p2.parseResponse(null);
            fail();
        } catch (CommandExecutionException e) {
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
        try {
            p2.parseResponse(resp0);
            fail();
        } catch (CommandExecutionException e) {
            assertEquals(CommandExecutionException.Error.INVALID_RESPONSE, e.getError());
        }
    }
}