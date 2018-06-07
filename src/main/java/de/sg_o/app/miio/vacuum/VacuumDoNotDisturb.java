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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class VacuumDoNotDisturb implements Serializable {
    private static final long serialVersionUID = -3037903760872324136L;

    private LocalTime start;
    private LocalTime end;
    private boolean enabled;

    /**
     * Create a new do not disturb settings object.
     * @param start The start time of the do not disturb period.
     * @param end The end time of the the do not disturb period.
     * @param enabled Weather the timer should be enabled.
     */
    public VacuumDoNotDisturb(LocalTime start, LocalTime end, boolean enabled) {
        if (start == null) start = LocalTime.of(22, 0);
        this.start = start;
        if (end == null) end = LocalTime.of(8, 0);
        this.end = end;
        this.enabled = enabled;
    }

    /**
     * Create a new enabled do not disturb settings object.
     * @param start The start time of the do not disturb period.
     * @param end The end time of the the do not disturb period.
     */
    public VacuumDoNotDisturb(LocalTime start, LocalTime end) {
        if (start == null) start = LocalTime.of(22, 0);
        this.start = start;
        if (end == null) end = LocalTime.of(8, 0);
        this.end = end;
        this.enabled = true;
    }

    /**
     * Generate a new enabled do not disturb settings object from the response of a device.
     * @param dnd The response to parse.
     */
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

    /**
     * @return The start time of the do not disturb period.
     */
    public LocalTime getStart() {
        return start;
    }

    /**
     * @return The end time of the do not disturb period.
     */
    public LocalTime getEnd() {
        return end;
    }

    /**
     * @return Weather the do not disturb period is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled True to enable, false to disable the timer.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Construct the message the controlling device sends to the vacuum.
     * @return The constructed message.
     */
    public JSONArray construct() {
        return construct(false);
    }

    /**
     * Construct the message the vacuum sends to the controlling device or the device sends to the vacuum.
     * @param server True if the constructed message is in the format from vacuum to controlling device.
     * @return The constructed message.
     */
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
