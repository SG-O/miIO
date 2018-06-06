package de.sg_o.app.miio.serverTest;

import org.json.JSONArray;
import org.json.JSONObject;
import de.sg_o.app.miio.server.OnServerEventListener;

public class ServerGenericEvents implements OnServerEventListener {
    public enum UpdateStatus {
        IDLE("idle"),
        DOWNLOADING("downloading"),
        INSTALLING("installing"),
        FAILED("failed");

        public final String status;   // Symbol of unit

        UpdateStatus(String status) {
            this.status = status;
        }
    }

    private UpdateStatus updateStatus = UpdateStatus.IDLE;
    private int updateProgress = 0;

    public ServerGenericEvents() {
    }

    public UpdateStatus getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(UpdateStatus updateStatus) {
        this.updateStatus = updateStatus;
    }

    public int getUpdateProgress() {
        return updateProgress;
    }

    public void setUpdateProgress(int updateProgress) {
        this.updateProgress = updateProgress;
    }

    @Override
    public Object onCommandListener(String method, Object params) {
        JSONObject paramsObject = null;
        if (params != null){
            if (params.getClass() == JSONObject.class){
                paramsObject = (JSONObject) params;
            }
        }
        switch (method){
            case "miIO.ota":
                return update(paramsObject);
            case "miIO.get_ota_state":
                return updateStatus();
            case "miIO.get_ota_progress":
                return updateProgress();
            case "miIO.config_router":
                return configureRouter(paramsObject);
            default:
                return null;
        }
    }

    private Object update(JSONObject params){
        if (params == null) return null;
        if (!params.optString("mode").equals("normal")) return null;
        if (!params.optString("install").equals("1")) return null;
        if (params.optString("app_url").equals("")) return null;
        if (params.optString("file_md5").length() != 32) return null;
        if (!params.optString("proc").equals("dnld install")) return null;
        updateStatus = UpdateStatus.DOWNLOADING;
        return ok();
    }

    private Object updateStatus(){
        JSONArray ret = new JSONArray();
        ret.put(this.updateStatus.status);
        return ret;
    }

    private Object updateProgress(){
        JSONArray ret = new JSONArray();
        ret.put(this.updateProgress);
        return ret;
    }

    private Object configureRouter(JSONObject params){
        if (params == null) return null;
        if (params.optString("ssid").length() == 0) return null;
        if (params.optString("passwd").length() == 0) return null;
        if (params.optInt("uid", -1) == -1) return null;
        return ok();
    }

    private JSONArray ok(){
        JSONArray ret = new JSONArray();
        ret.put("ok");
        return ret;
    }
}
