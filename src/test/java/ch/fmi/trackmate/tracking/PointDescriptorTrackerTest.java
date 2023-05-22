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

import static org.junit.Assert.assertEquals;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Test;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;

public class PointDescriptorTrackerTest {

	@Test
	public void testPruned() {
		boolean doPrune = true;
		// setup SpotCollection and Model
		SpotCollection spotCollection = createTestSpots();
		// Run tracker
		PointDescriptorTracker tracker = new PointDescriptorTracker(spotCollection, 3, 3, 3, 10.0, 10.0, doPrune);
		tracker.setLogger(Logger.DEFAULT_LOGGER);
		tracker.process();
		SimpleWeightedGraph<Spot, DefaultWeightedEdge> graph = tracker.getResult();
		// Verify tracks (connected sets)
		assertEquals(16, graph.edgeSet().size()); // this is in the pruned graph
	}

	@Test
	public void testRaw() {
		boolean doPrune = false;
		// setup SpotCollection and Model
		SpotCollection spotCollection = createTestSpots();
		// Run tracker
		PointDescriptorTracker tracker = new PointDescriptorTracker(spotCollection, 3, 3, 3, 10.0, 10.0, doPrune);
		tracker.setLogger(Logger.DEFAULT_LOGGER);
		tracker.process();
		SimpleWeightedGraph<Spot, DefaultWeightedEdge> graph = tracker.getResult();
		// Verify tracks (connected sets)
		assertEquals(36, graph.edgeSet().size()); // this is in the pruned graph
	}

	private SpotCollection createTestSpots() {
		SpotCollection spots = new SpotCollection();
		double r = 1;
		double quality = 1;
		int frame = 0;
		spots.add(new Spot(0.0, 0.0, 0.0, r, quality), frame);
		spots.add(new Spot(1.0, 4.0, 0.0, r, quality), frame);
		spots.add(new Spot(4.0, 0.0, 0.0, r, quality), frame);
		spots.add(new Spot(6.0, 3.0, 0.0, r, quality), frame);
		frame++; // 1
		spots.add(new Spot(0.1, 0.0, 0.0, r, quality), frame);
		spots.add(new Spot(1.0, 3.9, 0.0, r, quality), frame);
		spots.add(new Spot(4.1, 0.0, 0.0, r, quality), frame);
		spots.add(new Spot(6.0, 3.1, 0.0, r, quality), frame);
		frame++; // 2
		spots.add(new Spot(0.1, -0.1, 0.0, r, quality), frame);
		spots.add(new Spot(1.1, 3.9, 0.0, r, quality), frame);
		spots.add(new Spot(4.1, 0.1, 0.0, r, quality), frame);
		spots.add(new Spot(5.9, 3.1, 0.0, r, quality), frame);
		frame++; // 3
		spots.add(new Spot(0.0, -0.1, 0.0, r, quality), frame);
		spots.add(new Spot(1.1, 4.0, 0.0, r, quality), frame);
		spots.add(new Spot(4.0, 0.1, 0.0, r, quality), frame);
		spots.add(new Spot(5.9, 3.0, 0.0, r, quality), frame);
		frame++; // 4
		spots.add(new Spot(-0.1, -0.1, 0.0, r, quality), frame);
		spots.add(new Spot(1.1, 4.1, 0.0, r, quality), frame);
		spots.add(new Spot(3.9, 0.1, 0.0, r, quality), frame);
		spots.add(new Spot(5.9, 2.9, 0.0, r, quality), frame);
		// TODO frames 5-8

		return spots;
	}

}
