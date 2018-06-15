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
import de.sg_o.app.miio.base.Device;
import de.sg_o.app.miio.base.Token;
import org.json.JSONArray;

import java.awt.*;
import java.net.InetAddress;
import java.util.Map;

/**
 * Baseclass for all yeelight devices.
 */
@SuppressWarnings("WeakerAccess")
public abstract class Light extends Device {

    /**
     * @param ip               The IP address of the light to connect to. If the address is null the first light that was found will be chosen.
     * @param token            The token for that device. If the token is null the token will be extracted from unprovisioned devices.
     * @param acceptableModels An array of acceptable devices to connect to.
     * @param timeout          The timeout for the communication
     * @param retries          The number of retries after a failed communication
     */
    public Light(InetAddress ip, Token token, String[] acceptableModels, int timeout, int retries) {
        super(ip, token, acceptableModels, timeout, retries);
    }

    /**
     * Get several property values at once from the device.
     * @param props The properties to get.
     * @return The property names and values.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public Map<Prop.Names, String> getProps(Prop.Names[] props) throws CommandExecutionException {
        Prop prop = new Prop(props);
        return prop.parseResponse(sendToArray("get_prop", prop.getRequestArray()));
    }

    /**
     * Get a single property value from the device.
     * @param prop The property to get.
     * @return The value of the specified property name.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public String getSingleProp(Prop.Names prop) throws CommandExecutionException {
        Map<Prop.Names, String> value = getProps(new Prop.Names[]{prop});
        String valueString = value.get(prop);
        if (valueString == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return valueString;
    }

    /**
     * Get a single property value and try to convert it to an int.
     * @param prop The property to get.
     * @return The value of the specified property name.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getIntProp(Prop.Names prop) throws CommandExecutionException {
        String value = getSingleProp(prop);
        if (value.equals("")) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        try {
            return Integer.valueOf(value);
        } catch (Exception e){
            throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        }
    }

    /**
     * @return 1: rgb mode; 2: color temperature mode; 3: Hue Saturation mode
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getDeviceMode() throws CommandExecutionException {
        return getIntProp(Prop.Names.COLOR_MODE);
    }

    /**
     * @param temperature Color temperature to set. 1700 to 6500(k) inclusive
     * @param smoothChange Whether to change instantly or smoothly.
     * @param duration The duration of the smooth change.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setColorTemperature(int temperature, boolean smoothChange, int duration) throws CommandExecutionException {
        if (temperature < 1700) temperature = 1700;
        if (temperature > 6500) temperature = 6500;
        if (duration < 30) duration = 30;
        JSONArray col = new JSONArray();
        col.put(temperature);
        col.put(smoothChange ? "smooth" : "sudden");
        col.put(duration);
        return sendOk("set_ct_abx", col);
    }

    /**
     * @return The color temperature the device is set to.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getColorTemperature() throws CommandExecutionException {
        return getIntProp(Prop.Names.COLOR_TEMPERATURE);
    }

    /**
     * @param c The color to change to.
     * @param smoothChange Whether to change instantly or smoothly.
     * @param duration The duration of the smooth change.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setRGB(Color c, boolean smoothChange, int duration) throws CommandExecutionException {
        if (c == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
        if (duration < 30) duration = 30;
        int colorInt = c.getRGB() & 0xFFFFFF;
        if (colorInt < 1) colorInt = 0x010101;
        JSONArray col = new JSONArray();
        col.put(colorInt);
        col.put(smoothChange ? "smooth" : "sudden");
        col.put(duration);
        return sendOk("set_rgb", col);
    }

    /**
     * @return The current color the device is set to.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public Color getRGB() throws CommandExecutionException {
            return new Color(getIntProp(Prop.Names.RGB_COLOR));
    }

    /**
     * @param hue The hue to change to.
     * @param saturation The saturation to change to.
     * @param smoothChange Whether to change instantly or smoothly.
     * @param duration The duration of the smooth change.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setHSV(int hue, int saturation, boolean smoothChange, int duration) throws CommandExecutionException {
        if (hue < 0) hue = 0;
        if (hue > 359) hue = 359;
        if (saturation < 0) saturation = 0;
        if (saturation > 100) saturation = 100;
        if (duration < 30) duration = 30;
        JSONArray col = new JSONArray();
        col.put(hue);
        col.put(saturation);
        col.put(smoothChange ? "smooth" : "sudden");
        col.put(duration);
        return sendOk("set_hsv", col);
    }

    /**
     * @return The hue the device is currently set to.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getHue() throws CommandExecutionException {
        return getIntProp(Prop.Names.HUE);
    }

    /**
     * @return The saturation the device is currently set to.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getSaturation() throws CommandExecutionException {
        return getIntProp(Prop.Names.SATURATION);
    }

    /**
     * @param brightness The brightness to change to.
     * @param smoothChange Whether to change instantly or smoothly.
     * @param duration The duration of the smooth change.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setBrightness(int brightness, boolean smoothChange, int duration) throws CommandExecutionException {
        if (brightness < 1) brightness = 1;
        if (brightness > 100) brightness = 100;
        if (duration < 30) duration = 30;
        JSONArray col = new JSONArray();
        col.put(brightness);
        col.put(smoothChange ? "smooth" : "sudden");
        col.put(duration);
        return sendOk("set_bright", col);
    }

    /**
     * @return The brightness the device is currently set to.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getBrightness() throws CommandExecutionException {
        return getIntProp(Prop.Names.BRIGHTNESS);
    }

    /**
     * @param on True: turn the device on; False: turn the device off.
     * @param smoothChange Whether to change instantly or smoothly.
     * @param duration The duration of the smooth change.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setPower(boolean on, boolean smoothChange, int duration) throws CommandExecutionException {
        JSONArray col = new JSONArray();
        col.put(on ? "on" : "off");
        col.put(smoothChange ? "smooth" : "sudden");
        col.put(duration);
        return sendOk("set_power", col);
    }

    /**
     * @return Thoggle the devices power
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean togglePower() throws CommandExecutionException {
        return sendOk("toggle", new JSONArray());
    }

    /**
     * @return The hue the device is currently set to.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean isOn() throws CommandExecutionException {
        return getSingleProp(Prop.Names.POWER).equals("on");
    }

    /**
     * Set the current settings as the default value that will be restored after a power loss.
     * @return True if the command was received successfully
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setAsDefault() throws CommandExecutionException {
        return sendOk("set_default", new JSONArray());
    }

    /**
     * Power the device off after some time.
     * @param minutes The time until the device is turned off in minutes.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean powerOffAfterTime(int minutes) throws CommandExecutionException {
        JSONArray col = new JSONArray();
        col.put(0);
        col.put(minutes);
        return sendOk("cron_add", col);
    }

    /**
     * Prevent the device from powering off after some time.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean stopPowerOffAfterTime() throws CommandExecutionException {
        JSONArray col = new JSONArray();
        col.put(0);
        return sendOk("cron_del", col);
    }

    /**
     * @return The brightness the device is currently set to.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getTimeUntilPowerOff() throws CommandExecutionException {
        return getIntProp(Prop.Names.SLEEP_TIME_LEFT);
    }

    /**
     * @param name The name to set the device to.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setName(String name) throws CommandExecutionException {
        if (name == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
        JSONArray col = new JSONArray();
        col.put(name);
        return sendOk("set_name", col);
    }

    /**
     * @return The devices name.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public String getName() throws CommandExecutionException {
        return getSingleProp(Prop.Names.DEVICE_NAME);
    }
}
