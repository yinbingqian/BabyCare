package com.lnpdit.util;

import java.io.UnsupportedEncodingException;

public class ComUtil {

	public byte[] intToByte(int i) {
		byte[] abyte0 = new byte[4];
		abyte0[0] = (byte) (0xff & i);
		abyte0[1] = (byte) ((0xff00 & i) >> 8);
		abyte0[2] = (byte) ((0xff0000 & i) >> 16);
		abyte0[3] = (byte) ((0xff000000 & i) >> 24);
		return abyte0;
	}

	// public int bytesToInt(byte[] bytes) {
	// int addr = bytes[0] & 0xFF;
	// addr |= ((bytes[1] << 8) & 0xFF00);
	// addr |= ((bytes[2] << 16) & 0xFF0000);
	// addr |= ((bytes[3] << 24) & 0xFF000000);
	// return addr;
	// }

	public String utf8ToUnicode(byte[] str) {
		byte temp[] = new byte[str.length];
		for (int i = 0; i < str.length; i++) {
			if (str[i] == 0) {
				break;
			}
			temp[i] = str[i];
		}
		String name = null;
		try {

			name = new String(temp, "GB2312");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return name.trim();
	}

	public int findHeader(byte[] head) {
		if (119 == head[0])
			;// 0x77
		else
			return -1;
		if (-120 == head[1])
			;// 0x88
		else
			return -1;
		if (85 == head[2])
			;// 0x55
		else
			return -1;
		switch (head[3]) {
		case -67:// 0xbd Device online
			return 0;
		case -86:// 0xaa -- one channel
			return 1;
		case -70:// 0xba -- two channel
			return 2;
		case -54:// 0xca -- three channel
			return 3;
		case -38:// 0xda -- four channel
			return 4;
		case -65:// 0xbf -- device off line
			return 5;
		}
		return -1;
	}

	// 以下 是整型数 和网络字节序的 byte[] 数组之间的转换
	public byte[] longToBytes(long n) {
		// byte[] b = new byte[8];
		// b[7] = (byte) (n & 0xff);
		// b[6] = (byte) (n >> 8 & 0xff);
		// b[5] = (byte) (n >> 16 & 0xff);
		// b[4] = (byte) (n >> 24 & 0xff);
		// b[3] = (byte) (n >> 32 & 0xff);
		// b[2] = (byte) (n >> 40 & 0xff);
		// b[1] = (byte) (n >> 48 & 0xff);
		// b[0] = (byte) (n >> 56 & 0xff);
		// return b;
		byte[] buf = { 0, 0, 0, 0 };
		buf[3] = (byte) (n >> 0 & 0xff);
		buf[2] = (byte) (n >> 8 & 0xff);
		buf[1] = (byte) (n >> 16 & 0xff);
		buf[0] = (byte) (n >> 24 & 0xff);
		return buf;
	}

	public static void longToBytes(long n, byte[] array, int offset) {
		array[7 + offset] = (byte) (n & 0xff);
		array[6 + offset] = (byte) (n >> 8 & 0xff);
		array[5 + offset] = (byte) (n >> 16 & 0xff);
		array[4 + offset] = (byte) (n >> 24 & 0xff);
		array[3 + offset] = (byte) (n >> 32 & 0xff);
		array[2 + offset] = (byte) (n >> 40 & 0xff);
		array[1 + offset] = (byte) (n >> 48 & 0xff);
		array[0 + offset] = (byte) (n >> 56 & 0xff);
	}

	public static long bytesToLong(byte[] array) {
		return ((((long) array[0] & 0xff) << 56)
				| (((long) array[1] & 0xff) << 48)
				| (((long) array[2] & 0xff) << 40)
				| (((long) array[3] & 0xff) << 32)
				| (((long) array[4] & 0xff) << 24)
				| (((long) array[5] & 0xff) << 16)
				| (((long) array[6] & 0xff) << 8) | (((long) array[7] & 0xff) << 0));
	}

	public static long bytesToLong(byte[] array, int offset) {
		return ((((long) array[offset + 0] & 0xff) << 56)
				| (((long) array[offset + 1] & 0xff) << 48)
				| (((long) array[offset + 2] & 0xff) << 40)
				| (((long) array[offset + 3] & 0xff) << 32)
				| (((long) array[offset + 4] & 0xff) << 24)
				| (((long) array[offset + 5] & 0xff) << 16)
				| (((long) array[offset + 6] & 0xff) << 8) | (((long) array[offset + 7] & 0xff) << 0));
	}

	public static byte[] intToBytes(int n) {
		byte[] b = new byte[4];
		b[3] = (byte) (n & 0xff);
		b[2] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n >> 16 & 0xff);
		b[0] = (byte) (n >> 24 & 0xff);
		return b;
	}

	public static void intToBytes(int n, byte[] array, int offset) {
		array[3 + offset] = (byte) (n & 0xff);
		array[2 + offset] = (byte) (n >> 8 & 0xff);
		array[1 + offset] = (byte) (n >> 16 & 0xff);
		array[offset] = (byte) (n >> 24 & 0xff);
	}

	public int bytesToInt(byte b[]) {
		return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16
				| (b[0] & 0xff) << 24;
	}

	public static int bytesToInt(byte b[], int offset) {
		return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8
				| (b[offset + 1] & 0xff) << 16 | (b[offset] & 0xff) << 24;
	}

	public static byte[] uintToBytes(long n) {
		byte[] b = new byte[4];
		b[3] = (byte) (n & 0xff);
		b[2] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n >> 16 & 0xff);
		b[0] = (byte) (n >> 24 & 0xff);

		return b;
	}

	public static void uintToBytes(long n, byte[] array, int offset) {
		array[3 + offset] = (byte) (n);
		array[2 + offset] = (byte) (n >> 8 & 0xff);
		array[1 + offset] = (byte) (n >> 16 & 0xff);
		array[offset] = (byte) (n >> 24 & 0xff);
	}

	public static long bytesToUint(byte[] array) {
		return ((long) (array[3] & 0xff)) | ((long) (array[2] & 0xff)) << 8
				| ((long) (array[1] & 0xff)) << 16
				| ((long) (array[0] & 0xff)) << 24;
	}

	public static long bytesToUint(byte[] array, int offset) {
		return ((long) (array[offset + 3] & 0xff))
				| ((long) (array[offset + 2] & 0xff)) << 8
				| ((long) (array[offset + 1] & 0xff)) << 16
				| ((long) (array[offset] & 0xff)) << 24;
	}

	public static byte[] shortToBytes(short n) {
		byte[] b = new byte[2];
		b[1] = (byte) (n & 0xff);
		b[0] = (byte) ((n >> 8) & 0xff);
		return b;
	}

	public static void shortToBytes(short n, byte[] array, int offset) {
		array[offset + 1] = (byte) (n & 0xff);
		array[offset] = (byte) ((n >> 8) & 0xff);
	}

	public static short bytesToShort(byte[] b) {
		return (short) (b[1] & 0xff | (b[0] & 0xff) << 8);
	}

	public static short bytesToShort(byte[] b, int offset) {
		return (short) (b[offset + 1] & 0xff | (b[offset] & 0xff) << 8);
	}

	public static byte[] ushortToBytes(int n) {
		byte[] b = new byte[2];
		b[1] = (byte) (n & 0xff);
		b[0] = (byte) ((n >> 8) & 0xff);
		return b;
	}

	public static void ushortToBytes(int n, byte[] array, int offset) {
		array[offset + 1] = (byte) (n & 0xff);
		array[offset] = (byte) ((n >> 8) & 0xff);
	}

	public static int bytesToUshort(byte b[]) {
		return b[1] & 0xff | (b[0] & 0xff) << 8;
	}

	public static int bytesToUshort(byte b[], int offset) {
		return b[offset + 1] & 0xff | (b[offset] & 0xff) << 8;
	}

	public static byte[] ubyteToBytes(int n) {
		byte[] b = new byte[1];
		b[0] = (byte) (n & 0xff);
		return b;
	}

	public static void ubyteToBytes(int n, byte[] array, int offset) {
		array[0] = (byte) (n & 0xff);
	}

	public static int bytesToUbyte(byte[] array) {
		return array[0] & 0xff;
	}

	public static int bytesToUbyte(byte[] array, int offset) {
		return array[offset] & 0xff;
	}
	// char 类型、 float、double 类型和 byte[] 数组之间的转换关系还需继续研究实现。
}
