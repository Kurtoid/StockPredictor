package com.kurt.americanspiel;

import java.util.ArrayList;

/**
 * Created by Kurt on 9/14/2015.
 * some random functions
 */
public class KurtUtils {
	public static double[] convertDoubles(ArrayList<Double> doubles) {
		double[] ret = new double[doubles.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = doubles.get(i).intValue();
		}
		return ret;
	}

	public static double getPercentChange(double from, double to) {
		return ((to - from) / from);
	}
}
