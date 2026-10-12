import { createContext, useContext, useState } from "react";

// Context = a box of data that any page can read without passing props again and again.
// Here the box holds the logged-in user (name, role, token).
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // start with the user saved in the browser (so refresh does not log you out)
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem("queueless_user");
    return saved ? JSON.parse(saved) : null;
  });

  function login(data) {
    localStorage.setItem("queueless_user", JSON.stringify(data));
    setUser(data);
  }

  function logout() {
    localStorage.removeItem("queueless_user");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}

// Which page should each role see after login?
export function homeFor(role) {
  if (role === "ADMIN") return "/admin";
  if (role === "STAFF") return "/staff";
  return "/student";
}
