package device.vacuum;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.Objects;

public class VacuumDoNotDisturb {
    private LocalTime start;
    private LocalTime end;
    private boolean enabled;

    public VacuumDoNotDisturb(LocalTime start, LocalTime end, boolean enabled) {
        if (start == null) start = LocalTime.of(22, 0);
        this.start = start;
        if (end == null) end = LocalTime.of(8, 0);
        this.end = end;
        this.enabled = enabled;
    }

    public VacuumDoNotDisturb(LocalTime start, LocalTime end) {
        if (start == null) start = LocalTime.of(22, 0);
        this.start = start;
        if (end == null) end = LocalTime.of(8, 0);
        this.end = end;
        this.enabled = true;
    }

    public VacuumDoNotDisturb(JSONArray dnd){
        JSONObject obj = dnd.optJSONObject(0);
        if (obj != null){
            start = LocalTime.of(obj.optInt("start_hour"), obj.optInt("start_minute"));
            end = LocalTime.of(obj.optInt("end_hour"), obj.optInt("end_minute"));
            enabled = obj.optInt("enabled", 1) == 1;
        } else {
            start = LocalTime.of(dnd.optInt(0), dnd.optInt(1));
            end = LocalTime.of(dnd.optInt(2), dnd.optInt(3));
            enabled = true;
        }
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public JSONArray construct() {
        return construct(false);
    }

    public JSONArray construct(boolean server) {
        JSONArray dnd = new JSONArray();
        if (server){
            JSONObject srv = new JSONObject();
            srv.put("start_hour", start.getHour());
            srv.put("start_minute", start.getMinute());
            srv.put("end_hour", end.getHour());
            srv.put("end_minute", end.getMinute());
            srv.put("enabled", enabled ? 1 : 0);
            dnd.put(srv);
        } else {
            dnd.put(start.getHour());
            dnd.put(start.getMinute());
            dnd.put(end.getHour());
            dnd.put(end.getMinute());
        }
        return dnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacuumDoNotDisturb that = (VacuumDoNotDisturb) o;
        return enabled == that.enabled &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, enabled);
    }

    @Override
    public String toString() {
        return "VacuumDoNotDisturb{" +
                "start=" + start +
                ", end=" + end +
                ", enabled=" + enabled +
                '}';
    }
}
