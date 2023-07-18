package com.project.toybox.entity;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.Map;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주소값이 아닌 String 타입으로 변경해 주는 어노테이션
@Entity(name = "Sign")
public class Sign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 50, unique = true)
    private String id;

    @Column(length = 255)
    private String pwd;

    @Column(length = 50, unique = true, nullable = false)
    private String email;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 10, nullable = false)
    private String birthday;

    @Column(length = 15, unique = true, nullable = false)
    private String phoneNumber;

    @Column(length = 10, nullable = false)
    private String platform;

    @Column(length = 100, nullable = false)
    private String roleName;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////DTO
    // 이메일 인증 Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @ToString
    public static class rpCheckEmail {
        private String email;
        private String msg;

        // Entity를 DTO로 변환 (생성자 방식) - 이메일 전송 실패
        public rpCheckEmail(String email, String msg) { // 파라미터로 서비스에서 넘어온 체크 값과 메시지를 받아온다.
            // emailId : 0 / msg : 에러 메세지
            // emailId : -1 / msg : 전송 실패 에러 메세지
            this.email = email; // emailId는 전송 및 에러 체크 값으로 사용한다.
            this.msg = msg; // msg는 알람 메세지로 사용한다.
        }
        // Entity를 DTO로 변환 (생성자 방식) - 이메일 전송 성공
        public rpCheckEmail(String email, String msg, PasswordEncoder passwordEncoder) { // 파라미터로 서비스에서 넘어온 체크 값과 메시지를 받아온다.
            // 이메일 전송 성공할 경우 - emailId : 이메일 아이디 / 이메일 인증 번호
            // 비밀번호 암호화를 사용하여 이메일 인증 번호를 암호화 한다.
            String enPassword = passwordEncoder.encode(msg);
            this.email = email; // emailId는 전송 및 에러 체크 값으로 사용한다.
            this.msg = enPassword; // msg는 위에서 만든 암호화된 이메일 인증 번호로 사용한다.
        }
    }

    // 휴대폰 인증 Request DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rqCheckPhone {
        private String name;
        private String birthday;
        private String phoneNumber;
        private String type;

        // DTO를 Entity로 변환 (빌더 방식)
        public Sign toEntity() {
            return Sign.builder()
                    .id(type)
                    .name(name)
                    .birthday(birthday)
                    .phoneNumber(phoneNumber)
                    .build();
        }
    }

    // 휴대폰 인증 Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rpCheckPhone {
        private String name;
        private String birthday;
        private String phoneNumber;

        public rpCheckPhone(Sign sign) {
            this.name = sign.getName();
            this.birthday = sign.getBirthday();
            this.phoneNumber = sign.getPhoneNumber();
        }

        public rpCheckPhone(String check, String mag) {
            this.name = check;
            this.phoneNumber = mag;
        }
    }

    // 아이디 찾기 Request DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rqFindId {
        private String name;
        private String birthday;
        private String phoneNumber;

        // DTO를 Entity로 변환 (빌더 방식)
        public Sign toEntity() {
            return Sign.builder()
                    .name(name)
                    .birthday(birthday)
                    .phoneNumber(phoneNumber)
                    .build();
        }
    }

    // 아이디 찾기 Response DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @ToString
    public static class rpFindId {
        private String name;
        private String id;

        public rpFindId(Sign sign) {
            this.name = sign.getName();
            this.id = sign.getId();
        }

        public rpFindId(String no) {
            this.name = no;
            this.id = no;
        }

        public rpFindId(String platform, String msg) {
            this.name = platform;
            this.id = msg;
        }
    }

    // 비밀번호 재설정 RequestDTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rqResetPwd {
        private String id;
        private String pwd;

        // DTO를 Entity로 변환 (빌더 방식)
        public Sign toEntity(PasswordEncoder passwordEncoder) {
            String enPassword = passwordEncoder.encode(pwd);
            return Sign.builder()
                    .id(id)
                    .pwd(enPassword)
                    .build();
        }
    }

    // 플랫폼 회원가입 Request DTO
    @Getter // getter 어노테이션
    @Setter // setter 어노테이션
    @NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
    @AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
    @Builder // 빌더 사용 어노테이션
    @ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
    public static class rqJoinUser {
        private String id;
        private String pwd;
        private String email;
        private String name;
        private String birthday;
        private String phoneNumber;

        // DTO를 Entity로 변환 (빌더 방식)
        public Sign toEntity(PasswordEncoder passwordEncoder) { // 파라미터로 서비스에서 넘어온 비밀번호 암호화 메소드를 받아온다.
            // 비밀번호 암호화
            String enPassword = passwordEncoder.encode(pwd);
            // 변환된 Entity를 반환한다.
            return Sign.builder()
                    .idx(null)
                    .id(id)
                    .pwd(enPassword) // 암호화된 비밀번호 저장
                    .email(email)
                    .name(name)
                    .birthday(birthday)
                    .phoneNumber(phoneNumber)
                    .platform("ToyBox")
                    .roleName("USER") // Spring Security 권한에 USER로 설정
                    .build();
        }
    }

    // Social 회원가입 Request DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rqJoinSocialUser {
        private String id;
        private String email;
        private String name;
        private String birthday;
        private String phoneNumber;
        private String platform;

        // DTO를 Entity로 변환 (빌더 방식)
        public Sign toNaver() { // 네이버 회원가입일 경우
            return Sign.builder()
                    .idx(null)
                    .id(null)
                    .pwd(null)
                    .email(email + "@naver.com")
                    .name(name)
                    .birthday(birthday)
                    .phoneNumber(phoneNumber)
                    .platform(platform)
                    .roleName("ROLE_USER") // Spring Security 권한에 USER로 설정
                    .build();
        }

        // DTO를 Entity로 변환 (빌더 방식)
        public Sign toGoogle() { // 구글 회원가입일 경우
            return Sign.builder()
                    .idx(null)
                    .id(id)
                    .pwd(null)
                    .email(email)
                    .name(name)
                    .birthday(birthday)
                    .phoneNumber(phoneNumber)
                    .platform(platform)
                    .roleName("ROLE_USER") // Spring Security 권한에 USER로 설정
                    .build();
        }
    }

    // OAuth - Social DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class oauthAttributes {
        private Map<String, Object> attributes;
        private String nameAttributeKey;
        private String id;
        private String email;
        private String name;
        private String birthday;
        private String phoneNumber;
        private String platform;

        // DTO를 Entity로 변환 (빌더 방식)
        public Sign toNaverEntity() { // 네이버 로그인일 경우
            return Sign.builder()
                    .phoneNumber(phoneNumber.replaceAll("[^0-9]", ""))
                    .build();
        }
        public Sign toGoogleEntity() { // 구글 로그인일 경우
            return Sign.builder()
                    .email(email)
                    .build();
        }

        // Entity를 DTO로 변환 (생성자 방식)
        @Builder
        public oauthAttributes(Map<String, Object> attributes,
                               String nameAttributeKey,
                               String id,
                               String email,
                               String name,
                               String birthyear,
                               String birthday,
                               String phoneNumber,
                               String platform) { // 파라미터로 9에서 넘어온 값들을 받아온다.
            // 파라미터로 가져온 값중 플랫폼을 가져와 어느 플랫폼으로 로그인 진행중인지 체크한다.
            if ("naver".equals(platform)) { // 네이버 로그인일 경우
                this.attributes = attributes; // 유저 정보 Map
                this.nameAttributeKey = nameAttributeKey; // 필드값
                this.id = id; // 고유 식별번호
                this.email = email; // 이메일
                this.name = name; // 이름
                this.birthday = birthyear + birthday.replaceAll("[^0-9]", ""); // 생년월일은 DB 규칙에 맞게 '-' 하이픈을 제거하고 생년과 생일을 합쳐서 전달한다.
                this.phoneNumber = phoneNumber.replaceAll("[^0-9]", ""); // 핸드폰 번호
                this.platform = platform; // 플랫폼
            } else { // 구글 로그인일 경우
                this.attributes = attributes; // 유저 정보 Map
                this.nameAttributeKey = nameAttributeKey; // 필드값
                this.id = id; // 고유 식별번호
                this.email = email; // 이메일
                this.name = name; // 이름
                this.platform = platform; // 플랫폼
            }
        }

        // 로그인한 플랫폼 구별용
        public static oauthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) { // 파라미터로 서비스에서 넘어온 플랫폼과 필드값과 DTO를 받아온다.
            // 파라미터로 가져온 값중 플랫폼을 가져와 어느 플랫폼으로 로그인 진행중인지 체크한다.
            if ("naver".equals(registrationId)) { // 네이버 로그인일 경우
                // 네이버 로그인 메소드로 파라미터에서 받아온 값들을 모두 전달한다.
                return ofNaver(registrationId, userNameAttributeName, attributes);
            } else { // 구글 로그인일 경우
                // 구글 로그인 메소드로 파라미터에서 받아온 값들을 모두 전달한다.
                return ofGoogle(registrationId, userNameAttributeName, attributes);
            }
        }

        // 각 로그인 플랫폼에 해당하는 메소드로 이동한다.
        // 네이버 로그인
        public static oauthAttributes ofNaver(String registrationId, String userNameAttributeName, Map<String, Object> attributes) { // 파라미터로 위에서 넘어온 플랫폼과 필드값과 DTO를 받아온다.
            // 네이버는 구글이랑 다르게 attributes가 유저 정보만 깔끔하게 들어오는게 아니기에 내부에서 유저 정보를 가지고있는 "response"를 가져와 Map으로 새로 만든다.
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            // Entity를 DTO로 변환하는 메소드로 빌드한다.
            return oauthAttributes.builder()
                    // (String) - 값을 받는쪽의 타입은 String인데, Map에서 키로 가져온 값의 타입은 Object이기 때문에, 이를 String 타입으로 변환한다.
                    .id((String) response.get("id")) // 고유 식별번호
                    .name((String) response.get("name")) // 이름
                    .birthyear((String) response.get("birthyear")) // 생년
                    .birthday(((String) response.get("birthday")).replaceAll("[^0-9]", "")) // 생일
                    .phoneNumber(((String) response.get("mobile")).replaceAll("[^0-9]", "")) // 핸드폰 번호
                    .platform(registrationId) // 플랫폼
                    .attributes(response) // 57-1-2에서 새로 만든 유저 정보 Map
                    .nameAttributeKey(userNameAttributeName) // 필드값
                    .build();
        }
        // 구글 로그인
        public static oauthAttributes ofGoogle(String registrationId, String userNameAttributeName, Map<String,Object> attributes){ // 파라미터로 위에서 넘어온 플랫폼과 필드값과 DTO를 받아온다.
            // Entity를 DTO로 변환하는 메소드로 빌드한다.
            return oauthAttributes.builder()
                    // (String) - 값을 받는쪽의 타입은 String인데, Map에서 키로 가져온 값의 타입은 Object이기 때문에, 이를 String 타입으로 변환한다.
                    .id((String) attributes.get(userNameAttributeName)) // 고유 식별번호
                    .email((String) attributes.get("email")) // 이메일
                    .name((String) attributes.get("name")) // 이름
                    .platform(registrationId) // 플랫폼
                    .attributes(attributes) // 유저 정보 Map
                    .nameAttributeKey(userNameAttributeName) // 필드값
                    .build();
        }
    }
}
