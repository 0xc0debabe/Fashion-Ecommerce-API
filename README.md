# 👔 Fashion-Ecommerce-API
패션 제품을 관리하고, 사용자들이 온라인에서 쇼핑할 수 있도록 지원하는 RESTful API입니다.  
이 API는 제품 검색, 카트 관리, 주문 처리 등 다양한 기능을 제공합니다.

## 📆 프로젝트 기간
24.10.14 ~ 24.11.18

## 🛠️ 기술 스택
- **Language** : JAVA
- **Framework** : Spring Boot
- **Build** : GRADLE
- **Database** : MySql
- **JDK** : OpenJDK-17
- **Cache** : Redis
- **Authentication** : JWT
- **Persistence & Querying** : Spring Data JPA, QueryDSL

## 📢 주요 기능 설명

### 📌 Member
- **회원가입 기능**
    - 해당 로그인 아이디가 중복되지 않을 경우 가입 가능
    - 사용자는 로그인, Id, 비밀번호, 주소 등으로 회원가입 가능
    - 비밀번호는 BCryptPasswordEncoder 암호화 되어서 저장됨
    - 역할로는 '멤버'와 '셀러'로 구성됨
    
- **로그아웃 기능**
    - 사용자에게 로그아웃 기능을 제공
    - 로그아웃 시, 해당 JWT 토큰 무효화
    
- **중복체크 기능**
    - 로그인 ID가 중복되었는지 체크 기능
    
- **이메일 인증 처리**
    - 회원가입 후, 이메일 인증을 하면 랜덤으로 구성된 6자리 UUID를 해당 이메일로 메일 발송
    - 유저가 해당 이메일에 온 6자리 UUID를 입력할 경우 이메일 인증 완료

### 📌 Email
- **이메일 인증 코드 전송**
    - 사용자가 이메일 인증 요청을 하면, 해당 이메일 주소로 랜덤으로 구성된 6자리 UUID 인증 코드를 발송
      
### 📌 Item
- **상품 등록**
    - '셀러' 회원만이 상품 등록 가능
    
- **상품 목록 조회** (메인 페이지)
    - 누구나 조회 가능
    - 메인 페이지에 표시할 상품 목록을 조회
    - 매주 월요일 오전 6시에 조회수 Top 15 아이템을 책정한 뒤, 새로 등록된 상품 15개와 메인 페이지에 조회
    - 상세 페이지에 접근 시, 조회수 랭킹에 +1점을 부여하여 책정(쿠키를 이용해 재방문 중복 제거)
    
- **상품 상세 조회**
    - 누구나 조회 가능
    - 특정 상품의 상세 정보를 조회
    
- **상품 삭제**
    - 해당 아이템 등록자와 삭제하는 사람이 일치하는 지 확인 후, 상품을 삭제
      
- **상품 수정**
    - 해당 아이템 등록자와 수정하는 사람이 일치하는 지 확인 후, 상품을 수정
      
- **상품 검색 (카테고리, 타입)**
    - 상품을 카테고리 및 타입을 기준으로 동적쿼리를 이용한 검색
    - Ex) [카테고리]상의, [타입]맨투맨으로 검색 시 상의와 맨투맨에 맞는 아이템 최신순으로 조회
    - Ex) [카테고리]상의, [타입]Null 검색 시, 상의에 맞는 아이템 최신순으로 조회

### 📌 Cart(유저 이탈을 방지하기 위해 로그인을 하지 않아도 장바구니에 등록되게끔 구현)
- **장바구니에 아이템 추가**
    - 비로그인 상태 : 쿠키에 암호화 해서 장바구니 저장
    - 로그인 상태 : 최초 로그인 시, 쿠키에 있던 암호화 된 장바구니를 복호화해서 레디스에 저장
    
- **장바구니 아이템 조회**
    - 비로그인 상태 : 쿠키에 암호화 된 장바구니를 복호화해서 조회
    - 로그인 상태 : 최초 로그인 시, 쿠키에 있던 암호화 된 장바구니를 복호화해서 레디스에 저장한 후 조회
      
- **장바구니 아이템 수정**
    - 비로그인 상태 : 쿠키에 있는 장바구니를 수정
    - 로그인 상태 : 레디스에 있는 장바구니를 수정
      
- **장바구니 아이템 삭제**
    - 비로그인 상태 : 쿠키에 있는 장바구니를 삭제
    - 로그인 상태 : 레디스에 있는 장바구니를 수정

### 📌 Order
- **주문생성 기능**
        - 
- **주문 목록 조회** : 인증된 회원만 자신의 주문 목록을 조회할 수 있습니다.
- **주문 취소 기능** : 아직 완료된 주문이 아니면 자신의 주문을 취소할 수 있습니다.
- **판매자의 주문 목록 조회** : 판매자는 자신의 판매 주문 목록을 조회할 수 있습니다.
- **판매자 주문 완료** : 판매자만 자신의 판매 주문을 완료할 수 있습니다.

### 📌 Review
- **리뷰 생성** : 인증된 회원만 상품에 리뷰를 생성할 수 있습니다.
- **리뷰 수정** : 인증된 회원만 자신이 작성한 리뷰를 수정할 수 있습니다.
- **리뷰 삭제** : 인증된 회원만 자신이 작성한 리뷰를 삭제할 수 있습니다.
- **리뷰 조회 (최신순)** : 상품에 대한 최신 리뷰 목록을 조회합니다.
- **리뷰 조회 (오래된순)** : 상품에 대한 오래된 리뷰 목록을 조회합니다.
- **리뷰 조회 (평점 오름차순)** : 상품에 대한 리뷰를 평점 오름차순으로 조회합니다.
- **리뷰 조회 (평점 내림차순)** : 상품에 대한 리뷰를 평점 내림차순으로 조회합니다.


## 🧾 ERD
<img width="788" alt="스크린샷 2024-11-15 오후 12 31 05" src="https://github.com/user-attachments/assets/f0fc129b-19e4-46c4-8f9e-91d49ea875b6">
