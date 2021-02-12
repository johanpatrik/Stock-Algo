package com.web.socket.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockResult {

    private List<Stock> upTrend;
    private List<Stock> downTrend;
    private List<Stock> stocksNotified;
}
