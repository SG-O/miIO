package device.vacuum;

import base.CommandExecutionException;
import base.Device;
import base.Token;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.net.InetAddress;
import java.time.ZoneId;

public class Vacuum extends Device {
    private  int manualControlSequence = -1;

    /**
     * Create an object for communicating with the Mi Robot an the Roborock.
     * @param ip The IP address of the device to connect to. If the address is null the first device that is an acceptableModel will be chosen.
     * @param token The token for that device. If the token is null the token will be extracted from unprovisioned devices.
     * @param timeout The timeout for the communication
     * @param retries The number of retries after a failed communication
     */
    public Vacuum(InetAddress ip, Token token, int timeout, int retries) {
        super(ip, token, new String[]{"rockrobo.vacuum.v1", "rockrobo.vacuum.v2"}, timeout, retries);
    }

    /**
     * Get the vacuums status.
     * @return The vacuums status.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public VacuumStatus status() throws CommandExecutionException {
        JSONArray resp = sendToArray("get_status");
        JSONObject stat = resp.optJSONObject(0);
        if (stat == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return new VacuumStatus(stat);
    }

    /**
     * Get the vacuums current timezone.
     * @return The current timezone.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public ZoneId getTimezone() throws CommandExecutionException {
        JSONArray resp = sendToArray("get_timezone");
        String zone = resp.optString(0, null);
        if (zone == null ) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return ZoneId.of(zone);
    }

    /**
     * Set the vacuums timezone
     * @param zone The new timezone to set.
     * @return True if the command has been received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setTimezone(ZoneId zone) throws CommandExecutionException {
        JSONArray tz = new JSONArray();
        tz.put(zone.getId());
        return sendOk("set_timezone", tz);
    }

    /**
     * Get the vacuums consumables status.
     * @return The consumables status.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public VacuumConsumableStatus consumableStatus() throws CommandExecutionException {
        JSONArray resp = sendToArray("get_consumable");
        JSONObject stat = resp.optJSONObject(0);
        if (stat == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return new VacuumConsumableStatus(stat);
    }

    /**
     * Reset a vacuums consumable.
     * @param consumable The consumable to reset
     * @return True if the consumable has been reset.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean resetConsumable(VacuumConsumableStatus.Names consumable) throws CommandExecutionException {
        JSONArray params = new JSONArray();
        params.put(consumable.toString());
        return sendOk("reset_consumable", params);
    }

    /**
     * Start the cleanup.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean start() throws CommandExecutionException {
        return sendOk("app_start");
    }

    /**
     * Pause the cleanup.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean pause() throws CommandExecutionException {
        return sendOk("app_pause");
    }

    /**
     * Stop the cleanup.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean stop() throws CommandExecutionException {
        return sendOk("app_stop");
    }

    /**
     * Send the vacuum to its docking station.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean home() throws CommandExecutionException {
        stop();
        return sendOk("app_charge");
    }

    /**
     * Clean the area around the vacuums current position
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean spotCleaning() throws CommandExecutionException {
        return sendOk("app_spot");
    }

    /**
     * Make the vacuum make a sound to find it
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean findMe() throws CommandExecutionException {
        return sendOk("find_me");
    }

    /**
     * Get the vacuums current fan speed setting.
     * @return The fan speed.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getFanSpeed() throws CommandExecutionException {
        int resp = sendToArray("get_custom_mode").optInt(0, -1);
        if ((resp < 0) || (resp > 100)) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return resp;
    }

    /**
     * Set the vacuums fan speed setting.
     * @param speed The new speed to set.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setFanSpeed(int speed) throws CommandExecutionException {
        JSONArray params = new JSONArray();
        params.put(speed);
        return sendOk("set_custom_mode", params);
    }

    /**
     * Get all stored scheduled cleanups.
     * @return An array with all timers. Is empty if no timer has been set on the device.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public VacuumTimer[] getTimers() throws CommandExecutionException {
        JSONArray tm = sendToArray("get_timer");
        if (tm == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        VacuumTimer[] timers = new VacuumTimer[tm.length()];
        for (int i = 0; i < tm.length(); i++){
            timers[i] = new VacuumTimer(tm.optJSONArray(i));
        }
        return timers;
    }

    /**
     * Add a new scheduled cleanup.
     * @param timer The new timer to set.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean addTimer(VacuumTimer timer) throws CommandExecutionException {
        if (timer == null) return false;
        JSONArray tm = timer.construct();
        if (tm == null) return false;
        JSONArray payload = new JSONArray();
        payload.put(tm);
        return sendOk("set_timer", payload);
    }

    /**
     * Enable or disable a scheduled cleanup.
     * @param timer The timer to update.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setTimerEnabled(VacuumTimer timer) throws CommandExecutionException {
        if (timer == null) return false;
        JSONArray payload = new JSONArray();
        payload.put(timer.getID());
        payload.put(timer.getOnOff());
        return sendOk("upd_timer", payload);
    }

    /**
     * Delete a scheduled cleanup.
     * @param timer The timer to remove.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean removeTimer(VacuumTimer timer) throws CommandExecutionException {
        if (timer == null) return false;
        JSONArray payload = new JSONArray();
        payload.put(timer.getID());
        return sendOk("del_timer", payload);
    }

    /**
     * Get the current do not disturb settings from the device
     * @return The do not disturb settings.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public VacuumDoNotDisturb getDoNotDisturb() throws CommandExecutionException {
        return new VacuumDoNotDisturb(sendToArray("get_dnd_timer"));
    }

    /**
     * Set the do not disturb timer.
     * @param dnd The new settings to set.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setDoNotDisturb(VacuumDoNotDisturb dnd) throws CommandExecutionException {
        return sendOk("set_dnd_timer", dnd.construct());
    }

    /**
     * Disable the do not disturb timer.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean disableDoNotDisturb() throws CommandExecutionException {
        return sendOk("close_dnd_timer");
    }

    /**
     * Go to the specified position on the map.
     * @param x The x position to move to in pixel. 512 pixel is the center of the map.
     * @param y The y position to move to in pixel. 512 pixel is the center of the map.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean goToMapPosition(int x, int y) throws CommandExecutionException {
        y = (y - 512) * -1 + 512;
        return goTo(x * 0.05f, y * 0.05f);
    }

    /**
     * Go to the specified position on the map.
     * @param x The x position to move to in meters. 25.6 meters is the center of the map.
     * @param y The y position to move to in meters. 25.6 meters is the center of the map.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean goTo(float x, float y) throws CommandExecutionException {
        Point p = new Point();
        p.setLocation(x * 1000d, y * 1000d);
        return goTo(p);
    }

    /**
     * Go to the specified position on the map.
     * @param p The position to move to in millimeters. 25600 millimeter both in x and y is the center of the map. The origin of the map is 0 millimeters in x and y.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean goTo(Point p) throws CommandExecutionException {
        if (p == null) return false;
        JSONArray payload = new JSONArray();
        payload.put(p.x);
        payload.put(p.y);
        return sendOk("app_goto_target" , payload);
    }

    /**
     * Clean the specified area on the map.
     * @param x0 The x position of the first point defining the area in pixel. 512 pixel is the center of the map.
     * @param y0 The y position of the first point defining the area in pixel. 512 pixel is the center of the map.
     * @param x1 The x position of the second point defining the area in pixel. 512 pixel is the center of the map.
     * @param y1 The y position of the second point defining the area in pixel. 512 pixel is the center of the map.
     * @param passes The number of times to clean this area.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean cleanAreaFromMap(int x0, int y0, int x1, int y1, int passes) throws CommandExecutionException {
        y0 = (y0 - 512) * -1 + 512;
        y1 = (y1 - 512) * -1 + 512;
        return cleanArea(x0 * 0.05f, y0 * 0.05f, x1 * 0.05f, y1 * 0.05f, passes);
    }

    /**
     * Clean the specified area on the map.
     * @param x0 The x position of the first point defining the area in meters. 25.6 meters is the center of the map.
     * @param y0 The y position of the first point defining the area in meters. 25.6 meters is the center of the map.
     * @param x1 The x position of the second point defining the area in meters. 25.6 meters is the center of the map.
     * @param y1 The y position of the second point defining the area in meters. 25.6 meters is the center of the map.
     * @param passes The number of times to clean this area.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean cleanArea(float x0, float y0, float x1, float y1, int passes) throws CommandExecutionException {
        float t;
        if (x0 > x1) {
            t = x0;
            x0 = x1;
            x1 = t;

        }
        if (y0 > y1) {
            t = y0;
            y0 = y1;
            y1 = t;

        }
        Point p0 = new Point();
        p0.setLocation(x0 * 1000d, y0 * 1000d);
        Point p1 = new Point();
        p1.setLocation(x1 * 1000d, y1 * 1000d);
        return cleanArea(p0, p1, passes);
    }

    /**
     * Clean the specified area on the map.
     * @param bottomLeft The bottom left position of the area in millimeters. 25600 millimeter both in x and y is the center of the map. The origin of the map is 0 millimeters in x and y.
     * @param topRight The top right position of the area in millimeters. 25600 millimeter both in x and y is the center of the map. The origin of the map is 0 millimeters in x and y.
     * @param passes The number of times to clean this area.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean cleanArea(Point bottomLeft, Point topRight, int passes) throws CommandExecutionException {
        if (bottomLeft == null || topRight == null || passes < 1) return false;
        JSONArray payload = new JSONArray();
        payload.put(bottomLeft.x);
        payload.put(bottomLeft.y);
        payload.put(topRight.x);
        payload.put(topRight.y);
        payload.put(passes);
        JSONArray wrapper = new JSONArray();
        wrapper.put(payload);
        return sendOk("app_zoned_clean" , wrapper);
    }

    private JSONArray getCleaningSummary() throws CommandExecutionException {
        return sendToArray("get_clean_summary");
    }

    /**
     * Get the total time the vacuum has been cleaning.
     * @return The time the device spent cleaning.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public long getTotalCleaningTime() throws CommandExecutionException {
        return getCleaningSummary().optLong(0);
    }

    /**
     * Get the vacuums total cleaned area.
     * @return The total area the device has cleaned.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public long getTotalCleanedArea() throws CommandExecutionException {
        return getCleaningSummary().optLong(1);
    }

    /**
     * Get the total number of cleanups the vacuum has performed.
     * @return The number of cleanups the device has performed.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public long getTotalCleans() throws CommandExecutionException {
        return getCleaningSummary().optLong(2);
    }

    /**
     * Get an array with the details of all cleanups.
     * @return An array with the details of all cleanups. Empty if no cleanups were performed.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public VacuumCleanup[] getAllCleanups() throws CommandExecutionException {
        JSONArray cleanupIDs = getCleaningSummary().optJSONArray(3);
        if (cleanupIDs == null) return null;
        VacuumCleanup[] res = new VacuumCleanup[cleanupIDs.length()];
        for (int i = 0; i < cleanupIDs.length(); i++){
            JSONArray send = new JSONArray();
            send.put(cleanupIDs.optLong(i));
            JSONArray ar = sendToArray("get_clean_record", send).optJSONArray(0);
            res[i] = new VacuumCleanup(ar);
        }
        return res;
    }

    /**
     * Get the current volume.
     * @return The current set volume of the device between 0 and 100.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public int getSoundVolume() throws CommandExecutionException {
        JSONArray res = sendToArray("get_sound_volume");
        if (res == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        int vol = res.optInt(0, -1);
        if (vol < 0) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return vol;
    }

    /**
     * Set the vacuums volume.
     * @param volume The volume between 0 and 100.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setSoundVolume(int volume) throws CommandExecutionException {
        if (volume < 0) volume = 0;
        if (volume > 100) volume = 100;
        JSONArray payload = new JSONArray();
        payload.put(volume);
        return sendOk("change_sound_volume", payload);
    }

    /** Make the vacuum produce a test sound.
     * @return True if the command was received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean testSoundVolume() throws CommandExecutionException {
        return sendOk("test_sound_volume");
    }

    /**
     * Enable manual remote controls.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean manualControlStart() throws CommandExecutionException {
        manualControlSequence = 1;
        return sendOk("app_rc_start");
    }

    /**
     * Disable manual remote controls.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean manualControlStop() throws CommandExecutionException {
        manualControlSequence = -1;
        return sendOk("app_rc_end");
    }

    /**
     * Manually control the robot
     * @param rotationSpeed The speed of rotation in deg/s.
     * @param speed The speed of the robot in m/s. Must be greater then -0.3 and less then 0.3.
     * @param runDuration The time to run the command in ms.
     * @return True if the command has been received correctly.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean manualControlMove(float rotationSpeed, float speed, int runDuration) throws CommandExecutionException {
        if (manualControlSequence < 1) manualControlStart();
        JSONObject payload = new JSONObject();
        if (rotationSpeed > 180.0f) rotationSpeed = 180.0f;
        if (rotationSpeed < -180.0f) rotationSpeed = -180.0f;
        float rotationRadians = Math.round((rotationSpeed / 180.0f) * 272.0d) / 100.0f; //Found out after LOADS of tests.
        payload.put("omega", rotationRadians);
        if (speed >= 0.3f) speed = 0.29f;
        if (speed <= -0.3f) speed = -0.29f;
        payload.put("velocity", speed);
        if (runDuration < 0) runDuration = 1000;
        payload.put("duration", runDuration);
        payload.put("seqnum", manualControlSequence);
        manualControlSequence++;
        JSONArray send = new JSONArray();
        send.put(payload);
        return sendOk("app_rc_move", send);
    }

    /**
     * Install a new soundpack to the device.
     * @param url The url the soundpack should be downloaded from.
     * @param md5 The md5 checksum of the new soundpack.
     * @param soundId The ID of the soundpacks language.
     * @return The current installation status.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public VacuumSounpackInstallState installSoundpack(String url, String md5, int soundId) throws CommandExecutionException {
        if (url == null || md5 == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_PARAMETERS);
        JSONObject install = new JSONObject();
        install.put("url", url);
        install.put("md5", md5);
        install.put("sid", soundId);
        JSONArray ret = sendToArray("dnld_install_sound", install);
        if (ret == null) return null;
        return new VacuumSounpackInstallState(ret.optJSONObject(0));
    }

    /**
     * Get the current soundpack installation status.
     * @return The current soundpack installation status.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public VacuumSounpackInstallState soundpackInstallStatus() throws CommandExecutionException {
        JSONArray ret = sendToArray("get_sound_progress");
        if (ret == null) return null;
        return new VacuumSounpackInstallState(ret.optJSONObject(0));
    }

    /**
     * Get the current carpet cleaning settings.
     * @return The current carpet cleaning settings.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public JSONObject getCarpetModeState() throws CommandExecutionException {
        JSONArray ret = sendToArray("get_carpet_mode");
        if (ret == null) return null;
        return ret.optJSONObject(0);
    }

    /**
     * Change the carped cleaning settings.
     * @param enabled Weather the carped cleanup mode should be enabled.
     * @param high The high parameter of the carped cleanup.
     * @param low The low parameter of the carped cleanup.
     * @param integral The integral parameter of the carped cleanup.
     * @param stallTime The stall time parameter of the carped cleanup.
     * @return True if the command has been received successfully.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public boolean setCarpetMode(boolean enabled, int high, int low, int integral, int stallTime) throws CommandExecutionException {
        JSONObject payload = new JSONObject();
        payload.put("enable", enabled ? 1:0);
        payload.put("current_high", high);
        payload.put("current_low", low);
        payload.put("current_integral", integral);
        payload.put("stall_time", stallTime);
        JSONArray send = new JSONArray();
        send.put(payload);
        return sendOk("set_carpet_mode", send);
    }

    /**
     * Get the vacuums serial number.
     * @return The vacuums serial number.
     * @throws CommandExecutionException When there has been a error during the communication or the response was invalid.
     */
    public JSONObject getSerialnumber() throws CommandExecutionException {
        JSONArray ret = sendToArray("get_serial_number");
        if (ret == null) return null;
        return ret.optJSONObject(0);
    }
}
