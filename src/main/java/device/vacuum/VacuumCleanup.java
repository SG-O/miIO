package device.vacuum;

import org.json.JSONArray;

import java.time.Instant;
import java.util.Objects;

public class VacuumCleanup {
    private Instant start;
    private Instant end;
    private long runtime;
    private long area;
    private boolean completed;

    public VacuumCleanup(Instant start, Instant end, long runtime, long area, boolean completed) {
        if (start == null || end == null || runtime < 0){
            start = Instant.now();
            end = start.plusSeconds(1);
            runtime = 1;
        }
        this.start = start;
        this.end = end;
        this.runtime = runtime;
        if (area < 0) area = 0;
        this.area = area;
        this.completed = completed;
    }

    public VacuumCleanup(JSONArray msg) {
        if (msg == null){
            this.start = Instant.now();
            this.end = start.plusSeconds(1);
            this.runtime = 1;
        } else {
            this.start = Instant.ofEpochSecond(msg.optLong(0, Instant.now().getEpochSecond()));
            this.end = Instant.ofEpochSecond(msg.optLong(1, start.plusSeconds(1).getEpochSecond()));
            this.runtime = msg.optLong(2, 1);
            this.area = msg.optLong(3);
            this.completed = msg.optInt(5) == 1;
        }
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public long getRuntime() {
        return runtime;
    }

    public long getArea() {
        return area;
    }

    public boolean isCompleted() {
        return completed;
    }

    public JSONArray construct(){
        JSONArray ret = new JSONArray();
        ret.put(start.getEpochSecond());
        ret.put(end.getEpochSecond());
        ret.put(runtime);
        ret.put(area);
        ret.put(0);
        ret.put(completed ? 1 : 0);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacuumCleanup that = (VacuumCleanup) o;
        return runtime == that.runtime &&
                area == that.area &&
                completed == that.completed &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, runtime, area, completed);
    }

    @Override
    public String toString() {
        return "VacuumCleanup{" +
                "start=" + start +
                ", end=" + end +
                ", runtime=" + runtime +
                ", area=" + area +
                ", completed=" + completed +
                '}';
    }
}
