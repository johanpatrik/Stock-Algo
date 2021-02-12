package com.web.socket.websocket.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class StockRefreshController {

    private StockRefreshService stockRefreshService;

    @Autowired
    public StockRefreshController(StockRefreshService stockRefreshService) {
        this.stockRefreshService = stockRefreshService;
    }

    @GetMapping("/start-stock/savewhenclosing")
    public void startStockRefreshJob() {
      log.info("Starting saveStockWhenClosing manually");
      stockRefreshService.saveStockWhenClosing();
      log.info("Ending saveStockWhenClosing manually");
    }


    @GetMapping("/start-stock/populatelists")
    public void startPopulatingLists() {
        log.info("Starting populating lists manually");
        stockRefreshService.populateLists();
        log.info("Ending populating lists manually");
    }

    @GetMapping("/start-stock/clearlists")
    public void startClearingLists() {
        log.info("Starting clearing lists manually");
        stockRefreshService.clearLists();
        log.info("Ending clearing lists manually");
    }

    @GetMapping("/start-stock/live")
    public void live() {
        log.info("Starting live manually");
        stockRefreshService.live();
        log.info("Ending live manually");
    }

    @GetMapping("/start-stock/test")
    public void testing() {
        log.info("Starting Test");
        stockRefreshService.clearLists();
        stockRefreshService.populateLists();
        stockRefreshService.saveStockWhenClosing();
        stockRefreshService.live();
        log.info("Ending Test");
    }
}
