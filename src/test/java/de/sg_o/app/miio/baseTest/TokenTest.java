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

package de.sg_o.app.miio.baseTest;

import de.sg_o.app.miio.base.Token;
import de.sg_o.app.miio.util.ByteArray;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TokenTest {
    private byte[] b0 = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    private byte[] b1 = {};

    private Token tk0 = new Token(b0);
    private Token tk1 = new Token(b1);
    private Token tk2 = new Token(ByteArray.bytesToHex(b0), 0);
    private Token tk3 = new Token(ByteArray.bytesToHex(b1), 0);
    private Token tk4 = new Token("Hello", 1);
    private Token tk5 = new Token(null);
    private Token tk6 = new Token(null, 0);

    @Test
    public void getTokenTest() {
        assertArrayEquals(b0, tk0.getToken());
        assertArrayEquals(b1, tk1.getToken());
        assertArrayEquals(b0, tk2.getToken());
        assertArrayEquals(b1, tk3.getToken());
        assertEquals(1, tk4.getToken().length);
        assertArrayEquals(b1, tk5.getToken());
        assertArrayEquals(b1, tk6.getToken());
    }

    @Test
    public void checkTokenTest() {
        assertTrue(tk0.checkToken());
        assertFalse(tk1.checkToken());
        assertTrue(tk2.checkToken());
        assertFalse(tk3.checkToken());
        assertFalse(tk4.checkToken());
        assertFalse(tk5.checkToken());
        assertFalse(tk6.checkToken());
    }

    @Test
    public void getMd5Test() throws Exception {
        assertEquals("[26, -63, -17, 1, -23, 108, -81, 27, -32, -45, 41, 51, 26, 79, -62, -88]", Arrays.toString(tk0.getMd5()));
        assertEquals("[26, -63, -17, 1, -23, 108, -81, 27, -32, -45, 41, 51, 26, 79, -62, -88]", Arrays.toString(tk2.getMd5()));
        assertEquals("[-44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126]", Arrays.toString(tk1.getMd5()));
        assertEquals("[-44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126]", Arrays.toString(tk3.getMd5()));
        assertEquals("[-44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126]", Arrays.toString(tk5.getMd5()));
        assertEquals("[-44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126]", Arrays.toString(tk6.getMd5()));
        assertEquals("[-109, -72, -123, -83, -2, 13, -96, -119, -51, -10, 52, -112, 79, -43, -97, 113]", Arrays.toString(tk4.getMd5()));
    }

    @Test
    public void getIvTest() throws Exception {
        assertEquals("[-102, -27, 56, 62, 60, 110, 6, 6, -63, 93, -89, -124, 127, -51, 29, 66]", Arrays.toString(tk0.getIv()));
        assertEquals("[-102, -27, 56, 62, 60, 110, 6, 6, -63, 93, -89, -124, 127, -51, 29, 66]", Arrays.toString(tk2.getIv()));
        assertEquals("[89, -83, -78, 78, -13, -51, -66, 2, -105, -16, 91, 57, 88, 39, 69, 63]", Arrays.toString(tk1.getIv()));
        assertEquals("[89, -83, -78, 78, -13, -51, -66, 2, -105, -16, 91, 57, 88, 39, 69, 63]", Arrays.toString(tk3.getIv()));
        assertEquals("[89, -83, -78, 78, -13, -51, -66, 2, -105, -16, 91, 57, 88, 39, 69, 63]", Arrays.toString(tk5.getIv()));
        assertEquals("[89, -83, -78, 78, -13, -51, -66, 2, -105, -16, 91, 57, 88, 39, 69, 63]", Arrays.toString(tk6.getIv()));
        assertEquals("[-54, -77, 56, -63, 86, -44, 34, 3, -1, 85, 120, -123, -63, -42, -94, -22]", Arrays.toString(tk4.getIv()));
    }

    @SuppressWarnings({"ConstantConditions", "SimplifiableJUnitAssertion", "ObjectEqualsNull"})
    @Test
    public void equalsTest() {
        assertTrue(tk0.equals(tk2));
        assertTrue(tk2.equals(tk0));
        assertFalse(tk0.equals(tk1));
        assertFalse(tk0.equals(null));
        assertFalse(tk0.equals(new Object()));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(tk0.hashCode(), tk2.hashCode());
        assertNotEquals(tk0.hashCode(), tk1.hashCode());
    }

    @Test
    public void toStringTest() {
        assertEquals(tk0.toString(), tk2.toString());
        assertNotEquals(tk0.toString(), tk1.toString());
    }

    @Test
    public void cryptoTest() {
        byte[] msg = ByteArray.hexToBytes("7b226964223a2032322c20226d6574686f64223a20226765745f737461747573227d00");
        byte[] enc0 = tk0.encrypt(msg);
        byte[] dec0 = tk2.decrypt(enc0);
        assertArrayEquals(msg, dec0);
        byte[] enc1 = tk1.encrypt(msg);
        byte[] dec1 = tk3.decrypt(enc1);
        assertArrayEquals(msg, dec1);
        byte[] dec2 = tk3.decrypt(enc0);
        assertNull(dec2);
        byte[] msgEmpty = new byte[0];
        assertNotNull(tk0.encrypt(msgEmpty));
        assertNull(tk0.encrypt(null));
        assertNull(tk0.decrypt(null));
    }
}