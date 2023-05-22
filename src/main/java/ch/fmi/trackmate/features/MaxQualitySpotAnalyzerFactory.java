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
import java.util.Collections;
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

@Plugin(type = SpotAnalyzerFactory.class, priority = 0d)
public class MaxQualitySpotAnalyzerFactory<T extends RealType<T> & NativeType<T>> implements SpotAnalyzerFactory<T> {

	private static final String KEY = "HAS_MAX_QUALITY_IN_FRAME";
	public static final String HAS_MAX_QUALITY_IN_FRAME = "HAS_MAX_QUALITY_IN_FRAME";
	public static final List<String> FEATURES = new ArrayList<>(1);
	private static final Map<String, Boolean> IS_INT = new HashMap<>(1);
	public static final Map<String, String> FEATURE_NAMES = new HashMap<>(1);
	public static final Map<String, String> FEATURE_SHORT_NAMES = new HashMap<>(1);
	public static final Map<String, Dimension> FEATURE_DIMENSIONS = new HashMap<>(1);

	private static final String NAME = "Has Max Quality in Frame";

	static {
		FEATURES.add(HAS_MAX_QUALITY_IN_FRAME);
		IS_INT.put(HAS_MAX_QUALITY_IN_FRAME, true);
		FEATURE_SHORT_NAMES.put(HAS_MAX_QUALITY_IN_FRAME, "Max Quality");
		FEATURE_NAMES.put(HAS_MAX_QUALITY_IN_FRAME, "Has max quality");
		FEATURE_DIMENSIONS.put(HAS_MAX_QUALITY_IN_FRAME, Dimension.NONE);
	}

	@Override
	public Map<String, Dimension> getFeatureDimensions() {
		return FEATURE_DIMENSIONS;
	}

	@Override
	public Map<String, String> getFeatureNames() {
		return FEATURE_NAMES;
	}

	@Override
	public Map<String, String> getFeatureShortNames() {
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public List<String> getFeatures() {
		return FEATURES;
	}

	@Override
	public Map<String, Boolean> getIsIntFeature() {
		return Collections.unmodifiableMap(IS_INT);
	}

	@Override
	public boolean isManualFeature() {
		return false;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getInfoText() {
		return "1 if this spot has the maximum quality in the frame, 0 otherwise";
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
	public SpotAnalyzer< T > getAnalyzer( final ImgPlus< T > img, final int frame, final int channel ) {
		return new MaxQualitySpotAnalyzer<>();
	}
}
