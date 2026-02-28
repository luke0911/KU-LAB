# KU-LAB

다중 건물 실내 네비게이션 시스템을 위한 PDR(보행자 관성 항법) 알고리즘 및 바닥층 감지 기술 개발 프로젝트

## 개요

본 프로젝트는 Korea University 정보보호학과 연구실에서 진행하는 **실내 위치 추적 및 네비게이션 시스템** 연구입니다. IMU(관성 측정 장치) 센서 기반의 PDR 알고리즘, 계단 감지 기술, 그리고 다중 플랫폼 클라이언트-서버 인프라를 통해 건물 내에서 높은 정확도의 위치 추적을 구현합니다.

**핵심 개발 영역:**
- PDR 알고리즘 개선 및 검증
- IMU 센서 기반 바닥층 자동 감지
- Android 기반 실내 네비게이션 앱
- Unity 3D 시뮬레이션 환경
- Express.js 및 Spring Boot 백엔드 서버
- OpenSearch 기반 데이터 인덱싱

---

## 기술 스택

### 프로그래밍 언어
- **C++17** — PDR 시뮬레이션, IMU 센서 처리
- **Java / Kotlin** — Android 앱, Spring Boot 백엔드
- **JavaScript (Node.js)** — Express.js 서버
- **C#** — Unity 게임 엔진 시뮬레이션
- **Python** — 데이터 분석 및 시각화

### 핵심 라이브러리 & 프레임워크
- **OpenCV 4.x** — 신호 처리 및 시각화
- **CMake 3.14+** — C++ 빌드 관리
- **Spring Boot 3.4.5** — 엔터프라이즈 백엔드
- **Express.js 5.1.0** — 경량 API 서버
- **OpenSearch / Elasticsearch** — 센서 데이터 인덱싱
- **Redis** — 캐싱 및 세션 관리
- **Firebase Admin SDK** — 사용자 인증 및 알림
- **Unity 2023 LTS** — 3D 시뮬레이션

### 개발 도구
- Git, CMake, Gradle, Maven
- IntelliJ IDEA, Android Studio, Visual Studio Code
- Swagger/OpenAPI for API 문서화

---

## 프로젝트 구조

```
KU-LAB/
├── PDR_simulation/                      # PDR 알고리즘 시뮬레이터 (C++)
│   ├── main.cpp / viz.cpp               # 시뮬레이션 엔진 및 OpenCV 시각화
│   ├── src/                             # 개선된 PDR 알고리즘 (신규 버전)
│   ├── src_old/                         # 원본 Android PDR 알고리즘 (기존 버전)
│   ├── include/                         # 알고리즘 헤더 파일
│   └── data/                            # 센서 데이터 및 지도 파일
│
├── IMU_Floor_deteciton_simulation/      # 바닥층 감지 시뮬레이터 (C++)
│   ├── imu_floor_detection.h/cpp        # 계단/평지 감지 알고리즘
│   ├── main.cpp / viz.cpp               # 시뮬레이션 및 3개 윈도우 시각화
│   └── data/Floor_sim_data/             # 다양한 보행 패턴 센서 데이터
│
├── KoreaUniv/                           # 고려대 캠퍼스 Android 네비게이션 앱
│   ├── app/                             # Android 메인 앱 (Kotlin)
│   ├── maplocationlib/                  # 위치 추적 라이브러리
│   ├── PDR_EXT/                         # PDR 알고리즘 확장 모듈
│   └── unityLibrary/                    # Unity 연동 라이브러리
│
├── Daewoo_109/                          # 대우아파트 Unity 3D 시뮬레이션
│   ├── Assets/                          # 3D 모델, 씬, 스크립트
│   ├── ProjectSettings/                 # Unity 프로젝트 설정
│   └── Packages/                        # Unity 의존성 패키지
│
├── zeromap-daewoo-apartment-android/    # ZeroMap 아파트 버전 (Kotlin)
│   ├── app/                             # ZeroMap 앱 (IL2CPP 네이티브 지원)
│   ├── maplocationlib/                  # 실내맵 라이브러리
│   └── PDR_EXT/                         # PDR 모듈
│
├── Server_DB/                           # 백엔드 서버 인프라
│   ├── Server_express/                  # Express.js 서버
│   │   └── digitaltwin-backend-expressjs-main/
│   │       ├── src/server.js            # 메인 서버 파일
│   │       ├── package.json             # 의존성 (axios, redis, websocket 등)
│   │       └── routes/                  # API 라우트 정의
│   │
│   └── Server_spring/                   # Spring Boot 서버
│       └── digitaltwin-backend-springboot-main/
│           ├── src/main/java/          # Java 소스코드
│           ├── build.gradle            # Spring Boot 3.4.5 설정
│           └── resources/              # 설정 파일 (application.yml 등)
│
├── zeromap-korea_university_campus-android/  # ZeroMap 캠퍼스 버전
│   ├── app/                             # 고려대 캠퍼스 지도 앱
│   ├── maplocationlib/                  # 멀티 플로어 맵 라이브러리
│   └── PDR_EXT/                         # PDR 추적 모듈
│
├── TestApplication/                     # 테스트 및 검증 앱
│   ├── app/                             # 테스트 앱 (Kotlin)
│   └── build.gradle                     # 테스트 설정
│
├── ZeroMapRefactoring/                  # ZeroMap 리팩토링 (진행 중)
│   └── ...
│
├── PDR_simulation-Debug/                # PDR 시뮬레이터 디버그 버전
│
├── PDR data/                            # 센서 데이터 샘플
│
└── 무제/                                 # 대우 109 기본 프로젝트
    └── maplocationlib/                  # 초기 맵 라이브러리

```

### 주요 하위 프로젝트별 설명

#### 1. **PDR_simulation/** (C++ 시뮬레이터)
- **목적:** 개선된 PDR 알고리즘을 PC 환경에서 검증
- **기능:**
  - 듀얼 파이프라인: 신규 알고리즘 vs 기존 Android 원본 알고리즘 동시 실행
  - Step Detection, Step Length 추정
  - 3개 시각화 윈도우 (PDR Dashboard, WorldMap, Analysis)
  - 키보드 상호작용으로 경로 조정 및 분석
- **입출력:** 센서 데이터(TSV) → PDR 경로 시각화 + 분석 결과

#### 2. **IMU_Floor_deteciton_simulation/** (C++ 시뮬레이터)
- **목적:** 계단 감지 알고리즘 성능 검증
- **기능:**
  - Weighted Scoring FSM 기반 계단/평지 판정
  - 일반/살살/거친 보행 패턴 분류
  - 3개 분석 윈도우: 신호 파형, 산점도, 피처 점수
  - 배치 모드로 일괄 처리 가능
- **알고리즘:**
  - 신호 전처리: HPF/LPF, 좌표 변환
  - 피처 추출: stairScore, varYZRatio, dirR1, stepVarH
  - 커트라인 60점 이상 → "계단" 판정

#### 3. **KoreaUniv/** (Android 네비게이션)
- **목적:** 고려대 캠퍼스 실내 네비게이션
- **기술 스택:** Kotlin, Android Gradle, Dagger-Hilt, Room DB
- **주요 모듈:**
  - `maplocationlib`: 멀티 플로어 맵 렌더링
  - `PDR_EXT`: PDR 추적 모듈 (JNI로 C++ 알고리즘 호출)
  - `unityLibrary`: Unity AR 통합

#### 4. **Daewoo_109/** (Unity 3D 시뮬레이션)
- **목적:** 대우아파트 3D 가시화 및 시뮬레이션
- **기술:** Unity 2023 LTS, C# Burst 컴파일러
- **기능:** 건물 모델링, 경로 시뮬레이션, AR 통합

#### 5. **zeromap-daewoo-apartment-android/** (아파트 네비게이션)
- **목적:** 대우아파트 전용 네비게이션 앱
- **특징:** IL2CPP 네이티브 코드 지원 (고성능)
- **구조:** Spring Boot 서버와 연동

#### 6. **Server_DB/** (백엔드 서버)

**Express.js 서버** (`Server_express/`)
- **역할:** 경량 API 서버 (실시간 업데이트)
- **주요 의존성:**
  - Express 5.1.0
  - Redis 5.0.0 (캐싱)
  - OpenSearch 3.5.1 (센서 데이터 인덱싱)
  - WebSocket (실시간 통신)
  - Swagger UI (API 문서)
- **기능:** 센서 데이터 수집, 실시간 위치 추적, WebSocket 스트리밍

**Spring Boot 서버** (`Server_spring/`)
- **역할:** 엔터프라이즈 백엔드 (인증, DB 관리)
- **버전:** Spring Boot 3.4.5, Java 17
- **주요 의존성:**
  - Spring Data JPA (ORM)
  - Spring Security + JWT (인증)
  - MySQL (사용자 DB)
  - OpenSearch (데이터 검색)
  - Firebase Admin SDK (푸시 알림)
  - Swagger/OpenAPI (API 문서)
- **기능:** 사용자 관리, 지도 데이터 CRUD, 권한 관리

#### 7. **TestApplication/** (통합 테스트)
- **목적:** Android 시스템 통합 테스트
- **기능:** 센서 데이터 수집, PDR 검증, 맵 렌더링 테스트

---

## 주요 기능

### 1. PDR(Pedestrian Dead Reckoning) 알고리즘
- **Step Detection:** 가속도 신호에서 보행 스텝 자동 감지
- **Step Length 추정:** 개인 특성에 맞춘 동적 스텝 길이 계산
- **Heading Estimation:** 자이로 센서로 방위각 추정
- **누적 오차 보정:** 칼만 필터 및 적응형 필터링
- **점화 상태별 구분:** OnHand, InPocket, HandSwing 등 다양한 센서 배치 지원

### 2. 바닥층 감지 (IMU Floor Detection)
- **실시간 계단/평지 판정:** Weighted Scoring FSM
- **보행 패턴 인식:** 일반/살살/거친 걸음 구분
- **사용자 맞춤:** 개인의 걷기 특성에 적응
- **높은 정확도:** Cohen's d 분석으로 세 패턴 모두에서 유효성 입증

### 3. 다중 건물 네비게이션
- **멀티 플로어 지원:** 바닥층 자동 감지로 층수 추적
- **고려대 캠퍼스 & 대우 아파트:** 두 가지 환경 지원
- **오프라인 맵:** JSON 기반 벡터 맵 (실시간 업데이트 가능)
- **경로 계획:** Dijkstra 알고리즘 기반 최단 경로 제공

### 4. 실시간 데이터 처리
- **OpenSearch 인덱싱:** 센서 데이터 고속 검색
- **Redis 캐싱:** 자주 접근하는 맵 데이터 캐시
- **WebSocket 스트리밍:** 실시간 위치 업데이트
- **JWT 인증:** 보안 API 엔드포인트

### 5. 시뮬레이션 & 검증
- **PDR 시뮬레이터:** 새로운 알고리즘 빠른 검증
- **Floor Detection 시뮬레이터:** 다양한 보행 데이터로 성능 평가
- **OpenCV 시각화:** 신호 파형, 경로, 점수 실시간 표시
- **배치 분석:** 전체 데이터셋에 대한 일괄 처리 및 통계

### 6. 크로스 플랫폼 지원
- **Android:** Kotlin + C++ NDK (고성능 알고리즘)
- **Desktop:** C++ CLI (PDR/Floor Detection 검증)
- **Web:** REST API (Express/Spring 서버)
- **3D 시뮬레이션:** Unity (AR 통합 가능)

---

## 빌드 및 실행

### PDR Simulation 빌드
```bash
cd PDR_simulation
mkdir -p build && cd build
cmake ..
make -j$(nproc)
./pdr_sim
```

### IMU Floor Detection 빌드
```bash
cd IMU_Floor_deteciton_simulation
mkdir -p build && cd build
cmake ..
make -j$(nproc)
./floor_sim ../data/Floor_sim_data/Floor_sim_data_sample.txt
```

### Android 앱 빌드 (KoreaUniv)
```bash
cd KoreaUniv
./gradlew assembleDebug
# 또는 Android Studio에서 빌드
```

### Express.js 서버 실행
```bash
cd Server_DB/Server_express/digitaltwin-backend-expressjs-main
npm install
npm start
```

### Spring Boot 서버 실행
```bash
cd Server_DB/Server_spring/digitaltwin-backend-springboot-main
./gradlew bootRun
```

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────┐
│         Android Client Apps              │
│  ┌──────────────────────────────────┐   │
│  │ KoreaUniv / ZeroMap / TestApp     │   │
│  │ (Kotlin + C++ NDK + Unity AR)     │   │
│  └──────────────────────────────────┘   │
└────────────────┬────────────────────────┘
                 │ RESTful API + WebSocket
     ┌───────────┴────────────┐
     │                        │
┌────▼──────────────┐  ┌──────▼─────────────┐
│  Express.js       │  │  Spring Boot       │
│  (Real-time API)  │  │  (Core Services)   │
│  • WebSocket      │  │  • JWT Auth        │
│  • Redis Cache    │  │  • JPA ORM         │
│  • Swagger UI     │  │  • Firebase        │
└────┬──────────────┘  └──────┬─────────────┘
     │                        │
     └────────────┬───────────┘
                  │
     ┌────────────┴────────────┐
     │                         │
┌────▼───────────┐  ┌──────────▼──────┐
│  OpenSearch    │  │  MySQL / Redis   │
│  (Sensor Data) │  │  (Persistent DB) │
└────────────────┘  └──────────────────┘
```

---

## 참고 자료

- **PDR 알고리즘 개선 보고서:** `/PDR_simulation/PDR_알고리즘_개선_보고서.md`
- **시뮬레이터 사용 설명서:** 각 프로젝트의 `README.md`
- **API 문서:** `/Server_DB/Server_express` 및 `/Server_spring` Swagger 엔드포인트
- **백엔드 배포 가이드:** `/Server_DB/백엔드 배포 가이드.pdf`

---

## 라이선스 및 기여

본 프로젝트는 Korea University IPLab 연구실 내부 프로젝트입니다.
각 서브프로젝트별 라이선스는 해당 디렉토리의 LICENSE 파일을 참고하세요.

### 관련 Repository
- Express.js 서버: https://github.com/KU-IPLab/digitaltwin-backend-expressjs
- Spring Boot 서버: https://github.com/KU-IPLab/digitaltwin-backend-springboot
- ZeroMap Android: https://github.com/anthropics/zeromap-korea_university_campus-android

---

**마지막 업데이트:** 2026-02-28
**주요 개발자:** Korea University IPLab
