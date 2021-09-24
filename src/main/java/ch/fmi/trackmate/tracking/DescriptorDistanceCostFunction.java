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
import fiji.plugin.trackmate.tracking.sparselap.costfunction.CostFunction;
import mpicbg.pointdescriptor.SimplePointDescriptor;
import process.Particle;

public class DescriptorDistanceCostFunction implements CostFunction<Spot, Spot> {
	
	private Map<Integer, SimplePointDescriptor<Particle>> spotMapping;

	public DescriptorDistanceCostFunction(Map<Integer, SimplePointDescriptor<Particle>> spotMapping) {
		this.spotMapping = spotMapping;
	}

	@Override
	public double linkingCost(Spot s1, Spot s2) {
		SimplePointDescriptor<Particle> d1 = spotMapping.get(s1.ID());
		SimplePointDescriptor<Particle> d2 = spotMapping.get(s2.ID());
		// TODO check for null?
		return d1.descriptorDistance(d2);
	}

}
