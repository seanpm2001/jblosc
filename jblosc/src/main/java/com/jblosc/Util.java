package com.jblosc;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class Util {
	public static String getArchPlatform() {
		String archDataModel = System.getProperty("sun.arch.data.model");
		if (archDataModel.equals("64")) {
			archDataModel = "";
		}
		return archDataModel;
	}

	public static ByteBuffer Array2ByteArray(double[] values) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(PrimitiveSizes.DOUBLE_FIELD_SIZE * values.length);

		for (double value : values) {
			buffer.putDouble(value);
		}

		return buffer;
	}

	public static ByteBuffer Array2ByteArray(float[] values) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(PrimitiveSizes.FLOAT_FIELD_SIZE * values.length);

		for (float value : values) {
			buffer.putFloat(value);
		}

		return buffer;
	}

	public static double[] toDoubleArray(ByteBuffer buffer) {
		DoubleBuffer db = buffer.asDoubleBuffer();
		double[] doubleArray = new double[db.limit()];
		db.get(doubleArray);
		return doubleArray;
	}

}
