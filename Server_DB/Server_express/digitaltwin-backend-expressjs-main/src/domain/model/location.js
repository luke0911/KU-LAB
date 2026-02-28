class Location {
    constructor({ mapId, userId, userX, userY, userZ, userDirection, userFloor, userStatus, background }) {

      if (!userId || typeof userId === 'number') {
        throw new Error('Invalid or missing userId');
      }

      const requiredNumberFields = {
        mapId,
        userX,
        userY,
        userZ,
        userDirection,
        userFloor
      };

      for (const [key, value] of Object.entries(requiredNumberFields)) {
        if (typeof value !== 'number') {
          throw new Error(`Invalid or missing value for ${key}`);
        }
      }

      if (userDirection < 0 || userDirection > 360) {
        throw new Error('Direction must be between 0 and 360');
      }

      if (userStatus !== 'Active' && userStatus !== 'Inactive') {
        throw new Error('User status must be Active or Inactive');
      }

      if (background !== undefined && typeof background !== 'boolean') {
        throw new Error('background must be a boolean value');
      }

      this.mapId = mapId;
      this.userId = userId;
      this.userX = userX;
      this.userY = userY;
      this.userZ = userZ;
      this.userDirection = userDirection;
      this.userFloor = userFloor;
      this.userStatus = userStatus;
      this.background = background;
      this.timestamp = Location.getKSTTimestamp();
    }

    static getKSTTimestamp() {
        const now = new Date();
        const kst = new Date(now.getTime() + 9 * 60 * 60 * 1000);
        return kst.toISOString();
    }
  
    toJSON() {
      const base = {
        mapId: this.mapId,
        userId: this.userId,
        userX: this.userX,
        userY: this.userY,
        userZ: this.userZ,
        userDirection: this.userDirection,
        userFloor: this.userFloor,
        userStatus: this.userStatus,
        timestamp: this.timestamp,
      };

      // 동적으로 붙은 필드만 추가 (undefined면 안 붙음)
      // POI
      if (this.matchedPoi !== undefined) base.matchedPoi = this.matchedPoi;
      if (this.poiEnterTime !== undefined) base.poiEnterTime = this.poiEnterTime;

      // Event Area
      if (this.matchedEventArea !== undefined) base.matchedEventArea = this.matchedEventArea;
      if (this.eventAreaEnterTime !== undefined) base.eventAreaEnterTime = this.eventAreaEnterTime;

      // User Profile 정보 (undefined/null 체크 후 추가)
      if (this.userName !== undefined) base.userName = this.userName;
      if (this.ageRange !== undefined) base.ageRange = this.ageRange;
      if (this.gender !== undefined) base.gender = this.gender;

      // Background 필드
      if (this.background !== undefined) base.background = this.background;

      return base;
    }
  }
  
  module.exports = Location;
  