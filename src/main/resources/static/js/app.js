const echoGrid = document.querySelector("#echo-grid");
const scoreForm = document.querySelector("#score-form");
const errorElement = document.querySelector("#error");
const resultElement = document.querySelector("#result");
const saveButton = document.querySelector("#save-button");
const saveMessage = document.querySelector("#save-message");
const authForm = document.querySelector("#auth-form");
const authMessage = document.querySelector("#auth-message");
const signupButton = document.querySelector("#signup-button");
const logoutButton = document.querySelector("#logout-button");
const signedInPanel = document.querySelector("#signed-in");
const currentUsername = document.querySelector("#current-username");
const loginHint = document.querySelector("#login-hint");
const savedSection = document.querySelector("#saved-section");
const savedGrid = document.querySelector("#saved-grid");
const emptyState = document.querySelector("#empty-state");
const refreshLoadouts = document.querySelector("#refresh-loadouts");

let currentUser = null;
let lastRequest = null;
let savedLoadouts = [];
const defaultCosts = ["COST_4", "COST_3", "COST_3", "COST_1", "COST_1"];

buildEchoInputs();
restoreSession();

scoreForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessages();
    lastRequest = readScoreRequest();

    try {
        const result = await api("/api/v1/scores/calculate", {
            method: "POST",
            body: JSON.stringify(lastRequest)
        });
        renderResult(result);
    } catch (error) {
        errorElement.textContent = error.message;
        resultElement.classList.add("hidden");
    }
});

saveButton.addEventListener("click", async () => {
    clearMessages();
    if (!currentUser) {
        authMessage.textContent = "먼저 로그인하거나 회원가입해주세요.";
        document.querySelector(".account-panel").scrollIntoView({behavior: "smooth", block: "center"});
        return;
    }
    if (!lastRequest) {
        errorElement.textContent = "먼저 점수를 계산해주세요.";
        return;
    }

    saveButton.disabled = true;
    try {
        const saved = await api("/api/v1/loadouts", {
            method: "POST",
            body: JSON.stringify({
                name: document.querySelector("#loadout-name").value,
                scoreRequest: lastRequest
            })
        });
        saveMessage.textContent = `"${saved.name || "이름 없는 세트"}" 저장 완료`;
        await loadSavedLoadouts();
    } catch (error) {
        errorElement.textContent = error.message;
    } finally {
        updateAccountUi();
    }
});

authForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await authenticate("login");
});

signupButton.addEventListener("click", async () => {
    if (!authForm.reportValidity()) {
        return;
    }
    await authenticate("signup");
});

logoutButton.addEventListener("click", async () => {
    try {
        await api("/api/v1/auth/logout", {method: "POST"}, true);
    } finally {
        currentUser = null;
        savedLoadouts = [];
        authForm.reset();
        authMessage.textContent = "";
        updateAccountUi();
    }
});

refreshLoadouts.addEventListener("click", loadSavedLoadouts);

savedGrid.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-loadout-id]");
    if (!button) {
        return;
    }
    const loadout = savedLoadouts.find((item) => item.id === button.dataset.loadoutId);
    if (!loadout) {
        return;
    }
    applyLoadout(loadout);
    lastRequest = readScoreRequest();
    try {
        renderResult(await api("/api/v1/scores/calculate", {
            method: "POST",
            body: JSON.stringify(lastRequest)
        }));
        saveMessage.textContent = `"${loadout.name || "이름 없는 세트"}"를 계산기에 불러왔습니다.`;
        scoreForm.scrollIntoView({behavior: "smooth", block: "start"});
    } catch (error) {
        errorElement.textContent = error.message;
    }
});

function buildEchoInputs() {
    for (let index = 1; index <= 5; index += 1) {
        const costOptions = [
            ["COST_4", "4 코스트"],
            ["COST_3", "3 코스트"],
            ["COST_1", "1 코스트"]
        ].map(([value, label]) =>
            `<option value="${value}" ${defaultCosts[index - 1] === value ? "selected" : ""}>${label}</option>`
        ).join("");
        const costControl = index === 1
            ? `<input type="hidden" name="cost-1" value="COST_4">
               <span class="fixed-cost">4 코스트 <small>고정</small></span>`
            : `<select name="cost-${index}" required>${costOptions}</select>`;

        echoGrid.insertAdjacentHTML("beforeend", `
            <article class="echo-card">
                <div class="echo-number">${index}</div>
                <h2>${index}번 에코</h2>
                <label>
                    <span>에코 코스트</span>
                    ${costControl}
                </label>
                <label>
                    <span>치명타 확률</span>
                    <span class="input-wrap"><input name="critRate-${index}" type="number" min="0" max="100"
                        step="0.1" value="0" required><b>%</b></span>
                </label>
                <label>
                    <span>치명타 피해</span>
                    <span class="input-wrap"><input name="critDamage-${index}" type="number" min="0" max="200"
                        step="0.1" value="0" required><b>%</b></span>
                </label>
                <div class="echo-evaluation hidden" id="echo-evaluation-${index}">
                    <strong id="echo-score-${index}">0.0점</strong>
                    <span class="echo-grade" id="echo-grade-${index}"></span>
                </div>
            </article>
        `);
    }
}

async function restoreSession() {
    try {
        currentUser = await api("/api/v1/auth/me");
        await loadSavedLoadouts();
    } catch (error) {
        currentUser = null;
    }
    updateAccountUi();
}

async function authenticate(action) {
    authMessage.textContent = "";
    const credentials = {
        username: document.querySelector("#username").value,
        password: document.querySelector("#password").value
    };
    try {
        currentUser = await api(`/api/v1/auth/${action}`, {
            method: "POST",
            body: JSON.stringify(credentials)
        });
        document.querySelector("#password").value = "";
        updateAccountUi();
        await loadSavedLoadouts();
    } catch (error) {
        authMessage.textContent = error.message;
    }
}

async function loadSavedLoadouts() {
    if (!currentUser) {
        return;
    }
    try {
        savedLoadouts = await api("/api/v1/loadouts");
        renderSavedLoadouts();
    } catch (error) {
        if (error.status === 401) {
            currentUser = null;
            updateAccountUi();
            return;
        }
        savedGrid.innerHTML = `<p class="error">${escapeHtml(error.message)}</p>`;
    }
}

function readScoreRequest() {
    const data = new FormData(scoreForm);
    return {
        firstEchoMainStat: data.get("mainStat"),
        echoes: Array.from({length: 5}, (_, index) => ({
            cost: data.get(`cost-${index + 1}`),
            critRate: Number(data.get(`critRate-${index + 1}`)),
            critDamage: Number(data.get(`critDamage-${index + 1}`))
        }))
    };
}

function applyLoadout(loadout) {
    const mainStat = document.querySelector(`input[name="mainStat"][value="${loadout.firstEchoMainStat}"]`);
    if (mainStat) {
        mainStat.checked = true;
    }
    document.querySelector("#loadout-name").value = loadout.name || "";
    loadout.echoes.forEach((echo) => {
        const slot = echo.slotNumber;
        const costControl = scoreForm.elements[`cost-${slot}`];
        if (costControl) {
            costControl.value = `COST_${echo.cost}`;
        }
        scoreForm.elements[`critRate-${slot}`].value = echo.critRate;
        scoreForm.elements[`critDamage-${slot}`].value = echo.critDamage;
    });
}

function renderResult(result) {
    result.echoScores.forEach((echo) => {
        const evaluation = document.querySelector(`#echo-evaluation-${echo.slotNumber}`);
        const grade = document.querySelector(`#echo-grade-${echo.slotNumber}`);
        document.querySelector(`#echo-score-${echo.slotNumber}`).textContent = `${Number(echo.score).toFixed(1)}점`;
        grade.textContent = echo.gradeLabel;
        grade.dataset.grade = echo.grade;
        evaluation.classList.remove("hidden");
    });
    document.querySelector("#score").textContent = Number(result.score).toFixed(1);
    document.querySelector("#grade").textContent = result.gradeLabel;
    document.querySelector("#grade").dataset.grade = result.grade;
    document.querySelector("#total-crit-rate").textContent = `${Number(result.totalCritRate).toFixed(1)}%`;
    document.querySelector("#total-crit-damage").textContent = `${Number(result.totalCritDamage).toFixed(1)}%`;
    document.querySelector("#main-stat-score").textContent = `${Number(result.mainStatScore).toFixed(1)}점`;
    document.querySelector("#next-grade").textContent = `${Number(result.pointsToNextGrade).toFixed(1)}점`;
    document.querySelector("#next-grade-row").classList.toggle("hidden", result.grade === "EXTREME");
    resultElement.classList.remove("hidden");
    resultElement.scrollIntoView({behavior: "smooth", block: "nearest"});
}

function renderSavedLoadouts() {
    emptyState.classList.toggle("hidden", savedLoadouts.length !== 0);
    savedGrid.innerHTML = savedLoadouts.map((loadout) => `
        <article class="saved-card">
            <div class="saved-card-heading">
                <div>
                    <h3>${escapeHtml(loadout.name || "이름 없는 세트")}</h3>
                    <time>${formatDate(loadout.createdAt)}</time>
                </div>
                <div class="saved-score">
                    <strong>${Number(loadout.score).toFixed(1)}</strong>
                    <span data-grade="${loadout.grade}">${escapeHtml(loadout.gradeLabel)}</span>
                </div>
            </div>
            <p class="main-option">${loadout.firstEchoMainStat === "CRIT_RATE" ? "치명타 확률" : "치명타 피해"} 주옵션</p>
            <div class="echo-summary">
                ${loadout.echoes.map((echo) => `
                    <span><b>${echo.slotNumber}</b> ${echo.cost}C · 치확 ${Number(echo.critRate).toFixed(1)} · 치피 ${Number(echo.critDamage).toFixed(1)}</span>
                `).join("")}
            </div>
            <button class="load-button" data-loadout-id="${loadout.id}" type="button">계산기에 불러오기</button>
        </article>
    `).join("");
}

function updateAccountUi() {
    const signedIn = Boolean(currentUser);
    authForm.classList.toggle("hidden", signedIn);
    signedInPanel.classList.toggle("hidden", !signedIn);
    savedSection.classList.toggle("hidden", !signedIn);
    loginHint.classList.toggle("hidden", signedIn);
    saveButton.disabled = !signedIn;
    if (signedIn) {
        currentUsername.textContent = currentUser.username;
    } else {
        savedGrid.innerHTML = "";
        emptyState.classList.add("hidden");
    }
}

function clearMessages() {
    errorElement.textContent = "";
    saveMessage.textContent = "";
}

async function api(url, options = {}, allowEmpty = false) {
    const headers = options.body ? {"Content-Type": "application/json"} : {};
    const response = await fetch(url, {...options, headers: {...headers, ...options.headers}});
    if (!response.ok) {
        let message = "요청을 처리하지 못했습니다.";
        try {
            const error = await response.json();
            message = error.detail || error.message || message;
            if (error.errors?.length) {
                message = error.errors.map((item) => item.defaultMessage).join(" ");
            }
        } catch (ignored) {
            // JSON 오류 본문이 아닌 경우 기본 메시지를 사용합니다.
        }
        const requestError = new Error(message);
        requestError.status = response.status;
        throw requestError;
    }
    if (allowEmpty || response.status === 204) {
        return null;
    }
    return response.json();
}

function formatDate(value) {
    return new Intl.DateTimeFormat("ko-KR", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
