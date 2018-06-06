package de.sg_o.app.miio.vacuum;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class VacuumSounpackInstallState {
    public enum State {
        UNKNOWN(0),
        DOWNLOAD(1),
        INSTALLING(2),
        DONE(3),
        ERROR(4);
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
        ERROR_1(1),
        DOWNLOAD(2),
        CHECKSUM(3),
        ERROR_4(4),
        UNKNOWN(5);
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

    private int progress;
    private State state;
    private Error error;
    private int sid;

    /**
     * Create a new sounpack installation status object.
     * @param progress The installation progress. Must be between 0 including and 100 including.
     * @param state The installation status.
     * @param error The installation error.
     * @param sid The sounpack language id.
     */
    public VacuumSounpackInstallState(int progress, State state, Error error, int sid) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        this.progress = progress;
        if (state == null) state = State.UNKNOWN;
        this.state = state;
        if (error == null) error = Error.UNKNOWN;
        this.error = error;
        this.sid = sid;
    }

    /**
     * Generate a new sounpack installation status object from the response of a device.
     * @param info The response to parse.
     */
    public VacuumSounpackInstallState(JSONObject info) {
        if (info == null) {
            this.progress = 0;
            this.state = State.UNKNOWN;
            this.error = Error.UNKNOWN;
            this.sid = -1;
        } else {
            this.progress = info.optInt("progress", 0);
            this.state = State.valueOf(info.optInt("state", State.UNKNOWN.getState()));
            this.error = Error.valueOf(info.optInt("error", Error.UNKNOWN.getCode()));
            this.sid = info.optInt("sid_in_progress", -1);
        }
    }

    /**
     * @return The installation progress.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @param progress The installation progress. Must be between 0 including and 100 including.
     */
    public void setProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        this.progress = progress;
    }

    /**
     * @return The installation status.
     */
    public State getState() {
        return state;
    }

    /**
     * @param state The installation status.
     */
    public void setState(State state) {
        if (state == null) state = State.UNKNOWN;
        this.state = state;
    }

    /**
     * @return The installation error.
     */
    public Error getError() {
        return error;
    }

    /**
     * @param error The installation error.
     */
    public void setError(Error error) {
        if (error == null) error = Error.UNKNOWN;
        this.error = error;
    }

    /**
     * @return The sounpack language id.
     */
    public int getSid() {
        return sid;
    }

    /**
     * @param sid The sounpack language id.
     */
    public void setSid(int sid) {
        if (error == null) error = Error.UNKNOWN;
        this.sid = sid;
    }

    /**
     * Construct the message the vacuum sends to the controlling device.
     * @param includeSid Whether to include the sounpack language id.
     * @return The constructed message.
     */
    public JSONObject construct(boolean includeSid){
        JSONObject ret = new JSONObject();
        ret.put("progress", progress);
        ret.put("state", state.getState());
        ret.put("error", error.getCode());
        if (includeSid) ret.put("sid_in_progress", sid);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacuumSounpackInstallState that = (VacuumSounpackInstallState) o;
        return progress == that.progress &&
                sid == that.sid &&
                state == that.state &&
                error == that.error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(progress, state, error, sid);
    }

    @Override
    public String toString() {
        return "VacuumSounpackInstallState{" +
                "progress=" + progress +
                ", state=" + state.toString() +
                ", error=" + error.toString() +
                ", sid=" + sid +
                '}';
    }
}
