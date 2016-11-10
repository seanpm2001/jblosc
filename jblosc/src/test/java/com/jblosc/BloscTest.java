package com.jblosc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;

public class BloscTest {

	@Test
	public void testCompressDecompressJNA() {
		int SIZE = 100 * 100 * 100;
		float data[] = new float[SIZE];
		for (int i = 0; i < SIZE; i++) {
			data[i] = i * 2;
		}
		float data_out[] = new float[SIZE];
		long isize = SIZE * 4;
		Memory m = new Memory(isize);
		m.write(0, data, 0, SIZE);
		Memory m2 = new Memory(isize);
		IBloscDll iBlosc = (IBloscDll) Native.loadLibrary("blosc" + Util.getArchPlatform(), IBloscDll.class);
		iBlosc.blosc_init();
		int size = iBlosc.blosc_compress(5, 1, new NativeLong(4), new NativeLong(isize), m, m2, new NativeLong(isize));
		data_out = m2.getFloatArray(0, SIZE);
		Memory m3 = new Memory(isize);
		iBlosc.blosc_decompress(m2, m3, new NativeLong(isize));
		float[] data_in = m3.getFloatArray(0, SIZE);
		assertArrayEquals(data, data_in, (float) 0);
		iBlosc.blosc_destroy();
		assertNotNull(data_out);
		assert (size < isize);
	}

	@Test
	public void testSetCompressor() {
		System.out.println("*** testSetCompressor ***");
		int SIZE = 262144;
		double data[] = new double[SIZE];
		for (int i = 0; i < SIZE; i++) {
			// data[i] = Math.random();
			data[i] = i * 2;
		}
		BloscWrapper bw = new BloscWrapper();
		bw.init();
		System.out.println("Blosc version " + bw.getVersionString());
		bw.setNumThreads(4);
		System.out.println("Working with " + bw.getNumThreads() + " threads");
		assertEquals(bw.getNumThreads(), 4);
		String compnames = bw.listCompressors();
		String compnames_array[] = compnames.split(",");
		for (String compname : compnames_array) {
			bw.setCompressor(compname);
			String compname_out = bw.getCompressor();
			assertEquals(compname, compname_out);
			String[] ci = bw.getComplibInfo(compname);
			int compcode = bw.compnameToCompcode(compname);
			compname_out = bw.compcodeToCompname(compcode);
			assertEquals(compname, compname_out);
			System.out
					.println("Working with compressor " + compname + " (code " + compcode + ") " + ci[0] + " " + ci[1]);
			long startTime = System.currentTimeMillis();
			byte[] data_out = bw.compress(5, Shuffle.BYTE_SHUFFLE, data);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			printRatio(bw, "Double Array", data_out);
			BufferSizes bs = bw.cbufferSizes(data_out);
			double mb = bs.getNbytes() * 1.0 / (1024 * 1024);
			System.out.println("Compress time " + elapsedTime + " ms. "
					+ String.format("%.2f", (mb / elapsedTime) * 1000) + " Mb/s");
			startTime = System.currentTimeMillis();
			double[] data_again = bw.decompressToDoubleArray(data_out, bs.getNbytes());
			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			mb = (bs.getNbytes() * 1.0) / (1024 * 1024);
			System.out.println("Decompress time " + elapsedTime + " ms. "
					+ String.format("%.2f", (mb / elapsedTime) * 1000) + " Mb/s");
			assertArrayEquals(data, data_again, (float) 0);
		}
		bw.freeResources();
		bw.destroy();
	}

	private void printRatio(BloscWrapper bw, String title, byte[] cbuffer) {
		BufferSizes bs = bw.cbufferSizes(cbuffer);
		System.out.println(title + ": " + bs.getCbytes() + " from " + bs.getNbytes() + ". Ratio: "
				+ (String.format("%.2f", (0.0 + bs.getNbytes()) / bs.getCbytes())));
	}

	@Test
	public void testCompressDecompressFloat() {
		int SIZE = 100 * 100 * 100;
		float data[] = new float[SIZE];
		for (int i = 0; i < SIZE; i++) {
			data[i] = i * 2;
		}
		BloscWrapper bw = new BloscWrapper();
		bw.init();
		byte[] data_out = bw.compress(5, Shuffle.BYTE_SHUFFLE, data);
		printRatio(bw, "Float", data_out);
		float[] data_again = bw.decompressToFloatArray(data_out);
		bw.destroy();
		assertArrayEquals(data, data_again, (float) 0);
	}

	@Test
	public void testCompressDecompressDouble() {
		int SIZE = 100 * 100 * 100;
		double data[] = new double[SIZE];
		for (int i = 0; i < SIZE; i++) {
			data[i] = i * 2;
		}
		BloscWrapper bw = new BloscWrapper();
		bw.init();
		byte[] data_out = bw.compress(5, Shuffle.BYTE_SHUFFLE, data);
		printRatio(bw, "Double", data_out);
		double[] data_again = bw.decompressToDoubleArray(data_out);
		bw.destroy();
		assertArrayEquals(data, data_again, (float) 0);
	}

	@Test
	public void testCompressDecompressByte() {
		int SIZE = 100 * 100 * 100;
		byte[] data = new byte[SIZE];
		for (int i = 0; i < SIZE; i++) {
			data[i] = (byte) (i * 2);
		}
		BloscWrapper bw = new BloscWrapper();
		bw.init();
		byte[] data_out = bw.compress(5, Shuffle.BYTE_SHUFFLE, data);
		printRatio(bw, "Byte", data_out);
		byte[] data_again = bw.decompressToByteArray(data_out);
		bw.destroy();
		assertArrayEquals(data, data_again);
	}

	@Test
	public void testCompressDecompressInt() {
		int SIZE = 100 * 100 * 100;
		int[] data = new int[SIZE];
		for (int i = 0; i < SIZE; i++) {
			data[i] = (i * 2);
		}
		BloscWrapper bw = new BloscWrapper();
		bw.init();
		byte[] data_out = bw.compress(5, Shuffle.BYTE_SHUFFLE, data);
		printRatio(bw, "Int", data_out);
		int[] data_again = bw.decompressToIntArray(data_out);
		bw.destroy();
		assertArrayEquals(data, data_again);
	}

	@Test
	public void testCompressDecompressLong() {
		int SIZE = 100 * 100 * 100;
		long[] data = new long[SIZE];
		for (int i = 0; i < SIZE; i++) {
			data[i] = (i * 2);
		}
		BloscWrapper bw = new BloscWrapper();
		bw.init();
		byte[] data_out = bw.compress(5, Shuffle.BYTE_SHUFFLE, data);
		printRatio(bw, "Long", data_out);
		long[] data_again = bw.decompressToLongArray(data_out);
		bw.destroy();
		assertArrayEquals(data, data_again);
	}

	@Test
	public void testCompressDecompressShort() {
		int SIZE = 100 * 100 * 100;
		short[] data = new short[SIZE];
		for (int i = 0; i < SIZE; i++) {
			data[i] = (short) (i * 2);
		}
		BloscWrapper bw = new BloscWrapper();
		bw.init();
		byte[] data_out = bw.compress(5, Shuffle.BYTE_SHUFFLE, data);
		printRatio(bw, "Short", data_out);
		short[] data_again = bw.decompressToShortArray(data_out);
		bw.destroy();
		assertArrayEquals(data, data_again);
	}

	/*
	 * @Test public void testCompressDecompressChar() { int SIZE = 100 * 100;
	 * char[] data = new char[SIZE]; for (int i = 0; i < SIZE; i++) { data[i] =
	 * 'a'; } BloscWrapper bw = new BloscWrapper(); bw.init();
	 * bw.setCompressor("lz4"); System.out.println("Before compress"); byte[]
	 * data_out = bw.compress(5, Shuffle.NO_SHUFFLE, data); System.out.println(
	 * "After compress"); printRatio(bw, "Char", data_out); System.out.println(
	 * "Before decompress"); char[] data_again =
	 * bw.decompressToCharArray(data_out); System.out.println("After decompress"
	 * ); bw.destroy(); System.out.println("Before array equals");
	 * assertArrayEquals(data, data_again); System.out.println(
	 * "After array equals");
	 * 
	 * }
	 */
}
