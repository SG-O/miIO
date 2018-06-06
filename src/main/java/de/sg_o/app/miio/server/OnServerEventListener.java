package de.sg_o.app.miio.server;

public interface OnServerEventListener {
    Object onCommandListener(String method, Object params);
}
