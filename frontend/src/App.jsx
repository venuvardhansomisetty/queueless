import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth, homeFor } from "./AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Register from "./pages/Register";
import StudentHome from "./pages/StudentHome";
import StaffHome from "./pages/StaffHome";
import AdminHome from "./pages/AdminHome";

// "/" sends the user to the right page for their role
function Home() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={homeFor(user.role)} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route path="/student" element={<ProtectedRoute role="STUDENT"><StudentHome /></ProtectedRoute>} />
      <Route path="/staff" element={<ProtectedRoute role="STAFF"><StaffHome /></ProtectedRoute>} />
      <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminHome /></ProtectedRoute>} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
