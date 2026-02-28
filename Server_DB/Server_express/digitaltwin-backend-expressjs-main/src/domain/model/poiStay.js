class PoiStay {
    constructor({ mapId, userId, poiId, enterTime, exitTime, stayDuration, timestamp }) {
      // 필수값 검증
      if (typeof mapId !== 'number') throw new Error('Invalid or missing mapId');
      if (typeof poiId !== 'number') throw new Error('Invalid or missing poiId');
      if (!userId || typeof userId !== 'string') throw new Error('Invalid or missing userId');
      if (!enterTime) throw new Error('Missing enterTime');
      if (!exitTime) throw new Error('Missing exitTime');
  
      // 날짜 파싱
      this.enterTime = PoiStay.parseDate(enterTime);
      this.exitTime = PoiStay.parseDate(exitTime);
  
      // 체류 시간 계산 (초 단위)
      this.stayDuration =
        typeof stayDuration === 'number'
          ? stayDuration
          : (this.exitTime.getTime() - this.enterTime.getTime()) / 1000;
  
      if (this.stayDuration < 0)
        throw new Error('stayDuration cannot be negative');
  
      this.mapId = mapId;
      this.userId = userId;
      this.poiId = poiId;
  
      // 기록 시점 (없으면 KST now)
      this.timestamp = timestamp
        ? PoiStay.parseDate(timestamp).toISOString()
        : PoiStay.getKSTTimestamp();
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
        poiId: this.poiId,
        enterTime: this.enterTime.toISOString(),
        exitTime: this.exitTime.toISOString(),
        stayDuration: this.stayDuration,
        timestamp: this.timestamp,
      };
    }
  
    /**
     * 정적 팩토리 (추천) - 이전 체류 로그, 현재 시간 기준 생성
     */
    static fromPoiChange({ mapId, userId, prevPoi, prevPoiEnterTime, curTime }) {
      return new PoiStay({
        mapId,
        userId,
        poiId: prevPoi,
        enterTime: prevPoiEnterTime,
        exitTime: curTime,
      });
    }
  }
  
  module.exports = PoiStay;