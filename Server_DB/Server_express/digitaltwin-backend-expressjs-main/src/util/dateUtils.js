/**
 * ISO Week 및 연도 계산 (패키지 설치 없이 사용 가능)
 * @param {Date | string} date
 * @returns {{ year: number, week: string }}
 */
function getISOWeekAndYear(date) {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
  
    d.setDate(d.getDate() + 3 - ((d.getDay() + 6) % 7));
    const week1 = new Date(d.getFullYear(), 0, 4);
    week1.setDate(week1.getDate() + 3 - ((week1.getDay() + 6) % 7));
  
    const weekNumber = 1 + Math.round(((d - week1) / 86400000 - 3 + ((week1.getDay() + 6) % 7)) / 7);
    const year = d.getFullYear();
  
    return {
      year,
      week: weekNumber.toString().padStart(2, '0')
    };
  }
  
  /**
   * 주어진 기간 내 ISO 주차별 인덱스 목록 반환
   * @param {string} fromISO - 시작일 (ISO 문자열)
   * @param {string} toISO - 종료일 (ISO 문자열)
   * @param {string} prefix - 인덱스 접두어 (예시: 'user_locations')
   * @returns {string[]} - 인덱스 이름 배열
   */
  function getWeeksInRange(fromISO, toISO, prefix) {
    const from = new Date(fromISO);
    const to = new Date(toISO);
    const indexNames = new Set();
  
    let current = new Date(from);
    while (current <= to) {
      const { year, week } = getISOWeekAndYear(current);
      indexNames.add(`${prefix}_${year}-${week}`);
      current.setDate(current.getDate() + 7);
    }
  
    return Array.from(indexNames);
  }
  
  module.exports = {
    getISOWeekAndYear,
    getWeeksInRange,
  };