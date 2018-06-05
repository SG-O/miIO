package device.vacuum;

import base.CommandExecutionException;
import org.json.JSONArray;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class VacuumTimer {
    private String ID;
    private boolean enabled;
    private LocalTime time;
    private Set<DayOfWeek> runDays;
    private JSONArray job;

    /**
     * Create a new vacuum timer object.
     * @param ID The timer ID. If null a new DI will be created from the current system time.
     * @param enabled Whether the timer is enabled.
     * @param time The time to start the job.
     * @param runDays The days to run the job at. If null or an empty set, it will be run every day.
     * @param job The job description.
     */
    public VacuumTimer(String ID, boolean enabled, LocalTime time, Set<DayOfWeek> runDays, JSONArray job) {
        if (ID == null) ID = Long.valueOf(System.currentTimeMillis()).toString();
        this.ID = ID;
        this.enabled = enabled;
        this.time = time;
        if (runDays == null) runDays = new HashSet<>();
        this.runDays = runDays;
        this.job = job;
    }

    /**
     * Create a new vacuum timer object.
     * @param ID The timer ID. If null a new DI will be created from the current system time.
     * @param enabled Whether the timer is enabled.
     * @param hour The hour to start the job.
     * @param minute The minute to start the job at.
     * @param runDays The days to run the job at. If null or an empty set, it will be run every day.
     */
    public VacuumTimer(String ID, boolean enabled, int hour, int minute, Set<DayOfWeek> runDays) {
        if (ID == null) ID = Long.valueOf(System.currentTimeMillis()).toString();
        this.ID = ID;
        this.enabled = enabled;
        if (hour > 23) hour = 23;
        if (hour < 0) hour = 0;
        if (minute > 59) minute = 59;
        if (minute < 0) minute = 0;
        this.time = LocalTime.of(hour,minute);
        if (runDays == null) runDays = new HashSet<>();
        this.runDays = runDays;
    }

    /**
     * Generate a new vacuum timer object from the response of a device.
     * @param timer The response to parse.
     * @throws CommandExecutionException WHen the response was invalid.
     */
    public VacuumTimer(JSONArray timer) throws CommandExecutionException {
        if (timer == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        int length = timer.length();
        this.runDays = new HashSet<>();
        this.ID = timer.optString(0, Long.valueOf(System.currentTimeMillis()).toString());
        JSONArray jobArray;
        if (length > 2) {
            this.enabled = timer.optString(1).equals("on");
            jobArray = timer.optJSONArray(2);
        } else {
            this.enabled = true;
            jobArray = timer.optJSONArray(1);
        }
        if (jobArray != null){
            String cron = jobArray.optString(0, null);
            if (cron == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
            parseCron(cron);
            this.job = jobArray.optJSONArray(1);
        } else {
            throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        }
    }

    private void parseCron(String cron) throws CommandExecutionException {
        if (cron == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        String[] values = cron.split("\\s+");
        if (values.length != 5) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        try {
            int minute = Integer.valueOf(values[0]);
            int hour = Integer.valueOf(values[1]);
            this.time = LocalTime.of(hour, minute);
        } catch (NumberFormatException ignore){
            throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        }
        String[] dayValues = values[4].split(",");
        if (dayValues.length > 0) {
            if (!dayValues[0].equals("*")) {
                try {
                    for (String day : dayValues) {
                        int d = Integer.valueOf(day);
                        if (d > 6) continue;
                        if (d == 0) d = 7;
                        runDays.add(DayOfWeek.of(d));
                    }
                } catch (NumberFormatException ignore) {
                    throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
                }
            }
        }
    }

    private String generateCron(){
        if (time == null) return null;
        StringBuilder days = new StringBuilder();
        if (runDays.size() > 0){
            for (DayOfWeek d : runDays) {
                int dValue = d.getValue();
                if (dValue == 7) dValue = 0;
                days.append(dValue);
                days.append(",");
            }
            days.delete(days.length() - 1, days.length());
        } else {
            days.append("*");
        }
        return time.getMinute() + " " + time.getHour() + " * * " + days.toString();
    }

    /**
     * Construct the message the controlling device sends to the vacuum.
     * @return The constructed message.
     */
    public JSONArray construct(){
        return construct(false);
    }

    /**
     * Construct the message the vacuum sends to the controlling device or the device sends to the vacuum.
     * @param server True if the constructed message is in the format from vacuum to controlling device.
     * @return The constructed message.
     */
    public JSONArray construct(boolean server){
        JSONArray jobArray = new JSONArray();
        String cron = generateCron();
        if (cron == null) return null;
        jobArray.put(cron);
        if (server) {
            if (job == null) return null;
            jobArray.put(job);
        } else {
            JSONArray empty = new JSONArray();
            empty.put("");
            empty.put("");
            jobArray.put(empty);
        }
        JSONArray timer = new JSONArray();
        timer.put(ID);
        if (server) timer.put(getOnOff());
        timer.put(jobArray);
        return timer;
    }

    /**
     * @return The timers ID.
     */
    public String getID() {
        return ID;
    }

    /**
     * @return Whether the timer is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return Whether the timer is enabled Either "on" or "fff".
     */
    public String getOnOff(){
        return enabled ? "on" : "off";
    }

    /**
     * @param enabled Whether the timer is enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return The time to start the job.
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * @return The days to run the job at. If an empty set, it will be run every day.
     */
    public Set<DayOfWeek> getRunDays() {
        return runDays;
    }

    /**
     * @return The job description.
     */
    public JSONArray getJob() {
        return job;
    }

    /**
     * @param job The job description.
     */
    public void setJob(JSONArray job) {
        this.job = job;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacuumTimer that = (VacuumTimer) o;
        return enabled == that.enabled &&
                Objects.equals(ID, that.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, enabled);
    }

    @Override
    public String toString() {
        return "VacuumTimer{" +
                "ID='" + ID + '\'' +
                ", enabled=" + enabled +
                ", time=" + time +
                ", runDays=" + runDays +
                ", job=" + job +
                '}';
    }
}
