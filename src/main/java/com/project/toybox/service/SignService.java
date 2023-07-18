package com.project.toybox.service;

import com.project.toybox.config.UserDetailsConfig;
import com.project.toybox.dto.MailKeyDTO;
import com.project.toybox.entity.Sign;
import com.project.toybox.repository.SignRepository;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Properties;

@Service
public class SignService implements UserDetailsService {
    @Autowired
    SignRepository signRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String userName(String id) {
        Sign sign = signRepository.findById(id);
        return sign.getName();
    }

    public Sign.rpFindId findId(Sign.rqFindId rqFindId) {
        Sign sign = rqFindId.toEntity();
        Sign user = signRepository.findByNameAndBirthdayAndPhoneNumber(sign.getName(), sign.getBirthday(), sign.getPhoneNumber());
        Sign.rpFindId rpFindId = new Sign.rpFindId();
        if (user == null) {
            rpFindId = new Sign.rpFindId("no");
        } else if (user.getPlatform().equals("naver")){
            rpFindId = new Sign.rpFindId("platform", user.getName() + "님은 네이버로 가입하셨습니다.\n해당 플랫폼으로 로그인 해주시기 바랍니다.");
        } else if (user.getPlatform().equals("google")) {
            rpFindId = new Sign.rpFindId("platform", user.getName() + "님은 구글로 가입하셨습니다.\n해당 플랫폼으로 로그인 해주시기 바랍니다.");
        } else {
            rpFindId = new Sign.rpFindId(user);
        }
        return rpFindId;
    }

    public String findPwdId(String id) {
        Sign sign = signRepository.findById(id);

        if (sign == null) {
            return "해당 아이디로 가입된 사용자를 찾을수 없습니다.";
        } else {
            return "true";
        }
    }

    public void resetPwd(Sign.rqResetPwd rqResetPwd, PasswordEncoder passwordEncoder) {
        Sign sign = rqResetPwd.toEntity(passwordEncoder);
        signRepository.updateByPwd(sign.getId(), sign.getPwd());
    }

    public Sign.rpCheckPhone checkPhone(Sign.rqCheckPhone rqCheckPhone) {
        Sign sign = rqCheckPhone.toEntity();
        Sign.rpCheckPhone rpCheckPhone = new Sign.rpCheckPhone();

        if (rqCheckPhone.getType().equals("join")) {
            Sign user = signRepository.findByNameAndBirthdayAndPhoneNumber(sign.getName(), sign.getBirthday(), sign.getPhoneNumber());
            if (user != null) {
                rpCheckPhone = new Sign.rpCheckPhone("no", "이미 가입된 회원정보가 있습니다.\n로그인 혹은 아이디/비밀번호 찾기를 이용해 주세요");
            } else {
                rpCheckPhone = new Sign.rpCheckPhone(sign);
            }
        } else if (rqCheckPhone.getType().equals("findId")) {
            Sign user = signRepository.findByNameAndBirthdayAndPhoneNumber(sign.getName(), sign.getBirthday(), sign.getPhoneNumber());
            if (user != null) {
                if (user.getPlatform().equals("naver")) {
                    rpCheckPhone = new Sign.rpCheckPhone("0", user.getName() + "님은 네이버로 가입하셨습니다.\n해당 플랫폼으로 로그인 해주시기 바랍니다.");
                } else if (user.getPlatform().equals("google")) {
                    rpCheckPhone = new Sign.rpCheckPhone("0", user.getName() + "님은 구글로 가입하셨습니다.\n해당 플랫폼으로 로그인 해주시기 바랍니다.");
                } else {
                    rpCheckPhone = new Sign.rpCheckPhone("1", user.getName() + "님 아이디는 " + user.getId() + "입니다.");
                }
            } else {
                rpCheckPhone = new Sign.rpCheckPhone("-1", "해당 정보로 가입된 사용자를 찾을수 없습니다.");
            }
        } else {
            Sign user = signRepository.findByIdAndNameAndBirthdayAndPhoneNumber(sign.getId(), sign.getName(), sign.getBirthday(), sign.getPhoneNumber());
            if (user != null) {
                rpCheckPhone = new Sign.rpCheckPhone("1", "비밀번호 재설정 페이지로 이동합니다.");
            } else {
                rpCheckPhone = new Sign.rpCheckPhone("-1", "해당 휴대폰 정보와 아이디가 일치하지 않습니다.\n휴대폰 인증을 다시 시도해 주세요.");
            }
        }

        return rpCheckPhone;
    }

    public String idCheck(String id) {
        Sign sign = signRepository.findById(id);

        if (sign == null) {
            return id;
        } else {
            return "no";
        }
    }

    public void join(Sign.rqJoinUser rqJoinUser, PasswordEncoder passwordEncoder) {
        signRepository.save(rqJoinUser.toEntity(passwordEncoder));
    }

    public Sign.rpCheckEmail sendEmail(HashMap<String, String> map, PasswordEncoder passwordEncoder, String naverId, String naverPwd) {
        Sign sign = new Sign();

        if (map.get("id") != null) {
            sign = signRepository.findByIdAndNameAndEmail(map.get("id"),map.get("name"), map.get("email"));
            if (sign == null) {
                Sign.rpCheckEmail rpCheckEmail = new Sign.rpCheckEmail("0", "해당 정보로 가입된 사용자를 찾을수 없습니다.\n입력하신 정보를 다시 확인해 주세요.");
                return rpCheckEmail;
            }
        } else {
            sign = signRepository.findByEmail(map.get("email"));
            if(sign != null) {
                Sign.rpCheckEmail rpCheckEmail = new Sign.rpCheckEmail("0", "이미 존재하는 아이디입니다.");
                return rpCheckEmail;
            }
        }

        // MailKeyDTO를 사용하여 인증 번호를 생성한다.
        String mailKey = new MailKeyDTO().getKey(7, false);

        // 8. 메일 발송에 필요한 값들을 미리 설정한다.
        // 8-1. Mail Server
        String charSet = "UTF-8"; // 사용할 언어셋
        String hostSMTP = "smtp.naver.com"; // 사용할 SMTP
        String hostSMTPid = naverId; // 사용할 SMTP에 해당하는 이메일
        String hostSMTPpwd = naverPwd; // 사용할 hostSMTPid에 해당하는 PWD
        // 8-2. 메일 발신자 및 수신자
        String fromEmail = naverId; // 메일 발신자 이메일 - hostSMTPid와 동일하게 작성한다.
        String fromName = "관리자"; // 메일 발신자 이름
        String mail = map.get("email"); // 메일 수신자 이메일 - 3에서 파라미터로 받아온 아이디를 작성한다.
        // 8-3. 가장 중요한 TLS - 이것이 없으면 신뢰성 에러가 나온다
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        try {
            // 9. 메일 발송을 맡아줄 HtmlEmail을 생성한다.
            HtmlEmail htmlEmail = new HtmlEmail(); // Email 생성
            // 10. 9에서 생성한 HtmlEmail에 8에서 미리 설정해놓은 값들과 추가로 필요한 값들을 모두 전달한다.
            htmlEmail.setDebug(true); // 디버그 사용
            htmlEmail.setCharset(charSet); // 언어셋 사용
            htmlEmail.setHostName(hostSMTP); // SMTP 사용
            htmlEmail.setSmtpPort(587);	// SMTP 포트 번호 입력
            htmlEmail.setAuthentication(hostSMTPid, hostSMTPpwd); // SMTP 이메일 및 비밀번호
            htmlEmail.addTo(mail); // 메일 수신자 이메일
            htmlEmail.setFrom(fromEmail, fromName, charSet); // 메일 발신자 정보
            htmlEmail.setSubject("[ToyBox] 이메일 인증번호 발송 안내입니다."); // 메일 제목
            htmlEmail.setHtmlMsg("<p>" + "[메일 인증 안내입니다.]" + "</p>" +
                                 "<p>" + "ToyBox를 사용해 주셔서 감사드립니다." + "</p>" +
                                 "<p>" + "아래 인증 코드를 '인증번호' 란에 입력해 주세요." + "</p>" +
                                 "<p>" + mailKey + "</p>"); // 메일 내용 - 7에서 생성한 인증 번호를 여기서 전달한다.
            // 11. 10에서 전달한 값들을 토대로 9에서 생성한 HtmlEmail을 사용하여 메일을 발송한다.
            htmlEmail.send();
            // 12. 메일이 정상적으로 발송됬는지 체크한다.
            // 12-1. 메일 발송이 성공한 경우
            // 12-1-1. 4에서 파라미터로 받아온 이메일 아이디와 7에서 생성한 인증 번호를 비밀번호 암호화 메소드를 사용하여 DTO로 변환한다.
            Sign.rpCheckEmail rpCheckEmail = new Sign.rpCheckEmail(map.get("email"), mailKey, passwordEncoder);
            // 12-1-2. 12-1-1에서 변환된 DTO를 반환한다.
            return rpCheckEmail;
            // 12-2. 메일 발송이 실패한 경우
        } catch (Exception e) {
            //System.out.println(e);
            // 12-2-1. 에러 체크 값과 에러 메세지를 DTO로 변환한다.
            Sign.rpCheckEmail rpCheckEmail = new Sign.rpCheckEmail("-1", "메일 발송에 실패하였습니다.\n다시 시도해주시기 바랍니다.");
            // 12-2-2. 12-2-1에서 변환된 DTO를 반환한다.
            return rpCheckEmail;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Sign user = signRepository.findById(id);
        if (user == null) {
            throw new UsernameNotFoundException(id);
        }
        return UserDetailsConfig.builder()
                .id(user.getId())
                .pwd(user.getPwd())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRoleName())
                .build();
    }
}
