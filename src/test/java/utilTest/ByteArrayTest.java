package utilTest;

import org.junit.Test;

import java.util.Random;

import static util.ByteArray.*;
import static org.junit.Assert.*;

public class ByteArrayTest {
    private byte[] b0 = {0,1,2,3};
    private byte[] b1 = {9,10,127,-1};
    private byte[] b2 = {};
    private byte[] b3 = null;

    private String s0 = "00010203";
    private String s1 = "090A7FFF";
    private String s2 = "";

    @Test
    public void bytesToHexTest() {
        assertEquals(s0, bytesToHex(b0));
        assertEquals(s1, bytesToHex(b1));
        assertEquals(s2, bytesToHex(b2));
        assertEquals(s2, bytesToHex(b3));
    }

    @Test
    public void hexToBytesTest() {
        assertArrayEquals(b0, hexToBytes(s0));
        assertArrayEquals(b1, hexToBytes(s1));
        assertArrayEquals(b1, hexToBytes("090a7fff"));
        assertArrayEquals(b2, hexToBytes(s2));
    }

    @Test
    public void appendTest() {
        byte[] b4 = {0,1,2,3,9,10,127,-1};
        assertArrayEquals(b4, append(b0, b1));
        assertArrayEquals(b0, append(b0, b2));
        assertArrayEquals(b0, append(b2, b0));
        assertNull(append(b0, b3));
        assertNull(append(b3, b0));
        assertNull(append(b3, b3));
    }

    @Test
    public void toFromBytesTest() {
        byte[] o0 = toBytes(-1,8);
        byte[] o1 = toBytes(10000,8);
        byte[] o2 = toBytes(Long.MAX_VALUE,8);
        byte[] o3 = toBytes(Long.MIN_VALUE,8);
        byte[] o4 = toBytes(0,8);
        byte[] o5 = toBytes(0,9);
        byte[] o6 = toBytes(0,-5);

        assertEquals("FFFFFFFFFFFFFFFF", bytesToHex(o0));
        assertEquals(-1, fromBytes(o0));
        assertEquals("0000000000002710", bytesToHex(o1));
        assertEquals(10000, fromBytes(o1));
        assertEquals("7FFFFFFFFFFFFFFF", bytesToHex(o2));
        assertEquals(Long.MAX_VALUE, fromBytes(o2));
        assertEquals("8000000000000000", bytesToHex(o3));
        assertEquals(Long.MIN_VALUE, fromBytes(o3));
        assertEquals("0000000000000000", bytesToHex(o4));
        assertEquals(0, fromBytes(o4));
        assertEquals(0, fromBytes(null));

        assertEquals("0000000000000000", bytesToHex(o5));
        assertEquals("", bytesToHex(o6));

        byte[] b4 = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        assertEquals(-1, fromBytes(b4));

        Random r = new Random();
        long l;
        byte[] o;
        for(int i = 0; i < 1000; i++){
            l = r.nextLong();
            o = toBytes(l,8);
            assertEquals(l, fromBytes(o));
        }
        int j;
        for(int i = 0; i < 1000; i++){
            j = r.nextInt();
            o = toBytes(j,4);
            assertEquals(j, (int)fromBytes(o));
        }
        short s;
        for(int i = 0; i < 1000; i++){
            s = (short) r.nextInt();
            o = toBytes(s,2);
            assertEquals(s, (short) fromBytes(o));
        }
    }
}