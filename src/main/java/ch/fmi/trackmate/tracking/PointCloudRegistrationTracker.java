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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Vector;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.SpotTracker;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.models.RigidModel3D;
import plugin.DescriptorParameters;
import process.ComparePair;
import process.Matching;
import process.Particle;

/**
 * {@link SpotTracker} implementation using the Descriptor-based registration
 * plugin to create links between point correspondences.
 * 
 * @author Jan Eglinger
 * @deprecated superseded by {@link PointDescriptorTracker}
 */
public class PointCloudRegistrationTracker implements SpotTracker {

	private SimpleWeightedGraph<Spot, DefaultWeightedEdge> graph;
	private SimpleWeightedGraph<Spot, DefaultWeightedEdge> prunedGraph;
	private final SpotCollection spots;

	private int minNumInliers;
	private int frameRange;
	private boolean discardLowCoverage;
	private double minCoverage;

	private String errorMessage;

	public PointCloudRegistrationTracker(final SpotCollection spots, int minNumInliers, int frameRange, boolean discardLowCoverage, double minCoverage) {
		this.minNumInliers = minNumInliers;
		this.frameRange = frameRange;
		this.discardLowCoverage = discardLowCoverage;
		this.minCoverage = minCoverage;
		this.spots = spots;
	}

	@Override
	public SimpleWeightedGraph<Spot, DefaultWeightedEdge> getResult() {
		return prunedGraph;
	}

	@Override
	public boolean checkInput() {
		if (spots == null) {
			errorMessage = "SpotCollection is null";
			return false;
		}
		return true;
	}

	@Override
	public boolean process() {
		// Create list of peaks
		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakListList = new ArrayList<>();
		spots.keySet().forEach(f -> {
			peakListList.add(spotsToPeakList(spots.iterable(f, false)));
		});

		// Get parameters for matching
		DescriptorParameters params = createDescriptorParameters(frameRange);

		Vector<ComparePair> comparePairs = Matching.descriptorMatching(peakListList, peakListList.size(), params, 1.0f);

		// Create and populate graph
		// TODO multi-threaded?
		// TODO implement discard low coverage
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		comparePairs.forEach(pair -> {
			if (pair.inliers.size() < minNumInliers) return;
			ArrayList<Spot> thisFrameSpotList = Lists.newArrayList(spots.iterable(pair.indexA, false));
			ArrayList<Spot> otherFrameSpotList = Lists.newArrayList(spots.iterable(pair.indexB, false));
			pair.inliers.forEach(pointMatch -> {
				long thisIndex = ((Particle) pointMatch.getP1()).getID();
				long otherIndex = ((Particle) pointMatch.getP2()).getID() - peakListList.get(pair.indexA).size();
				Spot thisSpot = thisFrameSpotList.get((int) thisIndex);
				Spot otherSpot = otherFrameSpotList.get((int) otherIndex);
				graph.addVertex(thisSpot);
				graph.addVertex(otherSpot);
				DefaultWeightedEdge edge = graph.addEdge(thisSpot, otherSpot);
				graph.setEdgeWeight(edge, pair.model.getCost());
			});
		});

		prunedGraph = Tracks.prune(graph, false);

		return true;
	}

	private DescriptorParameters createDescriptorParameters(int range) {
		DescriptorParameters params = new DescriptorParameters();
		params.model = new RigidModel3D();
		params.dimensionality = 3;
		params.numNeighbors = 3;
		params.significance = 3.0;
		params.similarOrientation = true;
		params.ransacThreshold = 5;
		params.redundancy = 1;
		params.globalOpt = 1; // all-to-all within range
		params.range = range;
		return params;
	}

	private ArrayList<DifferenceOfGaussianPeak<FloatType>> spotsToPeakList(Iterable<Spot> iterable) {
		ArrayList<DifferenceOfGaussianPeak<FloatType>> list = new ArrayList<>();
		double[] realPosition = new double[3];
		int[] position = new int[3];
		iterable.forEach(spot -> {
			list.add(Spots.createPeak(spot, realPosition, position));
		});
		return list;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void setNumThreads() {
		// Ignore for now
	}

	@Override
	public void setNumThreads(int numThreads) {
		// Ignore for now
	}

	@Override
	public int getNumThreads() {
		return 1;
	}

	@Override
	public void setLogger(Logger logger) {
		// Ignore for now
	}

}
