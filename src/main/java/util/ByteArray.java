package util;

public class ByteArray {
    public static final int UNSIGNED_FFFFFFFF = -1;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
        try {
            if (s == null) return new byte[0];
            s = s.toUpperCase();
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static byte[] append(byte[] first, byte[] second) {
        if ((first == null) || (second == null)) return null;
        byte[] output = new byte[first.length + second.length];
        System.arraycopy(first, 0, output, 0, first.length);
        System.arraycopy(second, 0, output, first.length, second.length);
        return output;
    }

    public static byte[] toBytes(long value, int length) {
        if (length <= 0) return new byte[0];
        if (length > 8) length = 8;
        byte[] out = new byte[length];
        for (int i = length - 1; i >= 0; i--){
            out[i] = (byte)(value & 0xFFL);
            value = value >> 8;
        }
        return out;
    }

    public static long fromBytes(byte[] value){
        if (value == null) return 0;
        long out = 0;
        int length = value.length;
        if (length > 8) length = 8;
        for (int i = 0; i < length; i++){
            out = (out << 8) + (value[i] & 0xff);
        }
        return out;
    }
}