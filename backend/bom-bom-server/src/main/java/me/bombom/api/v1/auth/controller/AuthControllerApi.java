package me.bombom.api.v1.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.auth.dto.request.SignupValidateRequest;
import me.bombom.api.v1.auth.dto.response.NativeLoginResponse;
import me.bombom.api.v1.auth.enums.SignupValidateStatus;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
            summary = "회원가입 필드 중복 체크",
            description = "회원가입에 사용되는 필드의 중복을 체크하여 true/false를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 체크 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    SignupValidateStatus validateSignupField(
            @Parameter(description = "중복 체크 요청 데이터") @ModelAttribute SignupValidateRequest request
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
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException;

    @Operation(
        summary = "네이티브 OAuth2 로그인",
        description = "모바일 앱에서 받은 id_token과 authorization_code로 서버에서 토큰 교환 및 로그인 분기를 수행합니다. 기존 회원이면 '/'로, 신규면 '/signup'으로 리다이렉트합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "리다이렉트 수행 (기존: '/', 신규: '/signup')"),
        @ApiResponse(responseCode = "400", description = "지원하지 않는 제공자"),
        @ApiResponse(responseCode = "401", description = "토큰 검증 실패 또는 교환 실패")
    })
    NativeLoginResponse nativeLogin(
        @PathVariable("provider") String provider,
        @RequestBody NativeLoginRequest nativeLoginRequest,
        HttpServletRequest request
    ) throws IOException;

    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 무효화하여 로그아웃합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    })
    void logout(HttpServletRequest request, HttpServletResponse response);

    @Operation(
        summary = "회원 탈퇴",
        description = "현재 로그인한 회원의 계정을 삭제합니다. Apple 로그인 사용자의 경우 Apple 토큰도 함께 철회됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    void withdraw(
            @Parameter(hidden = true) @LoginMember Member member,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException;
} 
