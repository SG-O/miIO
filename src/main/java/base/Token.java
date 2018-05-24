package base;

import util.ByteArray;

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

    public Token(byte[] token) {
        if (token == null) token = new byte[0];
        this.token = token;
    }

    public Token(String token, int fallbackLength) {
        byte[] tmp = ByteArray.hexToBytes(token);
        if (tmp == null) tmp = new byte[fallbackLength];
        this.token = tmp;
    }

    public byte[] getToken() {
        return token;
    }

    public boolean checkToken() {
        return token.length == 16;
    }

    public byte[] getMd5() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(token);
        return md.digest();
    }

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
