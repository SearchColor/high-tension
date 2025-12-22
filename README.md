# 🛍️ 온라인 쇼핑 플랫폼 - HIGH TENSION
<img src="https://github.com/user-attachments/assets/3735ebcf-4fee-46a2-be2f-03271c7814b2" alt="high-tension" width="100%" />


<br>
<br>

# 🛒 Project Overview (프로젝트 개요)
프로젝트 개요 / 설명

## 📝 사용자 주요 기능
- 주요 기능 간단하게
- 주요 기능 간단하게

## 🗓️ 개발 기간
2025.xx.xx ~ 2025.xx.xx

</br>

# ⚒️ Technology Stack (기술 스택)

- 필요한 뱃지 정리 필요

![Java](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.5.8-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-228B22?style=for-the-badge&logo=java&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

![Kafka](https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![MySql](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)


<br>
<br>

# ⭐ Team Members (팀원)
| name | role |  담당 파트   |                                                                       Github                                                                        |
|:----:|:----:|:--------:|:---------------------------------------------------------------------------------------------------------------------------------------------------:|
| 박용재  |  팀장  | 담당파트 | <a href="https://github.com/SearchColor"><img src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white"></a> |
| 박근용  |  멤버  | 담당파트 | <a href="https://github.com/GoodNyong"><img src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white"></a>    |
| 박진성  |  멤버  |  담당파트 |  <a href="https://github.com/Sp-PJS"><img src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white"></a>  |
| 정민지  |  멤버  |  담당파트 |  <a href="https://github.com/6uiwj"><img src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white"></a>  |
| 안소나  |  멤버  |  담당파트 |  <a href="https://github.com/sonaanweb"><img src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white"></a>   |
| 김현수  |  멤버  |  담당파트 |  <a href="https://github.com/kinhyunsu"><img src="https://img.shields.io/badge/Github-181717?style=for-the-badge&logo=Github&logoColor=white"></a>    |

<br>
<br>

# 🗝️ Key Features (주요 기능)

기존 내용은 수정하여 작성해주세요 (어떤 내용을 포함하고, 어떤 서술식으로 할 지 논의 필요)

- **상품**
    - MASTER, HUB_MANAGER, COMPANY_MANAGER 권한 사용자는 상품 등록, 수정, 조회 기능을 사용할 수 있으며, 삭제는 COMPANY_MANAGER를 제외한 권한에서만 가능합니다.
        - 상품 조회 시 상품명을 기준으로 검색을 지원합니다.
        - 필수 속성 검증을 통해 상품 등록 시 데이터 정합성을 보장합니다.
    - 재고 연동
        - 주문 서비스와의 연동을 위해 재고 차감 및 증가 기능을 제공하는 internal API를 구성하여 안정적이고 일관성 있는 재고 관리를 지원하도록 구현하였습니다.

- **배송**
    - 배송 생성: 주문 생성 `Kafka 이벤트`를 구독하여 배송 정보를 자동으로 생성합니다.
        - (출발/도착 허브 정보, 수령자 정보를 함께 저장하며 기본 상태는 허브 대기(WAITING_AT_HUB)로 설정됩니다.)
    - 배송 담당자 관리
        - 허브 별 배송 담당자 등록 및 조회 기능을 제공합니다. 배송 상태가 허브 대기일 때만 담당자 배정이 가능합니다.
    - 배송 상태 흐름은 허브 대기 -> 허브 이동 -> 목적지 도착 -> 배송 중 -> 완료로 이루어지며, 잘못된 상태 변경, 또는 비정상 요청 시 표준화된 예외를 반환합니다.

- **알림 서비스**
    - AI 자연어 분석을 통해 출발 시간을 계산하고, Gemini API를 이용해 사용자에게 도착 기한, 출발/목적지 허브 정보 기반으로 최적의 출발 시간을 산출하여 제공합니다.
    - 알림 발송
        - 자동 및 수동 알림 발송 지원, REST API, Kafka 이벤트를 통한 발송 처리
        - `Kafka`: 주문 생성 및 배송 상태 변경 이벤트를 구독하며 멱등성 보장 구현
    - 안정성 보장
        - FeignClient Fallback을 적용해 user-service 연동 시 발생 가능한 NPE 위험 제거
        - Circuit Breaker와 Fallback 패턴을 적용해 외부 API 장애 시 서비스 안정성을 확보하고, 스냅샷 패턴으로 발송 시점의 발신자 정보를 저장해 데이터 정합성 보장
    - 조회: 발송된 알림(Slack/Gemini API)의 상세 조회를 포함해 페이징, 필터링하여 조회할 수 있습니다.
    - 통계 및 모니터링: 알림 통계(일 별/기간 별 발송 성공, 실패율), API 통계(성공률, 평균 응답시간, 총 비용)

<br>
<br>

# 🌐 ERD 및 시스템 아키텍처

수정 필요 - ERD / 아키텍처가 최종본인지 확인 필요합니다. 아래는 다른 프로젝트 이미지 샘플입니다

<img src="https://github.com/user-attachments/assets/8655eac9-07ce-42a3-9f91-01082bb34747" alt="ERD" width="100%" />
<img src="https://github.com/user-attachments/assets/d70e94f2-dd90-490e-a9e1-f0a8f2aee5f1" alt="Archi" width="100%" />


# 📂 Project Structure (프로젝트 구조)
```plaintext
각 서비스 모듈은 4계층 레이어드 아키텍처를 기본으로 합니다.
생성 시 추가
```

<br>
<br>

# ✈️ Development Workflow (개발 워크플로우)

## 브랜치 전략, 커밋 컨벤션
Git Flow를 기반으로 하며, 다음과 같은 브랜치를 사용합니다.

- **Branch**
    - **전략**

      | Branch Type | Description                                       |
            |-------------|---------------------------------------------------|
      | `dev`       | 주요 개발 branch, `main`으로 merge 전 거치는 branch |
      | `feature`   | 각자 개발할 branch, 기능 단위로 생성하기, 할 일 issue 등록 후 branch 생성 및 작업 |

    - **네이밍**
        - `{header}/#{issue number}`
        - 예) `feat/#issueNumr`


- **커밋 메시지 규칙**
    ```bash
    > type: 기능 요약

      - chore: 내부 파일 수정
      - feat: 새로운 기능 구현
      - add: feat 이외의 부수적인 코드 추가, 라이브러리 추가, 새로운 파일 생성 시
      - fix: 코드 수정, 버그, 오류 해결
      - del: 쓸모없는 코드 삭제
      - docs: README나 WIKI 등의 문서 개정
      - move: 프로젝트 내 파일이나 코드의 이동
      - rename: 파일 이름의 변경
      - merge: 다른 브랜치를 merge하는 경우
      - style: 코드가 아닌 스타일 변경을 하는 경우
      - init: Initial commit을 하는 경우
      - refactor: 로직은 변경 없는 클린 코드를 위한 코드 수정

      ex) feat: 게시글 목록 조회 API 구현
    ```

<br>
<br>

# 📄 기술 문서
[API 명세서 보기](https://teamsparta.notion.site/2b52dc3ef51481c9a79be84dbfdadcff?v=2b52dc3ef51480c1b562000c3d687efe)
<br>
[테이블 명세서 보기](https://www.notion.so/teamsparta/2b62dc3ef5148036b008e0eb4db77d0d)
