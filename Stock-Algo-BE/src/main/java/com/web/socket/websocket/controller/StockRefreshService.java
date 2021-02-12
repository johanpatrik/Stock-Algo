package com.web.socket.websocket.controller;

import com.web.socket.websocket.model.Stock;
import com.web.socket.websocket.model.StockResult;
import com.web.socket.websocket.model.TrendType;
import com.web.socket.websocket.repository.StockRepository;
import com.web.socket.websocket.utility.StockComparator;
import com.web.socket.websocket.utility.StockFetcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.web.socket.websocket.utility.NumberUtilities.NINETY_TWO_PERCENT;
import static com.web.socket.websocket.utility.NumberUtilities.THREE_PERCENT;

@Slf4j
@Service
@EnableScheduling
public class StockRefreshService {

    private StockFetcher stockFetcher;
    private SimpMessagingTemplate template;
    private StockRepository stockRepository;
    private List<Stock> stocksNotified = new ArrayList<>();
    List<Stock> upTrend = new ArrayList<>();
    List<Stock> downTrend = new ArrayList<>();
    List<Stock> unDecided = new ArrayList<>();
    List<Stock> combinedLists = new ArrayList<>();

    @Autowired
    public StockRefreshService(StockFetcher stockFetcher, SimpMessagingTemplate template, StockRepository stockRepository) {
        this.stockFetcher = stockFetcher;
        this.template = template;
        this.stockRepository = stockRepository;
    }

    /**
     * Clear all lists and reset stocks notified
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void clearLists() {
        log.info("Clearing Lists");
        stocksNotified.forEach(stock -> stock.setNotified(false));
        stocksNotified.clear();
        upTrend.clear();
        downTrend.clear();
        unDecided.clear();
        combinedLists.clear();
    }

    /**
     * Collect all lists unto one
     */
    private void combineLists() {
        combinedLists.clear();
        combinedLists = Stream.of(upTrend, downTrend, unDecided).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Check so that the User has not been notified about the stock AND if it contains within the stocks notified list
     *
     * @param existing
     * @return
     */
    private boolean isNotified(Stock existing) {
        return stocksNotified.contains(existing) && !existing.isNotified();
    }

    /**
     * Populating the lists with the data from the database, sorting the stocks in lists after their trend
     */
    @Scheduled(cron = "0 15 6 * * *")
    public void populateLists() {
        log.info("Populating Lists");
        List<Stock> newStocks = getStocks();
        combineLists();

        newStocks.forEach(newStock -> {
            Optional<Stock> existingStock = stockRepository.findById(newStock.getName());

            existingStock.ifPresent(existing -> {


                if (!combinedLists.contains(existing)) {

                    TrendType stocksTrendType = existingStock.get().getTrendType();

                    if (stocksTrendType == TrendType.UP) {
                        upTrend.add(existingStock.get());
                    } else if (stocksTrendType == TrendType.DOWN) {
                        downTrend.add(existingStock.get());
                    } else if (stocksTrendType == TrendType.UNDECIDED) {
                        unDecided.add(existingStock.get());
                    }
                }
            });
        });
    }

    /**
     * Qualifying the stocks to see if the user shall be notified, if so they are added to the stocks notified list
     */
    @Scheduled(cron = "* * 9-23 * * *")
    public void live() {
        log.info("Qualifying Stocks");
        List<Stock> newStocks = getStocks();

        newStocks.forEach(newStock -> {
            Optional<Stock> existingStock = stockRepository.findById(newStock.getName());

            existingStock.ifPresent(existing -> {
                existing.setLastPrice(newStock.getLastPrice());
                existing.setEntry();
                if (isNotified(existing)) {
                    existing.setNotified(true);
                }

                if (existing.isDownTrend() && existing.getEntry() >= 0 && !stocksNotified.contains(existing)) {
                    existing.setTimeOfBuySignal(LocalTime.now().withNano(0));

                    stocksNotified.add(existing);
                    stockRepository.save(existing);
                }
            });
        });

        sortLists();
        convertAndSend();
    }

    /**
     * Converts and sends the data to the destination /topic/stock
     */
    private void convertAndSend() {
        this.template.convertAndSend("/topic/stock", new StockResult(upTrend, downTrend, stocksNotified));
    }

    /**
     * Sorting the stocks after data that is most relevant to the user
     */
    private void sortLists() {
        downTrend.sort(new StockComparator().reversed());
        upTrend.sort(new StockComparator());
        stocksNotified.sort(Comparator.comparing(Stock::getTimeOfBuySignal).reversed());
    }

    /**
     * Receiving the stocks after the filters I've provided
     *
     * @return
     */
    private List<Stock> getStocks() {
        return stockFetcher.getStocks();
    }

    /**
     * Remove the stocks from the database that are not in the filters anymore
     *
     * @param newStocks
     */
    private void removeStockNotFound(List<Stock> newStocks) {
        for (Stock stock : stockRepository.findAll()) {
            if (!newStocks.contains(stock)) {
                log.info("Removing Stock Not Found: " + stock.getName());
                stockRepository.delete(stock);
            }
        }
    }

    /**
     * Is the stock eligible to move from undecided OR uptrend list to the downtrend list?
     *
     * @param existing
     * @param newStock
     * @return
     */
    private boolean isMovingToDowntrend(Stock existing, Stock newStock) {
        return existing.isUpOrUndecidedTrend() && (existing.getEntry() < 0 || hasDecreasedEightPercent(newStock, existing));
    }

    /**
     * Is the stock eligible to move from undecided OR downtrend list to the uptrend list?
     *
     * @param existing
     * @param newStock
     * @return
     */
    private boolean isMovingToUptrend(Stock existing, Stock newStock) {
        return hasIncreasedThreePercent(newStock, existing) && existing.isDownOrUndecidedTrend();
    }

    /**
     * Fetches all the data when the stock market closes and manipulates data accordingly and then save to the database
     */
    @Scheduled(cron = "* 15 18 * * *")
    public void saveStockWhenClosing() {
        log.info("Initiating: Save Stock When Closing");

        List<Stock> newStocks = getStocks();

        combineLists();

        newStocks.forEach(newStock -> {
            Optional<Stock> existingStock = stockRepository.findById(newStock.getName());

            existingStock.ifPresentOrElse(existing -> {
                log.info(existing.getName() + ": Existing Stock is present");

                existing.setLastPrice(newStock.getLastPrice());

                existing.setHighestClosingPrice();
                existing.setLowestClosingPrice();
                existing.setEntry();


                if (isMovingToUptrend(existing, newStock)) {
                    log.info("Stock has increased 3%");
                    log.info("Stock is moving to Uptrend");
                    moveFromDownToUpTrend(existing);
                } else if (isMovingToDowntrend(existing, newStock)) {
                    log.info("Stock has decreased 8% OR Entry below 0");
                    log.info("Stock is moving to Downtrend");
                    moveFromUpOrUndecidedToDownTrend(existing);
                }
            }, () -> {
                log.info(newStock.getName() + ": Existing Stock is NOT Present");
                newStock.setLowestClosingPrice(newStock.getLastPrice());
                newStock.setHighestClosingPrice(newStock.getLastPrice());
                newStock.setEntry(0);
                newStock.setLastPrice(newStock.getLastPrice());
                newStock.setTrendType(TrendType.UNDECIDED);
                unDecided.add(newStock);
            });
        });
        combineLists();

        stockRepository.saveAll(combinedLists);
        removeStockNotFound(newStocks);
        convertAndSend();

    }


    /**
     * Moving the stocks from Undecided or Uptrend to the downtrend list
     *
     * @param existing
     */
    private void moveFromUpOrUndecidedToDownTrend(Stock existing) {
        if (upTrend.contains(existing)) {
            log.info("Moving Stock from Up Trend to Down Trend");
            existing.setLowestClosingPrice(existing.getLastPrice());
            upTrend.remove(existing);
        } else {
            log.info("Moving Stock from Undecided Trend to Down Trend");
            existing.setLowestClosingPrice(existing.getLastPrice());
            unDecided.remove(existing);
        }
        existing.setTrendType(TrendType.DOWN);
        downTrend.add(existing);
    }


    /**
     * Moving the stocks from Undecided or downtrend to the uptrend list
     *
     * @param existing
     */
    private void moveFromDownToUpTrend(Stock existing) {
        if (downTrend.contains(existing)) {
            log.info("Moving Stock from Down Trend to Up Trend");
            existing.setHighestClosingPrice(existing.getLastPrice());
            downTrend.remove(existing);
        } else {
            log.info("Moving Stock from Undecided Trend to Up Trend");
            existing.setHighestClosingPrice(existing.getLastPrice());
            unDecided.remove(existing);
        }
        existing.setTrendType(TrendType.UP);
        upTrend.add(existing);
    }

    /**
     * Has the stock decreased 8 percent?
     *
     * @param newStock
     * @param existing
     * @return
     */
    private boolean hasDecreasedEightPercent(Stock newStock, Stock existing) {
        return ((newStock.getLastPrice() / existing.getHighestClosingPrice()) < NINETY_TWO_PERCENT);
    }

    /**
     * Has the stock increased 3 percent from the lowest closing price?
     *
     * @param newStock
     * @param existingStock
     * @return
     */
    private boolean hasIncreasedThreePercent(Stock newStock, Stock existingStock) {
        return (newStock.getLastPrice() >= (existingStock.getLowestClosingPrice() * THREE_PERCENT));
    }
}