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

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.gui.components.ConfigurationPanel;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

/**
 * {@link SpotTrackerFactory} implementation for {@link PointDescriptorTracker}.
 * 
 * @author Jan Eglinger
 *
 */
@Plugin(type = SpotTrackerFactory.class)
public class PointDescriptorTrackerFactory implements SpotTrackerFactory {

	private static final String INFO_TEXT = "<html>This tracker matches point descriptors to link points between point clouds.</html>";
	private static final String KEY = "DESCRIPTOR-BASED_TRACKER";
	private static final String NAME = "Descriptor-based tracker";

	static final String SUBSET_NEIGHBORS = "SUBSET_NEIGHBORS";
	static final String NUM_NEIGHBORS = "NUM_NEIGHBORS";
	static final String MAX_INTERVAL = "MAX_INTERVAL";
	static final String COST_THRESHOLD = "COST_THRESHOLD";
	static final String MAX_LINKING_DISTANCE = "MAX_LINKING_DISTANCE";
	static final String PRUNE_GRAPH = "PRUNE_GRAPH";

	static final Integer DEFAULT_SUBSET_NEIGHBORS = 5;
	static final Integer DEFAULT_NUM_NEIGHBORS = 7;
	static final Integer DEFAULT_MAX_INTERVAL = 5;
	static final Double DEFAULT_COST_THRESHOLD = 100d;
	static final Double DEFAULT_MAX_LINKING_DISTANCE = 10d;
	static final Boolean DEFAULT_PRUNE_GRAPH = true;

	private String errorMessage;

	@Override
	public ImageIcon getIcon() {
		return null; // No icon needed.
	}

	@Override
	public String getInfoText() {
		return INFO_TEXT;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean checkSettingsValidity(final Map<String, Object> settings) {
		if (settings == null) {
			errorMessage = "Settings map is null.\n";
			return false;
		}
		if (settings.size() < 2) {
			errorMessage = "Too few settings entries.\n";
			return false;
		}
		if (!settings.containsKey(SUBSET_NEIGHBORS)
				|| !(settings.get(SUBSET_NEIGHBORS) instanceof Integer)
				|| (int) settings.get(SUBSET_NEIGHBORS) < 2)
		{
			errorMessage = "Wrong parameter for " + SUBSET_NEIGHBORS;
			return false;
		}
		if (!settings.containsKey(NUM_NEIGHBORS)
				|| !(settings.get(NUM_NEIGHBORS) instanceof Integer)
				|| (int) settings.get(NUM_NEIGHBORS) < 2)
		{
			errorMessage = "Wrong parameter for " + NUM_NEIGHBORS;
			return false;
		}
		if (!settings.containsKey(MAX_INTERVAL)
				|| !(settings.get(MAX_INTERVAL) instanceof Integer)
				|| (int) settings.get(MAX_INTERVAL) < 1)
		{
			errorMessage = "Wrong parameter for " + NUM_NEIGHBORS;
			return false;
		}
		if (!settings.containsKey(COST_THRESHOLD)
				|| !(settings.get(COST_THRESHOLD) instanceof Double)
				|| (double) settings.get(COST_THRESHOLD) <= 0)
		{
			errorMessage = "Wrong parameter for " + COST_THRESHOLD;
			return false;
		}
		if (!settings.containsKey(MAX_LINKING_DISTANCE)
				|| !(settings.get(MAX_LINKING_DISTANCE) instanceof Double)
				|| (double) settings.get(MAX_LINKING_DISTANCE) <= 0)
		{
			errorMessage = "Wrong parameter for " + MAX_LINKING_DISTANCE;
			return false;
		}
		if (!settings.containsKey(PRUNE_GRAPH)
				|| !(settings.get(PRUNE_GRAPH) instanceof Boolean))
		{
			errorMessage = "Wrong parameter for " + PRUNE_GRAPH;
			return false;
		}
		return true;
	}

	@Override
	public SpotTracker create(final SpotCollection spots, final Map<String, Object> settings) {
		final int subsetSize = (int) settings.get(SUBSET_NEIGHBORS);
		final int numNeighbors = (int) settings.get(NUM_NEIGHBORS);
		final int maxInterval = (int) settings.get(MAX_INTERVAL);
		final double costThreshold = (double) settings.get(COST_THRESHOLD);
		final double maxDistance = (double) settings.get(MAX_LINKING_DISTANCE);
		final boolean pruneGraph = (boolean) settings.get(PRUNE_GRAPH);
		return new PointDescriptorTracker(spots, subsetSize, numNeighbors, maxInterval, costThreshold, maxDistance*maxDistance, pruneGraph);
	}

	@Override
	public Map<String, Object> getDefaultSettings() {
		final Map<String, Object> settings = new HashMap<>(5);

		settings.put(SUBSET_NEIGHBORS, DEFAULT_SUBSET_NEIGHBORS);
		settings.put(NUM_NEIGHBORS, DEFAULT_NUM_NEIGHBORS);
		settings.put(MAX_INTERVAL, DEFAULT_MAX_INTERVAL);
		settings.put(COST_THRESHOLD, DEFAULT_COST_THRESHOLD);
		settings.put(MAX_LINKING_DISTANCE, DEFAULT_MAX_LINKING_DISTANCE);
		settings.put(PRUNE_GRAPH, DEFAULT_PRUNE_GRAPH);

		return settings;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public ConfigurationPanel getTrackerConfigurationPanel(final Model model) {
		return new PointDescriptorTrackerConfigPanel();
	}

	@Override
	public boolean marshall(final Map<String, Object> settings, final Element element) {
		if (!checkSettingsValidity(settings)) return false;

		final int subsetSize = (int) settings.get(SUBSET_NEIGHBORS);
		element.setAttribute(SUBSET_NEIGHBORS, "" + subsetSize);
		final int numNeighbors = (int) settings.get(NUM_NEIGHBORS);
		element.setAttribute(NUM_NEIGHBORS, "" + numNeighbors);
		final int maxInterval = (int) settings.get(MAX_INTERVAL);
		element.setAttribute(MAX_INTERVAL, "" + maxInterval);
		final double costThreshold = (double) settings.get(COST_THRESHOLD);
		element.setAttribute(COST_THRESHOLD, "" + costThreshold);
		final double maxDistance = (double) settings.get(MAX_LINKING_DISTANCE);
		element.setAttribute(MAX_LINKING_DISTANCE, "" + maxDistance);
		final boolean doPrune = (boolean) settings.get(PRUNE_GRAPH);
		element.setAttribute(PRUNE_GRAPH, "" + doPrune);

		return true;
	}

	@Override
	public String toString(final Map<String, Object> settings) {
		if (!checkSettingsValidity(settings)) return errorMessage;

		final StringBuilder str = new StringBuilder();
		final int subsetSize = (int) settings.get(SUBSET_NEIGHBORS);
		str.append("  Minimal number of spots in matching subset: " + subsetSize + ".\n");
		final int numNeighbors = (int) settings.get(NUM_NEIGHBORS);
		str.append("  Number of spots for subset selection: " + numNeighbors + ".\n");
		final int maxInterval = (int) settings.get(MAX_INTERVAL);
		str.append("  Maximal frame interval: " + maxInterval + ".\n");
		final double costThreshold = (double) settings.get(COST_THRESHOLD);
		str.append("  Cost threshold: " + costThreshold + ".\n");
		final double maxDistance = (double) settings.get(MAX_LINKING_DISTANCE);
		str.append("  Maximal linking distance: " + maxDistance + ".\n");
		final boolean doPrune = (boolean) settings.get(PRUNE_GRAPH);
		str.append("  Return pruned graph: " + doPrune + ".\n");

		return str.toString();
	}

	@Override
	public boolean unmarshall(final Element element, final Map<String, Object> settings) {
		try {
			final int subsetSize = element.getAttribute(SUBSET_NEIGHBORS).getIntValue();
			settings.put(SUBSET_NEIGHBORS, subsetSize);

			final int numNeighbors = element.getAttribute(NUM_NEIGHBORS).getIntValue();
			settings.put(NUM_NEIGHBORS, numNeighbors);

			final int maxInterval = element.getAttribute(MAX_INTERVAL).getIntValue();
			settings.put(MAX_INTERVAL, maxInterval);

			final double costThreshold = element.getAttribute(COST_THRESHOLD).getDoubleValue();
			settings.put(COST_THRESHOLD, costThreshold);

			final double maxDistance = element.getAttribute(MAX_LINKING_DISTANCE).getDoubleValue();
			settings.put(MAX_LINKING_DISTANCE, maxDistance);

			final boolean doPrune = element.getAttribute(PRUNE_GRAPH).getBooleanValue();
			settings.put(PRUNE_GRAPH, doPrune);
		}
		catch (final DataConversionException exc) {
			errorMessage = "Error retrieving settings from XML: " + exc.toString();
			return false;
		}
		return true;
	}

	@Override
	public SpotTrackerFactory copy() {
		return new PointDescriptorTrackerFactory();
	}
}
