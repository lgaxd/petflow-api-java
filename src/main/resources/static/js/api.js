// Camada fininha sobre fetch(): injeta o header Authorization, trata JSON
// e padroniza o tratamento de erro usando o ErrorResponse da API.

const Auth = {
  getToken() { return localStorage.getItem("petflow_token"); },
  getUser() {
    const raw = localStorage.getItem("petflow_user");
    return raw ? JSON.parse(raw) : null;
  },
  setSession(token, user) {
    localStorage.setItem("petflow_token", token);
    localStorage.setItem("petflow_user", JSON.stringify(user));
  },
  logout() {
    localStorage.removeItem("petflow_token");
    localStorage.removeItem("petflow_user");
    window.location.href = "index.html";
  },
  isAdmin() {
    const user = this.getUser();
    return !!user && user.role === "ADMIN";
  },
  // Protege uma página: se não houver token, manda pro login;
  // se a página exigir um papel específico e o usuário não tiver, redireciona.
  requireRole(role) {
    const token = this.getToken();
    const user = this.getUser();
    if (!token || !user) {
      window.location.href = "index.html";
      return null;
    }
    if (role && user.role !== role) {
      window.location.href = user.role === "ADMIN" ? "admin.html" : "tutor.html";
      return null;
    }
    return user;
  }
};

const Api = {
  async request(path, { method = "GET", body = null, auth = true } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (auth) {
      const token = Auth.getToken();
      if (token) headers["Authorization"] = "Bearer " + token;
    }

    const response = await fetch(path, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined
    });

    if (response.status === 401 && auth) {
      Auth.logout();
      return;
    }

    const isJson = response.headers.get("content-type")?.includes("application/json");
    const data = isJson ? await response.json().catch(() => null) : null;

    if (!response.ok) {
      // Pega a mensagem do backend ou usa uma mensagem genérica
      const message = (data && (data.message || data.error)) || `Erro ${response.status}`;
      const error = new Error(message);
      error.details = data;
      error.status = response.status;
      throw error;
    }

    return data;
  },

  get(path) { return this.request(path); },
  post(path, body) { return this.request(path, { method: "POST", body }); },
  put(path, body) { return this.request(path, { method: "PUT", body }); },
  patch(path, body) { return this.request(path, { method: "PATCH", body }); },
  del(path) { return this.request(path, { method: "DELETE" }); }
};

function showMessage(elementId, text, type = "error") {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.textContent = text;
  el.className = "msg " + type;
  el.style.display = "block";
}

function hideMessage(elementId) {
  const el = document.getElementById(elementId);
  if (el) el.style.display = "none";
}

function fmtDate(value) {
  if (!value) return "-";
  return new Date(value).toLocaleDateString("pt-BR");
}
