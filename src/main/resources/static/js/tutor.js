const user = Auth.requireRole("TUTOR");
if (user) {
  document.getElementById("userName").textContent = `${user.name} (Tutor)`;
}

let myPets = [];

// ---------- Tabs ----------
document.querySelectorAll(".tab-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach(p => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById("tab-" + btn.dataset.tab).classList.add("active");
  });
});

const statusBadge = (status, map) => {
  const cls = map[status] || "gray";
  return `<span class="badge ${cls}">${status}</span>`;
};

// ---------- Pets ----------
async function loadPets() {
  const data = await Api.get(`/pets?tutorId=${user.id}&size=100`);
  myPets = data.content || [];

  const tbody = document.getElementById("petsTable");
  tbody.innerHTML = myPets.map(p => `
    <tr>
      <td>${p.name}</td>
      <td>${p.breed || "-"}</td>
      <td>${p.weight ? p.weight + " kg" : "-"}</td>
      <td>${fmtDate(p.birthDate)}</td>
    </tr>`).join("");
  document.getElementById("petsEmpty").style.display = myPets.length ? "none" : "block";

  const petSelects = [document.getElementById("eventPet"), document.getElementById("subPet")];
  petSelects.forEach(sel => {
    sel.innerHTML = myPets.map(p => `<option value="${p.id}">${p.name}</option>`).join("")
      || `<option value="">Cadastre um pet primeiro</option>`;
  });
}

document.getElementById("petForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  hideMessage("petMsg");
  try {
    await Api.post("/pets", {
      name: document.getElementById("petName").value.trim(),
      breed: document.getElementById("petBreed").value.trim() || null,
      birthDate: document.getElementById("petBirth").value || null,
      weight: document.getElementById("petWeight").value || null,
      tutorId: user.id,
      speciesId: Number(document.getElementById("petSpecies").value)
    });
    showMessage("petMsg", "Pet cadastrado com sucesso!", "success");
    e.target.reset();
    await loadPets();
  } catch (err) {
    showMessage("petMsg", err.message);
  }
});

// ---------- Eventos de saúde ----------
async function loadEvents() {
  if (!myPets.length) {
    document.getElementById("eventsEmpty").style.display = "block";
    return;
  }
  const results = await Promise.all(myPets.map(p => Api.get(`/health-events?petId=${p.id}&size=50`)));
  const events = results.flatMap(r => r.content || []);

  const map = { REALIZADO: "green", AGENDADO: "yellow", CANCELADO: "red" };
  const tbody = document.getElementById("eventsTable");
  tbody.innerHTML = events.map(ev => `
    <tr>
      <td>${ev.petName}</td>
      <td>${ev.description || "-"}</td>
      <td>${fmtDate(ev.eventDate)}</td>
      <td>${statusBadge(ev.status, map)}</td>
    </tr>`).join("");
  document.getElementById("eventsEmpty").style.display = events.length ? "none" : "block";
}

document.getElementById("eventForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  hideMessage("eventMsg");
  try {
    await Api.post("/health-events", {
      description: document.getElementById("eventDescription").value.trim() || null,
      eventDate: document.getElementById("eventDate").value,
      status: document.getElementById("eventStatus").value,
      petId: Number(document.getElementById("eventPet").value),
      eventTypeId: Number(document.getElementById("eventType").value)
    });
    showMessage("eventMsg", "Evento registrado com sucesso!", "success");
    e.target.reset();
    await loadEvents();
  } catch (err) {
    showMessage("eventMsg", err.message);
  }
});

// ---------- Assinaturas ----------
async function loadPlansIntoSelect() {
  const data = await Api.get("/plans?size=100");
  const sel = document.getElementById("subPlan");
  sel.innerHTML = (data.content || [])
    .map(p => `<option value="${p.id}">${p.name} — R$ ${Number(p.price).toFixed(2)}</option>`)
    .join("");
}

async function loadSubscriptions() {
  if (!myPets.length) {
    document.getElementById("subsEmpty").style.display = "block";
    return;
  }
  const results = await Promise.all(myPets.map(p => Api.get(`/subscriptions?petId=${p.id}&size=50`)));
  const subs = results.flatMap(r => r.content || []);

  const map = { ATIVO: "green", ENCERRADO: "gray", CANCELADO: "red", EXPIRADO: "yellow" };
  const tbody = document.getElementById("subsTable");
  tbody.innerHTML = subs.map(s => `
    <tr>
      <td>${s.petName}</td>
      <td>${s.planName}</td>
      <td>${fmtDate(s.startDate)}</td>
      <td>${fmtDate(s.endDate)}</td>
      <td>${statusBadge(s.status, map)}</td>
      <td>${s.status === "ATIVO" ? `<button class="small" onclick="cancelSub(${s.id})">Cancelar</button>` : ""}</td>
    </tr>`).join("");
  document.getElementById("subsEmpty").style.display = subs.length ? "none" : "block";
}

async function cancelSub(id) {
  await Api.request(`/subscriptions/${id}/status?status=CANCELADO`, { method: "PUT" });
  await loadSubscriptions();
}

document.getElementById("subForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  hideMessage("subMsg");
  try {
    await Api.post("/subscriptions", {
      petId: Number(document.getElementById("subPet").value),
      planId: Number(document.getElementById("subPlan").value),
      startDate: document.getElementById("subStart").value
    });
    showMessage("subMsg", "Assinatura criada com sucesso!", "success");
    e.target.reset();
    await loadSubscriptions();
  } catch (err) {
    showMessage("subMsg", err.message);
  }
});

// ---------- Cupons & resgate ----------
let selectedCoupon = null;

async function loadCoupons() {
  const data = await Api.get("/coupons?status=DISPONIVEL&size=50");
  const coupons = data.content || [];
  const tbody = document.getElementById("couponsTable");
  tbody.innerHTML = coupons.map(c => `
    <tr>
      <td>${c.code}</td>
      <td>${fmtDate(c.expirationDate)}</td>
      <td>${statusBadge(c.status, { DISPONIVEL: "green" })}</td>
      <td><button class="small" onclick='openRedeem(${JSON.stringify(c).replace(/'/g, "&apos;")})'>Resgatar</button></td>
    </tr>`).join("");
  document.getElementById("couponsEmpty").style.display = coupons.length ? "none" : "block";
}

async function loadRedeems() {
  const data = await Api.get(`/redeems?tutorId=${user.id}&size=50`);
  const redeems = data.content || [];
  const tbody = document.getElementById("redeemsTable");
  tbody.innerHTML = redeems.map(r => `
    <tr><td>${r.couponCode}</td><td>${r.pointsUsed}</td><td>${fmtDate(r.createdAt)}</td></tr>
  `).join("");
  document.getElementById("redeemsEmpty").style.display = redeems.length ? "none" : "block";
}

function openRedeem(coupon) {
  selectedCoupon = coupon;
  document.getElementById("redeemCouponCode").textContent = `Cupom: ${coupon.code}`;
  document.getElementById("redeemPoints").value = "";
  hideMessage("redeemMsg");
  document.getElementById("redeemDialog").showModal();
}

document.getElementById("confirmRedeem").addEventListener("click", async () => {
  hideMessage("redeemMsg");
  const points = Number(document.getElementById("redeemPoints").value);
  if (!points || points <= 0) {
    showMessage("redeemMsg", "Informe uma quantidade de pontos válida.");
    return;
  }
  try {
    await Api.post("/redeems", {
      pointsUsed: points,
      tutorId: user.id,
      couponId: selectedCoupon.id
    });
    document.getElementById("redeemDialog").close();
    await Promise.all([loadCoupons(), loadRedeems()]);
  } catch (err) {
    showMessage("redeemMsg", err.message);
  }
});

// ---------- Bootstrap ----------
(async function init() {
  try {
    await loadPets();
    await Promise.all([loadEvents(), loadPlansIntoSelect(), loadSubscriptions(), loadCoupons(), loadRedeems()]);
  } catch (err) {
    console.error(err);
  }
})();
