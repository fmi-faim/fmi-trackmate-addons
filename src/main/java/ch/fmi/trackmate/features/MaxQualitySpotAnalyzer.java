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

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.features.spot.SpotAnalyzer;

public class MaxQualitySpotAnalyzer<T> implements SpotAnalyzer<T> {

	@Override
	public void process( final Iterable< Spot > spotIt ) {
		/*
		 * Map that stores the spots with the highest quality in a frame as
		 * values, with the frames of these spots as keys.
		 */
		final Map< Integer, Spot > maxQualitySpots = new HashMap<>();

		/*
		 * First iteration to get spot with max quality in every frame.
		 */

		for ( final Spot spot : spotIt ) {
			final Integer frame = Integer.valueOf( spot.getFeature( Spot.FRAME ).intValue() );
			final Spot spotMax = maxQualitySpots.get( frame );

			if ( spotMax == null ) {
				// We don't have a spot for this frame yet. Initialize then loop.
				maxQualitySpots.put( frame, spot );
				continue;
			}
			
			// Is the current spot having a larger quality? If yes we store it.
			final double qualityMax = spotMax.getFeature( Spot.QUALITY ).doubleValue();
			final double quality = spot.getFeature( Spot.QUALITY ).doubleValue();
			if ( quality > qualityMax )
				maxQualitySpots.put( frame, spot );
		}

		/*
		 * Second iteration to label spot with max quality
		 */

		// All spots get the FALSE label first.
		final Double falseVal = Double.valueOf( 0. );
		spotIt.forEach( s -> s.putFeature( MaxQualitySpotAnalyzerFactory.HAS_MAX_QUALITY_IN_FRAME, falseVal ) );

		// The spot we flagged in 1st iteration get labeled with TRUE.
		final Double trueVal = Double.valueOf( 1. );
		maxQualitySpots.values().forEach( s -> s.putFeature( MaxQualitySpotAnalyzerFactory.HAS_MAX_QUALITY_IN_FRAME, trueVal ) );
	}
}
