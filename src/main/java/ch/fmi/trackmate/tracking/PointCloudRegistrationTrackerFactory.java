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
 * {@link SpotTrackerFactory} for {@link PointCloudRegistrationTracker}
 * 
 * @author Jan Eglinger
 * @deprecated superseded by {@link PointDescriptorTrackerFactory}
 */
@Deprecated
@Plugin(type = SpotTrackerFactory.class)
public class PointCloudRegistrationTrackerFactory implements
	SpotTrackerFactory
{

	private static final String INFO_TEXT = "<html>"
			+ "This tracker uses descriptor-based registration to link points between point clouds.<br/>"
			+ "The descriptor-based registration is robust to outliers, which is beneficial"
			+ "in terms of false positive detections, but has the drawback that some movements"
			+ "that are very distinct from a 'common' ensemble movement might not be detected."
			+ "Consider using the descriptor-based tracker that takes into account the local neighbors"
			+ "of each spot and is supposed to better deal with small ensembles showing a different"
			+ "movement from the entire point cloud."
			+ "</html>";
	private static final String KEY = "POINT_CLOUD_REGISTRATION_TRACKER";
	private static final String NAME = "Point-cloud registration tracker (old)";

	// Minimal number of inlier spots per comparison
	static final String MIN_NUM_INLIERS = "MIN_NUM_INLIERS";
	// Range of frame intervals being compared
	static final String FRAME_RANGE = "FRAME_RANGE";
	// Discard pairs with low field-of-view coverage
	static final String DISCARD_LOW_COVERAGE = "DISCARD_LOW_COVERAGE";
	// Fraction of each image dimension that has to be covered by inliers
	static final String MIN_COVERAGE_FACTOR = "MIN_COVERAGE_FACTOR";

	static final Integer DEFAULT_MIN_NUM_INLIERS = 10;
	static final Integer DEFAULT_FRAME_RANGE = 10;
	static final Boolean DEFAULT_DISCARD_LOW_COVERAGE = true;
	static final Double DEFAULT_MIN_COVERAGE_FACTOR = 0.2;

	private String errorMessage;

	@Override
	public String getInfoText() {
		return INFO_TEXT;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
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
	public SpotTracker create(final SpotCollection spots,
		final Map<String, Object> settings)
	{
		final int minNumInliers = (int) settings.get(MIN_NUM_INLIERS);
		final int frameRange = (int) settings.get(FRAME_RANGE);
		final boolean discardLowCoverage = (boolean) settings.get(DISCARD_LOW_COVERAGE);
		final double minCoverage = (double) settings.get(MIN_COVERAGE_FACTOR);
		return new PointCloudRegistrationTracker(spots, minNumInliers, frameRange, discardLowCoverage, minCoverage);
	}

	@Override
	public ConfigurationPanel getTrackerConfigurationPanel(final Model model) {
		return new PointCloudRegistrationTrackerConfigPanel();
	}

	@Override
	public boolean marshall(final Map<String, Object> settings, final Element element) {
		if (!checkSettingsValidity(settings)) return false;

		final int minNumInliers = (int) settings.get(MIN_NUM_INLIERS);
		element.setAttribute(MIN_NUM_INLIERS, "" + minNumInliers);
		final int frameRange = (int) settings.get(FRAME_RANGE);
		element.setAttribute(FRAME_RANGE, "" + frameRange);
		final boolean discardLowCoverage = (boolean) settings.get(DISCARD_LOW_COVERAGE);
		element.setAttribute(DISCARD_LOW_COVERAGE, "" + discardLowCoverage);
		final double minCoverage = (double) settings.get(MIN_COVERAGE_FACTOR);
		element.setAttribute(MIN_COVERAGE_FACTOR, "" + minCoverage);

		return true;
	}

	@Override
	public boolean unmarshall(final Element element, final Map<String, Object> settings) {
		try {
			final int minNumInliers = element.getAttribute(MIN_NUM_INLIERS).getIntValue();
			settings.put(MIN_NUM_INLIERS, minNumInliers);

			final int frameRange = element.getAttribute(FRAME_RANGE).getIntValue();
			settings.put(FRAME_RANGE, frameRange);

			final boolean discardLowCoverage = element.getAttribute(DISCARD_LOW_COVERAGE).getBooleanValue();
			settings.put(DISCARD_LOW_COVERAGE, discardLowCoverage);

			final double minCoverage = element.getAttribute(MIN_COVERAGE_FACTOR).getDoubleValue();
			settings.put(MIN_COVERAGE_FACTOR, minCoverage);
		}
		catch (final DataConversionException exc) {
			errorMessage = "Error retrieving settings from XML: " + exc.toString();
			return false;
		}
		return true;
	}

	@Override
	public String toString(final Map<String, Object> sm) {
		if (!checkSettingsValidity(sm)) return errorMessage;

		final StringBuilder str = new StringBuilder();
		final int minNumInliers = (int) sm.get(MIN_NUM_INLIERS);
		str.append("  Minimal number of inlier spots per comparison: " + minNumInliers + ".\n");
		final int frameRange = (int) sm.get(FRAME_RANGE);
		str.append("  Range of frame intervals being compared: " + frameRange + ".\n");
		final boolean discardLowCoverage = (boolean) sm.get(DISCARD_LOW_COVERAGE);
		str.append("  Discard pairs with low field-of-view coverage: " + discardLowCoverage + ".\n");
		final double minCoverage = (double) sm.get(MIN_COVERAGE_FACTOR);
		str.append("  Minimal fraction (of each dimension) covered by inliers within single frame: " + minCoverage + ".\n");
		return str.toString();
	}

	@Override
	public Map<String, Object> getDefaultSettings() {
		final Map<String, Object> settings = new HashMap<>(5);
		// Model choice?


		// Minimal number of inliers per compared pair
		settings.put(MIN_NUM_INLIERS, DEFAULT_MIN_NUM_INLIERS);

		// Frame interval range
		settings.put(FRAME_RANGE, DEFAULT_FRAME_RANGE);

		// Discard pairs with low field-of-view coverage
		settings.put(DISCARD_LOW_COVERAGE, DEFAULT_DISCARD_LOW_COVERAGE);

		// Minimal distance(xyz)/area/volume covered by inliers within single frame (um/percentage?)
		settings.put(MIN_COVERAGE_FACTOR, DEFAULT_MIN_COVERAGE_FACTOR);

		return settings;
	}

	@Override
	public boolean checkSettingsValidity(final Map<String, Object> settings) {
		if (settings == null) {
			errorMessage = "Settings map is null.\n";
			return false;
		}
		if (settings.size() < 4) {
			errorMessage = "Too few settings entries";
			return false;
		}
		if (!settings.containsKey(MIN_NUM_INLIERS)
				|| !(settings.get(MIN_NUM_INLIERS) instanceof Integer)
				|| (int) settings.get(MIN_NUM_INLIERS) < 0)
		{
			errorMessage = "Wrong parameter for " + MIN_NUM_INLIERS;
			return false;
		}
		if (!settings.containsKey(FRAME_RANGE)
				|| !(settings.get(FRAME_RANGE) instanceof Integer)
				|| (int) settings.get(FRAME_RANGE) < 0)
		{
			errorMessage = "Wrong parameter for " + FRAME_RANGE;
			return false;
		}
		if (!settings.containsKey(DISCARD_LOW_COVERAGE)
				|| !(settings.get(DISCARD_LOW_COVERAGE) instanceof Boolean))
		{
			errorMessage = "Wrong parameter for " + DISCARD_LOW_COVERAGE;
			return false;
		}
		if (!settings.containsKey(MIN_COVERAGE_FACTOR)
				|| !(settings.get(MIN_COVERAGE_FACTOR) instanceof Double)
				|| (double) settings.get(MIN_COVERAGE_FACTOR) < 0
				|| (double) settings.get(MIN_COVERAGE_FACTOR) > 1)
		{
			errorMessage = "Wrong parameter for " + MIN_COVERAGE_FACTOR;
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
