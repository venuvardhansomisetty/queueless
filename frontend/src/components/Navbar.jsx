import { useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header className="navbar">
      <span className="brand">QueueLess</span>
      <div className="navbar-right">
        <span className="who">{user.name} ({user.role.toLowerCase()})</span>
        <button className="btn btn-ghost" onClick={handleLogout}>Log out</button>
      </div>
    </header>
  );
}
