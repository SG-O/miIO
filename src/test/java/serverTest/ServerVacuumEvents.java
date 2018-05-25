package serverTest;

import org.json.JSONArray;
import org.json.JSONObject;
import server.OnServerEventListener;

public class ServerVacuumEvents implements OnServerEventListener {
    private boolean dndEnabled = false;
    private boolean mapPresent = false;
    private int cleanArea = 0;
    private int fanPower = 60;
    private int msgVersion = 8;
    private boolean inCleaning = false;
    private int errorCode = 0;
    private int state = 3;
    private int battery = 100;
    private int msgSeq = 0;
    private int cleanTime = 0;

    private int mainBrushWork = 0;
    private int sensorDirty = 0;
    private int sideBrushWork = 0;
    private int filterWork = 0;

    public ServerVacuumEvents() {
    }

    @Override
    public Object onCommandListener(String method, Object params) {
        JSONObject paramsObject = null;
        JSONArray paramsArray = null;
        if (params != null){
            if (params.getClass() == JSONObject.class){
                paramsObject = (JSONObject) params;
            }
            if (params.getClass() == JSONArray.class){
                paramsArray = (JSONArray) params;
            }
        }
        switch (method){
            case "get_status":
                return status();
            case "get_consumable":
                return consumableStatus();
            case "get_custom_mode":
                return getFanSpeed();
            case "set_custom_mode":
                return setFanSpeed(paramsArray);
            case "app_start":
                return start();
            case "app_pause":
                return pause();
            case "app_stop":
                return stop();
            case "app_charge":
                return home();
            case "app_spot":
                return spotCleaning();
            case "find_me":
                return findMe();
            default:
                return null;
        }
    }

    private Object status(){
        JSONObject payload = new JSONObject();
        payload.put("dnd_enabled", dndEnabled ? 1 : 0);
        payload.put("map_present", mapPresent ? 1 : 0);
        payload.put("clean_area", cleanArea);
        payload.put("fan_power", fanPower);
        payload.put("msg_ver", msgVersion);
        payload.put("in_cleaning", inCleaning ? 1 : 0);
        payload.put("error_code", errorCode);
        payload.put("state", state);
        payload.put("battery", battery);
        payload.put("msg_seq", msgSeq);
        payload.put("clean_time", cleanTime);

        JSONArray ret = new JSONArray();
        ret.put(payload);
        return ret;
    }

    private Object consumableStatus(){
        JSONObject payload = new JSONObject();
        payload.put("main_brush_work_time", mainBrushWork);
        payload.put("sensor_dirty_time", sensorDirty);
        payload.put("side_brush_work_time", sideBrushWork);
        payload.put("filter_work_time", filterWork);

        JSONArray ret = new JSONArray();
        ret.put(payload);
        return ret;
    }

    private Object getFanSpeed(){
        JSONArray ret = new JSONArray();
        ret.put(fanPower);
        return ret;
    }

    private Object setFanSpeed(JSONArray params){
        if (params == null) return null;
        int speed = params.optInt(0, -1);
        if ((speed < 0) || (speed > 100)) return null;
        this.fanPower = speed;
        return ok();
    }

    private Object start()  {
        state = 5;
        return ok();
    }

    private Object pause() {
        state = 10;
        return ok();
    }

    private Object stop() {
        state = 3;
        return ok();
    }

    private Object home() {
        state = 8;
        return ok();
    }

    private Object spotCleaning() {
        state = 17;
        return ok();
    }

    private Object findMe() {
        return ok();
    }

    private JSONArray ok(){
        JSONArray ret = new JSONArray();
        ret.put("ok");
        return ret;
    }
}
