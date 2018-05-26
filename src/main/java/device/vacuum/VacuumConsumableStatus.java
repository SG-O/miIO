package device.vacuum;

import org.json.JSONObject;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class VacuumConsumableStatus {
    public static final String MAIN_BRUSH_NAME = "main_brush_work_time";
    public static final String SIDE_BRUSH_NAME = "side_brush_work_time";
    public static final String FILTER_NAME = "filter_work_time";
    public static final String SENSOR_NAME = "sensor_dirty_time";

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
            this.mainBrushWorkTime = consumables.optInt(MAIN_BRUSH_NAME);
            this.sensorTimeSinceCleaning = consumables.optInt(SENSOR_NAME);
            this.sideBrushWorkTime = consumables.optInt(SIDE_BRUSH_NAME);
            this.filterWorkTime = consumables.optInt(FILTER_NAME);
        }
    }

    public JSONObject construct(){
        JSONObject payload = new JSONObject();
        payload.put(MAIN_BRUSH_NAME, mainBrushWorkTime);
        payload.put(SENSOR_NAME, sensorTimeSinceCleaning);
        payload.put(SIDE_BRUSH_NAME, sideBrushWorkTime);
        payload.put(FILTER_NAME, filterWorkTime);
        return payload;
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
