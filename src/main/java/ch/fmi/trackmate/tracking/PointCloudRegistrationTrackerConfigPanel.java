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
 * {@link ConfigurationPanel} for {@link PointCloudRegistrationTrackerFactory}
 * 
 * @author Jan Eglinger
 * @deprecated superseded by {@link PointDescriptorTrackerConfigPanel}
 */
@Deprecated
public class PointCloudRegistrationTrackerConfigPanel extends
	ConfigurationPanel
{

	private static final long serialVersionUID = 1L;
	
	private JFormattedTextField minInliersTextField;
	private JFormattedTextField frameIntervalTextField;
	private JCheckBox lowCoverageCheckbox;
	private JFormattedTextField minFractionTextField;

	public PointCloudRegistrationTrackerConfigPanel() {
		initGui();
	}

	private void initGui() {
		add(new JLabel("Minimal number of inliers per compared pair:"));
		minInliersTextField = new JFormattedTextField( PointCloudRegistrationTrackerFactory.DEFAULT_MIN_NUM_INLIERS );
		add(minInliersTextField);
		
		add(new JLabel("Frame interval range:"));
		frameIntervalTextField = new JFormattedTextField( PointCloudRegistrationTrackerFactory.DEFAULT_FRAME_RANGE );
		add(frameIntervalTextField);

		//add(new JLabel("Discard pairs with low field-of-view coverage"));
		lowCoverageCheckbox = new JCheckBox("Discard pairs with low field-of-view coverage", PointCloudRegistrationTrackerFactory.DEFAULT_DISCARD_LOW_COVERAGE);
		add(lowCoverageCheckbox);

		add(new JLabel("Minimal fraction (of each dimension) covered by inliers within single frame:"));
		minFractionTextField = new JFormattedTextField( PointCloudRegistrationTrackerFactory.DEFAULT_MIN_COVERAGE_FACTOR );
		add(minFractionTextField);
	}

	@Override
	public void setSettings(final Map<String, Object> settings) {
		minInliersTextField.setText("" + settings.get(PointCloudRegistrationTrackerFactory.MIN_NUM_INLIERS));
		frameIntervalTextField.setText("" + settings.get(PointCloudRegistrationTrackerFactory.FRAME_RANGE));
		lowCoverageCheckbox.setSelected((boolean) settings.get(PointCloudRegistrationTrackerFactory.DISCARD_LOW_COVERAGE));
		minFractionTextField.setText("" + settings.get(PointCloudRegistrationTrackerFactory.MIN_COVERAGE_FACTOR));
	}

	@Override
	public Map<String, Object> getSettings() {
		final Map<String, Object> map = new HashMap<>(5);
		map.put(PointCloudRegistrationTrackerFactory.MIN_NUM_INLIERS, (int) minInliersTextField.getValue());
		map.put(PointCloudRegistrationTrackerFactory.FRAME_RANGE, (int) frameIntervalTextField.getValue());
		map.put(PointCloudRegistrationTrackerFactory.DISCARD_LOW_COVERAGE, lowCoverageCheckbox.isSelected());
		map.put(PointCloudRegistrationTrackerFactory.MIN_COVERAGE_FACTOR, minFractionTextField.getValue());
		return map;
	}

	@Override
	public void clean() {
		// Nothing to do
	}

}
