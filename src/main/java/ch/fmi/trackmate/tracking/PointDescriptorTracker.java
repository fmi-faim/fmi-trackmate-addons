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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.JaqamanLinkingCostMatrixCreator;
import fiji.plugin.trackmate.tracking.jaqaman.JaqamanLinker;
import fiji.util.KDTree;
import fiji.util.NNearestNeighborSearch;
// TODO use multiview-reconstruction
import mpicbg.pointdescriptor.SimplePointDescriptor;
import mpicbg.pointdescriptor.exception.NoSuitablePointsException;
import mpicbg.pointdescriptor.matcher.Matcher;
import mpicbg.pointdescriptor.matcher.SubsetMatcher;
import mpicbg.pointdescriptor.similarity.SimilarityMeasure;
import mpicbg.pointdescriptor.similarity.SquareDistance;
import net.imglib2.algorithm.BenchmarkAlgorithm;
import net.imglib2.parallel.Parallelization;
import process.Particle;

/**
 * A {@link SpotTracker} that uses point descriptors and descriptor distances to
 * create links between spots.
 * 
 * @author Jan Eglinger
 *
 */
public class PointDescriptorTracker extends BenchmarkAlgorithm implements SpotTracker {

	private Logger logger;
	private final SpotCollection spots;
	private Matcher matcher;
	private int subsetSize;
	private int numNeighbors;
	private int numThreads;
	private int maxInterval;
	private double costThreshold;
	private double squareDistThreshold;
	private boolean doPruneGraph;
	private DescriptorDistanceCostFunction costFunction;
	private AtomicInteger atomicInteger;
	private AtomicBoolean ok;
	private Set<Integer> excludedFrames;
	private SimpleWeightedGraph<Spot, DefaultWeightedEdge> graph;
	private SimpleWeightedGraph<Spot, DefaultWeightedEdge> prunedGraph;

	public PointDescriptorTracker(SpotCollection spots, int subsetSize, int numNeighbors, int maxInterval, double costThreshold, double squareDistThreshold, boolean pruneGraph) {
		this.spots = spots;
		this.subsetSize = subsetSize;
		this.numNeighbors = numNeighbors;
		this.maxInterval = maxInterval;
		this.costThreshold = costThreshold;
		this.squareDistThreshold = squareDistThreshold;
		this.doPruneGraph = pruneGraph;
	}

	@Override
	public SimpleWeightedGraph<Spot, DefaultWeightedEdge> getResult() {
		return doPruneGraph ? prunedGraph : graph;
	}

	@Override
	public boolean checkInput() {
		if (spots == null) {
			errorMessage = "SpotCollection is null.";
			return false;
		}
		return true;
	}

	@Override
	public boolean process() {
		logger.log("Matching descriptors...\n");
		long startTime = System.currentTimeMillis();
		ok = new AtomicBoolean(true);

		matcher = new SubsetMatcher(subsetSize, numNeighbors);
		excludedFrames = new HashSet<>();
		// create mapping
		// make flat map
		Map<Integer, SimplePointDescriptor<Particle>> spotMapping = createSpotDescriptorMapping(spots);
		// generate spot descriptors for all spots
		// compute descriptor distances for pairs of spots
		// cost function: lookup descriptor for spots, compute descriptor distance
		costFunction = new DistanceConstrainedDescriptorDistanceCostFunction(spotMapping, squareDistThreshold);
		// generate framePairs

		final ArrayList<int[]> framePairs = generateFramePairs();

		atomicInteger = new AtomicInteger(0);
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// multithreaded over all pairs: create cost matrix and run linker
		logger.log("Computing links on " + numThreads + " threads.\n");
		Parallelization.runWithNumThreads(numThreads, () -> createLinks(framePairs));

		// TODO link segments to allow split and merge events (and gaps?)
		// for track starts (graph root nodes), search previous frame
		// for track ends (graph leaf nodes), search next frame

		if (doPruneGraph) {
			// prune graph
			logger.log("Pruning graph...\n");
			prunedGraph = Tracks.prune(graph, true);
		}

		long endTime = System.currentTimeMillis();
		processingTime = endTime - startTime;
		return ok.get();
	}

	private void createLinks(ArrayList<int[]> framePairs) {
		for (int i = atomicInteger.getAndIncrement(); i < framePairs.size(); i = atomicInteger.getAndIncrement()) {
			if (!ok.get()) break;
			int sourceFrame = framePairs.get(i)[0];
			int targetFrame = framePairs.get(i)[1];
			logger.log("Linking spots in frames " + sourceFrame + " and " + targetFrame + ".\n");

			// sources and targets
			Iterable<Spot> sources = spots.iterable(sourceFrame, true);
			Iterable<Spot> targets = spots.iterable(targetFrame, true);
			if (!sources.iterator().hasNext()) continue;
			if (!targets.iterator().hasNext()) continue;

			// create cost matrix
			JaqamanLinkingCostMatrixCreator<Spot, Spot> costMatrixCreator = new JaqamanLinkingCostMatrixCreator<>(sources, targets, costFunction, costThreshold , 2.0d, 1d);
			JaqamanLinker<Spot, Spot> linker = new JaqamanLinker<>(costMatrixCreator);
			if (!linker.checkInput() || !linker.process()) {
				// update error message
				errorMessage = "Linking failed: source frame: " + sourceFrame + ",  target frame: " + targetFrame + ".";
				ok.set(false);
				return;
			}

			//
			Map<Spot, Spot> linkMap = linker.getResult();
			Map<Spot, Double> costs = linker.getAssignmentCosts();
			synchronized (graph) {
				linkMap.forEach((sourceSpot, targetSpot) -> {
					logger.log("Linking spots: " + sourceSpot + " -> " + targetSpot + ".\n");
					// add edges to graph
					double cost = costs.get(sourceSpot);
					graph.addVertex(sourceSpot);
					graph.addVertex(targetSpot);
					DefaultWeightedEdge edge = graph.addEdge(sourceSpot, targetSpot);
					if (edge == null) {
						logger.error("Error creating edge.\n"); // Edge already present!
					} else {
						graph.setEdgeWeight(edge, cost);
					}
				});
			}
		}
	}

	private ArrayList<int[]> generateFramePairs() {
		NavigableSet<Integer> keySet = spots.keySet();

		final ArrayList<int[]> framePairs = new ArrayList<>(keySet.size() - 1);
		final Iterator<Integer> frameIteratorA = keySet.iterator();
		while (frameIteratorA.hasNext()) { // ascending order
			final Integer frameA = frameIteratorA.next();
			if (excludedFrames.contains(frameA)) {
				continue;
			}
			final Iterator<Integer> frameIteratorB = keySet.tailSet(frameA, false).iterator();
			while (frameIteratorB.hasNext()) {
				final Integer frameB = frameIteratorB.next();
				if (excludedFrames.contains(frameB)) {
					continue;
				}
				// add framePair if difference smaller/equal maxInterval
				if (frameB - frameA <= maxInterval) {
					logger.log("Adding frame pair: " + frameA + "," + frameB + "\n");
					framePairs.add(new int[] { frameA, frameB });					
				}
			}
		}
		return framePairs;
	}

	private Map<Integer, SimplePointDescriptor<Particle>> createSpotDescriptorMapping(SpotCollection spotCollection) {
		Map<Integer, SimplePointDescriptor<Particle>> spotDescriptorMap = new HashMap<>(spotCollection.getNSpots(true));
		for (Integer frame : spotCollection.keySet()) {
			// only process frames with enough spots
			// add excluded frames to excludedFrames
			if (spots.getNSpots(frame, true) <= numNeighbors) {
				excludedFrames.add(frame);
				logger.log("[WARNING] Ignoring frame " + frame + ": too few spots.\n");
			} else {
				appendDescriptorMapping(spots.iterable(frame, true), spotDescriptorMap);
			}
		}
		return spotDescriptorMap;
	}

	private void appendDescriptorMapping(Iterable<Spot> spotIterable, Map<Integer, SimplePointDescriptor<Particle>> mapping) {
		ArrayList<Particle> list = new ArrayList<>();
		double[] realPosition = new double[3];
		int[] position = new int[3];
		for (Spot spot : spotIterable) {
			// create new particle
			list.add(new Particle(spot.ID(), Spots.createPeak(spot, realPosition, position), 1.0f));
		}
		KDTree<Particle> tree = new KDTree<>(list);
		SimilarityMeasure similarityMeasure = new SquareDistance();
		ArrayList<SimplePointDescriptor<Particle>> pointDescriptors = createSimplePointDescriptors(tree, list, numNeighbors, matcher, similarityMeasure);
		// TODO the following loop should probably be inside the above function call
		for (SimplePointDescriptor<Particle> pointDescriptor : pointDescriptors) {
			mapping.put((int) pointDescriptor.getBasisPoint().getID(), pointDescriptor);
		}
	}

	private ArrayList<SimplePointDescriptor<Particle>> createSimplePointDescriptors(KDTree<Particle> tree,
			ArrayList<Particle> list, int nNeighbors, Matcher matcher, SimilarityMeasure similarityMeasure) {
		ArrayList<SimplePointDescriptor<Particle>> descriptors = new ArrayList<>();
		NNearestNeighborSearch<Particle> nnsearch = new NNearestNeighborSearch<>(tree);
		for (Particle p : list) {
			ArrayList<Particle> neighbors = new ArrayList<>();
			Particle[] neighborList = nnsearch.findNNearestNeighbors(p, nNeighbors + 1);
			for (int i = 1; i < neighborList.length; i++) {
				neighbors.add(neighborList[i]);
			}
			try {
				descriptors.add(new SimplePointDescriptor<Particle>(p, neighbors, similarityMeasure, matcher));
			} catch (NoSuitablePointsException e) {
				logger.error("Not enough neighbors to create descriptor for spot " + p.getID());
				e.printStackTrace();
			}
		}
		return descriptors;
	}

	@Override
	public int getNumThreads() {
		return numThreads;
	}

	@Override
	public void setNumThreads() {
		numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
