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
