/*-
 * #%L
 * FMI Add-ons for TrackMate in Fiji
 * %%
 * Copyright (C) 2017 - 2021 Friedrich Miescher Institute for Biomedical Research
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

import java.util.Map;

import fiji.plugin.trackmate.Spot;
import mpicbg.pointdescriptor.SimplePointDescriptor;
import process.Particle;

public class DistanceConstrainedDescriptorDistanceCostFunction extends DescriptorDistanceCostFunction {

	private double squareDistanceThreshold;

	public DistanceConstrainedDescriptorDistanceCostFunction(Map<Integer, SimplePointDescriptor<Particle>> spotMapping,
			double distanceThreshold) {
		super(spotMapping);
		this.squareDistanceThreshold = distanceThreshold;
	}

	@Override
	public double linkingCost(Spot s1, Spot s2) {
		// return high cost when real distance above threshold
		if (s1.squareDistanceTo(s2) > squareDistanceThreshold) return Double.POSITIVE_INFINITY;
		// otherwise, return linking cost from superclass
		return super.linkingCost(s1, s2);
	}

}
