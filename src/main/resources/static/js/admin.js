const user = Auth.requireRole("ADMIN");
if (user) {
  document.getElementById("userName").textContent = `${user.name} (Admin)`;
}

document.querySelectorAll(".tab-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach(p => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById("tab-" + btn.dataset.tab).classList.add("active");
  });
});

// ---------- Clínicas ----------
async function loadClinics() {
  const data = await Api.get("/clinics?size=100");
  const clinics = data.content || [];

  document.getElementById("clinicsTable").innerHTML = clinics.map(c => `
    <tr>
      <td>${c.id}</td><td>${c.name}</td><td>${c.cnpj}</td><td>${c.phone || "-"}</td>
      <td><button class="small" onclick="deleteClinic(${c.id})">Excluir</button></td>
    </tr>`).join("") || `<tr><td colspan="5" class="empty">Nenhuma clínica cadastrada.</td></tr>`;

  const sel = document.getElementById("planClinic");
  sel.innerHTML = clinics.map(c => `<option value="${c.id}">${c.name}</option>`).join("");
  return clinics;
}

async function deleteClinic(id) {
  if (!confirm("Remover esta clínica?")) return;
  try {
    await Api.del(`/clinics/${id}`);
    await loadClinics();
  } catch (err) {
    alert(err.message);
  }
}

document.getElementById("clinicForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  hideMessage("clinicMsg");
  try {
    await Api.post("/clinics", {
      name: document.getElementById("clinicName").value.trim(),
      cnpj: document.getElementById("clinicCnpj").value.trim(),
      address: document.getElementById("clinicAddress").value.trim() || null,
      phone: document.getElementById("clinicPhone").value.trim() || null
    });
    showMessage("clinicMsg", "Clínica cadastrada com sucesso!", "success");
    e.target.reset();
    await loadClinics();
  } catch (err) {
    showMessage("clinicMsg", err.message);
  }
});

// ---------- Planos ----------
async function loadPlans() {
  const data = await Api.get("/plans?size=100");
  const plans = data.content || [];
  document.getElementById("plansTable").innerHTML = plans.map(p => `
    <tr>
      <td>${p.name}</td><td>${p.clinicName}</td>
      <td>R$ ${Number(p.price).toFixed(2)}</td><td>${p.durationDays} dias</td>
      <td><button class="small" onclick="deletePlan(${p.id})">Excluir</button></td>
    </tr>`).join("") || `<tr><td colspan="5" class="empty">Nenhum plano cadastrado.</td></tr>`;
}

async function deletePlan(id) {
  if (!confirm("Remover este plano?")) return;
  try {
    await Api.del(`/plans/${id}`);
    await loadPlans();
  } catch (err) {
    alert(err.message);
  }
}

document.getElementById("planForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  hideMessage("planMsg");
  try {
    await Api.post("/plans", {
      name: document.getElementById("planName").value.trim(),
      description: document.getElementById("planDescription").value.trim() || null,
      price: Number(document.getElementById("planPrice").value),
      durationDays: Number(document.getElementById("planDuration").value),
      pointsPerEvent: Number(document.getElementById("planPoints").value),
      clinicId: Number(document.getElementById("planClinic").value)
    });
    showMessage("planMsg", "Plano cadastrado com sucesso!", "success");
    e.target.reset();
    await loadPlans();
  } catch (err) {
    showMessage("planMsg", err.message);
  }
});

// ---------- Cupons ----------
async function loadCoupons() {
  const data = await Api.get("/coupons?size=100");
  const coupons = data.content || [];
  const map = { DISPONIVEL: "green", RESGATADO: "yellow", UTILIZADO: "gray" };
  document.getElementById("couponsTable").innerHTML = coupons.map(c => `
    <tr>
      <td>${c.code}</td><td>${fmtDate(c.expirationDate)}</td>
      <td><span class="badge ${map[c.status] || "gray"}">${c.status}</span></td>
      <td><button class="small" onclick="deleteCoupon(${c.id})">Excluir</button></td>
    </tr>`).join("") || `<tr><td colspan="4" class="empty">Nenhum cupom cadastrado.</td></tr>`;
}

async function deleteCoupon(id) {
  if (!confirm("Remover este cupom?")) return;
  try {
    await Api.del(`/coupons/${id}`);
    await loadCoupons();
  } catch (err) {
    alert(err.message);
  }
}

document.getElementById("couponForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  hideMessage("couponMsg");
  try {
    await Api.post("/coupons", {
      code: document.getElementById("couponCode").value.trim(),
      status: document.getElementById("couponStatus").value,
      expirationDate: document.getElementById("couponExpiration").value,
      templateId: Number(document.getElementById("couponTemplate").value)
    });
    showMessage("couponMsg", "Cupom gerado com sucesso!", "success");
    e.target.reset();
    document.getElementById("couponTemplate").value = 1;
    await loadCoupons();
  } catch (err) {
    showMessage("couponMsg", err.message);
  }
});

// ---------- Tutores ----------
async function loadTutors() {
  const data = await Api.get("/tutors?size=100");
  const tutors = data.content || [];
  document.getElementById("tutorsTable").innerHTML = tutors.map(t => `
    <tr>
      <td>${t.id}</td><td>${t.name}</td><td>${t.email}</td>
      <td>${t.phone || "-"}</td><td>${fmtDate(t.createdAt)}</td>
    </tr>`).join("") || `<tr><td colspan="5" class="empty">Nenhum tutor cadastrado.</td></tr>`;
}

// ---------- Bootstrap ----------
(async function init() {
  try {
    await loadClinics();
    await Promise.all([loadPlans(), loadCoupons(), loadTutors()]);
  } catch (err) {
    console.error(err);
  }
})();
