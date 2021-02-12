import React, { Component } from 'react';
import SockJsClient from 'react-stomp';
import './css/StockFetchDisplay.css';
import Notification from './Notification';

class StockFetchDisplay extends Component {
  constructor(props) {
    super(props);
    this.state = {
      upTrend: [],
      downTrend: [],
      stocksNotified: []
    }
  }

  /**
   * Open Stock page in Avanza When clicking on the stock
   */
  onClickOpenStockInAvanza = (stock) => {
    window.open("//" + stock.website, '_blank');
  }

  /**
   * Display all the stocks that I've been notified about
   */
  displayNotifiedStocks = () => {
    return (
      <div className="ml-6 col-sm-3">
        <h1>Stocks Notified</h1>
        <table className="table table-striped table-responsive ">
          <thead>
            <tr>
              <th scope="col">Time</th>
              <th scope="col">Name</th>
              <th scope="col">Entry</th>
            </tr>
          </thead>
          <tbody>
            {this.state.stocksNotified.map((stock, idx) => {
              return (
                <tr key={idx} onClick={() => this.onClickOpenStockInAvanza(stock)}>
                  <td>{stock.timeOfBuySignal}</td>
                  <td>{stock.name}</td>
                  <td>{stock.entry}</td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    );
  }

  /**
   * Display all the stocks that are in a downward trend
   */
  displayDownTrendStocks = () => {
    return (
      <div className="ml-6 col-sm-3">
        <h1>Down Trend</h1>
        <table className="table table-striped table-responsive ">
          <thead>
            <tr>
              <th scope="col">Name</th>
              <th scope="col">Senast</th>
              <th scope="col">Entry</th>
            </tr>
          </thead>
          <tbody>
            {this.state.downTrend.map((stock, idx) => {
              return (
                <tr key={idx} onClick={() => this.onClickOpenStockInAvanza(stock)}>
                  <td>{stock.name}</td>
                  <td>{stock.lastPrice}</td>
                  <td>{stock.entry}</td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    )
  }

  /**
   * Display all the stocks that are in a upward trend
   */
  displayUpTrendStocks = () => {
    return (
      <div className="ml-6 col-sm-3">
        <h1>Up Trend</h1>
        <table className="table table-striped table-responsive ">
          <thead>
            <tr>
              <th scope="col">Name</th>
              <th scope="col">Senast</th>
              <th scope="col">Entry</th>
            </tr>
          </thead>
          <tbody>
            {this.state.upTrend.map((stock, idx) => {
              return (
                <tr key={idx} onClick={() => this.onClickOpenStockInAvanza(stock)}>
                  <td>{stock.name}</td>
                  <td>{stock.lastPrice}</td>
                  <td>{stock.entry}</td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    )
  }

  render() {
    return (
      /**
       * Display the stocks in a row
       * Notification - Send all stocks that are in notified list to the Notification component
       * SockJSClient - Accept the data sent from the backend and assign to the lists within the state of the component
       */
      <div className="row">
        {this.displayUpTrendStocks()}
        {this.displayDownTrendStocks()}
        {this.displayNotifiedStocks()}
        <Notification stocksNotified={this.state.stocksNotified} />

        <SockJsClient url='http://hitechdynasty.se:8085/stock-list/'
          topics={['/topic/stock']}
          onConnect={() => {
            console.log("connected");
          }}
          onDisconnect={() => {
            console.log("Disconnected");
          }}
          onMessage={(resp) => {
            this.setState({
              upTrend: resp.upTrend,
              downTrend: resp.downTrend,
              stocksNotified: resp.stocksNotified
            })
          }}
          ref={(client) => {
            this.clientRef = client
          }}
        />
      </div>
    )
  }
}

export default StockFetchDisplay;