// src/domain/model/workerInfo.js

class WorkerInfo {
  constructor({
    userId, // UUID, 필수
    entryTime, // 출입 시간
    exitTime, // 외출/퇴근 시간
    workDetails, // 작업 내용
    position, // 직책
  }) {
    if (!userId) {
      throw new Error('Invalid or missing userId');
    }

    this.userId = userId;
    this.entryTime = entryTime || null;
    this.exitTime = exitTime || null;
    this.workDetails = workDetails || null;
    this.position = position || null;
    this.lastUpdated = new Date().toISOString();
  }

  toJSON() {
    return {
      userId: this.userId,
      entryTime: this.entryTime,
      exitTime: this.exitTime,
      workDetails: this.workDetails,
      position: this.position,
      lastUpdated: this.lastUpdated,
    };
  }
}

module.exports = WorkerInfo;