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
const memberSearchForm = document.querySelector("#member-search-form");
const memberQuery = document.querySelector("#member-query");
const memberSearchMessage = document.querySelector("#member-search-message");
const memberResults = document.querySelector("#member-results");
const passwordChangeForm = document.querySelector("#password-change-form");
const passwordChangeUsername = document.querySelector("#password-change-username");
const passwordChangeMessage = document.querySelector("#password-change-message");
const cancelPasswordChange = document.querySelector("#cancel-password-change");
const openPasswordChangeButton = document.querySelector("#open-password-change");
const characterChart = document.querySelector("#character-chart");
const dashboardMessage = document.querySelector("#dashboard-message");
const costTotal = document.querySelector("#cost-total");
const totalCost = document.querySelector("#total-cost");

let currentUser = null;
let lastRequest = null;
let savedLoadouts = [];
let csrfProtection = null;
const defaultCosts = ["COST_4", "COST_3", "COST_3", "COST_1", "COST_1"];
const maxTotalCost = 12;
const critRateValues = [0, 6.3, 6.9, 7.5, 8.1, 8.7, 9.3, 9.9, 10.5];
const critDamageValues = [0, 12.6, 13.8, 15, 16.2, 17.4, 18.6, 19.8, 21];

buildEchoInputs();
updateCostUi();
loadCharacterDashboard();
restoreSession();

scoreForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessages();
    if (!isTotalCostValid()) {
        errorElement.textContent = "에코 코스트 합계는 12 이하여야 합니다.";
        return;
    }
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
        await loadCharacterDashboard();
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
        csrfProtection = null;
        currentUser = null;
        savedLoadouts = [];
        authForm.reset();
        authMessage.textContent = "";
        resetCalculator();
        updateAccountUi();
    }
});

refreshLoadouts.addEventListener("click", loadSavedLoadouts);

scoreForm.addEventListener("change", (event) => {
    if (event.target.matches('select[name^="cost-"]')) {
        updateCostUi();
    }
});

memberSearchForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    memberSearchMessage.textContent = "";
    memberResults.innerHTML = "";

    try {
        const users = await api(`/api/v1/users/search?query=${encodeURIComponent(memberQuery.value)}`);
        renderMemberResults(users);
    } catch (error) {
        memberSearchMessage.textContent = error.message;
    }
});

memberResults.addEventListener("click", (event) => {
    const button = event.target.closest("[data-password-change]");
    if (!button || !currentUser) {
        return;
    }
    openPasswordChange();
});

openPasswordChangeButton.addEventListener("click", openPasswordChange);
cancelPasswordChange.addEventListener("click", closePasswordChange);

passwordChangeForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    passwordChangeMessage.textContent = "";

    try {
        await api("/api/v1/auth/password", {
            method: "PATCH",
            body: JSON.stringify({
                currentPassword: document.querySelector("#current-password").value,
                newPassword: document.querySelector("#new-password").value
            })
        }, true);
        passwordChangeForm.reset();
        passwordChangeMessage.textContent = "비밀번호를 변경했습니다.";
    } catch (error) {
        passwordChangeMessage.textContent = error.message;
    }
});

savedGrid.addEventListener("click", async (event) => {
    const deleteButton = event.target.closest("[data-delete-loadout-id]");
    if (deleteButton) {
        await deleteLoadout(deleteButton);
        return;
    }

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
    const statOptions = (values) => values.map((value) =>
        `<option value="${value}">${value === 0 ? "없음 (0%)" : `${value}%`}</option>`
    ).join("");

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
                    <select name="critRate-${index}" required>
                        ${statOptions(critRateValues)}
                    </select>
                </label>
                <label>
                    <span>치명타 피해</span>
                    <select name="critDamage-${index}" required>
                        ${statOptions(critDamageValues)}
                    </select>
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
    updateCostUi();
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
            <div class="saved-actions">
                <button class="load-button" data-loadout-id="${loadout.id}" type="button">계산기에 불러오기</button>
                <button class="delete-button" data-delete-loadout-id="${loadout.id}" type="button"
                        aria-label="${escapeHtml(loadout.name || "이름 없는 세트")} 삭제">삭제</button>
            </div>
        </article>
    `).join("");
}

async function deleteLoadout(button) {
    const loadout = savedLoadouts.find((item) => item.id === button.dataset.deleteLoadoutId);
    if (!loadout) {
        return;
    }

    const name = loadout.name || "이름 없는 세트";
    if (!window.confirm(`"${name}" 프리셋을 삭제할까요?`)) {
        return;
    }

    button.disabled = true;
    try {
        await api(`/api/v1/loadouts/${loadout.id}`, {method: "DELETE"}, true);
        savedLoadouts = savedLoadouts.filter((item) => item.id !== loadout.id);
        renderSavedLoadouts();
        await loadCharacterDashboard();
        saveMessage.textContent = `"${name}" 프리셋을 삭제했습니다.`;
    } catch (error) {
        button.disabled = false;
        saveMessage.textContent = "";
        errorElement.textContent = error.message;
    }
}

function renderMemberResults(users) {
    if (users.length === 0) {
        memberSearchMessage.textContent = "검색 결과가 없습니다.";
        return;
    }

    memberSearchMessage.textContent = `${users.length}명의 회원을 찾았습니다.`;
    memberResults.innerHTML = users.map((user) => {
        const isCurrentUser = user.username === currentUser?.username;
        return `
            <article class="member-card">
                <div class="member-identity">
                    <strong>${escapeHtml(user.username)}</strong>
                    ${isCurrentUser ? '<span class="current-user-badge">본인</span>' : ""}
                </div>
                <div class="member-meta">
                    <time>가입일 ${formatDate(user.joinedAt)}</time>
                    ${isCurrentUser
                        ? '<button class="secondary-small" data-password-change type="button">비밀번호 변경</button>'
                        : ""}
                </div>
            </article>
        `;
    }).join("");
}

async function loadCharacterDashboard() {
    dashboardMessage.textContent = "대시보드를 불러오는 중입니다.";
    try {
        const characters = await api("/api/v1/dashboard/characters");
        renderCharacterDashboard(characters);
    } catch (error) {
        characterChart.innerHTML = "";
        dashboardMessage.textContent = error.message;
    }
}

function renderCharacterDashboard(characters) {
    if (characters.length === 0) {
        characterChart.innerHTML = "";
        dashboardMessage.textContent = "캐릭터 이름으로 저장된 세트가 아직 없습니다.";
        return;
    }

    characterChart.innerHTML = characters.map((character) => {
        const score = Number(character.bestScore);
        const barValue = Math.min(Math.max(score, 0), 250);
        const countLabel = character.loadoutCount > 1 ? ` · ${character.loadoutCount}개 중 최고` : "";
        return `
            <article class="character-chart-row">
                <div class="character-chart-label">
                    <strong>${escapeHtml(character.characterName)}</strong>
                    <span>${escapeHtml(character.gradeLabel)}${countLabel}</span>
                </div>
                <div class="character-chart-track"
                     role="progressbar"
                     aria-label="${escapeHtml(character.characterName)} 최고 점수"
                     aria-valuemin="0"
                     aria-valuemax="250"
                     aria-valuenow="${score.toFixed(1)}">
                    <svg class="character-chart-bar" data-grade="${character.grade}"
                         viewBox="0 0 250 34" preserveAspectRatio="none" aria-hidden="true">
                        <rect width="${barValue.toFixed(1)}" height="34"></rect>
                    </svg>
                    <span class="complete-threshold" aria-hidden="true"></span>
                    <strong class="character-chart-score">${score.toFixed(1)}</strong>
                </div>
            </article>
        `;
    }).join("");
    dashboardMessage.textContent = `총 ${characters.length}명의 캐릭터 최고 기록입니다.`;
}

function closePasswordChange() {
    passwordChangeForm.reset();
    passwordChangeMessage.textContent = "";
    passwordChangeForm.classList.add("hidden");
}

function openPasswordChange() {
    if (!currentUser) {
        return;
    }
    passwordChangeForm.reset();
    passwordChangeMessage.textContent = "";
    passwordChangeUsername.textContent = currentUser.username;
    passwordChangeForm.classList.remove("hidden");
    document.querySelector("#current-password").focus();
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
        memberSearchForm.reset();
        memberSearchMessage.textContent = "";
        memberResults.innerHTML = "";
        closePasswordChange();
    }
}

function getTotalCost() {
    return Array.from({length: 5}, (_, index) => {
        const value = scoreForm.elements[`cost-${index + 1}`].value;
        return Number(value.replace("COST_", ""));
    }).reduce((sum, value) => sum + value, 0);
}

function isTotalCostValid() {
    return getTotalCost() <= maxTotalCost;
}

function updateCostUi() {
    const currentTotal = getTotalCost();
    totalCost.textContent = currentTotal;
    costTotal.classList.toggle("invalid", currentTotal > maxTotalCost);

    scoreForm.querySelectorAll('select[name^="cost-"]').forEach((select) => {
        const selectedCost = Number(select.value.replace("COST_", ""));
        Array.from(select.options).forEach((option) => {
            const candidateCost = Number(option.value.replace("COST_", ""));
            option.disabled = option.value !== select.value
                && currentTotal - selectedCost + candidateCost > maxTotalCost;
        });
    });
}

function clearMessages() {
    errorElement.textContent = "";
    saveMessage.textContent = "";
}

function resetCalculator() {
    scoreForm.reset();
    updateCostUi();
    lastRequest = null;
    clearMessages();
    resultElement.classList.add("hidden");
    document.querySelectorAll(".echo-evaluation").forEach((evaluation) => {
        evaluation.classList.add("hidden");
    });
}

async function api(url, options = {}, allowEmpty = false, csrfRetried = false) {
    const method = (options.method || "GET").toUpperCase();
    const requiresCsrf = !["GET", "HEAD", "OPTIONS", "TRACE"].includes(method);
    const headers = options.body ? {"Content-Type": "application/json"} : {};
    if (requiresCsrf) {
        const csrf = await getCsrfProtection();
        headers[csrf.headerName] = csrf.token;
    }

    const response = await fetch(url, {...options, headers: {...headers, ...options.headers}});
    if (response.status === 403 && requiresCsrf && !csrfRetried) {
        csrfProtection = null;
        return api(url, options, allowEmpty, true);
    }
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

async function getCsrfProtection() {
    if (csrfProtection) {
        return csrfProtection;
    }

    const response = await fetch("/api/v1/csrf", {
        method: "GET",
        headers: {"Accept": "application/json"}
    });
    if (!response.ok) {
        throw new Error("보안 토큰을 발급받지 못했습니다. 페이지를 새로고침해 주세요.");
    }
    csrfProtection = await response.json();
    return csrfProtection;
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
