import React, { useEffect } from 'react';
import 'react-toastify/dist/ReactToastify.min.css';
import { toast } from 'react-toastify';

/**
 * Configuring the toast library for visual notifications
 */
toast.configure();

function Notification(props) {
  /**
   * Getting the reference of the window speech synthesis and assigning it
   */
  const synthRef = React.useRef(window.speechSynthesis);

  /**
   * Use Effect - React hook that is called when component is rendered
   * Set interval - This function is called every 30 seconds
   * If user has not been notified about the stock, notify the user using a visual and audible notification
   */
  useEffect(() => {
    setInterval(() => {
      props.stocksNotified.map((stock) => {
        toast("Buy Signal: " + stock.name);
        notify("Buy Signal: " + stock.name)
      }
      );
    }, 30_000);
  }, []
  );

  /**
   * Utters the notification text it recieves.
   * @param {*} signalText 
   */
  const notify = (signalText) => {
    const utter = new SpeechSynthesisUtterance(signalText)
    synthRef.current.speak(utter);
  }
  return (
    <div>
    </div>
  );
}

export default Notification;