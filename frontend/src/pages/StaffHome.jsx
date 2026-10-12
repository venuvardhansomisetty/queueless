import { useEffect, useState } from "react";
import { api } from "../api";
import usePolling from "../usePolling";
import Navbar from "../components/Navbar";

export default function StaffHome() {
  const [services, setServices] = useState([]);
  const [selectedId, setSelectedId] = useState("");
  const [queue, setQueue] = useState([]);
  const [history, setHistory] = useState([]);
  const [message, setMessage] = useState("");

  // 1. When the page opens, load the services assigned to this staff member
  useEffect(() => {
    async function loadServices() {
      try {
        const data = await api.get("/staff/services");
        setServices(data);
        if (data.length > 0) setSelectedId(String(data[0].id));
      } catch (err) {
        setMessage(err.message);
      }
    }
    loadServices();
  }, []);

  // 2. Load the queue of the selected service
  async function loadQueue() {
    if (!selectedId) return;
    try {
      setQueue(await api.get("/staff/services/" + selectedId + "/queue"));
    } catch (err) {
      setMessage(err.message);
    }
  }

  // load again when the selected service changes
  useEffect(() => {
    setQueue([]);
    loadQueue();
  }, [selectedId]);

  // my finished work (completed and skipped people)
  async function loadHistory() {
    try {
      setHistory(await api.get("/staff/history"));
    } catch (err) {
      setMessage(err.message);
    }
  }

  // load the queue and the history together
  async function refresh() {
    await loadQueue();
    await loadHistory();
  }

  // and every 5 seconds
  usePolling(refresh, 5000);

  // "Clear history" hides the rows from MY page. The admin still keeps the full record.
  async function clearHistory() {
    if (!window.confirm("Clear your history? The admin will still keep the records.")) return;
    try {
      const result = await api.put("/staff/history/clear");
      setMessage(result.message);
    } catch (err) {
      setMessage(err.message);
    }
    refresh();
  }

  // runs one button action, shows the result message, then refreshes the queue
  async function doAction(action, successText) {
    try {
      await action();
      setMessage(successText);
    } catch (err) {
      setMessage(err.message);
    }
    refresh();
  }

  const minutes = (value) => (value === null || value === undefined ? "-" : value + " min");
  const today = new Date().toDateString();
  const doneToday = history.filter(
    (h) => h.outcome === "COMPLETED" && h.finishedAt && new Date(h.finishedAt).toDateString() === today
  ).length;

  const serving = queue.find((q) => q.status === "CALLED");
  const waiting = queue.filter((q) => q.status === "WAITING");

  return (
    <>
      <Navbar />
      <main className="container">
        {message && <div className="banner" onClick={() => setMessage("")}>{message}</div>}

        {services.length === 0 ? (
          <p className="muted">No service is assigned to you yet. Please ask the admin.</p>
        ) : (
          <>
            <div className="toolbar">
              <label>Service</label>
              <select value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
                {services.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
              <button
                className="btn btn-primary"
                onClick={() => doAction(() => api.put("/staff/services/" + selectedId + "/call-next"), "Next person called.")}
              >
                Call next
              </button>
            </div>

            <h2 className="section-title">Now serving</h2>
            {serving ? (
              <div className="ticket ticket-called">
                <div className="ticket-number">
                  <span className="ticket-label">Token</span>
                  <span className="ticket-value">{serving.tokenNumber}</span>
                </div>
                <div className="ticket-info">
                  <h3>{serving.userName}</h3>
                  <div className="row">
                    <button
                      className="btn btn-primary"
                      onClick={() => doAction(() => api.put("/staff/entries/" + serving.id + "/complete"), "Marked as completed.")}
                    >
                      Mark completed
                    </button>
                    <button
                      className="btn btn-danger"
                      onClick={() => doAction(() => api.put("/staff/entries/" + serving.id + "/skip"), "Person skipped.")}
                    >
                      Skip (did not come)
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <p className="muted">No one is being served right now.</p>
            )}

            <section className="card">
              <h2>Waiting ({waiting.length})</h2>
              {waiting.length === 0 && <p className="muted">The queue is empty.</p>}
              {waiting.length > 0 && (
                <table>
                  <thead>
                    <tr><th>Token</th><th>Name</th><th>Position</th><th>Joined</th><th></th></tr>
                  </thead>
                  <tbody>
                    {waiting.map((q) => (
                      <tr key={q.id}>
                        <td>{q.tokenNumber}</td>
                        <td>{q.userName}</td>
                        <td>{q.position}</td>
                        <td>{new Date(q.joinedAt).toLocaleTimeString()}</td>
                        <td>
                          <button
                            className="btn btn-ghost"
                            onClick={() => doAction(() => api.put("/staff/entries/" + q.id + "/skip"), "Person skipped.")}
                          >
                            Skip
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          </>
        )}

        <section className="card">
          <div className="card-head">
            <h2>My work history</h2>
            {history.length > 0 && (
              <button className="btn btn-ghost" onClick={clearHistory}>Clear history</button>
            )}
          </div>
          <p className="muted">People served today: <strong>{doneToday}</strong></p>

          {history.length === 0 && <p className="muted">No finished work to show yet.</p>}
          {history.length > 0 && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Token</th><th>Student</th><th>Service</th><th>Outcome</th>
                    <th>Waited</th><th>Served in</th><th>Finished</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((h) => (
                    <tr key={h.id}>
                      <td>{h.tokenNumber}</td>
                      <td>{h.studentName}</td>
                      <td>{h.serviceName}</td>
                      <td><span className={"badge badge-" + h.outcome.toLowerCase()}>{h.outcome.toLowerCase()}</span></td>
                      <td>{minutes(h.waitedMinutes)}</td>
                      <td>{minutes(h.serviceMinutes)}</td>
                      <td>{h.finishedAt ? new Date(h.finishedAt).toLocaleString() : "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </>
  );
}
