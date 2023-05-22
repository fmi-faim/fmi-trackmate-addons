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
package ch.fmi.trackmate.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.features.spot.SpotAnalyzer;
import fiji.plugin.trackmate.features.spot.SpotAnalyzerFactory;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

@Plugin(type = SpotAnalyzerFactory.class)
public class ManualFeatureSpotAnalyzerFactory<T extends RealType<T> & NativeType<T>> implements SpotAnalyzerFactory<T> {

	private static final String INT_FEATURE = "MANUAL_INTEGER_SPOT_FEATURE";
	private static final String DOUBLE_FEATURE = "MANUAL_DOUBLE_SPOT_FEATURE";

	private static final String KEY = "MANUAL_SPOT_FEATURE_ANALYZER";
	private static final String NAME = "Manual Spot Feature Analyzer";
	private static final String INFO = "<html>A (dummy) Spot Analyzer providing manual spot features</html>";

	private static List<String> FEATURES = new ArrayList<>(1);
	private static Map<String, String> FEATURE_SHORT_NAMES = new HashMap<>(1);
	private static Map<String, String> FEATURE_NAMES = new HashMap<>(1);
	private static Map<String, Dimension> FEATURE_DIMENSIONS = new HashMap<>(1);
	private static Map<String, Boolean> IS_INT = new HashMap<>(1);

	static {
		FEATURES.add(INT_FEATURE);
		FEATURES.add(DOUBLE_FEATURE);

		FEATURE_SHORT_NAMES.put(INT_FEATURE, "Integer Spot Feature");
		FEATURE_SHORT_NAMES.put(DOUBLE_FEATURE, "Double Spot Feature");

		FEATURE_NAMES.put(INT_FEATURE, "Custom Integer Spot Feature");
		FEATURE_NAMES.put(DOUBLE_FEATURE, "Custom Double Spot Feature");

		FEATURE_DIMENSIONS.put(INT_FEATURE, Dimension.NONE);
		FEATURE_DIMENSIONS.put(DOUBLE_FEATURE, Dimension.NONE);

		IS_INT.put(INT_FEATURE, true);
		IS_INT.put(DOUBLE_FEATURE, false);
	}

	@Override
	public List<String> getFeatures() {
		return FEATURES;
	}

	@Override
	public Map<String, String> getFeatureShortNames() {
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public Map<String, String> getFeatureNames() {
		return FEATURE_NAMES;
	}

	@Override
	public Map<String, Dimension> getFeatureDimensions() {
		return FEATURE_DIMENSIONS;
	}

	@Override
	public Map<String, Boolean> getIsIntFeature() {
		return IS_INT;
	}

	@Override
	public boolean isManualFeature() {
		return true;
	}

	@Override
	public String getInfoText() {
		return INFO;
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

	@SuppressWarnings( "unchecked" )
	@Override
	public SpotAnalyzer< T > getAnalyzer( final ImgPlus< T > img, final int frame, final int channel )	{
		return ( SpotAnalyzer< T > ) SpotAnalyzer.DUMMY_ANALYZER;
	}
}
