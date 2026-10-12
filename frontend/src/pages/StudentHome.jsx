import { useState } from "react";
import { api } from "../api";
import usePolling from "../usePolling";
import Navbar from "../components/Navbar";
import QueueCard from "../components/QueueCard";
import NotificationList from "../components/NotificationList";

export default function StudentHome() {
  const [services, setServices] = useState([]);
  const [tokens, setTokens] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [message, setMessage] = useState("");

  // Load everything from the backend in one go
  async function loadAll() {
    try {
      const [s, t, n] = await Promise.all([
        api.get("/services"),
        api.get("/queue/my"),
        api.get("/notifications"),
      ]);
      setServices(s);
      setTokens(t);
      setNotifications(n);
    } catch (err) {
      setMessage(err.message);
    }
  }

  // Ask the backend again every 5 seconds (this is the "real-time" update)
  usePolling(loadAll, 5000);

  async function joinQueue(serviceId) {
    try {
      await api.post("/queue/join/" + serviceId);
      setMessage("You joined the queue. Your token is below.");
      loadAll();
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function cancelToken(entryId) {
    try {
      await api.put("/queue/cancel/" + entryId);
      setMessage("Token cancelled.");
      loadAll();
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function markRead() {
    try {
      await api.put("/notifications/read-all");
      loadAll();
    } catch (err) {
      setMessage(err.message);
    }
  }

  const activeTokens = tokens.filter((t) => t.status === "WAITING" || t.status === "CALLED");
  const history = tokens.filter((t) => t.status !== "WAITING" && t.status !== "CALLED");
  const activeServiceIds = activeTokens.map((t) => t.serviceId);

  return (
    <>
      <Navbar />
      <main className="container">
        {message && (
          <div className="banner" onClick={() => setMessage("")}>{message}</div>
        )}

        <h2 className="section-title">My tokens</h2>
        {activeTokens.length === 0 && (
          <p className="muted">You are not in any queue. Pick a service below to get a token.</p>
        )}
        <div className="ticket-list">
          {activeTokens.map((t) => (
            <QueueCard key={t.id} token={t} onCancel={cancelToken} />
          ))}
        </div>

        <h2 className="section-title">Services</h2>
        {services.length === 0 && <p className="muted">No services yet. Ask the admin to add some.</p>}
        <div className="grid">
          {services.map((s) => (
            <section className="card" key={s.id}>
              <h3>{s.name}</h3>
              <p className="muted">{s.description || "No description"}</p>
              <p>{s.waitingCount} waiting. About {s.avgServiceMinutes} min per person.</p>
              <button
                className="btn btn-primary"
                disabled={activeServiceIds.includes(s.id)}
                onClick={() => joinQueue(s.id)}
              >
                {activeServiceIds.includes(s.id) ? "Already in queue" : "Join queue"}
              </button>
            </section>
          ))}
        </div>

        <NotificationList notifications={notifications} onMarkRead={markRead} />

        {history.length > 0 && (
          <section className="card">
            <h2>History</h2>
            <table>
              <thead>
                <tr><th>Token</th><th>Service</th><th>Status</th><th>Joined</th></tr>
              </thead>
              <tbody>
                {history.slice(0, 10).map((t) => (
                  <tr key={t.id}>
                    <td>{t.tokenNumber}</td>
                    <td>{t.serviceName}</td>
                    <td><span className={"badge badge-" + t.status.toLowerCase()}>{t.status.toLowerCase()}</span></td>
                    <td>{new Date(t.joinedAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        )}
      </main>
    </>
  );
}
