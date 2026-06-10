const LOTS = ["LOT_A"];

const API_BASE = window.location.origin;
const AUTO_REFRESH_MS = 10000;

const lotGrid = document.getElementById("lotGrid");
const logBox = document.getElementById("eventLog");
const refreshBtn = document.getElementById("refreshBtn");

refreshBtn.addEventListener("click", refreshDashboard);

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function fetchAvailabilityData() {
    const response = await fetch(`${API_BASE}/availability/total`, {
        cache: "no-store"
    });

    if (!response.ok) {
        throw new Error("Failed to fetch availability");
    }

    return await response.json();
}

async function fetchAvailability() {
    try {
        const data = await fetchAvailabilityData();

        document.getElementById("free").innerText = data.free;
        document.getElementById("capacity").innerText = data.capacity;
        document.getElementById("occupied").innerText = data.occupied;

        return data;

    } catch (err) {
        console.error(err);
        return null;
    }
}

async function waitForOccupiedChange(previousOccupied, delta) {
    const expectedOccupied = Math.max(0, previousOccupied + delta);

    for (let i = 0; i < 20; i++) {
        await sleep(150);

        const current = await fetchAvailabilityData();

        const changed = delta > 0
            ? current.occupied >= expectedOccupied
            : current.occupied <= expectedOccupied;

        if (changed) {
            await sleep(150);
            return current;
        }
    }

    console.warn("Occupancy did not change in time; refreshing dashboard anyway.");
    return null;
}

async function fetchPrediction(lotId) {
    const response = await fetch(
        `${API_BASE}/api/parking/predict/${lotId}?refresh=true`,
        { cache: "no-store" }
    );

    if (!response.ok) {
        throw new Error(`Failed to fetch prediction for ${lotId}`);
    }

    return await response.json();
}

async function renderLots() {
    lotGrid.innerHTML = "";

    for (const lotId of LOTS) {
        try {
            const data = await fetchPrediction(lotId);

            document.getElementById("activeModel").innerText = `${data.model_used}`;

            const probabilityPercent = (data.probability * 100).toFixed(1);
            const high = data.prediction === 1 || data.high_occupancy_next_hour === true;

            const card = document.createElement("div");
            card.className = "lot-card";

			card.innerHTML = `
			    <h3>${lotId}</h3>

			    <div class="metric">
			        <span>Forecast Horizon</span>
			        <span>Next 60 minutes</span>
			    </div>

			    <div class="metric">
			        <span>Predicted Status Next Hour</span>
			        <span class="${high ? "high" : "normal"}">
			            ${high ? "HIGH OCCUPANCY RISK" : "NORMAL RISK"}
			        </span>
			    </div>

			    <div class="metric">
			        <span>Probability of ≥85% Occupancy Next Hour</span>
			        <span>${probabilityPercent}%</span>
			    </div>

			    <div class="metric">
			        <span>Binary Prediction</span>
			        <span>${data.prediction === 1 ? "1 — high next hour" : "0 — not high next hour"}</span>
			    </div>

			    <div class="metric">
			        <span>Model Used</span>
			        <span>${data.model_used}</span>
			    </div>

			    <div class="progress-bar">
			        <div class="progress-fill" style="width:${probabilityPercent}%"></div>
			    </div>
			`;

            lotGrid.appendChild(card);

        } catch (err) {
            console.error(err);

            const errorCard = document.createElement("div");
            errorCard.className = "lot-card";
            errorCard.innerHTML = `
                <h3>${lotId}</h3>
                <p class="high">Prediction unavailable</p>
            `;

            lotGrid.appendChild(errorCard);
        }
    }
}

async function checkServices() {
    try {
        const backend = await fetch(`${API_BASE}/availability/total`, {
            cache: "no-store"
        });

        document.getElementById("backendStatus").innerText = backend.ok
            ? "ONLINE"
            : "OFFLINE";

    } catch {
        document.getElementById("backendStatus").innerText = "OFFLINE";
    }

	try {
	    const prediction = await fetch(`${API_BASE}/api/parking/predict/LOT_A?refresh=true`, {
	        cache: "no-store"
	    });

	    if (!prediction.ok) {
	        throw new Error("Prediction endpoint unavailable");
	    }

	    const data = await prediction.json();

	    document.getElementById("mlStatus").innerText =
	        data.model_used === "xgboost" ? "ONLINE" : "DEGRADED";

	} catch {
	    document.getElementById("mlStatus").innerText = "OFFLINE";
	}
}

async function generateTicket() {
    try {
        const before = await fetchAvailabilityData();

        const response = await fetch(`${API_BASE}/tickets/entry`, {
            method: "POST"
        });

        if (!response.ok) {
            throw new Error("ENTRY request failed");
        }

        const ticket = await response.json();

        addLog(`ENTRY processed -> ${ticket.ticketUuid}`);

        await waitForOccupiedChange(before.occupied, +1);
        await refreshDashboard();

    } catch (err) {
        console.error(err);
        addLog("ENTRY failed");
    }
}

async function simulateExit() {
    try {
        const before = await fetchAvailabilityData();

        const ticketsResponse = await fetch(`${API_BASE}/tickets`, {
            cache: "no-store"
        });

        if (!ticketsResponse.ok) {
            throw new Error("Failed to fetch active tickets");
        }

        const tickets = await ticketsResponse.json();
        const active = tickets.find(t => !t.exitTime);

        if (!active) {
            addLog("No active ticket available for EXIT");
            return;
        }

        const exitResponse = await fetch(`${API_BASE}/tickets/exit/${active.ticketUuid}`, {
            method: "PUT"
        });

        if (!exitResponse.ok) {
            throw new Error("EXIT request failed");
        }

        addLog(`EXIT processed -> ${active.ticketUuid}`);

        await waitForOccupiedChange(before.occupied, -1);
        await refreshDashboard();

    } catch (err) {
        console.error(err);
        addLog("EXIT failed");
    }
}

function addLog(message) {
    const entry = document.createElement("div");
    entry.className = "log-entry";

    const now = new Date().toLocaleTimeString();

    entry.innerText = `[${now}] ${message}`;

    logBox.prepend(entry);
}

async function refreshDashboard() {
    await fetchAvailability();
    await renderLots();
    await checkServices();
}

refreshDashboard();

setInterval(refreshDashboard, AUTO_REFRESH_MS);