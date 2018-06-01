package serverTest;

import base.CommandExecutionException;
import device.vacuum.*;
import org.json.JSONArray;
import org.json.JSONObject;
import server.OnServerEventListener;

import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServerVacuumEvents implements OnServerEventListener {
    private VacuumStatus state = new VacuumStatus(null);
    private VacuumConsumableStatus consumables = new VacuumConsumableStatus(null);
    private ZoneId timezone = ZoneId.systemDefault();
    private Map<String, VacuumTimer> timers = new LinkedHashMap<>();
    private VacuumDoNotDisturb dnd = new VacuumDoNotDisturb(null, null);
    private Map<Long, VacuumCleanup> cleanups = new LinkedHashMap<>();
    private int soundVolume = 90;

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
            case "get_dnd_timer":
                return getDoNotDisturb();
            case "set_dnd_timer":
                return setDoNotDisturb(paramsArray);
            case "close_dnd_timer":
                return disableDoNotDisturb();
            case "app_goto_target":
                return goTo(paramsArray);
            case "app_zoned_clean":
                return cleanArea(paramsArray);
            case "get_clean_summary":
                return getCleaningSummary();
            case "get_clean_record":
                return getCleanup(paramsArray);
            case "get_sound_volume":
                return getSoundVolume();
            case "change_sound_volume":
                return setSoundVolume(paramsArray);
            case "test_sound_volume":
                return testSoundVolume();
            case "app_rc_start":
                return manualControlStart();
            case "app_rc_end":
                return manualControlStop();
            case "app_rc_move":
                return manualControlMove(paramsArray);
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
        Instant start = Instant.now();
        cleanups.put((long) cleanups.size(), new VacuumCleanup(start, start.plusSeconds(200), 200, 30000, true));
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
        Instant start = Instant.now();
        cleanups.put((long) cleanups.size(), new VacuumCleanup(start, start.plusSeconds(100), 100, 10000, true));
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

    private Object getDoNotDisturb(){
        return dnd.construct(true);
    }

    private Object setDoNotDisturb(JSONArray doNotDisturb){
        if (doNotDisturb == null) return null;
        dnd = new VacuumDoNotDisturb(doNotDisturb);
        state.setDndEnabled(true);
        return ok();
    }

    private Object disableDoNotDisturb(){
        dnd.setEnabled(false);
        state.setDndEnabled(false);
        return ok();
    }

    private Object goTo(JSONArray p) {
        if (p == null) return null;
        state.setState(VacuumStatus.State.IDLE);
        return ok();
    }

    private Object cleanArea(JSONArray values) {
        if (values == null) return null;
        Instant start = Instant.now();
        cleanups.put((long) cleanups.size(), new VacuumCleanup(start, start.plusSeconds(150), 150, 20000, true));
        state.setState(VacuumStatus.State.CLEANING_ZONE);
        return ok();
    }

    private Object getCleaningSummary(){
        JSONArray cleans = new JSONArray();
        for (Long id : cleanups.keySet()){
            cleans.put(id.longValue());
        }
        long runtime = 0;
        long area = 0;
        for (VacuumCleanup c : cleanups.values()){
            runtime += c.getRuntime();
            area += c.getArea();
        }
        JSONArray ret = new JSONArray();
        ret.put(runtime);
        ret.put(area);
        ret.put(cleanups.size());
        ret.put(cleans);
        return ret;
    }

    private Object getCleanup(JSONArray id){
        if (id == null) return null;
        VacuumCleanup c = cleanups.get(id.optLong(0, -1));
        if (c == null) return null;
        JSONArray ret = new JSONArray();
        ret.put(c.construct());
        return ret;
    }

    private Object getSoundVolume(){
        JSONArray ret = new JSONArray();
        ret.put(soundVolume);
        return ret;
    }

    private Object setSoundVolume(JSONArray vol){
        if (vol == null) return null;
        int volVal = vol.optInt(0, -1);
        if (volVal > 100 | volVal < 0) return null;
        this.soundVolume = volVal;
        return ok();
    }

    private Object testSoundVolume(){
        return ok();
    }

    private Object manualControlStart() {
        state.setState(VacuumStatus.State.REMOTE_CONTROL);
        return ok();
    }

    private Object manualControlStop() {
        state.setState(VacuumStatus.State.IDLE);
        return ok();
    }

    private Object manualControlMove(JSONArray mov) {
        if (mov == null) return null;
        JSONObject ob = mov.optJSONObject(0);
        if (ob == null) return null;
        return ok();
    }

    private JSONArray ok(){
        JSONArray ret = new JSONArray();
        ret.put("ok");
        return ret;
    }

}
