class EventAreaStay {
    constructor({ mapId, userId, eventAreaId, enterTime, exitTime, stayDuration, timestamp }) {
      // 필수값 검증
      if (typeof mapId !== 'number') throw new Error('Invalid or missing mapId');
      if (typeof eventAreaId !== 'number') throw new Error('Invalid or missing eventAreaId');
      if (!userId || typeof userId !== 'string') throw new Error('Invalid or missing userId');
      if (!enterTime) throw new Error('Missing enterTime');
      if (!exitTime) throw new Error('Missing exitTime');
  
      // 날짜 파싱
      this.enterTime = EventAreaStay.parseDate(enterTime);
      this.exitTime = EventAreaStay.parseDate(exitTime);
  
      // 체류 시간 계산 (초 단위)
      this.stayDuration =
        typeof stayDuration === 'number'
          ? stayDuration
          : (this.exitTime.getTime() - this.enterTime.getTime()) / 1000;
  
      if (this.stayDuration < 0)
        throw new Error('stayDuration cannot be negative');
  
      this.mapId = mapId;
      this.userId = userId;
      this.eventAreaId = eventAreaId;
  
      // 기록 시점 (없으면 KST now)
      this.timestamp = timestamp
        ? EventAreaStay.parseDate(timestamp).toISOString()
        : EventAreaStay.getKSTTimestamp();
    }
  
    static parseDate(val) {
      if (val instanceof Date) return val;
      const d = new Date(val);
      if (isNaN(d.getTime())) throw new Error(`Invalid date: ${val}`);
      return d;
    }
  
    static getKSTTimestamp() {
      const now = new Date();
      const kst = new Date(now.getTime() + 9 * 60 * 60 * 1000);
      return kst.toISOString();
    }
  
    toJSON() {
      return {
        mapId: this.mapId,
        userId: this.userId,
        eventAreaId: this.eventAreaId,
        enterTime: this.enterTime.toISOString(),
        exitTime: this.exitTime.toISOString(),
        stayDuration: this.stayDuration,
        timestamp: this.timestamp,
      };
    }
  
    /**
     * 정적 팩토리 (추천) - 이전 체류 로그, 현재 시간 기준 생성
     */
    static fromEventAreaChange({ mapId, userId, prevEventArea, prevEventAreaEnterTime, curTime }) {
      return new EventAreaStay({
        mapId,
        userId,
        eventAreaId: prevEventArea,
        enterTime: prevEventAreaEnterTime,
        exitTime: curTime,
      });
    }
  }
  
  module.exports = EventAreaStay;