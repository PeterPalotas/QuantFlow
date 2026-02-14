package com.algodash.service;

import com.algodash.model.TimeFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;


import java.util.HashMap;
import java.util.Map;

/**
 * Manages the lifecycle and retrieval of ta4j indicators.
 * Strategies register the indicators they need, and this service handles creation
 * and provides access to their values.
 */
@Service
public class IndicatorManagerService {

    private final Ta4jAdapterService ta4jAdapterService;
    private final Map<String, Indicator<Num>> indicators = new HashMap<>();

    @Autowired
    public IndicatorManagerService(Ta4jAdapterService ta4jAdapterService) {
        this.ta4jAdapterService = ta4jAdapterService;
    }

    /**
     * Registers a Simple Moving Average (SMA) indicator.
     */
    public void registerSMA(String uniqueName, TimeFrame timeFrame, int barCount) {
        BarSeries series = ta4jAdapterService.getTa4jBarSeries(timeFrame);
        if (series != null) {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            Indicator<Num> sma = new SMAIndicator(closePrice, barCount);
            indicators.put(uniqueName, sma);
        }
    }

    /**
     * Registers a set of Bollinger Bands indicators.
     */
    public void registerBollingerBands(String uniqueName, TimeFrame timeFrame, int barCount, double multiplier) {
        BarSeries series = ta4jAdapterService.getTa4jBarSeries(timeFrame);
        if (series != null) {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            SMAIndicator sma = new SMAIndicator(closePrice, barCount);
            StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, barCount);

            BollingerBandsMiddleIndicator middleBand = new BollingerBandsMiddleIndicator(sma);

            Num k = series.numFactory().numOf(multiplier);

            BollingerBandsUpperIndicator upperBand = new BollingerBandsUpperIndicator(middleBand, sd, k);
            BollingerBandsLowerIndicator lowerBand = new BollingerBandsLowerIndicator(middleBand, sd, k);

            indicators.put(uniqueName + "_middle", middleBand);
            indicators.put(uniqueName + "_lower", lowerBand);
            indicators.put(uniqueName + "_upper", upperBand);
        }
    }

    /**
     * Gets the latest value of a registered indicator.
     */
    public Num getValue(String uniqueName) {
        Indicator<Num> indicator = indicators.get(uniqueName);
        if (indicator == null || indicator.getBarSeries().isEmpty()) {
            return null;
        }
        return indicator.getValue(indicator.getBarSeries().getEndIndex());
    }

    /**
     * Gets a registered indicator object itself.
     */
    public Indicator<Num> getIndicator(String uniqueName) {
        return indicators.get(uniqueName);
    }
}
