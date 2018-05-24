package server;

public interface OnServerEventListener {
    Object onCommandListener(String method, Object params);
}
