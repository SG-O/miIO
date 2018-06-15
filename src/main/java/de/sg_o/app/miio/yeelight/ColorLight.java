package de.sg_o.app.miio.yeelight;

import de.sg_o.app.miio.base.Token;

import java.net.InetAddress;

public class ColorLight extends Light {

    /**
     * @param ip               The IP address of the light to connect to. If the address is null the first light that was found will be chosen.
     * @param token            The token for that device. If the token is null the token will be extracted from unprovisioned devices.
     * @param timeout          The timeout for the communication
     * @param retries          The number of retries after a failed communication
     */
    public ColorLight(InetAddress ip, Token token, int timeout, int retries) {
        super(ip, token, new String[]{"yeelink.light.color1"}, timeout, retries);
    }
}
