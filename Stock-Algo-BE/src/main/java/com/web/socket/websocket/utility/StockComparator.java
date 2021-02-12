package com.web.socket.websocket.utility;

import com.web.socket.websocket.model.Stock;

import java.util.Comparator;

public class StockComparator implements Comparator<Stock> {
    /**
     * Compare the stocks entry to sort them accordingly
     * @param stock1
     * @param stock2
     * @return
     */
    @Override
    public int compare(Stock stock1, Stock stock2) {
        if(stock1.getEntry() > stock2.getEntry()) return 1;
        else if(stock1.getEntry() < stock2.getEntry()) return -1;
        else return 0;
    }
}
