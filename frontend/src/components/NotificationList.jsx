export default function NotificationList({ notifications, onMarkRead }) {
  const unread = notifications.filter((n) => !n.read).length;

  return (
    <section className="card">
      <div className="card-head">
        <h2>Notifications {unread > 0 && <span className="badge badge-alert">{unread} new</span>}</h2>
        {unread > 0 && (
          <button className="btn btn-ghost" onClick={onMarkRead}>Mark all as read</button>
        )}
      </div>

      {notifications.length === 0 && <p className="muted">No notifications yet.</p>}

      <ul className="notif-list">
        {notifications.slice(0, 8).map((n) => (
          <li key={n.id} className={n.read ? "notif" : "notif notif-new"}>
            <p>{n.message}</p>
            <small>{new Date(n.createdAt).toLocaleString()}</small>
          </li>
        ))}
      </ul>
    </section>
  );
}
