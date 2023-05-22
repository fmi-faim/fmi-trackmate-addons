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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import fiji.plugin.trackmate.Spot;

public class Tracks {
	private Tracks() {
		// prevent instantiation of static utility class
	}

	public static SimpleWeightedGraph<Spot, DefaultWeightedEdge> prune(SimpleWeightedGraph<Spot, DefaultWeightedEdge> graph, boolean setWeights) {
		
		SimpleWeightedGraph<Spot, DefaultWeightedEdge> prunedGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		ConnectivityInspector<Spot, DefaultWeightedEdge> graphInspector = new ConnectivityInspector<>(graph);
		// Consider changing to FloydWarshallShortestPaths
		DijkstraShortestPath<Spot, DefaultWeightedEdge> shortestPath = new DijkstraShortestPath<>(graph);

		List<Set<Spot>> connectedSets = graphInspector.connectedSets();

		for (Set<Spot> track : connectedSets) {
			// Sorting necessary?
			List<Spot> trackSpots = new ArrayList<>(track);
			Collections.sort(trackSpots, (s1, s2) -> {
				return s1.getFeature(Spot.FRAME).compareTo(s2.getFeature(Spot.FRAME));
			});

			// loop through list
			for (int i = 0; i < trackSpots.size(); i++) {
				Spot source = trackSpots.get(i);
				int sourceSpotFrame = source.getFeature(Spot.FRAME).intValue();
				int firstLinkedFrame = Integer.MAX_VALUE;
				for (int j = i + 1; j < trackSpots.size(); j++) {
					Spot target = trackSpots.get(j);
					int targetSpotFrame = target.getFeature(Spot.FRAME).intValue();
					if (targetSpotFrame > firstLinkedFrame) break;
					if (targetSpotFrame > sourceSpotFrame) {
						firstLinkedFrame = targetSpotFrame;
						prunedGraph.addVertex(source);
						prunedGraph.addVertex(target);
						DefaultWeightedEdge edge = prunedGraph.addEdge(source, target);
						if (setWeights) {
							prunedGraph.setEdgeWeight(edge, shortestPath.getPathWeight(source, target));
						} else {
							prunedGraph.setEdgeWeight(edge, -1);
						}
					}
				}
			}
		}

		return prunedGraph;
	}

}
