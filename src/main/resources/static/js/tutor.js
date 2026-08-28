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
    // Recarregar dados de gamificação se for a aba
    if (btn.dataset.tab === "gamification") {
      loadGamificationData();
    }
    // Recarregar cupons se for a aba de cupons
    if (btn.dataset.tab === "coupons") {
      loadAvailableCoupons();
      loadMyRedeems();
    }
  });
});

const statusBadge = (status, map) => {
  const cls = map[status] || "gray";
  return `<span class="badge ${cls}">${status}</span>`;
};

// ---------- Gamification ----------
async function loadGamificationData() {
  await Promise.all([
    loadMyPoints(),
    loadPetRisks(),
    loadAvailableCoupons(),
    loadMyRedeems()
  ]);
}

async function loadMyPoints() {
  try {
    const data = await Api.get("/gamification/points");
    document.getElementById("totalPoints").textContent = data.totalPoints || 0;
    document.getElementById("tutorPointsName").textContent = data.tutorName;
    
    const tbody = document.getElementById("pointsHistoryTable");
    tbody.innerHTML = (data.history || []).map(h => `
      <tr>
        <td>${h.reason || '-'}</td>
        <td>${h.points > 0 ? '+' : ''}${h.points}</td>
        <td>${fmtDate(h.createdAt)}</td>
      </tr>
    `).join("") || `<tr><td colspan="3" class="empty">Nenhum registro de pontos.</td></tr>`;
  } catch (err) {
    console.error("Erro ao carregar pontos:", err);
  }
}

async function loadPetRisks() {
  if (!myPets.length) {
    document.getElementById("petRisksTable").innerHTML = `<tr><td colspan="4" class="empty">Cadastre um pet para ver o score de risco.</td></tr>`;
    return;
  }
  const riskData = await Promise.all(myPets.map(p => 
    Api.get(`/gamification/pets/${p.id}/risk`).catch(() => null)
  ));
  const map = { BAIXO: "green", MEDIO: "yellow", ALTO: "red" };
  const descMap = {
    BAIXO: "🐾 Pet saudável",
    MEDIO: "⚠️ Atenção recomendada",
    ALTO: "🚨 Acompanhamento necessário"
  };
  const tbody = document.getElementById("petRisksTable");
  tbody.innerHTML = riskData.filter(r => r).map(r => `
    <tr>
      <td>${r.petName}</td>
      <td>${r.score}</td>
      <td><span class="badge ${map[r.riskLevel] || 'gray'}">${r.riskLevel}</span></td>
      <td>${descMap[r.riskLevel] || r.riskDescription || '-'}</td>
    </tr>
  `).join("");
}

async function loadAvailableCoupons() {
  try {
    const data = await Api.get("/gamification/coupons/available?size=50");
    const coupons = data.content || [];
    const tbody = document.getElementById("availableCouponsTable");
    tbody.innerHTML = coupons.map(c => `
      <tr>
        <td>${c.code}</td>
        <td>${c.title || '-'}</td>
        <td>${c.pointsRequired}</td>
        <td>${fmtDate(c.expirationDate)}</td>
        <td><button class="small" onclick="confirmRedeemCoupon(${c.id}, '${c.code}', ${c.pointsRequired})">Resgatar</button></td>
      </tr>
    `).join("") || `<tr><td colspan="5" class="empty">Nenhum cupom disponível no momento.</td></tr>`;
  } catch (err) {
    console.error("Erro ao carregar cupons:", err);
  }
}

async function loadMyRedeems() {
  try {
    const data = await Api.get(`/redeems?tutorId=${user.id}&size=50`);
    const redeems = data.content || [];
    const tbody = document.getElementById("myRedeemsTable");
    tbody.innerHTML = redeems.map(r => `
      <tr>
        <td>${r.couponCode}</td>
        <td>${r.pointsUsed}</td>
        <td>${fmtDate(r.createdAt)}</td>
      </tr>
    `).join("") || `<tr><td colspan="3" class="empty">Você ainda não resgatou nenhum cupom.</td></tr>`;
  } catch (err) {
    console.error("Erro ao carregar resgates:", err);
  }
}

function confirmRedeemCoupon(couponId, couponCode, pointsRequired) {
  if (confirm(`Deseja resgatar o cupom ${couponCode} por ${pointsRequired} pontos?`)) {
    redeemCoupon(couponId);
  }
}

async function redeemCoupon(couponId) {
  try {
    const result = await Api.post("/gamification/redeem", { couponId });
    showMessage("redeemMsg", `Cupom ${result.couponCode} resgatado com sucesso!`, "success");
    // Recarregar dados
    await Promise.all([loadMyPoints(), loadAvailableCoupons(), loadMyRedeems()]);
  } catch (err) {
    showMessage("redeemMsg", err.message || "Erro ao resgatar cupom.");
  }
}

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
    </tr>
  `).join("");
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
    showMessage("petMsg", "Pet cadastrado com sucesso! +5 pontos!", "success");
    e.target.reset();
    await loadPets();
    await loadGamificationData();
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
    </tr>
  `).join("");
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
    showMessage("eventMsg", "Evento registrado com sucesso! +10 pontos!", "success");
    e.target.reset();
    await loadEvents();
    await loadGamificationData();
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
    </tr>
  `).join("");
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
    showMessage("subMsg", "Assinatura criada com sucesso! +15 pontos!", "success");
    e.target.reset();
    await loadSubscriptions();
    await loadGamificationData();
  } catch (err) {
    showMessage("subMsg", err.message);
  }
});

// ---------- Bootstrap ----------
(async function init() {
  try {
    await loadPets();
    await Promise.all([
      loadEvents(),
      loadPlansIntoSelect(),
      loadSubscriptions(),
      loadGamificationData()
    ]);
  } catch (err) {
    console.error(err);
  }
})();