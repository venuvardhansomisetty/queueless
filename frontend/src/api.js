// All calls to the backend are written here, in ONE place.
const BASE_URL = "http://localhost:8080/api";

function getToken() {
  const saved = localStorage.getItem("queueless_user");
  return saved ? JSON.parse(saved).token : null;
}

async function request(method, path, body) {
  const headers = { "Content-Type": "application/json" };

  // If the user is logged in, send the JWT with every request
  const token = getToken();
  if (token) {
    headers["Authorization"] = "Bearer " + token;
  }

  const response = await fetch(BASE_URL + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // 401 = token missing or expired -> go to the login page
  if (response.status === 401) {
    localStorage.removeItem("queueless_user");
    window.location.href = "/login";
    throw new Error("Please login again");
  }

  // read the answer (it can also be empty)
  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch (e) {
    data = null;
  }

  if (!response.ok) {
    // our backend sends errors like {"message": "You already have an active token"}
    throw new Error((data && data.message) || "Something went wrong");
  }
  return data;
}

export const api = {
  get: (path) => request("GET", path),
  post: (path, body) => request("POST", path, body),
  put: (path, body) => request("PUT", path, body),
};
