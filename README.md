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


## 주요 기능

## 도메인

## API


