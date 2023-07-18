package com.project.toybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.toybox.entity.Sign;
import com.project.toybox.httpclient.GoogleLogin;
import com.project.toybox.httpclient.IamPortPass;
import com.project.toybox.service.SignOAuthService;
import com.project.toybox.service.SignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

@Controller
@RequestMapping("/loginform")
@PropertySource("classpath:application-information.properties")
public class SignController {
    @Autowired
    SignService signService;
    @Autowired
    SignOAuthService signOAuthService;
    @Autowired
    PasswordEncoder passwordEncoder;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // properties - SMTP ID/PWD
    @Value("${naverId:naverId}")
    private String naverId;
    @Value("${naverPwd:naverPwd}")
    private String naverPwd;

    // properties - IamPortPass
    @Value("${impKey:impKey}")
    private String impKey;
    @Value("${impSecret:impSecret}")
    private String impSecret;

    // properties - GoogleClient Id/Secret
    @Value("${googleClientId:googleClientId}")
    private String googleClientId;
    @Value("${googleClientSecret:googleClientSecret}")
    private String googleClientSecret;

    // properties - PeopleApi Key
    @Value("${peopleApiKey:peopleApiKey}")
    private String peopleApiKey;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////login
    @GetMapping("")
    public String loginForm(@RequestParam(value = "error", required = false) String error, // URL 파라미터를 통해 넘어오는 에러 체크값이 있을 경우 받는다.
                            @RequestParam(value = "errorMsg", required = false) String errorMsg, // URL 파라미터를 통해 넘어오는 에러 메세지가 있을 경우 받는다.
                            @RequestParam(value = "loginErrMsg", required = false) String loginErrMsg, // URL 파라미터를 통해 넘어오는 소셜 로그인 에러 메세지가 있을 경우 받는다.
                            Model model) {
        String googleUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                           "scope=" + "email profile https://www.googleapis.com/auth/user.birthday.read" +
                           "&access_type=" + "offline" +
                           "&include_granted_scopes=" + "true" +
                           "&response_type=" + "code" +
                           "&state=" + "security_token%3D138r5719ru3e1%26url%3Dhttps://oauth2.example.com/token" +
                           "&redirect_uri=" + "http://localhost:1111/loginform/googletoken" +
                           "&client_id=" + googleClientId;
        // 구글 로그인 URL을 바인딩한다.
        model.addAttribute("googleUrl", googleUrl);
        // 에러 체크값을 바인딩한다.
        model.addAttribute("error", error);
        // 에러 메세지를 바인딩한다.
        model.addAttribute("errorMsg", errorMsg);
        // 소셜 로그인 에러 메세지를 바인딩한다.
        model.addAttribute("loginErrMsg", loginErrMsg);
        return "Sign/LoginForm";
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////findId
    @GetMapping("/findidform")
    public String findIdForm(Model model) {
        model.addAttribute("userDTO", new Sign.rqFindId());
        return "Sign/FindIdForm";
    }

    @PostMapping("/findidform/findid")
    @ResponseBody
    public Sign.rpFindId findId(Sign.rqFindId rqFindId) {
        Sign.rpFindId rpFindId = signService.findId(rqFindId);
        return rpFindId;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////findPwd
    @GetMapping("/findpwdform")
    public String findPwdForm(Model model) {
        model.addAttribute("userDTO", new Sign.rqResetPwd());
        return "Sign/FindPwdForm";
    }

    @GetMapping("/findpwdform/id")
    @ResponseBody
    public String findPwdId(String id) {
        String check = signService.findPwdId(id);
        return check;
    }

    @PostMapping("/findpwdform/resetpwdform")
    public String resetPwdForm(Sign.rqResetPwd rqResetPwd, Model model) {
        model.addAttribute("userDTO", rqResetPwd);
        return "Sign/ResetPwdForm";
    }

    @PostMapping("/findpwdform/resetpwdform/resetpwd")
    public String resetPwd(Sign.rqResetPwd rqResetPwd) {
        signService.resetPwd(rqResetPwd, passwordEncoder);
        return "redirect:/loginform";
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////certification/Email
    @PostMapping("/certification/email")
    @ResponseBody
    public Sign.rpCheckEmail sendEmail(@RequestParam(value = "id", required = false) String id,
                                       @RequestParam(value = "name", required = false) String name,
                                       String email) {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        Sign.rpCheckEmail rpCheckEmail = signService.sendEmail(map, passwordEncoder, naverId, naverPwd);
        return rpCheckEmail;
    }

    @PostMapping("/certification/email/check")
    @ResponseBody
    public boolean checkEmail(String emailKey, String hEmailKey) {
        boolean isMatch = passwordEncoder.matches(emailKey, hEmailKey);
        // 반환된 결과 값(true/false)을 클라이언트로 반환한다.
        // true - 이메일 인증 번호 일치 / false - 이메일 인증 번호 불일치
        return isMatch;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////certification/phone
    // 휴대폰 본인인증 페이지
    @GetMapping("/certificationform")
    public String certificationForm(String type, Model model) {
        if (type == null) { // URL에 'type' 파라미터가 지워진 경우 예외처리
            return "redirect:/loginform";
        }

        if (type.equals("join")) { // 회원가입으로부터 들어올 경우
            model.addAttribute("joinDTO", new Sign.rqJoinUser());
            model.addAttribute("type", type);
        } else if (type.equals("findId")) { // 아이디 찾기로부터 들어올 경우
            model.addAttribute("type", type);
        } else { // 비밀번호 찾기로부터 들어올 경우
            Sign.rqResetPwd rqResetPwd = new Sign.rqResetPwd();
            rqResetPwd.setId(type);
            model.addAttribute("userDTO", rqResetPwd);
            model.addAttribute("type", "resetPwd");
        }

        return "Sign/CertificationForm";
    }

    // 휴대폰 본인인증 IamPort서버 통신
    @PostMapping("/certificationform/certification")
    @ResponseBody
    public Sign.rpCheckPhone certifications(String impUid, String type) {
        JsonNode jsonToken = IamPortPass.getToken(impKey, impSecret);
        String accessToken = jsonToken.get("response").get("access_token").asText();

        JsonNode userInfo = IamPortPass.getUserInfo(impUid, accessToken);
        String name = userInfo.get("response").get("name").asText();
        String birthday = userInfo.get("response").get("birthday").asText().replaceAll("[^0-9]", "");
        String phoneNumber = userInfo.get("response").get("phone").asText().replaceAll("[^0-9]", "");

        Sign.rqCheckPhone rqCheckPhone = new Sign.rqCheckPhone();
        rqCheckPhone.setName(name);
        rqCheckPhone.setBirthday(birthday);
        rqCheckPhone.setPhoneNumber(phoneNumber);
        rqCheckPhone.setType(type);

        Sign.rpCheckPhone rpCheckPhone = signService.checkPhone(rqCheckPhone);

        return rpCheckPhone;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////join
    @PostMapping("/joinform")
    public String joinForm(Sign.rqJoinUser rqJoinUser, Model model) {
        model.addAttribute("joinDTO", rqJoinUser);
        return "Sign/JoinForm";
    }

    @PostMapping("/joinform/idcheck")
    @ResponseBody
    public String idCheck(String id) {
        String check = signService.idCheck(id);
        return check;
    }

    @PostMapping("/joinform/join")
    public String join(Sign.rqJoinUser rqJoinUser) {
        signService.join(rqJoinUser, passwordEncoder);
        return "redirect:/loginform";
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////naver
    @GetMapping("/navercallback")
    public String naverCallback(Model model) {
        model.addAttribute("socialDTO", new Sign.rqJoinSocialUser());
        return "Sign/NaverCallback";
    }

    @PostMapping("/navercallback/certification")
    @ResponseBody
    public String naverCertification(String phoneNumber) {
        String check = signOAuthService.checkNaver(phoneNumber);
        return check;
    }

    @PostMapping("/navercallback/certification/add")
    public String naverAdd(Sign.rqJoinSocialUser rqJoinSocialUser, Model model) {
        model.addAttribute("socialDTO", rqJoinSocialUser);
        return "Sign/NaverAdd";
    }

    @PostMapping("/navercallback/certification/add/join")
    public String naverJoin(Sign.rqJoinSocialUser rqJoinSocialUser) {
        signOAuthService.socialJoin(rqJoinSocialUser);
        return "redirect:/loginform";
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////google
    // 구글 로그인 토큰 발급 및 유저 정보 조회
    @GetMapping("/googletoken")
    public String googleAuthentication(String code, Model model) { // 파라미터로 구글 로그인 URL을 통해 가져온 code를 받아온다.
        // 파라미터로 받아온 code로 이번엔 access_token을 받아와야 하기에 구글 서버와 통신하는 메소드에 code를 전달한다.
        JsonNode jsonToken = GoogleLogin.getAccessToken(googleClientId, googleClientSecret, code);
        // 반환받은 값에서 필요한 access_token을 가져와 토큰 변수에 전달한다.
        // toString - 객체나 값을 문자열로 변환하는 메소드로, 디버깅이나 출력용으로 사용한다. - "" 큰 따옴표가 항시 따라 붙는다.
        // asText - 일반적으로 사람이 읽을 수 있는 형식으로 값을 문자열로 변환하는 데 사용한다. - "" 큰 따옴표 없이 글자만 나온다.
        // 여기서 두가지의 눈에 띄는 차이는 "" 큰 따옴표의 유무인데, 값을 URL에 담아서 함께 보내는 GET 방식에는 데이터에 따라 붙는 이 "" 큰 따옴표가 걸리적 거리기에 반드시 asText를 사용해야 한다.
        String accessToken = jsonToken.get("access_token").asText();

        // 전달받은 access_token으로 이번엔 구글 로그인 유저 정보를 받아와야 하기에 구글 서버와 통신하는 메소드에 access_token을 전달한다.
        JsonNode userInfo = GoogleLogin.getGoogleUserInfo(accessToken);
        // 반환받은 값에서 필요한 유저 정보인 이메일과 이름을 가져온다.
        // 가져온 이메일을 이메일 변수에 전달한다.
        String email = userInfo.get("email").asText();
        // 가져온 이름을 이름 변수에 전달한다.
        String name = userInfo.get("name").asText();

        // 전달받은 access_token으로 이번엔 구글 로그인 유저의 추가 정보를 받아와야 하기에 구글 서버와 통신하는 메소드에 access_token을 전달한다.
        JsonNode people = GoogleLogin.getGooglePeople(peopleApiKey, accessToken);
        // 반환받은 값에서 필요한 추가 유저 정보인 생년월일과 성별을 가져온다.
        // 가져온 생년월일이 년, 월, 일로 따로따로 분리되서 나오기에 먼저 각각 변수로 받은뒤 그 다음 하나로 합쳐서 생일 변수에 전달한다.
        String year = people.get("birthdays").get(0).get("date").get("year").asText();
        String month = people.get("birthdays").get(0).get("date").get("month").asText();
        if (month.length() < 2) {
            month = "0" + month;
        }
        String day = people.get("birthdays").get(0).get("date").get("day").asText();
        if (day.length() < 2) {
            day = "0" + day;
        }
        String birthday = year + month + day;

        // 전달받은 아이디를 서비스에 전달한다.
        String check = signOAuthService.findByGoogleUser(email);
        // 반환받은 값이 있는지 체크한다.
        if (check == null) { // 반환받은 값이 없는 경우 - 미가입자로 여기서 받아온 구글 유저 정보들을 들고 구글 회원가입 추가입력 페이지로 이동한다.
            Sign.rqJoinSocialUser rqJoinSocialUser = new Sign.rqJoinSocialUser();
            rqJoinSocialUser.setEmail(email);
            rqJoinSocialUser.setName(name);
            rqJoinSocialUser.setBirthday(birthday);
            rqJoinSocialUser.setPlatform("google");
            // 구글 회원가입에 사용할 DTO를 바인딩한다.
            model.addAttribute("socialDTO", rqJoinSocialUser);
            // 구글 회원가입 추가입력 페이지로 이동한다.
            return "Sign/GoogleAdd";
        } else if (check.equals("google")){ // 반환받은 값이 google인 경우 - 구글 가입자
            // Spring Security가 관리하고 있는 OAuth2를 통해 OAuth2UserService로 리다이렉트한다.
            return "redirect:/oauth2/authorization/google";
        } else { // 반환받은 값이 google이 아닌 경우 - 타 플랫폼 가입자
            try {
                // 에러 메시지를 UTF-8 형식으로 인코딩하여 로그인 페이지로 리다이렉트한다.
                return "redirect:/loginform?loginErrMsg=" + URLEncoder.encode(check, "UTF-8");
            } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
                throw new RuntimeException(e);
            }
        }
    }

    @PostMapping("/googletoken/join")
    public String googleJoin(Sign.rqJoinSocialUser rqJoinSocialUser) {
        signOAuthService.socialJoin(rqJoinSocialUser);
        return "redirect:/loginform";
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
