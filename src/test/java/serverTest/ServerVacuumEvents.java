package serverTest;

import base.CommandExecutionException;
import device.vacuum.VacuumConsumableStatus;
import device.vacuum.VacuumStatus;
import device.vacuum.VacuumTimer;
import org.json.JSONArray;
import server.OnServerEventListener;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServerVacuumEvents implements OnServerEventListener {
    private VacuumStatus state = new VacuumStatus(null);
    private VacuumConsumableStatus consumables = new VacuumConsumableStatus(null);
    private ZoneId timezone = ZoneId.systemDefault();
    private Map<String, VacuumTimer> timers = new LinkedHashMap<>();

    public ServerVacuumEvents() {
    }

    public VacuumConsumableStatus getConsumables() {
        return consumables;
    }

    @Override
    public Object onCommandListener(String method, Object params) {
        JSONArray paramsArray = null;
        if (params != null){
            if (params.getClass() == JSONArray.class){
                paramsArray = (JSONArray) params;
            }
        }
        switch (method){
            case "get_status":
                return status();
            case "get_timezone":
                return getTimezone();
            case "set_timezone":
                return setTimezone(paramsArray);
            case "get_consumable":
                return consumableStatus();
            case "reset_consumable":
                return resetConsumableStatus(paramsArray);
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
            case "get_timer":
                return getTimers();
            case "set_timer":
                return addTimer(paramsArray);
            case "upd_timer":
                return setTimerEnabled(paramsArray);
            case "del_timer":
                return removeTimer(paramsArray);
            default:
                return null;
        }
    }

    private Object status(){
        JSONArray ret = new JSONArray();
        ret.put(state.construct());
        return ret;
    }

    private Object getTimezone(){
        JSONArray ret = new JSONArray();
        ret.put(timezone.getId());
        return ret;
    }

    private Object setTimezone(JSONArray tz){
        if(tz == null) return null;
        String zone = tz.optString(0, null);
        if (zone == null)  return null;
        this.timezone = ZoneId.of(zone);
        return ok();
    }

    private Object consumableStatus(){
        JSONArray ret = new JSONArray();
        ret.put(consumables.construct());
        return ret;
    }

    private Object resetConsumableStatus(JSONArray params){
        if (params == null) return null;
        String name = params.optString(0, "");
        consumables.reset(name);
        return ok();
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

    private Object getTimers(){
        JSONArray resp = new JSONArray();
        for (VacuumTimer t : timers.values()) {
            resp.put(t.construct(true));
        }
        return resp;
    }

    private Object addTimer(JSONArray timer) {
        if (timer == null) return null;
        JSONArray t = timer.optJSONArray(0);
        if (t == null) return null;
        try {
            VacuumTimer tm = new VacuumTimer(t);
            JSONArray job = new JSONArray();
            job.put("start_clean");
            job.put(-1);
            tm.setJob(job);
            timers.put(tm.getID(), tm);
        } catch (CommandExecutionException e) {
            return null;
        }
        return ok();
    }

    private Object setTimerEnabled(JSONArray timer){
        if (timer == null) return null;
        VacuumTimer t = timers.get(timer.optString(0));
        if (t == null) return null;
        t.setEnabled(timer.optString(1).equals("on"));
        return ok();
    }

    private Object removeTimer(JSONArray timer){
        if (timer == null) return null;
        VacuumTimer t = timers.remove(timer.optString(0));
        if (t == null) return null;
        return ok();
    }

    private JSONArray ok(){
        JSONArray ret = new JSONArray();
        ret.put("ok");
        return ret;
    }

}
