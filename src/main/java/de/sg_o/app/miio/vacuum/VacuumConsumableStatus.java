/*
 * Copyright (c) 2018 Joerg Bayer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg_o.app.miio.vacuum;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class VacuumConsumableStatus implements Serializable {
    private static final long serialVersionUID = 8580701665863054538L;

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

    /**
     * Create a new consumable status object.
     * @param mainBrushWorkTime The time the main brush has been running.
     * @param sensorTimeSinceCleaning The time since the cliff sensors have been cleaned.
     * @param sideBrushWorkTime The time the side brush has been running.
     * @param filterWorkTime The time the filter has been in use.
     */
    public VacuumConsumableStatus(int mainBrushWorkTime, int sensorTimeSinceCleaning, int sideBrushWorkTime, int filterWorkTime) {
        this.mainBrushWorkTime = mainBrushWorkTime;
        this.sensorTimeSinceCleaning = sensorTimeSinceCleaning;
        this.sideBrushWorkTime = sideBrushWorkTime;
        this.filterWorkTime = filterWorkTime;
    }

    /**
     * Generate a new consumable status object from the response of a device.
     * @param consumables The response to parse.
     */
    public VacuumConsumableStatus(JSONObject consumables) {
        if (consumables != null) {
            this.mainBrushWorkTime = consumables.optInt(Names.MAIN_BRUSH.toString());
            this.sensorTimeSinceCleaning = consumables.optInt(Names.SENSOR.toString());
            this.sideBrushWorkTime = consumables.optInt(Names.SIDE_BRUSH.toString());
            this.filterWorkTime = consumables.optInt(Names.FILTER.toString());
        }
    }

    /**
     * Construct the message the vacuum sends to the controlling device.
     * @return The constructed message.
     */
    public JSONObject construct(){
        JSONObject payload = new JSONObject();
        payload.put(Names.MAIN_BRUSH.toString(), mainBrushWorkTime);
        payload.put(Names.SENSOR.toString(), sensorTimeSinceCleaning);
        payload.put(Names.SIDE_BRUSH.toString(), sideBrushWorkTime);
        payload.put(Names.FILTER.toString(), filterWorkTime);
        return payload;
    }

    /**
     * Reset a consumable.
     * @param name The consumable to reset.
     */
    public void reset(String name){
        if (name.equals(Names.MAIN_BRUSH.toString())) mainBrushWorkTime = 0;
        if (name.equals(Names.SENSOR.toString())) sensorTimeSinceCleaning = 0;
        if (name.equals(Names.SIDE_BRUSH.toString())) sideBrushWorkTime = 0;
        if (name.equals(Names.FILTER.toString())) filterWorkTime = 0;
    }

    /**
     * @return The time the main brush has been working for.
     */
    public int getMainBrushWorkTime() {
        return mainBrushWorkTime;
    }

    /**
     * @return The time since the last time the cliff sensors have been cleaned.
     */
    public int getSensorTimeSinceCleaning() {
        return sensorTimeSinceCleaning;
    }

    /**
     * @return The time the side brush has been working for.
     */
    public int getSideBrushWorkTime() {
        return sideBrushWorkTime;
    }

    /**
     * @return The time the filter has been in use.
     */
    public int getFilterWorkTime() {
        return filterWorkTime;
    }

    /**
     * @param mainBrushWorkTime The time the main brush has been working for.
     */
    public void setMainBrushWorkTime(int mainBrushWorkTime) {
        this.mainBrushWorkTime = mainBrushWorkTime;
    }

    /**
     * @param sensorTimeSinceCleaning The time since the last time the cliff sensors have been cleaned.
     */
    public void setSensorTimeSinceCleaning(int sensorTimeSinceCleaning) {
        this.sensorTimeSinceCleaning = sensorTimeSinceCleaning;
    }

    /**
     * @param sideBrushWorkTime The time the side brush has been working for.
     */
    public void setSideBrushWorkTime(int sideBrushWorkTime) {
        this.sideBrushWorkTime = sideBrushWorkTime;
    }

    /**
     * @param filterWorkTime The time the filter has been in use.
     */
    public void setFilterWorkTime(int filterWorkTime) {
        this.filterWorkTime = filterWorkTime;
    }

    /**
     * @return The time until the main brush should be changed.
     */
    public int getMainBrushWorkTimeLeft() {
        return MAIN_BRUSH_TIME_BETWEEN_CHANGE - mainBrushWorkTime;
    }

    /**
     * @return The time until the cliff sensors should be cleaned.
     */
    public int getSensorTimeSinceCleaningLeft() {
        return SENSOR_TIME_BETWEEN_CLEANING - sensorTimeSinceCleaning;
    }

    /**
     * @return The time until the side brush should be changed.
     */
    public int getSideBrushWorkTimeLeft() {
        return SIDE_BRUSH_TIME_BETWEEN_CHANGE - sideBrushWorkTime;
    }

    /**
     * @return The time until the filter should be changed.
     */
    public int getFilterWorkTimeLeft() {
        return FILTER_TIME_BETWEEN_CHANGE - filterWorkTime;
    }

    /**
     * @return The approximate usage of the main brush.
     */
    public float getMainBrushWorkPercent() {
        return ((float)mainBrushWorkTime) / ((float) MAIN_BRUSH_TIME_BETWEEN_CHANGE) * 100.0f;
    }

    /**
     * @return The approximate dirtiness of the cliff sensor.
     */
    public float getSensorPercentSinceCleaning() {
        return ((float)sensorTimeSinceCleaning) / ((float) SENSOR_TIME_BETWEEN_CLEANING) * 100.0f;
    }

    /**
     * @return The approximate usage of the side brush.
     */
    public float getSideBrushWorkPercent() {
        return ((float)sideBrushWorkTime) / ((float) SIDE_BRUSH_TIME_BETWEEN_CHANGE) * 100.0f;
    }

    /**
     * @return The approximate dirtiness of the filter.
     */
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
