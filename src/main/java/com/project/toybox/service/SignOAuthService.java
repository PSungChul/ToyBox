package com.project.toybox.service;

import com.project.toybox.entity.Sign;
import com.project.toybox.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class SignOAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Autowired
    SignRepository signRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String checkNaver(String phoneNumber) {
        Sign sign = signRepository.findByPhoneNumber(phoneNumber);

        if (sign == null) {
            return "no";
        } else {
            return sign.getPlatform();
        }
    }

    // 구글 로그인 인증 - 미가입자, 가입자, 중복 가입자 비교
    public String findByGoogleUser(String email) { // 42. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 43. 42에서 파라미터로 받아온 아이디로 유저를 조회하고, 조회된 값을 받아온다.
        Sign sign = signRepository.findByEmail(email);
        // 44. 조회된 값이 있는지 체크한다.
        if (sign == null) { // 44-1. 조회된 값이 없는 경우 - 미가입자
            // 44-1-1. 눌값을 반환한다.
            return null;
        } else { // 44-2. 조회된 값이 있는 경우 - 구글 가입자 or 타 플랫폼 가입자
            // 46-1. 조회된 플랫폼이 google인 경우
            if (sign.getPlatform().equals("google")) {
                return sign.getPlatform();
            } else { // 46-2. 조회된 플랫폼이 google이 아닌 경우
                // 46-2-1. 46에서 조회하고 받아온 Entity를 DTO로 반환한다.
                // 46-1-1. 에러 메시지를 작성해 DTO로 반환한다.
                return sign.getPlatform() + "로 가입한 이력이 있습니다.\n해당 플랫폼으로 로그인 해주시기 바랍니다.";
            }
        }
    }

    public void socialJoin(Sign.rqJoinSocialUser rqJoinSocialUser) {
        if (rqJoinSocialUser.getPlatform().equals("naver")) {
            signRepository.save(rqJoinSocialUser.toNaver());
        } else {
            signRepository.save(rqJoinSocialUser.toGoogle());
        }
    }

    // 소셜 로그인시 인증 방식
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException { // 1. 파라미터로 OAuth2UserRequest를 받아온다.
        // OAuth2UserRequest - OAuth 2.0 프로토콜을 사용하여 인증하는데 사용되는 정보를 포함하는 객체
        // 2. OAuth2UserService 인터페이스의 구현체인 DefaultOAuth2UserService 객체를 생성한다.
        //    OAuth2UserService를 통해 OAuth2User 객체를 가져온다.
        //    DefaultOAuth2UserService - OAuth2UserService의 구현체이다.
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        // 3. 2에서 생성한 delegate 객체를 사용해 OAuth2 로그인을 처리하는 OAuth2User 객체를 생성한다.
        //    1에서 가져온 OAuth2User 객체에서 OAuth2UserRequest를 가져오는 코드로 소셜 로그인 진행중인 유저 정보를 가져온다.
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 4. 현재 로그인 진행중인 서비스를 구분하는 코드로 유저가 소셜 로그인 진행중인 플랫폼 이름을 가져온다. - ex) navar, google
        String registrationId = userRequest.getClientRegistration()
                                           .getRegistrationId();

        // 5. OAuth2 로그인 진행시 키가 되는 필드값 - Primary Key와 같은 의미이다.
        // 구글의 경우 기본적으로 코드("sub")를 지원하지만, 네이버 카카오 등은 기본 지원하지 않는다.
        // 그러기에 네이버 카카도 등은 따로 로그인 진행시 키가 되는 필드값을 찾아서 작성해야 한다.
//        String userNameAttributeName = userRequest.getClientRegistration()
//                                                  .getProviderDetails()
//                                                  .getUserInfoEndpoint()
//                                                  .getUserNameAttributeName();
        if (registrationId.equals("naver")) {
            // 5. OAuth2 로그인 진행시 키가 되는 필드값 - Primary Key와 같은 의미로 id 값을 사용해 구분지을 것이기에, 키가 되는 필드값을 id로 지정한다.
            String userNameAttributeName = "id";
            // 6. 4에서 가져온 플랫폼과 5에서 지정한 필드값과 3에서 가져온 OAuth2User의 attribute를 DTO에 전달하고 생성한다.
            Sign.oauthAttributes oauthSocial = Sign.oauthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
            // 13. 6에서 생성한 DTO를 Entity로 변환한다.
            Sign sign = oauthSocial.toNaverEntity();
            // 14. 13에서 변환된 Entity 값 중 아이디로 유저를 조회하고, 조회된 값을 받아온다.
            Sign socialLoginSign = signRepository.findByPhoneNumber(sign.getPhoneNumber());

            if (socialLoginSign.getId() == null) {
                signRepository.updateByNaverId(oauthSocial.getId(), sign.getPhoneNumber());
            }

            // 15. 14에서 조회된 값 중 Spring Security 권한 값을 가진 SimpleGrantedAuthority 객체와 6에서 생성한 DTO를 사용해 DefaultOAuth2User 객체를 생성하고 반환한다.
            // 15. 반환하는 객체는 DefaultOAuth2User 타입으로, 생성자에 3개의 파라미터를 전달하여 객체를 생성한다.
            //     첫번째 파라미터는 Collection 타입의 객체를 전달하여 권한(Authority)을 지정하는데, 여기서는 14에서 조회된 값 중 Spring Security 권한(roleName) 값을 기반으로 SimpleGrantedAuthority 객체를 생성하여 전달한다.
            //     두번째 파라미터는 소셜 로그인 과정에서 받아오는 유저 정보로, 여기서는 6에서 생성한 DTO 값 중 유저 정보(attributes)를 가져온다.
            //     세번째 파라미터는 사용자 정보에서 이메일 정보를 가져오는 키가 되는 필드값으로, 여기서는 6에서 생성한 DTO 값 중 필드값(nameAttributeKey)을 가져온다.
            //     이제 생성된 DefaultOAuth2User 객체를 반환하여, 로그인 과정에서 유저 정보를 제공한다.
            return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(socialLoginSign.getRoleName())), // Spring Security 권한
                                                                                          oauthSocial.getAttributes(), // 유저 정보 Map
                                                                                          oauthSocial.getNameAttributeKey()); // 필드값
        } else {
            // 5. OAuth2 로그인 진행시 키가 되는 필드값 - 구글의 경우 기본적으로 코드("sub")를 지원한다.
            String userNameAttributeName = userRequest.getClientRegistration()
                                                      .getProviderDetails()
                                                      .getUserInfoEndpoint()
                                                      .getUserNameAttributeName();
            // 6. 4에서 가져온 플랫폼과 5에서 지정한 필드값과 3에서 가져온 OAuth2User의 attribute를 DTO에 전달하고 생성한다.
            Sign.oauthAttributes oauthSocial = Sign.oauthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
            // 13. 6에서 생성한 DTO를 Entity로 변환한다.
            Sign sign = oauthSocial.toGoogleEntity();
            // 14. 13에서 변환된 Entity 값 중 아이디로 유저를 조회하고, 조회된 값을 받아온다.
            Sign socialLoginSign = signRepository.findByEmail(sign.getEmail());

            if (socialLoginSign.getId() == null) {
                signRepository.updateByGoogleId(oauthSocial.getId(), sign.getEmail());
            }

            // 15. 14에서 조회된 값 중 Spring Security 권한 값을 가진 SimpleGrantedAuthority 객체와 6에서 생성한 DTO를 사용해 DefaultOAuth2User 객체를 생성하고 반환한다.
            // 15. 반환하는 객체는 DefaultOAuth2User 타입으로, 생성자에 3개의 파라미터를 전달하여 객체를 생성한다.
            //     첫번째 파라미터는 Collection 타입의 객체를 전달하여 권한(Authority)을 지정하는데, 여기서는 14에서 조회된 값 중 Spring Security 권한(roleName) 값을 기반으로 SimpleGrantedAuthority 객체를 생성하여 전달한다.
            //     두번째 파라미터는 소셜 로그인 과정에서 받아오는 유저 정보로, 여기서는 6에서 생성한 DTO 값 중 유저 정보(attributes)를 가져온다.
            //     세번째 파라미터는 사용자 정보에서 이메일 정보를 가져오는 키가 되는 필드값으로, 여기서는 6에서 생성한 DTO 값 중 필드값(nameAttributeKey)을 가져온다.
            //     이제 생성된 DefaultOAuth2User 객체를 반환하여, 로그인 과정에서 유저 정보를 제공한다.
            return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(socialLoginSign.getRoleName())), // Spring Security 권한
                                                                                          oauthSocial.getAttributes(), // 유저 정보 Map
                                                                                          oauthSocial.getNameAttributeKey()); // 필드값
        }
    }
}
