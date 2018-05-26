package device.vacuum;

import base.CommandExecutionException;
import base.Device;
import base.Token;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;

public class Vacuum extends Device {

    public Vacuum(InetAddress ip, Token token, int timeout, int retries) {
        super(ip, token, new String[]{"rockrobo.vacuum.v1", "rockrobo.vacuum.v2"}, timeout, retries);
    }

    public VacuumStatus status() throws CommandExecutionException {
        JSONArray resp = sendToArray("get_status");
        JSONObject stat = resp.optJSONObject(0);
        if (stat == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return new VacuumStatus(stat);
    }

    public VacuumConsumableStatus consumableStatus() throws CommandExecutionException {
        JSONArray resp = sendToArray("get_consumable");
        JSONObject stat = resp.optJSONObject(0);
        if (stat == null) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return new VacuumConsumableStatus(stat);
    }

    public boolean resetConsumable(VacuumConsumableStatus.Names consumable) throws CommandExecutionException {
        JSONArray params = new JSONArray();
        params.put(consumable.toString());
        return sendOk("reset_consumable", params);
    }

    public boolean start() throws CommandExecutionException {
        return sendOk("app_start");
    }

    public boolean pause() throws CommandExecutionException {
        return sendOk("app_pause");
    }

    public boolean stop() throws CommandExecutionException {
        return sendOk("app_stop");
    }

    public boolean home() throws CommandExecutionException {
        stop();
        return sendOk("app_charge");
    }

    public boolean spotCleaning() throws CommandExecutionException {
        return sendOk("app_spot");
    }

    public boolean findMe() throws CommandExecutionException {
        return sendOk("find_me");
    }

    public int getFanSpeed() throws CommandExecutionException {
        int resp = sendToArray("get_custom_mode").optInt(0, -1);
        if ((resp < 0) || (resp > 100)) throw new CommandExecutionException(CommandExecutionException.Error.INVALID_RESPONSE);
        return resp;
    }

    public boolean setFanSpeed(int speed) throws CommandExecutionException {
        JSONArray params = new JSONArray();
        params.put(speed);
        return sendOk("set_custom_mode", params);
    }
}
