# Spring WebFlux Reactive Chat

## 개요 
- 이 프로젝트는 Spring WebFlux 기반의 실시간 채팅 시스템입니다
- 1:1대화와 그룹 채팅을 모두 지원하며, 사용자는 대화방을 생성하고 친구 혹은 친구코드를 통해 다른 사용자를 초대할 수 있습니다
- 핵심 목표는 완전 비동기/논블로킹 아키텍처입니다
- 본 프로젝트는 MVP(Minimum Viable Product) 수준의 채팅 서비스 구축을 통해 Spring WebFlux의 리액티브 프로그래밍 패턴을 학습하는 것을 목적으로 합니다

## 기술 스택
### Backend
- Spring Boot
  - Spring webFlux - 논블로킹 HTTP & WebSocket 
  - Spring Security + JWT - 인증/인가 처리
  - Spring Validation - 요청 데이터 검증
  - Lombok
- R2DBC + PostgreSQL - 비동기/리액티브 RDB 접근
- Spring Data Redis Reactive - Presence/Typing 상태 관리, 멀티노드 브로드캐스트
### Infrastructure
- PostgreSQL(RDS) - 메시지 및 사용자/채팅방 데이터 영속화
- Redis - Pub/Sub, 캐싱, 세션 상태
- Docker & Docker Compose - 로컬 개발 환경
- Gradle - 빌드 및 의존성 관리


## 주요 기능[



## 각 도메인에 존재하는 규칙
1. User
- friendCode: 대문자 알파벳 4자리 + 숫자 4자리 (예: QWER1234), unique 제약

2. FriendRequest
- 상태(FriendshipStatus)로 PENDING(요청보냈지만 받는쪽에서 아직 수락,거절을 안한상태)/ACCEPTED/REJECTED 가 존재
- 친구요청은 단방향
- 중복 요청 불가
- 이미 친구인 상태면 요청 불가
- 친구요청받은 사람이 수락시 -> 자동으로 Friendship 생성 / 거절시 -> 상태만 REJECTED

3. FriendShip
- 친구관계는 양방향
- 한쪽이 친구 삭제하면 반대쪽도 동시에 삭제됨

4. BlockList[TODO]
- 차단은 단방향(A가 B를 차단해도 B는 그 사실을 모름)

5. ChatRoom
- 채팅방 종류는 PRIVATE(1:1)/GROUP(2명이상)
- PRIVATE 채팅방 규칙
  - 반드시 서로 친구상태여야만 생성 가능
  - 이미 A와 B가 있는 PRIVATE 채팅방이 있을 경우 새로 생성하지않고 기존 방 재사용
  - 한쪽이 나간 경우, 아직 안 나간 다른 한쪽은 계속 방을 유지
  - 한쪽이 나간 경우, 둘 중 하나가 새 메시지 보내면 새 방 생성 (기존 방 재활용하지 않음)

- GROUP 채팅방 규칙 
  - 기본 최대 인원: 50명
  - 생성시, 초대한 사람 + 초대된 사람들 모두 추가
  - 기존에 PRIVATE방에서 멤버 추가시, 새로운 GROUP방 생성
  - 그룹 방은 나갔다가 다시 초대되면 기존 방에 재합류 (이전 기록 유지)

- 채팅방 이름 규칙
  - PRIVATE 채팅방: DB에 name 저장하지 않고 동적으로 표시, 내 기준으로 상대방 닉네임이 방이름으로 보임
  - GROUP 채팅방: 생성 시 멤버들의 닉네임 나열(예: 유저A,유저B,유저C). 추후의 채팅방 이름 변경은 현재 지원하지 않음[TODO]

6. ChatRoomMember
- isActive 상태: 현재 방에 참여 중인지 아닌지를 나타내는 boolean

7. ChatMessage
- 타입: TEXT/ IMAGE/ SYSTEM(방 생성, 초대, 퇴장 등의 시스템 메시지)
- 조회규칙
  - 방 입장 시, 최신 메시지 N개 조회
  - 무한 스크롤: 기준 시각(beforeTime) 이전 메시지
  - 채팅방목록 화면에서 방 미리보기로 최근 메시지 1개 보는 것 가능
- 읽음처리(읽지 않은 메시지 수)[TODO]


## API


