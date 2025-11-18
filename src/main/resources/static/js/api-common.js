// 다이어리 전체 불러오기 (데이터 반환)
async function loadDiaries() {
    try {
        const res = await axios.get('/api/getAllDiaries');
        if (res.data.returnCode === 'SUCCESS') {
            return res.data.resultData || [];
        }
        return [];
    } catch (e) {
        console.error(e);
        return [];
    }
}

// 월별 요약 통계 렌더링
// 필요한 엘리먼트와 다이어리 배열을 인자로 받음
function renderSummary(selectedDate, diaries, elements) {
    if (!elements) {
        console.warn("renderSummary: elements object is required");
        return;
    }
    const now = selectedDate;
    const monthDiaries = (diaries || []).filter(d => {
        const dd = new Date(d.created_at);
        return dd.getFullYear() === now.getFullYear() && dd.getMonth() === now.getMonth();
    });

    if (elements.currentMonthTotalCountEl) {
        elements.currentMonthTotalCountEl.textContent = monthDiaries.length;
    }
    if (elements.currentMonthLabel) {
        elements.currentMonthLabel.textContent = `${now.getMonth() + 1}월 총 일지`;
    }
    if (elements.happyCountEl) {
        elements.happyCountEl.textContent = monthDiaries.filter(d => d.mood === 'HAPPY').length;
    }
    if (elements.sadCountEl) {
        elements.sadCountEl.textContent = monthDiaries.filter(d => d.mood === 'SAD').length;
    }
    if (elements.angryCountEl) {
        elements.angryCountEl.textContent = monthDiaries.filter(d => d.mood === 'ANGRY').length;
    }
    if (elements.excitedCountEl) {
        elements.excitedCountEl.textContent = monthDiaries.filter(d => d.mood === 'EXCITED').length;
    }
}

// 스케줄(산책/병원) 월별 조회
async function loadMonthlySchedules(year, month, userId) {
    try {
        const formattedMonth = `${year}-${String(month + 1).padStart(2, "0")}`;
        const response = await axios.get(`/api/getSchedulesByMonth`, {
            params: { user_id: userId, month: formattedMonth }
        });
        if (response.data.returnCode === 'SUCCESS') {
            return response.data.resultData || [];
        }
        return [];
    } catch (error) {
        console.error('월별 일정 조회 실패:', error);
        return [];
    }
}

// 산책 API
async function createWalk(payload) {
    return axios.post('/api/createWalk', payload, { headers: { 'Content-Type': 'application/json' } });
}
async function updateWalk(payload) {
    return axios.post('/api/updateWalk', payload, { headers: { 'Content-Type': 'application/json' } });
}
async function deleteWalk(id) {
    return axios.post('/api/deleteWalk', { id });
}

// 병원 API
async function createHospital(payload) {
    return axios.post('/api/createHospital', payload, { headers: { 'Content-Type': 'application/json' } });
}
async function updateHospital(payload) {
    return axios.post('/api/updateHospital', payload, { headers: { 'Content-Type': 'application/json' } });
}
async function deleteHospital(id) {
    return axios.post('/api/deleteHospital', { id });
}

// 월별 통계 조회
async function loadMonthlyStatistics(userId) {
    try {
        const response = await axios.get('/api/getMonthlyStatistics', {
            params: { user_id: userId }
        });
        if (response.data.returnCode === 'SUCCESS') {
            return response.data.resultData || [];
        }
        return [];
    } catch (error) {
        console.error('월별 통계 조회 실패:', error);
        return [];
    }
}

// 전역 네임스페이스로 노출 (브라우저 스크립트 환경)
window.ApiCommon = {
    loadDiaries,
    renderSummary,
    loadMonthlySchedules,
    loadMonthlyStatistics,
    createWalk,
    updateWalk,
    deleteWalk,
    createHospital,
    updateHospital,
    deleteHospital,
};