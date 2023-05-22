/*-
 * #%L
 * FMI Add-ons for TrackMate in Fiji
 * %%
 * Copyright (C) 2017 - 2023 Friedrich Miescher Institute for Biomedical Research
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package ch.fmi.trackmate.tracking;

import fiji.plugin.trackmate.Spot;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussian.SpecialPoint;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * Utility class to convert TrackMate {@code Spot}s to other point
 * representations.
 * 
 * @author Jan Eglinger
 */
public class Spots {
	private Spots() {
		// prevent instantiation of static utility class
	}

	/**
	 * Creates a new {@code DifferenceOfGaussianPeak} for a TrackMate {@code Spot}
	 * 
	 * @param spot
	 *            {@code Spot} to be converted
	 * @param realPos
	 *            empty array to hold the real coordinates of the spot
	 * @param pos
	 *            empty array to hold the integer coordinates of the spot
	 * @return a {@code DifferenceOfGaussianPeak} corresponding to the input
	 *         {@code Spot}
	 */
	public static DifferenceOfGaussianPeak<FloatType> createPeak(Spot spot, double[] realPos, int[] pos) {
		spot.localize(realPos);
		for (int i = 0; i < realPos.length; i++) {
			pos[i] = (int) realPos[i];
		}
		DifferenceOfGaussianPeak<FloatType> p = new DifferenceOfGaussianPeak<>(pos, new FloatType(), SpecialPoint.MAX);
		for (int d = 0; d < realPos.length; d++) {
			p.setSubPixelLocationOffset((float) (realPos[d] - pos[d]), d);
		}
		return p;
	}
}
