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
