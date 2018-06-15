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

package de.sg_o.app.miio.serverTest;

import de.sg_o.app.miio.server.OnServerEventListener;
import org.json.JSONArray;

public class ServerYeelightColorEvents implements OnServerEventListener {
    private boolean on = false;
    private int brightness = 100;
    private int mode = 2;
    private int colorTemp = 4000;
    private int rgb = 0xFF0000;
    private int hue = 0;
    private int saturation = 100;
    private int offTime = 0;
    private String name = "";

    @Override
    public Object onCommandListener(String method, Object params) {
        JSONArray paramsArray = null;
        if (params != null){
            if (params.getClass() == JSONArray.class){
                paramsArray = (JSONArray) params;
            }
        }
        switch (method){
            case "get_prop":
                return getProps(paramsArray);
            case "set_power":
                return setPower(paramsArray);
            case "toggle":
                return togglePower();
            case "set_bright":
                return setBrightness(paramsArray);
            case "set_ct_abx":
                return setColorTemperature(paramsArray);
            case "set_rgb":
                return setRGB(paramsArray);
            case "set_hsv":
                return setHSV(paramsArray);
            case "cron_add":
                return powerOffAfterTime(paramsArray);
            case "cron_del":
                return stopPowerOffAfterTime();
            case "set_default":
                return setAsDefault();
            case "set_name":
                return setName(paramsArray);
            default:
                return null;
        }
    }

    private Object getProps(JSONArray arr){
        if (arr == null) return null;
        JSONArray resp = new JSONArray();
        for (int i = 0; i < arr.length(); i++){
            resp.put(getProp(arr.optString(i, "")));
        }
        return resp;
    }

    private String getProp(String prop){
        switch (prop){
            case "power":
                return on ? "on" : "off";
            case "bright":
                return Integer.toString(brightness);
            case "color_mode":
                return Integer.toString(mode);
            case "ct":
                return Integer.toString(colorTemp);
            case "rgb":
                return Integer.toString(rgb);
            case "hue":
                return Integer.toString(hue);
            case "sat":
                return Integer.toString(saturation);
            case "delayoff":
                return Integer.toString(offTime);
            case "name":
                return name;
            default:
                return "";
        }
    }

    private Object setColorTemperature(JSONArray arr) {
        if (arr == null) return null;
        if (arr.length() != 3) return null;
        colorTemp = arr.optInt(0);
        mode = 2;
        return ok();
    }

    private Object setRGB(JSONArray arr) {
        if (arr == null) return null;
        if (arr.length() != 3) return null;
        rgb = arr.optInt(0);
        mode = 1;
        return ok();
    }

    private Object setHSV(JSONArray arr) {
        if (arr == null) return null;
        if (arr.length() != 4) return null;
        hue = arr.optInt(0);
        saturation = arr.optInt(1);
        mode = 3;
        return ok();
    }

    private Object setBrightness(JSONArray arr) {
        if (arr == null) return null;
        if (arr.length() != 3) return null;
        brightness = arr.optInt(0);
        return ok();
    }

    private Object setPower(JSONArray arr) {
        if (arr == null) return null;
        if (arr.length() < 3) return null;
        if (arr.length() > 4) return null;
        on = arr.getString(0).equals("on");
        return ok();
    }

    private Object togglePower(){
        on = !on;
        return ok();
    }

    private Object setAsDefault(){
        return ok();
    }

    private Object powerOffAfterTime(JSONArray arr){
        if (arr == null) return null;
        if (arr.length() != 2) return null;
        offTime = arr.optInt(1);
        return ok();
    }

    private Object stopPowerOffAfterTime(){
        offTime = 0;
        return ok();
    }

    private Object setName(JSONArray arr) {
        if (arr == null) return null;
        if (arr.length() != 1) return null;
        name = arr.optString(0);
        return ok();
    }

    private Object ok(){
        JSONArray ret = new JSONArray();
        ret.put("ok");
        return ret;
    }
}
