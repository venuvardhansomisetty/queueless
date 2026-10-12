import { useEffect, useState } from "react";
import { api } from "../api";
import usePolling from "../usePolling";
import Navbar from "../components/Navbar";

export default function AdminHome() {
  const [services, setServices] = useState([]);
  const [staff, setStaff] = useState([]);
  const [message, setMessage] = useState("");

  // forms
  const [serviceForm, setServiceForm] = useState({ name: "", description: "", avgServiceMinutes: 5 });
  const [staffForm, setStaffForm] = useState({ name: "", email: "", password: "" });

  // which staff member is chosen in each service's dropdown  { serviceId: staffId }
  const [chosenStaff, setChosenStaff] = useState({});

  // the queue the admin is watching right now
  const [watching, setWatching] = useState(null); // { id, name }
  const [watchEntries, setWatchEntries] = useState([]);

  // work history of all staff, and the staff filter ("" = all staff)
  const [history, setHistory] = useState([]);
  const [historyStaff, setHistoryStaff] = useState("");

  async function loadServices() {
    try {
      setServices(await api.get("/admin/services"));
      setHistory(await api.get("/admin/history"));
      if (watching) {
        setWatchEntries(await api.get("/admin/queue/" + watching.id));
      }
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function loadStaff() {
    try {
      setStaff(await api.get("/admin/staff"));
    } catch (err) {
      setMessage(err.message);
    }
  }

  useEffect(() => {
    loadStaff();
  }, []);

  // services and the watched queue refresh every 5 seconds
  usePolling(loadServices, 5000);

  async function createService(e) {
    e.preventDefault();
    try {
      await api.post("/admin/services", {
        name: serviceForm.name,
        description: serviceForm.description,
        avgServiceMinutes: Number(serviceForm.avgServiceMinutes),
      });
      setServiceForm({ name: "", description: "", avgServiceMinutes: 5 });
      setMessage("Service created.");
      loadServices();
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function createStaff(e) {
    e.preventDefault();
    try {
      await api.post("/admin/staff", staffForm);
      setStaffForm({ name: "", email: "", password: "" });
      setMessage("Staff account created.");
      loadStaff();
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function assignStaff(service) {
    const staffId = chosenStaff[service.id] ?? service.staffId;
    if (!staffId) {
      setMessage("Choose a staff member first.");
      return;
    }
    try {
      await api.put("/admin/services/" + service.id + "/assign-staff", { staffId: Number(staffId) });
      setMessage("Staff assigned to " + service.name + ".");
      loadServices();
    } catch (err) {
      setMessage(err.message);
    }
  }

  async function watchQueue(service) {
    try {
      setWatching({ id: service.id, name: service.name });
      setWatchEntries(await api.get("/admin/queue/" + service.id));
    } catch (err) {
      setMessage(err.message);
    }
  }

  // ----- numbers for the history section -----
  const minutes = (value) => (value === null || value === undefined ? "-" : value + " min");

  // one summary row per staff member: completed, skipped, average serving time
  const summary = {};
  history.forEach((h) => {
    if (!h.staffId) return; // old records without a staff name
    if (!summary[h.staffId]) {
      summary[h.staffId] = { name: h.staffName, completed: 0, skipped: 0, totalMinutes: 0, counted: 0 };
    }
    const row = summary[h.staffId];
    if (h.outcome === "COMPLETED") row.completed += 1;
    if (h.outcome === "SKIPPED") row.skipped += 1;
    if (h.serviceMinutes !== null && h.serviceMinutes !== undefined) {
      row.totalMinutes += h.serviceMinutes;
      row.counted += 1;
    }
  });
  const summaryRows = Object.values(summary);

  const shownHistory = historyStaff ? history.filter((h) => String(h.staffId) === historyStaff) : history;

  return (
    <>
      <Navbar />
      <main className="container">
        {message && <div className="banner" onClick={() => setMessage("")}>{message}</div>}

        <div className="grid">
          <form className="card" onSubmit={createService}>
            <h2>Add a service</h2>
            <label>Name</label>
            <input value={serviceForm.name} onChange={(e) => setServiceForm({ ...serviceForm, name: e.target.value })} required />
            <label>Description</label>
            <input value={serviceForm.description} onChange={(e) => setServiceForm({ ...serviceForm, description: e.target.value })} />
            <label>Average minutes per person</label>
            <input type="number" min="1" value={serviceForm.avgServiceMinutes} onChange={(e) => setServiceForm({ ...serviceForm, avgServiceMinutes: e.target.value })} required />
            <button className="btn btn-primary" type="submit">Create service</button>
          </form>

          <form className="card" onSubmit={createStaff}>
            <h2>Add a staff member</h2>
            <label>Name</label>
            <input value={staffForm.name} onChange={(e) => setStaffForm({ ...staffForm, name: e.target.value })} required />
            <label>Email</label>
            <input type="email" value={staffForm.email} onChange={(e) => setStaffForm({ ...staffForm, email: e.target.value })} required />
            <label>Password (at least 6 characters)</label>
            <input type="password" value={staffForm.password} onChange={(e) => setStaffForm({ ...staffForm, password: e.target.value })} required />
            <button className="btn btn-primary" type="submit">Create staff</button>
          </form>
        </div>

        <section className="card">
          <h2>Services</h2>
          {services.length === 0 && <p className="muted">No services yet. Add the first one above.</p>}
          {services.length > 0 && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Service</th><th>Avg min</th><th>Waiting</th><th>Assigned staff</th><th></th></tr>
                </thead>
                <tbody>
                  {services.map((s) => (
                    <tr key={s.id}>
                      <td>{s.name}</td>
                      <td>{s.avgServiceMinutes}</td>
                      <td>{s.waitingCount}</td>
                      <td>
                        <select
                          value={chosenStaff[s.id] ?? s.staffId ?? ""}
                          onChange={(e) => setChosenStaff({ ...chosenStaff, [s.id]: e.target.value })}
                        >
                          <option value="">Not assigned</option>
                          {staff.map((st) => (
                            <option key={st.id} value={st.id}>{st.name}</option>
                          ))}
                        </select>
                      </td>
                      <td className="row">
                        <button className="btn btn-ghost" onClick={() => assignStaff(s)}>Assign</button>
                        <button className="btn btn-ghost" onClick={() => watchQueue(s)}>View queue</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {watching && (
          <section className="card">
            <div className="card-head">
              <h2>Live queue: {watching.name}</h2>
              <button className="btn btn-ghost" onClick={() => setWatching(null)}>Close</button>
            </div>
            {watchEntries.length === 0 && <p className="muted">No one is in this queue.</p>}
            {watchEntries.length > 0 && (
              <table>
                <thead>
                  <tr><th>Token</th><th>Name</th><th>Status</th><th>Position</th><th>Est. wait</th></tr>
                </thead>
                <tbody>
                  {watchEntries.map((q) => (
                    <tr key={q.id}>
                      <td>{q.tokenNumber}</td>
                      <td>{q.userName}</td>
                      <td><span className={"badge badge-" + q.status.toLowerCase()}>{q.status.toLowerCase()}</span></td>
                      <td>{q.position || "-"}</td>
                      <td>{q.status === "WAITING" ? q.estimatedWaitMinutes + " min" : "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        )}

        <section className="card">
          <h2>Staff performance</h2>
          {summaryRows.length === 0 && <p className="muted">No staff work recorded yet.</p>}
          {summaryRows.length > 0 && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Staff</th><th>Completed</th><th>Skipped</th><th>Average serving time</th></tr>
                </thead>
                <tbody>
                  {summaryRows.map((r) => (
                    <tr key={r.name}>
                      <td>{r.name}</td>
                      <td>{r.completed}</td>
                      <td>{r.skipped}</td>
                      <td>{r.counted > 0 ? Math.round(r.totalMinutes / r.counted) + " min" : "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="card">
          <div className="card-head">
            <h2>Work history of all staff</h2>
            <select value={historyStaff} onChange={(e) => setHistoryStaff(e.target.value)}>
              <option value="">All staff</option>
              {staff.map((st) => (
                <option key={st.id} value={st.id}>{st.name}</option>
              ))}
            </select>
          </div>
          <p className="muted">Shows the latest 200 records. Staff clearing their own history does not remove anything here.</p>

          {shownHistory.length === 0 && <p className="muted">No records to show.</p>}
          {shownHistory.length > 0 && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Staff</th><th>Token</th><th>Student</th><th>Service</th><th>Outcome</th>
                    <th>Waited</th><th>Served in</th><th>Finished</th>
                  </tr>
                </thead>
                <tbody>
                  {shownHistory.map((h) => (
                    <tr key={h.id}>
                      <td>{h.staffName || "-"}</td>
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

        <section className="card">
          <h2>Staff members</h2>
          {staff.length === 0 && <p className="muted">No staff yet.</p>}
          <ul className="plain-list">
            {staff.map((st) => (
              <li key={st.id}>{st.name} <span className="muted">({st.email})</span></li>
            ))}
          </ul>
        </section>
      </main>
    </>
  );
}
