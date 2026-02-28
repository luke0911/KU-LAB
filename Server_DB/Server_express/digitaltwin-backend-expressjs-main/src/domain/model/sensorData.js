class SensorData {
    constructor({
      mapId,
      userId,
      acceleration, // { x, y, z }
      magnetic,     // { x, y, z }
      gyro,         // { x, y, z }
      linearAcceleration, // { x, y, z }
      pressure,
      rotation,     // { x, y, z }
      light,
      proximity,
      rf,
      stepLength,
      userStateReal     // 0, 1, 2, 3 중 하나
    }) {
      if (!userId || typeof userId === 'number') {
        throw new Error('Invalid or missing userId');
      }
  
      // Helper function to validate XYZ objects
      const validateXYZ = (value, fieldName) => {
        if (typeof value !== 'object' || value === null ||
            typeof value.x !== 'number' || typeof value.y !== 'number' || typeof value.z !== 'number') {
          throw new Error(`Invalid or missing XYZ values for ${fieldName}. Expected { x: double, y: double, z: double }.`);
        }
      };

      // Validate fields that now require { x, y, z }
      validateXYZ(acceleration, '가속도');
      validateXYZ(magnetic, '자기장');
      validateXYZ(gyro, '자이로');
      validateXYZ(linearAcceleration, '선형가속도');
      validateXYZ(rotation, '로테이션');

      // Validate fields that remain single numbers
      if (typeof mapId !== 'number') {
        throw new Error('Invalid or missing value for mapId');
      }

      if (typeof pressure !== 'number') {
        throw new Error('Invalid or missing value for 기압센서');
      }
      if (typeof light !== 'number') {
        throw new Error('Invalid or missing value for 조도센서');
      }
      if (typeof proximity !== 'number') {
        throw new Error('Invalid or missing value for 근접센서');
      }
      if (typeof rf !== 'number') {
        throw new Error('Invalid or missing value for RF값');
      }
      if (typeof stepLength !== 'number') {
        throw new Error('Invalid or missing value for stepLength');
      }

      // // statereal to userStateReal 변환 // 
      // const userStateMap = {
      //   0:	'기본파지',
      //   1:	'손에 들고 흔들기',
      //   2:	'바지앞주머니',
      //   22:	'자켓주머니',
      //   32:	'바지 뒷주머니',
      //   3:	'전화중걷기',
      //   4:	'기타',
      //   15:	'휴대중 정지',
      //   25:	'미소지 정지',
      //   35: '낙상'
      // };

      // if (typeof userStateReal !== 'number' || !(userStateReal in userStateMap)) {
      //   throw new Error(
      //     `Invalid or missing userStateReal. Must be one of [${Object.keys(userStateMap).join(', ')}]`
      //   );
      // }

      // userStateReal 값을 숫자 그대로 저장합니다.
      if (typeof userStateReal !== 'number') {
        throw new Error('userStateReal must be a number.');
      }
  
      this.mapId = mapId;
      this.userId = userId;
      this.acceleration = acceleration;
      this.magnetic = magnetic;
      this.gyro = gyro;
      this.linearAcceleration = linearAcceleration;
      this.pressure = pressure;
      this.rotation = rotation;
      this.light = light;
      this.proximity = proximity;
      this.rf = rf;
      this.stepLength = stepLength;
      // this.userStateReal = userStateMap[userStateReal]; // 문자열 변환 결과
      this.userStateReal = userStateReal; // 인테자 형 반환
      this.timestamp = SensorData.getKSTTimestamp();
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
        acceleration: this.acceleration, // 가속도 (x, y, z)
        magnetic: this.magnetic,         // 자기장 (x, y, z)
        gyro: this.gyro,                 // 자이로 (x, y, z)
        linearAcceleration: this.linearAcceleration, // 선형가속도 (x, y, z)
        pressure: this.pressure,         // 기압
        rotation: this.rotation,         // 로테이션(자이로+가속도 융합 회전 벡터) (x, y, z)
        light: this.light,               // 조도 센서
        proximity: this.proximity,       // 근접 센서
        rf: this.rf,                     // RF 값
        stepLength: this.stepLength,     // 보폭
        userStateReal: this.userStateReal,
        timestamp: this.timestamp
      }

      // 동적으로 붙은 필드만 추가 (undefined면 안 붙음)
      // User Profile 정보 (undefined/null 체크 후 추가)
      if (this.userName !== undefined) base.userName = this.userName;

      return base;
    }
  }
  
  module.exports = SensorData;
  
