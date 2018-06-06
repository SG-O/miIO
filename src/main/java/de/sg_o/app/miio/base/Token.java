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

package de.sg_o.app.miio.base;

import de.sg_o.app.miio.util.ByteArray;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
public class Token {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_ALGORITHM_IMPLEMENTATION = "AES/CBC/PKCS5Padding";

    private byte[] token;

    /**
     * Create a token from a byte array.
     * @param token The byte array to use.
     */
    public Token(byte[] token) {
        if (token == null) token = new byte[0];
        this.token = token;
    }

    /**
     * Generate a token from a string of hexadecimal values.
     * @param token The value tho convert.
     * @param fallbackLength If the conversion fails a token of 0s with this length will be generated.
     */
    public Token(String token, int fallbackLength) {
        byte[] tmp = ByteArray.hexToBytes(token);
        if (tmp == null) tmp = new byte[fallbackLength];
        this.token = tmp;
    }

    /**
     * Get the token as a byte array.
     * @return The token as a byte array.
     */
    public byte[] getToken() {
        return token;
    }

    /**
     * @return Check whether the tokens length matches the expected length.
     */
    public boolean checkToken() {
        return token.length == 16;
    }

    /**
     * @return The md5 checksum of the token.
     * @throws NoSuchAlgorithmException When the platform doesn't support the md5 algorithm.
     */
    public byte[] getMd5() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(token);
        return md.digest();
    }

    /**
     * @return The initial value for encryption.
     * @throws NoSuchAlgorithmException When the platform doesn't support the md5 algorithm.
     */
    public byte[] getIv() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ByteArray.append(getMd5(),token));
        return md.digest();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token1 = (Token) o;
        return Arrays.equals(token, token1.token);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(token);
    }

    @Override
    public String toString() {
        return ByteArray.bytesToHex(token).toLowerCase();
    }

    /**
     * Encrypt a message with this token.
     * @param msg The message to encrypt.
     * @return The encrypted message. Null if encryption failed.
     */
    public byte[] encrypt(byte[] msg) {
        if (msg == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_IMPLEMENTATION);
            SecretKeySpec key = new SecretKeySpec(getMd5(), ENCRYPTION_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(getIv());
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return cipher.doFinal(msg);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decrypt a message with this token.
     * @param msg The message to decrypt.
     * @return The encrypted message. Null if decryption failed.
     */
    public byte[] decrypt(byte[] msg) {
        if (msg == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_IMPLEMENTATION);
            SecretKeySpec key = new SecretKeySpec(getMd5(), ENCRYPTION_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(getIv());
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(msg);
        } catch (Exception e) {
            return null;
        }
    }
}
