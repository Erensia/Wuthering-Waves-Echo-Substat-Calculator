const echoGrid = document.querySelector("#echo-grid");
const form = document.querySelector("#score-form");
const errorElement = document.querySelector("#error");
const resultElement = document.querySelector("#result");
const saveButton = document.querySelector("#save-button");
const saveMessage = document.querySelector("#save-message");
let lastRequest = null;
const defaultCosts = ["COST_4", "COST_3", "COST_3", "COST_1", "COST_1"];

for (let index = 1; index <= 5; index += 1) {
    const costOptions = [
        ["COST_4", "4코스트"],
        ["COST_3", "3코스트"],
        ["COST_1", "1코스트"]
    ].map(([value, label]) =>
        `<option value="${value}" ${defaultCosts[index - 1] === value ? "selected" : ""}>${label}</option>`
    ).join("");

    echoGrid.insertAdjacentHTML("beforeend", `
        <article class="echo-card">
            <div class="echo-number">${index}</div>
            <h2>${index}번 에코</h2>
            <label>
                <span>에코 코스트</span>
                <select name="cost-${index}" required>${costOptions}</select>
            </label>
            <label>
                <span>크리티컬</span>
                <span class="input-wrap"><input name="critRate-${index}" type="number" min="0" max="100"
                    step="0.1" value="0" required><b>%</b></span>
            </label>
            <label>
                <span>크리티컬 피해</span>
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

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorElement.textContent = "";

    const data = new FormData(form);
    const request = {
        firstEchoMainStat: data.get("mainStat"),
        echoes: Array.from({length: 5}, (_, index) => ({
            cost: data.get(`cost-${index + 1}`),
            critRate: Number(data.get(`critRate-${index + 1}`)),
            critDamage: Number(data.get(`critDamage-${index + 1}`))
        }))
    };
    lastRequest = request;
    saveMessage.textContent = "";

    try {
        const response = await fetch("/api/v1/scores/calculate", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(request)
        });
        if (!response.ok) {
            throw new Error("입력값을 확인해 주세요.");
        }
        renderResult(await response.json());
    } catch (error) {
        errorElement.textContent = error.message;
        resultElement.classList.add("hidden");
    }
});

saveButton.addEventListener("click", async () => {
    if (!lastRequest) {
        errorElement.textContent = "먼저 점수를 계산해 주세요.";
        return;
    }
    errorElement.textContent = "";
    saveMessage.textContent = "";
    saveButton.disabled = true;

    try {
        const response = await fetch("/api/v1/loadouts", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                name: document.querySelector("#loadout-name").value,
                scoreRequest: lastRequest
            })
        });
        if (!response.ok) {
            throw new Error("저장하지 못했습니다. PostgreSQL 연결을 확인해 주세요.");
        }
        const saved = await response.json();
        saveMessage.textContent = `저장 완료 · ID ${saved.id}`;
    } catch (error) {
        errorElement.textContent = error.message;
    } finally {
        saveButton.disabled = false;
    }
});

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
