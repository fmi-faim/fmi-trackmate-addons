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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import fiji.plugin.trackmate.gui.components.ConfigurationPanel;

/**
 * {@link ConfigurationPanel} implementation for {@link PointDescriptorTrackerFactory}.
 * 
 * @author Jan Eglinger
 *
 */
public class PointDescriptorTrackerConfigPanel extends ConfigurationPanel {

	private static final long serialVersionUID = 1L;

	private JFormattedTextField subsetSizeTextField;
	private JFormattedTextField numNeighborsTextField;
	private JFormattedTextField maxIntervalTextField;
	private JFormattedTextField costThresholdTextField;
	private JFormattedTextField maxDistanceTextField;
	private JCheckBox pruneCheckBox;

	public PointDescriptorTrackerConfigPanel() {
		initGui();
	}

	private void initGui() {
		add(new JLabel("Number of neighbors in subset"));
		subsetSizeTextField = new JFormattedTextField( PointDescriptorTrackerFactory.DEFAULT_SUBSET_NEIGHBORS );
		add(subsetSizeTextField);
		
		add(new JLabel("Number of neighbors to choose from"));
		numNeighborsTextField = new JFormattedTextField( PointDescriptorTrackerFactory.DEFAULT_NUM_NEIGHBORS );
		add(numNeighborsTextField);
		
		add(new JLabel("Maximum frame interval for matching"));
		maxIntervalTextField = new JFormattedTextField( PointDescriptorTrackerFactory.DEFAULT_MAX_INTERVAL );
		add(maxIntervalTextField);

		add(new JLabel("Cost (descriptor distance) threshold"));
		costThresholdTextField = new JFormattedTextField( PointDescriptorTrackerFactory.DEFAULT_COST_THRESHOLD );
		add(costThresholdTextField);

		add(new JLabel("Maximum linking distance"));
		maxDistanceTextField = new JFormattedTextField( PointDescriptorTrackerFactory.DEFAULT_MAX_LINKING_DISTANCE );
		add(maxDistanceTextField);

		add(new JLabel("Return pruned graph"));
		pruneCheckBox = new JCheckBox(PointDescriptorTrackerFactory.PRUNE_GRAPH, PointDescriptorTrackerFactory.DEFAULT_PRUNE_GRAPH);
		add(pruneCheckBox);
}

	@Override
	public void clean() {
		// Nothing to do
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map<String, Object> map = new HashMap<>(5);
		map.put(PointDescriptorTrackerFactory.SUBSET_NEIGHBORS, (int) subsetSizeTextField.getValue());
		map.put(PointDescriptorTrackerFactory.NUM_NEIGHBORS, (int) numNeighborsTextField.getValue());
		map.put(PointDescriptorTrackerFactory.MAX_INTERVAL, (int) maxIntervalTextField.getValue());
		map.put(PointDescriptorTrackerFactory.COST_THRESHOLD, (double) costThresholdTextField.getValue());
		map.put(PointDescriptorTrackerFactory.MAX_LINKING_DISTANCE, (double) maxDistanceTextField.getValue());
		map.put(PointDescriptorTrackerFactory.PRUNE_GRAPH, pruneCheckBox.isSelected());
		return map;
	}

	@Override
	public void setSettings(final Map<String, Object> settings) {
		subsetSizeTextField.setText("" + settings.get(PointDescriptorTrackerFactory.SUBSET_NEIGHBORS));
		numNeighborsTextField.setText("" + settings.get(PointDescriptorTrackerFactory.NUM_NEIGHBORS));
		maxIntervalTextField.setText("" + settings.get(PointDescriptorTrackerFactory.MAX_INTERVAL));
		costThresholdTextField.setText("" + settings.get(PointDescriptorTrackerFactory.COST_THRESHOLD));
		maxDistanceTextField.setText("" + settings.get(PointDescriptorTrackerFactory.MAX_LINKING_DISTANCE));
		pruneCheckBox.setSelected((boolean) settings.get(PointDescriptorTrackerFactory.PRUNE_GRAPH));
	}
}
