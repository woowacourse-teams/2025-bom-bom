package me.bombom.api.v1.auth.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthControllerApi {

    @Operation(
        summary = "회원가입",
        description = "OAuth2 인증 후 추가 정보를 입력하여 회원가입을 완료합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "OAuth2 인증 정보 없음")
    })
    void signup(
        @Parameter(description = "회원가입 요청 데이터") @RequestBody MemberSignupRequest signupRequest,
        HttpServletRequest request
    );

    @Operation(
        summary = "OAuth2 로그인",
        description = "지정된 OAuth2 제공자로 로그인을 시작합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "OAuth2 인증 페이지로 리다이렉트")
    })
    void login(
        @Parameter(description = "OAuth2 제공자 (google, kakao 등)", example = "google")
        @PathVariable("provider") String provider,
        @RequestParam(defaultValue = "deploy") String env,
        HttpServletResponse response,
        HttpSession httpSession
    ) throws IOException;

    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 무효화하여 로그아웃합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    })
    void logout(HttpServletRequest request);
} 
