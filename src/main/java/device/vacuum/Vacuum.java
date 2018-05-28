package device.vacuum;

import base.CommandExecutionException;
import base.Device;
import base.Token;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.time.ZoneId;

public class Vacuum extends Device {

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

    public ZoneId getTimezone() throws CommandExecutionException {
        JSONArray resp = sendToArray("get_timezone");
        String zone = resp.optString(0, null);
        if (zone == null ) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return ZoneId.of(zone);
    }

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

    public VacuumTimer[] getTimers() throws CommandExecutionException {
        JSONArray tm = sendToArray("get_timer");
        if (tm == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        VacuumTimer[] timers = new VacuumTimer[tm.length()];
        for (int i = 0; i < tm.length(); i++){
            timers[i] = new VacuumTimer(tm.optJSONArray(i));
        }
        return timers;
    }

    public boolean addTimer(VacuumTimer timer) throws CommandExecutionException {
        if (timer == null) return false;
        JSONArray tm = timer.construct();
        if (tm == null) return false;
        JSONArray payload = new JSONArray();
        payload.put(tm);
        return sendOk("set_timer", payload);
    }

    public boolean setTimerEnabled(VacuumTimer timer) throws CommandExecutionException {
        if (timer == null) return false;
        JSONArray payload = new JSONArray();
        payload.put(timer.getID());
        payload.put(timer.getOnOff());
        return sendOk("upd_timer", payload);
    }

    public boolean removeTimer(VacuumTimer timer) throws CommandExecutionException {
        if (timer == null) return false;
        JSONArray payload = new JSONArray();
        payload.put(timer.getID());
        return sendOk("del_timer", payload);
    }
}
