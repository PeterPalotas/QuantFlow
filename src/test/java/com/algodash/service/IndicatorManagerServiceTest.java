package com.algodash.service;

import com.algodash.model.TimeFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IndicatorManagerServiceTest {

    @Mock
    private Ta4jAdapterService mockTa4jAdapterService;

    private IndicatorManagerService indicatorManagerService;

    private BarSeries testBarSeries;
    private final Duration barDuration = Duration.ofMinutes(1);

    @BeforeEach
    void setUp() {
        testBarSeries = new BaseBarSeriesBuilder().withName("test-series").build();
        indicatorManagerService = new IndicatorManagerService(mockTa4jAdapterService);
    }

    private void addBar(double price, int minutesAgo) {
        testBarSeries.addBar(testBarSeries.barBuilder()
                .timePeriod(barDuration)
                .endTime(ZonedDateTime.now().minusMinutes(minutesAgo).toInstant())
                .openPrice(price)
                .highPrice(price)
                .lowPrice(price)
                .closePrice(price)
                .volume(1)
                .build());
    }

    @Test
    void testRegisterAndGetSMA() {

        when(mockTa4jAdapterService.getTa4jBarSeries(TimeFrame.M1)).thenReturn(testBarSeries);

        indicatorManagerService.registerSMA("MySMA", TimeFrame.M1, 5);

        addBar(100, 5);
        addBar(101, 4);
        addBar(102, 3);
        addBar(103, 2);
        addBar(104, 1);

        Num smaValue = indicatorManagerService.getValue("MySMA");
        assertNotNull(smaValue, "SMA value should not be null after enough bars have been added.");
        assertEquals(102.0, smaValue.doubleValue(), 0.001);
    }

    @Test
    void testRegisterAndGetBollingerBands() {

        when(mockTa4jAdapterService.getTa4jBarSeries(TimeFrame.M1)).thenReturn(testBarSeries);
        final String bbName = "MyBB";

        indicatorManagerService.registerBollingerBands(bbName, TimeFrame.M1, 3, 2.0);

        addBar(10, 3);
        addBar(20, 2);
        addBar(30, 1);

        Num middle = indicatorManagerService.getValue(bbName + "_middle");
        Num upper = indicatorManagerService.getValue(bbName + "_upper");
        Num lower = indicatorManagerService.getValue(bbName + "_lower");

        assertNotNull(middle, "Middle band should not be null.");
        assertNotNull(upper, "Upper band should not be null.");
        assertNotNull(lower, "Lower band should not be null.");

        //SMA of (10, 20, 30) is 20.
        assertEquals(20.0, middle.doubleValue(), 0.001);

        //Assert that the upper and lower bands were calculated and are not the same as the middle.
        assertNotEquals(middle.doubleValue(), upper.doubleValue());
        assertNotEquals(middle.doubleValue(), lower.doubleValue());
    }
}
