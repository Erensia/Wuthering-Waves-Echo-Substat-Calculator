const scoreForm = document.querySelector("#score-form");
const echoGrid = document.querySelector("#echo-grid");
const errorElement = document.querySelector("#error");
const resultElement = document.querySelector("#result");
const costTotal = document.querySelector("#cost-total");
const totalCost = document.querySelector("#total-cost");
const resetButton = document.querySelector("#reset-button");
const exportCsvButton = document.querySelector("#export-csv-button");
const importCsvButton = document.querySelector("#import-csv-button");
const exportJsonButton = document.querySelector("#export-json-button");
const importJsonButton = document.querySelector("#import-json-button");
const clearDraftButton = document.querySelector("#clear-draft-button");
const csvFileInput = document.querySelector("#csv-file-input");
const jsonFileInput = document.querySelector("#json-file-input");
const storageMessage = document.querySelector("#storage-message");

const stateVersion = 1;
const draftStorageKey = "echo-grader:draft:v1";
const defaultCosts = [4, 3, 3, 1, 1];
const maxTotalCost = 12;
const mainStatScore = 44.0;
const critRateValues = [0, 6.3, 6.9, 7.5, 8.1, 8.7, 9.3, 9.9, 10.5];
const critDamageValues = [0, 12.6, 13.8, 15, 16.2, 17.4, 18.6, 19.8, 21];

const gradeLabels = {
    EXTREME: "극종결",
    COMPLETE: "종결",
    NEAR_COMPLETE: "준종결",
    NEED_REBUILD: "재파밍 권장"
};

const csvColumns = [
    "version",
    "savedAt",
    "firstEchoMainStat",
    "totalCritRate",
    "totalCritDamage",
    "mainStatScore",
    "score",
    "grade",
    "gradeLabel",
    "pointsToNextGrade",
    "slot1Cost",
    "slot1CritRate",
    "slot1CritDamage",
    "slot1Score",
    "slot1Grade",
    "slot2Cost",
    "slot2CritRate",
    "slot2CritDamage",
    "slot2Score",
    "slot2Grade",
    "slot3Cost",
    "slot3CritRate",
    "slot3CritDamage",
    "slot3Score",
    "slot3Grade",
    "slot4Cost",
    "slot4CritRate",
    "slot4CritDamage",
    "slot4Score",
    "slot4Grade",
    "slot5Cost",
    "slot5CritRate",
    "slot5CritDamage",
    "slot5Score",
    "slot5Grade"
];

buildEchoInputs();
restoreDraft();
updateCostUi();

scoreForm.addEventListener("submit", (event) => {
    event.preventDefault();
    errorElement.textContent = "";

    if (!isTotalCostValid()) {
        errorElement.textContent = "총 에코 코스트는 12를 넘길 수 없습니다.";
        resultElement.classList.add("hidden");
        return;
    }

    const result = calculateScore(readScoreRequest());
    renderResult(result);
    saveDraft("계산 결과를 임시 저장했습니다.");
});

scoreForm.addEventListener("change", (event) => {
    if (event.target.matches('select[name^="cost-"]')) {
        updateCostUi();
    }
    saveDraft();
});

resetButton.addEventListener("click", () => {
    scoreForm.reset();
    updateCostUi();
    errorElement.textContent = "";
    resultElement.classList.add("hidden");
    document.querySelectorAll(".echo-evaluation").forEach((element) => {
        element.classList.add("hidden");
    });
    saveDraft("입력값을 초기화했습니다.");
});

exportCsvButton.addEventListener("click", () => {
    exportCsv();
});

importCsvButton.addEventListener("click", () => {
    csvFileInput.click();
});

exportJsonButton.addEventListener("click", () => {
    exportJson();
});

importJsonButton.addEventListener("click", () => {
    jsonFileInput.click();
});

clearDraftButton.addEventListener("click", () => {
    localStorage.removeItem(draftStorageKey);
    storageMessage.textContent = "브라우저 임시 저장을 삭제했습니다.";
});

csvFileInput.addEventListener("change", async () => {
    await importCsv(csvFileInput.files?.[0]);
    csvFileInput.value = "";
});

jsonFileInput.addEventListener("change", async () => {
    await importJson(jsonFileInput.files?.[0]);
    jsonFileInput.value = "";
});

function buildEchoInputs() {
    const makeOptions = (values) => values.map((value) =>
        `<option value="${value}">${value === 0 ? "없음 (0%)" : `${value}%`}</option>`
    ).join("");

    for (let slot = 1; slot <= 5; slot += 1) {
        const costControl = slot === 1
            ? `<input type="hidden" name="cost-${slot}" value="4">
               <span class="fixed-cost">4 코스트 고정</span>`
            : `<select name="cost-${slot}" required>
                   ${[4, 3, 1].map((cost) =>
                       `<option value="${cost}" ${defaultCosts[slot - 1] === cost ? "selected" : ""}>${cost} 코스트</option>`
                   ).join("")}
               </select>`;

        echoGrid.insertAdjacentHTML("beforeend", `
            <article class="echo-card" data-slot="${slot}">
                <h3>${slot}번 에코</h3>
                <div class="field">
                    <span>코스트</span>
                    ${costControl}
                </div>
                <label class="field">
                    <span>치명타 확률</span>
                    <select name="critRate-${slot}" required>${makeOptions(critRateValues)}</select>
                </label>
                <label class="field">
                    <span>치명타 피해</span>
                    <select name="critDamage-${slot}" required>${makeOptions(critDamageValues)}</select>
                </label>
                <div class="echo-evaluation hidden" id="echo-evaluation-${slot}">
                    <strong id="echo-score-${slot}">0.0점</strong>
                    <span class="badge" id="echo-grade-${slot}"></span>
                </div>
            </article>
        `);
    }
}

function readScoreRequest() {
    const data = new FormData(scoreForm);
    return {
        firstEchoMainStat: data.get("mainStat"),
        echoes: Array.from({length: 5}, (_, index) => ({
            slotNumber: index + 1,
            cost: Number(data.get(`cost-${index + 1}`)),
            critRate: Number(data.get(`critRate-${index + 1}`)),
            critDamage: Number(data.get(`critDamage-${index + 1}`))
        }))
    };
}

function applyScoreRequest(request) {
    validateScoreRequest(request);

    const mainStat = scoreForm.querySelector(`input[name="mainStat"][value="${request.firstEchoMainStat}"]`);
    if (mainStat) {
        mainStat.checked = true;
    }

    request.echoes.forEach((echo, index) => {
        const slot = index + 1;
        const costControl = scoreForm.elements[`cost-${slot}`];
        if (costControl) {
            costControl.value = String(echo.cost);
        }
        scoreForm.elements[`critRate-${slot}`].value = String(echo.critRate);
        scoreForm.elements[`critDamage-${slot}`].value = String(echo.critDamage);
    });

    updateCostUi();
    const result = calculateScore(readScoreRequest());
    renderResult(result);
    saveDraft();
}

function calculateScore(request) {
    const totalCritRate = roundOne(request.echoes.reduce((sum, echo) => sum + echo.critRate, 0));
    const totalCritDamage = roundOne(request.echoes.reduce((sum, echo) => sum + echo.critDamage, 0));
    const score = roundOne(totalCritRate * 2 + totalCritDamage + mainStatScore);
    const grade = getGrade(score);

    return {
        totalCritRate,
        totalCritDamage,
        mainStatScore,
        score,
        grade,
        gradeLabel: gradeLabels[grade],
        pointsToNextGrade: getPointsToNextGrade(score),
        echoScores: request.echoes.map((echo) => {
            const echoScore = roundOne(echo.critRate * 2 + echo.critDamage);
            const echoGrade = getEchoGrade(echoScore);
            return {
                ...echo,
                score: echoScore,
                grade: echoGrade,
                gradeLabel: gradeLabels[echoGrade]
            };
        })
    };
}

function renderResult(result) {
    result.echoScores.forEach((echo) => {
        const score = document.querySelector(`#echo-score-${echo.slotNumber}`);
        const grade = document.querySelector(`#echo-grade-${echo.slotNumber}`);
        score.textContent = `${formatOne(echo.score)}점`;
        grade.textContent = echo.gradeLabel;
        grade.dataset.grade = echo.grade;
        document.querySelector(`#echo-evaluation-${echo.slotNumber}`).classList.remove("hidden");
    });

    document.querySelector("#score").textContent = formatOne(result.score);
    const gradeElement = document.querySelector("#grade");
    gradeElement.textContent = result.gradeLabel;
    gradeElement.dataset.grade = result.grade;
    gradeElement.className = "grade badge";
    document.querySelector("#total-crit-rate").textContent = `${formatOne(result.totalCritRate)}%`;
    document.querySelector("#total-crit-damage").textContent = `${formatOne(result.totalCritDamage)}%`;
    document.querySelector("#main-stat-score").textContent = `${formatOne(result.mainStatScore)}점`;
    document.querySelector("#next-grade").textContent = `${formatOne(result.pointsToNextGrade)}점`;
    document.querySelector("#next-grade-row").classList.toggle("hidden", result.grade === "EXTREME");
    resultElement.classList.remove("hidden");
}

function makeSnapshot() {
    const request = readScoreRequest();
    const result = isTotalCostValid() ? calculateScore(request) : null;
    return {
        version: stateVersion,
        savedAt: new Date().toISOString(),
        request,
        result
    };
}

function saveDraft(message = "") {
    try {
        localStorage.setItem(draftStorageKey, JSON.stringify(makeSnapshot()));
        if (message) {
            storageMessage.textContent = message;
        }
    } catch (error) {
        storageMessage.textContent = "브라우저 임시 저장에 실패했습니다.";
    }
}

function restoreDraft() {
    try {
        const rawDraft = localStorage.getItem(draftStorageKey);
        if (!rawDraft) {
            return;
        }
        const draft = JSON.parse(rawDraft);
        if (draft?.request) {
            applyScoreRequest(draft.request);
            storageMessage.textContent = "브라우저 임시 저장값을 복원했습니다.";
        }
    } catch (error) {
        localStorage.removeItem(draftStorageKey);
        storageMessage.textContent = "손상된 임시 저장값을 삭제했습니다.";
    }
}

function exportCsv() {
    const snapshot = makeSnapshot();
    const row = snapshotToCsvRow(snapshot);
    const csv = `${csvColumns.join(",")}\n${csvColumns.map((column) => escapeCsv(row[column] ?? "")).join(",")}\n`;
    downloadBlob(csv, `echo-grader-${dateStamp()}.csv`, "text/csv;charset=utf-8");
    storageMessage.textContent = "CSV 파일을 내보냈습니다.";
}

async function importCsv(file) {
    if (!file) {
        return;
    }

    try {
        const rows = parseCsv(await file.text());
        if (rows.length < 2) {
            throw new Error("CSV 데이터가 비어 있습니다.");
        }

        const headers = rows[0].map((header) => header.trim());
        const values = rows[1];
        const row = Object.fromEntries(headers.map((header, index) => [header, values[index] ?? ""]));
        applyScoreRequest(csvRowToRequest(row));
        storageMessage.textContent = "CSV 파일에서 입력값을 복원했습니다.";
    } catch (error) {
        storageMessage.textContent = `CSV 가져오기 실패: ${error.message}`;
    }
}

function exportJson() {
    const json = JSON.stringify(makeSnapshot(), null, 2);
    downloadBlob(json, `echo-grader-${dateStamp()}.json`, "application/json;charset=utf-8");
    storageMessage.textContent = "JSON 백업 파일을 내보냈습니다.";
}

async function importJson(file) {
    if (!file) {
        return;
    }

    try {
        const snapshot = JSON.parse(await file.text());
        if (!snapshot?.request) {
            throw new Error("복원할 입력값이 없습니다.");
        }
        applyScoreRequest(snapshot.request);
        storageMessage.textContent = "JSON 백업에서 입력값을 복원했습니다.";
    } catch (error) {
        storageMessage.textContent = `JSON 복원 실패: ${error.message}`;
    }
}

function snapshotToCsvRow(snapshot) {
    const row = {
        version: snapshot.version,
        savedAt: snapshot.savedAt,
        firstEchoMainStat: snapshot.request.firstEchoMainStat,
        totalCritRate: snapshot.result?.totalCritRate ?? "",
        totalCritDamage: snapshot.result?.totalCritDamage ?? "",
        mainStatScore: snapshot.result?.mainStatScore ?? "",
        score: snapshot.result?.score ?? "",
        grade: snapshot.result?.grade ?? "",
        gradeLabel: snapshot.result?.gradeLabel ?? "",
        pointsToNextGrade: snapshot.result?.pointsToNextGrade ?? ""
    };

    snapshot.request.echoes.forEach((echo, index) => {
        const slot = index + 1;
        const echoResult = snapshot.result?.echoScores[index];
        row[`slot${slot}Cost`] = echo.cost;
        row[`slot${slot}CritRate`] = echo.critRate;
        row[`slot${slot}CritDamage`] = echo.critDamage;
        row[`slot${slot}Score`] = echoResult?.score ?? "";
        row[`slot${slot}Grade`] = echoResult?.grade ?? "";
    });

    return row;
}

function csvRowToRequest(row) {
    return {
        firstEchoMainStat: row.firstEchoMainStat,
        echoes: Array.from({length: 5}, (_, index) => {
            const slot = index + 1;
            return {
                slotNumber: slot,
                cost: Number(row[`slot${slot}Cost`]),
                critRate: Number(row[`slot${slot}CritRate`]),
                critDamage: Number(row[`slot${slot}CritDamage`])
            };
        })
    };
}

function validateScoreRequest(request) {
    if (!["CRIT_RATE", "CRIT_DAMAGE"].includes(request?.firstEchoMainStat)) {
        throw new Error("주옵션 값이 올바르지 않습니다.");
    }
    if (!Array.isArray(request.echoes) || request.echoes.length !== 5) {
        throw new Error("에코 입력값은 5개여야 합니다.");
    }

    request.echoes.forEach((echo, index) => {
        const validCosts = index === 0 ? [4] : [4, 3, 1];
        if (!validCosts.includes(Number(echo.cost))) {
            throw new Error(`${index + 1}번 에코 코스트가 올바르지 않습니다.`);
        }
        if (!critRateValues.includes(Number(echo.critRate))) {
            throw new Error(`${index + 1}번 에코 치명타 확률이 올바르지 않습니다.`);
        }
        if (!critDamageValues.includes(Number(echo.critDamage))) {
            throw new Error(`${index + 1}번 에코 치명타 피해가 올바르지 않습니다.`);
        }
    });

    const total = request.echoes.reduce((sum, echo) => sum + Number(echo.cost), 0);
    if (total > maxTotalCost) {
        throw new Error("총 에코 코스트는 12를 넘길 수 없습니다.");
    }
}

function parseCsv(text) {
    const rows = [];
    let row = [];
    let value = "";
    let quoted = false;

    for (let index = 0; index < text.length; index += 1) {
        const char = text[index];
        const next = text[index + 1];

        if (quoted) {
            if (char === '"' && next === '"') {
                value += '"';
                index += 1;
            } else if (char === '"') {
                quoted = false;
            } else {
                value += char;
            }
            continue;
        }

        if (char === '"') {
            quoted = true;
        } else if (char === ",") {
            row.push(value);
            value = "";
        } else if (char === "\n") {
            row.push(value);
            rows.push(row);
            row = [];
            value = "";
        } else if (char !== "\r") {
            value += char;
        }
    }

    row.push(value);
    if (row.some((cell) => cell !== "") || rows.length === 0) {
        rows.push(row);
    }

    return rows;
}

function escapeCsv(value) {
    const text = String(value);
    return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text;
}

function downloadBlob(content, filename, type) {
    const blob = new Blob([content], {type});
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename;
    document.body.append(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
}

function dateStamp() {
    return new Date().toISOString().slice(0, 19).replaceAll(":", "-");
}

function getGrade(score) {
    if (score >= 230) {
        return "EXTREME";
    }
    if (score >= 200) {
        return "COMPLETE";
    }
    if (score >= 170) {
        return "NEAR_COMPLETE";
    }
    return "NEED_REBUILD";
}

function getEchoGrade(score) {
    if (score >= 37.8) {
        return "EXTREME";
    }
    if (score >= 33.6) {
        return "COMPLETE";
    }
    if (score >= 29.4) {
        return "NEAR_COMPLETE";
    }
    return "NEED_REBUILD";
}

function getPointsToNextGrade(score) {
    const grade = getGrade(score);
    const target = {
        NEED_REBUILD: 170,
        NEAR_COMPLETE: 200,
        COMPLETE: 230,
        EXTREME: score
    }[grade];
    return roundOne(Math.max(target - score, 0));
}

function getTotalCost() {
    return Array.from({length: 5}, (_, index) =>
        Number(scoreForm.elements[`cost-${index + 1}`].value)
    ).reduce((sum, cost) => sum + cost, 0);
}

function isTotalCostValid() {
    return getTotalCost() <= maxTotalCost;
}

function updateCostUi() {
    const currentTotal = getTotalCost();
    totalCost.textContent = currentTotal;
    costTotal.classList.toggle("invalid", currentTotal > maxTotalCost);

    scoreForm.querySelectorAll('select[name^="cost-"]').forEach((select) => {
        const selectedCost = Number(select.value);
        Array.from(select.options).forEach((option) => {
            const candidateCost = Number(option.value);
            option.disabled = option.value !== select.value
                && currentTotal - selectedCost + candidateCost > maxTotalCost;
        });
    });
}

function roundOne(value) {
    return Math.round((value + Number.EPSILON) * 10) / 10;
}

function formatOne(value) {
    return Number(value).toFixed(1);
}
