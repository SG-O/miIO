package device.vacuum;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VacuumStatus {
    public enum State {
        UNKNOWN(0),
        STARTUP(1),
        CHARGER_CONNECTION_LOST(2),
        IDLE(3),
        REMOTE_CONTROL(4),
        CLEANING(5),
        GOING_HOME(6),
        MANUAL(7),
        CHARGING(8),
        CHARGING_ERROR(9),
        PAUSED(10),
        SPOT_CLEANUP(11),
        ERROR(12),
        SHUTDOWN(13),
        UPDATING(14),
        DOCKING(15),
        HEADING_TARGET(16),
        CLEANING_ZONE(17);
        private final int state;
        private static Map<Integer, State> map = new HashMap<>();

        State(int state) {
            this.state = state;
        }

        static {
            for (State st : State.values()) {
                map.put(st.state, st);
            }
        }

        public static State valueOf(int state) {
            State st = map.get(state);
            if (st == null) return State.UNKNOWN;
            return st;
        }

        public int getState() {
            return state;
        }
    }

    public enum Error {
        NONE(0),
        LDS(1),
        COLLISION_SENSOR(2),
        MOVE_DEVICE(3),
        CLEAN_CLIFF_SENSOR(4),
        CLEAN_MAIN_BRUSH(5),
        CLEAN_SIDE_BRUSH(6),
        WHEELS(7),
        REMOVE_OBSTACLES(8),
        REPLACE_DUSTBIN(9),
        CLEAN_FILTER(10),
        MAGNETIC_FIELD(11),
        BATTERY_LOW(12),
        CHARGING_FAULT(13),
        BATTERY_FAULT(14),
        WALL_SENSOR(15),
        UNEVEN_SURFACE(16),
        SIDE_BRUSH_SYSTEM_RESET(17),
        FAN_SYSTEM_REBOOT(18),
        BASE_POWERED(19),
        UNKNOWN(20);
        private final int code;
        private static Map<Integer, Error> map = new HashMap<>();

        Error(int code) {
            this.code = code;
        }

        static {
            for (Error er : Error.values()) {
                map.put(er.code, er);
            }
        }

        public static Error valueOf(int code) {
            Error er = map.get(code);
            if (er == null) return Error.UNKNOWN;
            return er;
        }

        public int getCode() {
            return code;
        }
    }

    private boolean dndEnabled = false;
    private boolean mapPresent = false;
    private int cleanArea = 0;
    private int fanPower = 60;
    private int msgVersion = 8;
    private boolean inCleaning = false;
    private Error errorCode = Error.UNKNOWN;
    private State state = State.UNKNOWN;
    private int battery = 100;
    private int msgSeq = 0;
    private int cleanTime = 0;

    public VacuumStatus(boolean dndEnabled, boolean mapPresent, int cleanArea, int fanPower, int msgVersion, boolean inCleaning, Error errorCode, State state, int battery, int msgSeq, int cleanTime) {
        this.dndEnabled = dndEnabled;
        this.mapPresent = mapPresent;
        this.cleanArea = cleanArea;
        this.fanPower = fanPower;
        this.msgVersion = msgVersion;
        this.inCleaning = inCleaning;
        this.errorCode = errorCode;
        this.state = state;
        this.battery = battery;
        this.msgSeq = msgSeq;
        this.cleanTime = cleanTime;
    }

    public VacuumStatus(JSONObject status) {
        if (status != null){
            dndEnabled = status.optInt("dnd_enabled") == 1;
            mapPresent = status.optInt("map_present") == 1;
            cleanArea = status.optInt("clean_area");
            fanPower = status.optInt("fan_power");
            msgVersion = status.optInt("msg_ver");
            inCleaning = status.optInt("in_cleaning") == 1;
            errorCode = Error.valueOf(status.optInt("error_code"));
            state = State.valueOf(status.optInt("state"));
            battery = status.optInt("battery");
            msgSeq = status.optInt("msg_seq");
            cleanTime = status.optInt("clean_time");
        }
    }

    public JSONObject construct(){
        JSONObject payload = new JSONObject();
        payload.put("dnd_enabled", dndEnabled ? 1 : 0);
        payload.put("map_present", mapPresent ? 1 : 0);
        payload.put("clean_area", cleanArea);
        payload.put("fan_power", fanPower);
        payload.put("msg_ver", msgVersion);
        payload.put("in_cleaning", inCleaning ? 1 : 0);
        payload.put("error_code", errorCode.getCode());
        payload.put("state", state.getState());
        payload.put("battery", battery);
        payload.put("msg_seq", msgSeq);
        payload.put("clean_time", cleanTime);
        return payload;
    }

    public boolean isDndEnabled() {
        return dndEnabled;
    }

    public void setDndEnabled(boolean dndEnabled) {
        this.dndEnabled = dndEnabled;
    }

    public boolean isMapPresent() {
        return mapPresent;
    }

    public void setMapPresent(boolean mapPresent) {
        this.mapPresent = mapPresent;
    }

    public int getCleanArea() {
        return cleanArea;
    }

    public void setCleanArea(int cleanArea) {
        this.cleanArea = cleanArea;
    }

    public int getFanPower() {
        return fanPower;
    }

    public void setFanPower(int fanPower) {
        this.fanPower = fanPower;
    }

    public int getMsgVersion() {
        return msgVersion;
    }

    public void setMsgVersion(int msgVersion) {
        this.msgVersion = msgVersion;
    }

    public boolean isInCleaning() {
        return inCleaning;
    }

    public void setInCleaning(boolean inCleaning) {
        this.inCleaning = inCleaning;
    }

    public Error getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Error errorCode) {
        this.errorCode = errorCode;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getMsgSeq() {
        return msgSeq;
    }

    public void setMsgSeq(int msgSeq) {
        this.msgSeq = msgSeq;
    }

    public int getCleanTime() {
        return cleanTime;
    }

    public void setCleanTime(int cleanTime) {
        this.cleanTime = cleanTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacuumStatus that = (VacuumStatus) o;
        return dndEnabled == that.dndEnabled &&
                mapPresent == that.mapPresent &&
                cleanArea == that.cleanArea &&
                fanPower == that.fanPower &&
                msgVersion == that.msgVersion &&
                inCleaning == that.inCleaning &&
                battery == that.battery &&
                msgSeq == that.msgSeq &&
                cleanTime == that.cleanTime &&
                errorCode == that.errorCode &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dndEnabled, mapPresent, cleanArea, fanPower, msgVersion, inCleaning, errorCode, state, battery, msgSeq, cleanTime);
    }

    @Override
    public String toString() {
        return "VacuumStatus{" +
                "dndEnabled=" + dndEnabled +
                ", mapPresent=" + mapPresent +
                ", cleanArea=" + cleanArea +
                ", fanPower=" + fanPower +
                ", msgVersion=" + msgVersion +
                ", inCleaning=" + inCleaning +
                ", errorCode=" + errorCode.toString() +
                ", state=" + state.toString() +
                ", battery=" + battery +
                ", msgSeq=" + msgSeq +
                ", cleanTime=" + cleanTime +
                '}';
    }
}
