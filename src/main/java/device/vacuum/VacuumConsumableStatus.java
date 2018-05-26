package device.vacuum;

import org.json.JSONObject;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class VacuumConsumableStatus {
    public enum Names {
        MAIN_BRUSH("main_brush_work_time"),
        SIDE_BRUSH("side_brush_work_time"),
        FILTER("filter_work_time"),
        SENSOR("sensor_dirty_time");

        private final String name;

        Names(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final int MAIN_BRUSH_TIME_BETWEEN_CHANGE = 300 * 3600;
    public static final int SIDE_BRUSH_TIME_BETWEEN_CHANGE = 200 * 3600;
    public static final int FILTER_TIME_BETWEEN_CHANGE = 150 * 3600;
    public static final int SENSOR_TIME_BETWEEN_CLEANING = 30 * 3600;

    private int mainBrushWorkTime = 0;
    private int sensorTimeSinceCleaning = 0;
    private int sideBrushWorkTime = 0;
    private int filterWorkTime = 0;

    public VacuumConsumableStatus(int mainBrushWorkTime, int sensorTimeSinceCleaning, int sideBrushWorkTime, int filterWorkTime) {
        this.mainBrushWorkTime = mainBrushWorkTime;
        this.sensorTimeSinceCleaning = sensorTimeSinceCleaning;
        this.sideBrushWorkTime = sideBrushWorkTime;
        this.filterWorkTime = filterWorkTime;
    }

    public VacuumConsumableStatus(JSONObject consumables) {
        if (consumables != null) {
            this.mainBrushWorkTime = consumables.optInt(Names.MAIN_BRUSH.toString());
            this.sensorTimeSinceCleaning = consumables.optInt(Names.SENSOR.toString());
            this.sideBrushWorkTime = consumables.optInt(Names.SIDE_BRUSH.toString());
            this.filterWorkTime = consumables.optInt(Names.FILTER.toString());
        }
    }

    public JSONObject construct(){
        JSONObject payload = new JSONObject();
        payload.put(Names.MAIN_BRUSH.toString(), mainBrushWorkTime);
        payload.put(Names.SENSOR.toString(), sensorTimeSinceCleaning);
        payload.put(Names.SIDE_BRUSH.toString(), sideBrushWorkTime);
        payload.put(Names.FILTER.toString(), filterWorkTime);
        return payload;
    }

    public void reset(String name){
        if (name.equals(Names.MAIN_BRUSH.toString())) mainBrushWorkTime = 0;
        if (name.equals(Names.SENSOR.toString())) sensorTimeSinceCleaning = 0;
        if (name.equals(Names.SIDE_BRUSH.toString())) sideBrushWorkTime = 0;
        if (name.equals(Names.FILTER.toString())) filterWorkTime = 0;
    }

    public int getMainBrushWorkTime() {
        return mainBrushWorkTime;
    }

    public int getSensorTimeSinceCleaning() {
        return sensorTimeSinceCleaning;
    }

    public int getSideBrushWorkTime() {
        return sideBrushWorkTime;
    }

    public int getFilterWorkTime() {
        return filterWorkTime;
    }

    public void setMainBrushWorkTime(int mainBrushWorkTime) {
        this.mainBrushWorkTime = mainBrushWorkTime;
    }

    public void setSensorTimeSinceCleaning(int sensorTimeSinceCleaning) {
        this.sensorTimeSinceCleaning = sensorTimeSinceCleaning;
    }

    public void setSideBrushWorkTime(int sideBrushWorkTime) {
        this.sideBrushWorkTime = sideBrushWorkTime;
    }

    public void setFilterWorkTime(int filterWorkTime) {
        this.filterWorkTime = filterWorkTime;
    }

    public int getMainBrushWorkTimeLeft() {
        return MAIN_BRUSH_TIME_BETWEEN_CHANGE - mainBrushWorkTime;
    }

    public int getSensorTimeSinceCleaningLeft() {
        return SENSOR_TIME_BETWEEN_CLEANING - sensorTimeSinceCleaning;
    }

    public int getSideBrushWorkTimeLeft() {
        return SIDE_BRUSH_TIME_BETWEEN_CHANGE - sideBrushWorkTime;
    }

    public int getFilterWorkTimeLeft() {
        return FILTER_TIME_BETWEEN_CHANGE - filterWorkTime;
    }

    public float getMainBrushWorkPercent() {
        return ((float)mainBrushWorkTime) / ((float) MAIN_BRUSH_TIME_BETWEEN_CHANGE) * 100.0f;
    }

    public float getSensorPercentSinceCleaning() {
        return ((float)sensorTimeSinceCleaning) / ((float) SENSOR_TIME_BETWEEN_CLEANING) * 100.0f;
    }

    public float getSideBrushWorkPercent() {
        return ((float)sideBrushWorkTime) / ((float) SIDE_BRUSH_TIME_BETWEEN_CHANGE) * 100.0f;
    }

    public float getFilterWorkPercent() {
        return ((float)filterWorkTime) / ((float) FILTER_TIME_BETWEEN_CHANGE) * 100.0f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacuumConsumableStatus that = (VacuumConsumableStatus) o;
        return mainBrushWorkTime == that.mainBrushWorkTime &&
                sensorTimeSinceCleaning == that.sensorTimeSinceCleaning &&
                sideBrushWorkTime == that.sideBrushWorkTime &&
                filterWorkTime == that.filterWorkTime;
    }

    @Override
    public int hashCode() {

        return Objects.hash(mainBrushWorkTime, sensorTimeSinceCleaning, sideBrushWorkTime, filterWorkTime);
    }

    @Override
    public String toString() {
        return "VacuumConsumableStatus{" +
                "mainBrushWorkTime=" + mainBrushWorkTime +
                ", sensorTimeSinceCleaning=" + sensorTimeSinceCleaning +
                ", sideBrushWorkTime=" + sideBrushWorkTime +
                ", filterWorkTime=" + filterWorkTime +
                '}';
    }
}
