import { Navigate } from "react-router-dom";
import { useAuth } from "../AuthContext";

// Wraps a page. Not logged in -> login page. Wrong role -> back to your own page.
export default function ProtectedRoute({ role, children }) {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== role) return <Navigate to="/" replace />;

  return children;
}
