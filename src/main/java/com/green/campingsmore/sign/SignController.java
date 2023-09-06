package com.green.campingsmore.sign;

import com.green.campingsmore.config.security.AuthenticationFacade;
import com.green.campingsmore.config.security.model.MyUserDetails;
import com.green.campingsmore.config.security.model.SearchUserDto;
import com.green.campingsmore.config.security.model.SignUpDto;
import com.green.campingsmore.sign.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "회원")
@RequestMapping("/api")
public class SignController {
    private final SignService SERVICE;
    private final AuthenticationFacade FACADE;

    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인",
            description = "Try it out -> Execute 눌러주세요 \n\n " +
                    "\"connected_at\": \"날짜 형식\",\n\n" +
                    "  \"id\": \"61616468 숫자\",\n\n" +
                    "  \"email\": \"rlahfld54@kakao.com 이런 형식\",\n\n" +
                    "  \"access_token\": \"string\",\n\n" +
                    "  \"refresh_token\": \"string\",\n\n" +
                    "\n\n 이 형식으로 오는 것 맞는지 확인해주세요!! - 황주은"
    )
    public int kakaoLogin(@RequestBody KaKaoLoginVo kaKaoLoginVo){
//        if(FACADE.getLoginUser() == null){  // 비로그인일 경우 false  ==> 찜하기 속성이 0? 인걸로 리스트 뱉어준다.
//            System.out.println("비로그인 !!!!");
//
//
//        } else { // 로그인 했을 경우 true ==> 로그인이 확인되었으니까 유저의 PK를 이제부터 조회할 수 있다.
//            FACADE.getLoginUserPk(); // 유저의 PK를 불러 오는 메서드이다. Long 타입 반환
//            log.info("로그를 찍어줘:{}");
//        }
        return SERVICE.kakaoLogin(kaKaoLoginVo);
    }

    //ApiParam은 문서 자동화를 위한 Swagger에서 쓰이는 어노테이션이고
    //RequestParam은 http 로부터 요청 온 정보를 받아오기 위한 스프링 어노테이션이다.
    //@AuthenticationPrincipal을 통해 로그인한 사용자 정보,PK를 받아 사용할 수 있다.
    @PostMapping("/oauth/authorize")
    @Operation(summary = "로그인",
            description = "Try it out -> Execute 눌러주세요 \n\n " +
                    "id:  아이디 \n\n " +
                    "password : 비밀번호 \n\n "
    )
    public SignInResultDto signIn(HttpServletRequest req
            ,@RequestBody UserLogin userLogin){
        String id = userLogin.getUid();
        String password = userLogin.getUpw();
        String ip = req.getRemoteAddr();
        log.info("[signIn] 로그인을 시도하고 있습니다. id: {}, pw: {}, ip: {}",id, password, ip);
        return SERVICE.signIn(userLogin,ip);
    }

    @GetMapping("/notuser")
    @Operation(summary = "비회원이 로그인을 하지 않고 들어올 시 방문자 Count 증가 API",
            description = "Try it out -> Execute 눌러주세요"
    )
    public ResponseEntity<?> IncreaseCount(){
        SERVICE.IncreaseCount();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/oauth/logout")
    @Operation(summary = "로그아웃",
            description = "Try it out -> Execute 눌러주세요 \n\n "
    )
    public ResponseEntity<?> logout(HttpServletRequest req) {
        SERVICE.logout(req);
        ResponseCookie responseCookie = ResponseCookie.from("refresh-token", "")
                .maxAge(0)
                .path("/")
                .build();

        log.info("ResponseCookie :{}",responseCookie);
        log.info("로그아웃 완료!!!");

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .build();
    }

    @PostMapping("/user")
    @Operation(summary = "회원가입",
            description = "Try it out -> Execute 눌러주세요 \n\n " +
                    "uid: 회원가입 아이디 \n\n " +
                    "upw : 비밀번호 ex)1234 \n\n " +
                    "email : 이메일 ex)rlahfld54@gmail.com \n\n " +
                    "name: 이름 \n\n " +
                    "birth_date: 생년월일 ex)1998-06-12 \n\n " +
                    "phone: 핸드폰 번호 \n\n " +
                    "gender: 성별 -> 숫자 0(남자) 과 1(여자) (기본값이 남자이고 0) \n\n " +
                    "user_address: 주소 \n\n " +
                    "user_address_detail: 상세 주소 \n\n " +
                    "role: USER 이거나 ADMIN\n\n "
    )
    public SignUpResultDto signUp(@RequestBody SignUpDto signUpDto) {
        log.info("[signUp] 회원가입을 수행합니다. id: {}, pw: {}, nm: {}, role: {}", signUpDto.getUid(), signUpDto.getUpw(), signUpDto.getName(), signUpDto.getRole());
        SignUpResultDto dto = SERVICE.signUp(signUpDto);
        log.info("[signUp] 회원가입 완료 id: {}", signUpDto.getUid());
        return dto;
    }

    @PostMapping("/oauth/token")
    @Operation(summary = "리프레쉬 토큰",
            description = "Try it out -> Execute 눌러주세요 \n\n " +
                    "refreshToken :  리프레쉬 토큰 \n\n "
    )
    public ResponseEntity<SignInResultDto> refreshToken(HttpServletRequest req, @RequestBody UserRefreshToken userRefreshToken) {
        String refreshToken = userRefreshToken.getRefreshToken();
        SignInResultDto dto = SERVICE.refreshToken(req, refreshToken);
        return dto == null ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null) : ResponseEntity.ok(dto);
    }

    @GetMapping("/search/id")
    @Operation(summary = "아이디 찾기",
            description = "Try it out -> Execute 눌러주세요 \n\n " +
                    "user_id :  아이디 찾기 \n\n "
    )
    public String searchID(@RequestParam String name
            , @RequestParam String phone
            , @RequestParam String birth
    ) {
        return SERVICE.searchID(name, phone, birth);
    }

    @DeleteMapping("/user/delete")
    @Operation(summary = "회원탈퇴",
            description = "Try it out -> Execute 눌러주세요 \n\n " +
                    "iuser:  iuser PK \n\n "
    )
    public int deleteUser(@RequestParam int iuser) {
        return SERVICE.deleteUser(iuser);
    }


    @GetMapping("/user/me")
    @Operation(summary = "로그인 했을때 , 본인 회원 정보 출력",
            description = "Try it out -> Execute 눌러주세요 \n\n ")
    public UserInfo getmyInfo(){
        return SERVICE.getmyInfo();
    }


    @PostMapping(value = "/user/update-profile",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "회원 정보 수정 => 회원이 자신의 정보를 수정할 수 있도록 하는 것",
            description = "Try it out -> Execute 눌러주세요 \n\n "+
                    "아이디,이름은 못 바꾸고 프론트에서도 고정시켜야함 \n\n "+
                    "프론트에서 로그인후 아이디를 백엔드로 보내줘야함- 본인인증..? \n\n "+
                    "uid : 아이디 => 프론트에서 아이디 보내줘야함\n\n " +
                    "upw : 비밀번호 \n\n " +
                    "email : 이메일\n\n " +
                    "name: 이름 \n\n " +
                    "birth_date: 생년월일 \n\n " +
                    "phone: 핸드폰 번호 \n\n " +
                    "user_address: 주소 \n\n " +
                    "user_address_detail : 상세주소 \n\n" +
                    "프로필 이미지까지 담아서 해야함.. 아마 프로필 이미지 null이면 안될듯..."
    )
    public int updateUserInfo(@AuthenticationPrincipal MyUserDetails user
            ,@RequestPart UpdateUserInfoDto updateUserInfoDto
            ,@RequestPart(required = false) MultipartFile pic) throws Exception {
        // 로그인 했을때만 수정할 수 있도록 해야함  // 본인 자신만 수정할 수 있도록 해야함..
//        log.info("controller-iuser {}", user.getIuser());
        System.out.println("실행되고 있나??");
        System.out.println("MyUserDetails = {}"+user);
        System.out.println("controller-iuser {}"+user.getIuser());
        boolean test = FACADE.isLogin();
        System.out.println("isLogin = "+test);
        SERVICE.test();

        System.out.println(updateUserInfoDto);
        return SERVICE.updateUserInfo(updateUserInfoDto,pic);
    }

    @PostMapping("/search/pw")
    @Operation(summary = "비밀번호 찾기 - 이메일로 임시 비밀번호 제공",
            description = "Try it out -> Execute 눌러주세요 \n\n " +
                    "user_id :  아이디 \n\n "+
                    "name :  이름 \n\n "+
                    "email :  이메일 \n\n "
    )
    public int searchPW(@RequestBody SearchUserDto searchUserDto) {
        return SERVICE.searchPW(searchUserDto);
    }
}
