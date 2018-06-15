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

package de.sg_o.app.miio.yeelight;

import de.sg_o.app.miio.base.CommandExecutionException;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the properties of a light.
 */
public class Prop {
    /**
     * The retrievable properties. Not all lights implement every property.
     */
    public enum Names {
        /**
         * The power state of the lamp. "on" or "off"
         */
        POWER("power"),
        /**
         * The brightness of the lamp. 1 to 100 inclusive
         */
        BRIGHTNESS("bright"),
        /**
         * Color temperature of the lamp in color temperature mode. 1700 to 6500(k) inclusive
         */
        COLOR_TEMPERATURE("ct"),
        /**
         * The color in rgb mode. 1 to 16777215 inclusive. Bits 0-7 are blue, 8-15 green and 16-23 red.
         */
        RGB_COLOR("rgb"),
        /**
         * The hue of the color in hsv mode. 0 to 359 inclusive
         */
        HUE("hue"),
        /**
         * The saturation of the color in hsv mode. 0 to 100 inclusive.
         */
        SATURATION("sat"),
        /**
         * The color mode. 1: rgb mode; 2: color temperature mode; 3: hsv mode
         */
        COLOR_MODE("color_mode"),
        /**
         * The state of the color flow mode. 0: disabled; 1: enabled
         */
        COLOR_FLOW_ENABLED("flowing"),
        /**
         * The remaining time of the sleep timer in minutes. 1 to 60 inclusive.
         */
        SLEEP_TIME_LEFT("delayoff"),
        /**
         * The parameters of the flow mode.
         */
        FLOW_PARAMETERS("flow_params"),
        /**
         * The state of the music mode. 0: disabled; 1: enabled
         */
        MUSIC_MODE_ENABLED("music_on"),
        /**
         * The lights name.
         */
        DEVICE_NAME("name"),
        /**
         * The background power state of the lamp. "on" or "off"
         */
        BACKGROUND_LIGHT_POWER("bg_power"),
        /**
         * The state of the color flow mode in background mode. 0: disabled; 1: enabled
         */
        BACKGROUND_LIGHT_FLOWING("bg_flowing"),
        /**
         * The parameters of the flow mode in background mode.
         */
        BACKGROUND_LIGHT_FLOW_PARAMETERS("bg_flow_params"),
        /**
         * The color in rgb mode in background mode. 1 to 16777215 inclusive
         */
        BACKGROUND_LIGHT_COLOR_TEMPERATURE("bg_ct"),
        /**
         * The color mode in background mode. 1: rgb mode; 2: color temperature mode; 3: hsv mode
         */
        BACKGROUND_LIGHT_COLOR_MODE("bg_lmode"),
        /**
         * The brightness of the lamp in background mode. 1 to 100 inclusive
         */
        BACKGROUND_LIGHT_BRIGHTNESS("bg_bright"),
        /**
         * The color in rgb mode in background mode. 1 to 16777215 inclusive
         */
        BACKGROUND_LIGHT_RGB_COLOR("bg_rgb"),
        /**
         * The hue of the color in hsv mode in background mode. 0 to 359 inclusive
         */
        BACKGROUND_LIGHT_HUE("bg_hue"),
        /**
         * The saturation of the color in hsv mode in background mode. 0 to 100 inclusive.
         */
        BACKGROUND_LIGHT_SATURATION("bg_sat"),
        /**
         * The brightness of the lamp in night mode. 1 to 100 inclusive
         */
        NIGHT_MODE_BRIGHTNESS("nl_br");

        private final String name;

        Names(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private Names[] props;

    /**
     * Create a new light properties object.
     * @param props The properties to retrieve.
     */
    public Prop(Names[] props) {
        if (props == null) props = new Names[0];
        this.props = props;
    }

    /**
     * @return The properties to retrieve.
     */
    public Names[] getProps() {
        return props;
    }

    /**
     * @return A JSONArray that is sent to the device to retrieve the property values.
     *      * @throws CommandExecutionException  When the properties array was invalid.
     */
    public JSONArray getRequestArray() throws CommandExecutionException {
        if (props.length < 1) throw  new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
        JSONArray ret = new JSONArray();
        for (Names p : props){
            ret.put(p.toString());
        }
        return ret;
    }

    /**
     * Parse the response from the device and return a map containing the property names and their values.
     * @param response The response from the device.
     * @return A map containing the property names as the keys and their values.
     *
     */
    public Map<Names, String> parseResponse(JSONArray response) throws CommandExecutionException {
        if (response == null) throw  new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        if (response.length() != props.length) throw  new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        Map<Names, String> ret = new HashMap<>(props.length);
        for (int i = 0; i < props.length; i++){
            ret.put(props[i], response.optString(i, ""));
        }
        return ret;
    }
}
