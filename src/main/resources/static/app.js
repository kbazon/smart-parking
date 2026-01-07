const apiBase = "http://localhost:8080/tickets";
const output = document.getElementById("output");

console.log("APP.JS LOADED v1");
//za prikaz rezultata
function showResult(text) {
    output.textContent = text;
}

//parsiranje greške iz backenda
async function parseError(response) {
    let errMsg = await response.text();
    try {
        const json = JSON.parse(errMsg);
        return json.error || json.message || errMsg;
    } catch {
        return errMsg;
    }
}

//generiranje karte
async function generateTicket() {
    try {
        const response = await fetch(`${apiBase}/entry`, { method: "POST" });
        if (!response.ok) throw new Error(await parseError(response));
        const ticket = await response.json();
        document.getElementById("generatedUuid").textContent = ticket.ticketUuid;
        document.getElementById("exitUuid").value = ticket.ticketUuid;
        showResult(`Karta generirana.\nUUID: ${ticket.ticketUuid}\nUlaz: ${ticket.entryTime}`);
		await refreshAvailability();
    } catch (err) {
        console.error(err);
        showResult("Greška: " + err.message);
		
    }
}

//izlaz iz parkinga
async function exitParking() {
    const uuid = document.getElementById("exitUuid").value.trim();
    if (!uuid) { showResult("Unesite UUID karte."); return; }

    try {
        const response = await fetch(`${apiBase}/exit/${uuid}`, { method: "PUT" });
        
        if (!response.ok) {
            if (response.status === 400) throw new Error("Neispravan UUID.");
            if (response.status === 404) throw new Error("Karta nije pronađena.");
            if (response.status === 409) throw new Error("Karta je već iskorištena.");
            throw new Error(await parseError(response));
        }

        const ticket = await response.json();
        showResult(`Izlaz uspješan.\nUUID: ${ticket.ticketUuid}\nUlaz: ${ticket.entryTime}\nIzlaz: ${ticket.exitTime}\nCijena: ${ticket.price}\nPlaćeno: ${ticket.paid}`);
		await refreshAvailability();
    } catch (err) {
        console.error(err);
        showResult("Greška: " + err.message);
    }
}

//dohvat svih karata
async function getAllTickets() {
    try {
        const response = await fetch(apiBase);
        if (!response.ok) throw new Error(await parseError(response));
        const tickets = await response.json();
        if (tickets.length === 0) {
            showResult("Nema karata u sustavu.");
            return;
        }
        let text = `Broj karata: ${tickets.length}\n\n`;
        tickets.forEach(t => {
            let status = t.exitTime ? "Izašla" : "Na parkingu";
            let paidStatus = t.paid ? "Plaćeno" : "Neplaćeno";
            text += `UUID: ${t.ticketUuid}, Ulaz: ${t.entryTime}, Izlaz: ${t.exitTime || "-"}, Cijena: ${t.price || "-"}, Status: ${status}, Plaćeno: ${paidStatus}\n`;
        });
        showResult(text);
    } catch (err) {
        console.error(err);
        showResult("Greška: " + err.message);
    }
}

//dohvat karte po UUID
async function getTicketByUuid() {
    const uuid = document.getElementById("searchUuid").value.trim();
    if (!uuid) { showResult("Unesite UUID karte."); return; }

    try {
        const response = await fetch(`${apiBase}/${uuid}`);
        if (!response.ok) {
            if (response.status === 400) throw new Error("Neispravan UUID.");
            if (response.status === 404) throw new Error("Karta nije pronađena.");
            throw new Error(await parseError(response));
        }

        const t = await response.json();
        const text = `UUID: ${t.ticketUuid}\nUlaz: ${t.entryTime}\nIzlaz: ${t.exitTime || "-"}\nCijena: ${t.price || "-"}\nPlaćeno: ${t.paid}`;
        showResult(text);
    } catch (err) {
        console.error(err);
        showResult("Greška: " + err.message);
    }
}

async function refreshAvailability() {
  console.log("refreshAvailability called");
  const res = await fetch("/availability/total");
  console.log("status", res.status);
  const data = await res.json();
  console.log("data", data);

  document.getElementById("free").textContent = data.free;
  document.getElementById("capacity").textContent = data.capacity;
  document.getElementById("occupied").textContent = data.occupied;
}

refreshAvailability();
setInterval(refreshAvailability, 1000);
