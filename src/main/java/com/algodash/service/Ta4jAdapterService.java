package com.algodash.service;

import com.algodash.model.Candle;
import com.algodash.model.TimeFrame;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
public class Ta4jAdapterService {

    //manages a ta4j BarSeries for each TimeFrame
    private final Map<TimeFrame, BarSeries> ta4jBarSeriesMap = new EnumMap<>(TimeFrame.class);
    //helper map to store the duration for each timeframe
    private final Map<TimeFrame, Duration> timeFrameDurations = new EnumMap<>(TimeFrame.class);

    public Ta4jAdapterService() {
        // Configure supported timeframes
        timeFrameDurations.put(TimeFrame.M1, Duration.ofMinutes(1));
        ta4jBarSeriesMap.put(TimeFrame.M1, new BaseBarSeriesBuilder().withName("BTC/USDT-1m").build());
    }

    /**
     * Converts custom Candle object to a ta4j Bar object and adds it to the
     * appropriate ta4j BarSeries.
     *
     * @param ourCandle The custom Candle object.
     * @param timeFrame The TimeFrame of the candle.
     */
    public void addBar(Candle ourCandle, TimeFrame timeFrame) {
        BarSeries barSeries = ta4jBarSeriesMap.get(timeFrame);
        Duration barDuration = timeFrameDurations.get(timeFrame);

        if (barSeries == null || barDuration == null) {
            System.err.println("Ta4jAdapterService: No configuration found for TimeFrame: " + timeFrame);
            return;
        }

        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ourCandle.getCloseTime()), ZoneOffset.UTC);

        barSeries.addBar(barSeries.barBuilder()
                .timePeriod(barDuration)
                .endTime(endTime.toInstant())
                .openPrice(ourCandle.getOpen())
                .highPrice(ourCandle.getHigh())
                .lowPrice(ourCandle.getLow())
                .closePrice(ourCandle.getClose())
                .volume(ourCandle.getVolume())
                .build());
    }

    /**
     * Retrieves the ta4j BarSeries for a given TimeFrame.
     * This BarSeries can then be used to create ta4j indicators.
     *
     * @param timeFrame The TimeFrame for which to retrieve the BarSeries.
     * @return The ta4j BarSeries, or null if not found for the given TimeFrame.
     */
    public BarSeries getTa4jBarSeries(TimeFrame timeFrame) {
        return ta4jBarSeriesMap.get(timeFrame);
    }
}
