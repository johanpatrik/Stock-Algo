package com.web.socket.websocket.utility;

import com.web.socket.websocket.model.Stock;
import com.web.socket.websocket.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class StockFetcher {

    private static final String _5m_30m3 = "";
    private static final String _10m_20m3_50y = "";
    private static final String _50m3 = "";
    private static final String _200thisYear = "";
    private static final String _200OneYear = "";
    private StockService stockService;

    public StockFetcher(StockService stockService) {
        this.stockService = stockService;
    }

    public List<Stock> getStocks() {
        try {
            return Stream.of(stockService.getStocks(_5m_30m3),
                    stockService.getStocks(_10m_20m3_50y),
                    stockService.getStocks(_50m3),
                    stockService.getStocks(_200thisYear),
                    stockService.getStocks(_200OneYear))
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.info("Error fetching stocks: " + e.getMessage());
            return null;
        }
    }
}
