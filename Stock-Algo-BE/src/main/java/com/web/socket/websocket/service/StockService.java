package com.web.socket.websocket.service;

import com.web.socket.websocket.model.Stock;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    /**
     * Fetching the node and assigning the data to a Stock object
     that's then added to a list that's returned to the StockRefreshService
     *
     * @param link
     * @return
     * @throws IOException
     */
    public List<Stock> getStocks(String link) throws IOException {
        List<Stock> stocks = new ArrayList<>();

        Document doc = Jsoup.connect(link).get();

        Elements select = doc.select("body").select("div").select("table").select("tbody").select("tr");

        for (int i = 0; i < select.size(); i++) {
            try {
                String name = select.get(i).childNodes().get(3).childNodes().get(1).childNodes().get(2).toString();
                String href = select.get(i).childNodes().get(3).childNodes().get(1).attributes().get("href");

                List<Node> prices = getStockPrice(doc, i);

                Stock stock = Stock.builder()
                        .name(name.trim())
                        .website("Website")
                        .lastPrice(toDouble(((Element) prices.get(0)).text()))
                        .build();

                stocks.add(stock);

            } catch (IndexOutOfBoundsException ignored) {
            }

        }
        return stocks;
    }

    /**
     * Getting the price from the node
     * @param doc
     * @param index
     * @return
     */
    private List<Node> getStockPrice(Document doc, int index) {
        List<Node> nodes = doc.select("body").select("div").get(0).childNodes().get(3).childNodes().get(1).childNodes().get(3).childNodes();

        return nodes.stream()
                .filter(node -> node.childNodeSize() > 0)
                .collect(Collectors.toList())
                .get(index).childNodes().stream()
                .filter(n -> n.childNodeSize() > 0)
                .collect(Collectors.toList());
    }

    /**
     * reformat the comma to a dot and parse the string to a double
     *
     * @param number
     * @return
     */
    private Double toDouble(String number) {
        String normalized = number.replaceAll(",", ".");
        return Double.parseDouble(normalized);
    }
}
