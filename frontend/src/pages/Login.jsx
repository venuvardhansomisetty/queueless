import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAuth, homeFor } from "../AuthContext";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault(); // stop the page from reloading
    setError("");
    try {
      const data = await api.post("/auth/login", { email, password });
      login(data); // save token + user
      navigate(homeFor(data.role));
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>QueueLess</h1>
        <p className="muted">Book your place in line. Skip the waiting room.</p>

        {error && <div className="banner banner-error">{error}</div>}

        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

        <button className="btn btn-primary" type="submit">Log in</button>
        <p className="muted">New student? <Link to="/register">Create an account</Link></p>
      </form>
    </div>
  );
}
