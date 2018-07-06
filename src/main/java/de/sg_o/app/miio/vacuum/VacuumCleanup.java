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

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;

import java.io.Serializable;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class VacuumCleanup implements Serializable {
    private static final long serialVersionUID = -4764098548055264774L;
    private Instant start;
    private Instant end;
    private long runtime;
    private long area;
    private boolean completed;

    /**
     * Create a new cleanup information object.
     * @param start The time the cleanup was started.
     * @param end The time the cleanup was finished.
     * @param runtime The total runtime of this cleanup.
     * @param area The cleaned area.
     * @param completed Weather this cleanup was completed.
     */
    public VacuumCleanup(Instant start, Instant end, long runtime, long area, boolean completed) {
        if (start == null || end == null || runtime < 0){
            start = Instant.now();
            end = start.plus(1000);
            runtime = 1;
        }
        this.start = start;
        this.end = end;
        this.runtime = runtime;
        if (area < 0) area = 0;
        this.area = area;
        this.completed = completed;
    }

    /**
     * Generate a new cleanup information object from the response of a device.
     * @param msg The response to parse.
     */
    public VacuumCleanup(JSONArray msg) {
        if (msg == null){
            this.start = Instant.now();
            this.end = start.plus(1000);
            this.runtime = 1;
        } else {
            this.start = Instant.ofEpochSecond(msg.optLong(0, Instant.now().getMillis()));
            this.end = Instant.ofEpochSecond(msg.optLong(1, start.plus(1000).getMillis()));
            this.runtime = msg.optLong(2, 1);
            this.area = msg.optLong(3);
            this.completed = msg.optInt(5) == 1;
        }
    }

    /**
     * @return The start time of the cleanup.
     */
    public Instant getStart() {
        return start;
    }

    /**
     * @return The end time of the cleanup.
     */
    public Instant getEnd() {
        return end;
    }

    /**
     * @return The total runtime of the cleanup.
     */
    public long getRuntime() {
        return runtime;
    }

    /**
     * @return The cleaned area.
     */
    public long getArea() {
        return area;
    }

    /**
     * @return True if the cleanup has been completed successfully.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Construct the message the vacuum sends to the controlling device.
     * @return The constructed message.
     */
    public JSONArray construct(){
        JSONArray ret = new JSONArray();
        ret.put(start.getMillis() / 1000);
        ret.put(end.getMillis() / 1000);
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
                "start=" + start.toString(ISODateTimeFormat.dateTimeNoMillis()) +
                ", end=" + end.toString(ISODateTimeFormat.dateTimeNoMillis()) +
                ", runtime=" + runtime +
                ", area=" + area +
                ", completed=" + completed +
                '}';
    }
}
