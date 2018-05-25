package serverTest;

import device.vacuum.VacuumStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import server.OnServerEventListener;

public class ServerVacuumEvents implements OnServerEventListener {
    private VacuumStatus state = new VacuumStatus(null);

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
        JSONObject payload = state.construct();
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
        ret.put(state.getFanPower());
        return ret;
    }

    private Object setFanSpeed(JSONArray params){
        if (params == null) return null;
        int speed = params.optInt(0, -1);
        if ((speed < 0) || (speed > 100)) return null;
        state.setFanPower(speed);
        return ok();
    }

    private Object start()  {
        state.setState(VacuumStatus.State.CLEANING);
        return ok();
    }

    private Object pause() {
        state.setState(VacuumStatus.State.PAUSED);
        return ok();
    }

    private Object stop() {
        state.setState(VacuumStatus.State.IDLE);
        return ok();
    }

    private Object home() {
        state.setState(VacuumStatus.State.CHARGING);
        return ok();
    }

    private Object spotCleaning() {
        state.setState(VacuumStatus.State.SPOT_CLEANUP);
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
