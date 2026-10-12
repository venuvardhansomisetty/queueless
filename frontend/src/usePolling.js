import { useEffect, useRef } from "react";

// "Polling" = asking the backend again and again every few seconds.
// This is how the queue position updates without refreshing the page.
export default function usePolling(callback, delayMs) {
  // keep the newest version of the function, so we never use old data
  const latest = useRef(callback);
  useEffect(() => {
    latest.current = callback;
  });

  useEffect(() => {
    latest.current(); // run once immediately
    const id = setInterval(() => latest.current(), delayMs);
    return () => clearInterval(id); // stop when the page is closed
  }, [delayMs]);
}
