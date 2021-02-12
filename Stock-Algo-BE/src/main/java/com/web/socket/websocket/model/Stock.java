package com.web.socket.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;

import static com.web.socket.websocket.utility.NumberUtilities.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Stock {

    @Id
    private String name;
    private String website;
    private double lastPrice;
    private double highestClosingPrice;
    private double lowestClosingPrice;
    private double entry;
    private boolean notified;
    private TrendType trendType;
    private LocalTime timeOfBuySignal;

    /**
     * Gets the most recent price of the stock,
     * Calculates the percentage of the entry point, entry >= 0 Means buy
     * Then formats the entry up to 2 decimals.
     */
    public void setEntry() {
        double calculatedEntry = ((lastPrice / (lowestClosingPrice * THREE_PERCENT))
                - CONVERT_TO_PERCENT_DIFFERENCE)
                * CONVERT_TO_WHOLE_NUMBER;

        double formattedEntry = Math.round(calculatedEntry * 100.0) / 100.0;
        entry = formattedEntry;
    }

    /**
     * Setting the lowest closing price for the day of this stock
     */
    public void setLowestClosingPrice() {
        if (lowestClosingPrice > lastPrice) {
            log.info("Setting Lowest Closing Price");
            lowestClosingPrice = lastPrice;
        }
    }

    /**
     * Setting the highest closing price for the day of this stock
     */
    public void setHighestClosingPrice() {
        if (highestClosingPrice < lastPrice) {
            log.info("Setting Highest Closing Price");
            this.highestClosingPrice = lastPrice;
        }
    }

    public boolean isUpOrUndecidedTrend() {
        return trendType == TrendType.UP || trendType == TrendType.UNDECIDED;
    }

    public boolean isDownOrUndecidedTrend() {
        return trendType == TrendType.DOWN || trendType == TrendType.UNDECIDED;
    }

    public boolean isDownTrend() {
        return trendType == TrendType.DOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock)) return false;
        Stock stock = (Stock) o;
        return Objects.equals(getName(), stock.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}