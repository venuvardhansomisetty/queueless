// One active token, drawn like a paper ticket.
export default function QueueCard({ token, onCancel }) {
  const called = token.status === "CALLED";

  return (
    <div className={"ticket" + (called ? " ticket-called" : "")}>
      <div className="ticket-number">
        <span className="ticket-label">Token</span>
        <span className="ticket-value">{token.tokenNumber}</span>
      </div>

      <div className="ticket-info">
        <h3>{token.serviceName}</h3>

        {called ? (
          <p className="called-text">It is your turn. Please go to the counter now.</p>
        ) : (
          <>
            <p>Your position: <strong>{token.position}</strong></p>
            <p>
              Estimated wait:{" "}
              <strong>
                {token.estimatedWaitMinutes === 0
                  ? "you are next"
                  : "about " + token.estimatedWaitMinutes + " min"}
              </strong>
            </p>
            <button className="btn btn-danger" onClick={() => onCancel(token.id)}>
              Cancel token
            </button>
          </>
        )}
      </div>
    </div>
  );
}
