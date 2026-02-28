# 🛰️ 디지털 트윈 백엔드 API 문서

Spring Boot 기반 디지털 트윈 백엔드 프로젝트의 RESTful API 명세입니다.
- Project Base URL : `/spring`

---

## 📁 API 목차

- [👤 User API](#-user-api)
- [🗺️ Map API](#-맵-생성)
- [📍 POI API](#-poi-api)

---

## 👤 User API

### 🔸 사용자 생성

- **URL**: `POST /spring/api/users`
- **요약**: 사용자 계정 생성
- **요청 필드 요약 (UserCreateReqDto)**

| 필드명     | 설명         | 필수 여부 | 유효성 조건                  |
|------------|--------------|-----------|------------------------------|
| orgCode    | 조직 코드     | 선택      | 숫자                         |
| accountId  | 계정 ID       | 필수      | 고유 문자열                  |
| password   | 비밀번호      | 필수      | 9~30자, 영대/소/숫자/특수문자 중 3종류 이상 |
| name       | 사용자 이름   | 필수      | 문자열                       |
| birthdate  | 생년월일      | 선택      | yyyy-MM-dd 형식              |
| role       | 사용자 역할   | 필수      | USER/ADMIN                   |
| adminCode  | 관리자 코드   | 선택      | 문자열                       |

---

### 🔸 사용자 정보 조회

- **URL**: `GET /spring/api/users/{userId}`
- **요약**: userId로 사용자 상세 조회

---

### 🔸 사용자 정보 수정

- **URL**: `PUT /spring/api/users`
- **헤더**: `X-USER-ID: {userId}`
- **요약**: 사용자 비밀번호 등 정보 수정
- **요청 필드 요약 (UserUpdateReqDto)**

| 필드명     | 설명         | 필수 여부 | 유효성 조건                  |
|------------|--------------|-----------|------------------------------|
| orgCode    | 조직 코드     | 선택      | 숫자                         |
| userId     | 사용자 ID     | 선택      | 숫자                         |
| password   | 새 비밀번호   | 필수      | 9~30자, 영대/소/숫자/특수문자 중 3종류 이상 |

---

### 🔸 사용자 탈퇴

- **URL**: `DELETE /spring/api/users/{targetUserId}`
- **헤더**: `X-USER-ID: {requestUserId}`
- **요약**: 본인 또는 관리자에 의한 사용자 탈퇴

---

### 🔸 로그인

- **URL**: `POST /spring/api/users/login`
- **요약**: 사용자 로그인 처리

---

### 🔸 ID 중복 체크

- **URL**: `GET /spring/api/users/idCheck?accountId={id}`
- **요약**: 계정 ID 중복 여부 확인

---

### 🔐 비밀번호 정책

- 9자 이상 30자 이하
- 영대문자, 영소문자, 숫자, 특수문자 중 3종류 이상 포함

---

## 🗺️ Map API

### 🔸 맵 생성

- **URL**: `POST /spring/api/maps`
- **헤더**: `X-USER-ID: {userId}`
- **요약**: 사용자 맵 생성
- **요청 필드 요약 (MapCreateReqDto)**

| 필드명   | 설명       | 필수 여부 | 유효성 조건                  |
|----------|------------|-----------|------------------------------|
| mapName  | 맵 이름     | 필수      | 최대 50자                    |

---

### 🔸 맵 정보 조회

- **URL**: `GET /spring/api/maps/{mapId}`
- **요약**: 맵 ID로 상세 정보 조회

---

### 🔸 맵 삭제

- **URL**: `DELETE /spring/api/maps/{mapId}`
- **헤더**: `X-USER-ID: {userId}`
- **요약**: 사용자 맵 삭제

---

## 📍 POI API

### 🔸 POI 생성

- **URL**: `POST /spring/api/pois`
- **헤더**: `X-USER-ID: {userId}`
- **요약**: POI 생성
- **요청 필드 요약 (PoiCreateReqDto)**

| 필드명           | 설명           | 필수 여부 | 유효성 조건            |
|------------------|----------------|-----------|------------------------|
| poiName          | POI 이름       | 필수      | 최대 50자              |
| mapId            | 맵 ID          | 필수      | 숫자                   |
| poiFloor         | 층수           | 선택      | 숫자                   |
| poiPoints        | 경계 좌표 배열 | 필수      | Point 리스트           |
| poiCategory      | 카테고리       | 선택      | ENUM (POI 카테고리)    |
| poiDescription   | 설명           | 선택      | 문자열                 |

---

### 🔸 POI 조회

- **URL**: `GET /spring/api/pois/{poiId}`
- **요약**: POI ID로 상세 정보 조회

---

### 🔸 POI 수정

- **URL**: `PUT /spring/api/pois`
- **헤더**: `X-USER-ID: {userId}`
- **요약**: POI 정보 수정
- **요청 필드 요약 (PoiUpdateReqDto)**

| 필드명           | 설명           | 필수 여부 | 유효성 조건           |
|------------------|----------------|-----------|-----------------------|
| poiId            | POI ID         | 필수      | 숫자                  |
| poiName          | POI 이름       | 필수      | 최대 50자             |
| poiFloor         | 층수           | 선택      | 숫자                  |
| poiPoints        | 경계 좌표 배열 | 선택      | Point 리스트          |
| poiCategory      | 카테고리       | 선택      | ENUM (POI 카테고리)   |
| poiDescription   | 설명           | 선택      | 문자열                |

---

### 🔸 POI 삭제

- **URL**: `DELETE /spring/api/pois/{poiId}`
- **헤더**: `X-USER-ID: {userId}`
- **요약**: 특정 POI 삭제

---

### 🔸 POI 검색

- **URL**: `GET /spring/api/pois/search`
- **요약**: POI 키워드 기반 검색
- **Query Parameters**

| 파라미터명 | 설명           | 필수 여부 | 예시 값                            |
|------------|----------------|-----------|---------------------------------|
| keyword    | 검색어         | 선택      | "스타벅스", "올리브영" 등                |
| mapId      | 맵 ID          | 필수      | 숫자                              |
| pageSize   | 페이지 크기    | 선택      | 10, 30, 100                     |
| page       | 페이지 번호    | 선택      | 1, 2, 3 ...                     |
| sort       | 정렬 기준      | 선택      | CREATED_AT(default), UPDATED_AT |
| direction  | 정렬 방향      | 선택      | DESC(default), ASC              |

---

## 🧱 공통 응답 형식 예시

```json
{
  "statusCode": 200,
  "data": { ... },
  "message": "메시지"
}
```

---

## ✅ 보안 관련 주의사항

- 민감 작업은 `X-USER-ID` 헤더를 통해 인증된 사용자만 수행할 수 있습니다.
- 향후 Spring Security 등 인증 도입 예정

---


