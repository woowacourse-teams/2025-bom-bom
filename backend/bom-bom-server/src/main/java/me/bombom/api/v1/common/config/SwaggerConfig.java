package me.bombom.api.v1.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
@Profile("!test")
public class SwaggerConfig {

    // 환경별 서버 URL 설정
    private static final String PROD_URL = "https://api.bombom.news";
    private static final String DEV_URL = "https://api-dev.bombom.news";
    private static final String LOCAL_URL = "http://localhost:";

    // 프로파일 상수
    private static final String PROD_PROFILE = "prod";
    private static final String DEV_PROFILE = "dev";
    private static final String LOCAL_PROFILE = "local";

    private static final String SECURITY_SCHEME_NAME = "googleOAuth";

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(createInfo())
                .servers(setApiServer())
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createOAuth2Scheme())
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private List<Server> setApiServer() {
        if (isProfileActive(PROD_PROFILE)) {
            return List.of(createServer(PROD_URL, "봄봄 Production API"));
        }
        if (isProfileActive(DEV_PROFILE)) {
            return List.of(createServer(DEV_URL, "봄봄 Development API"));
        }
        return List.of(createServer(LOCAL_URL.concat(serverPort), "봄봄 Local API"));
    }

    private Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }

    private boolean isProfileActive(String profile) {
        return activeProfile.equalsIgnoreCase(profile);
    }

    private Info createInfo() {
        String version = getApiVersion();
        String description = getApiDescription();

        return new Info()
                .title("봄봄 Server API")
                .version(version)
                .description(description);
    }

    private String getApiVersion() {
        if (isProfileActive(PROD_PROFILE)) {
            return "v1.0.0";
        } else if (isProfileActive(DEV_PROFILE)) {
            return "v1.0.0-dev";
        } else {
            return "v1.0.0-local";
        }
    }

    private String getApiDescription() {
        StringBuilder description = new StringBuilder();
        description.append("봄봄 서비스 공식 API 서버입니다.");

        if (isProfileActive(PROD_PROFILE)) {
            description.append("\n\n**운영 환경**");
            description.append("\n- 실제 서비스용 API입니다.");
            description.append("\n- 모든 기능이 활성화되어 있습니다.");
        } else if (isProfileActive(DEV_PROFILE)) {
            description.append("\n\n**개발 환경**");
            description.append("\n- 개발 및 테스트용 API입니다.");
            description.append("\n- 일부 기능이 제한될 수 있습니다.");
        } else {
            description.append("\n\n**로컬 환경**");
            description.append("\n- 로컬 개발용 API입니다.");
            description.append("\n- 테스트 데이터를 사용합니다.");
        }

        description.append("\n\n**인증 방식**: Google OAuth2 + Session Cookie (JSESSIONID)");

        return description.toString();
    }

    private SecurityScheme createOAuth2Scheme() {
        String authUrl = getOAuthAuthorizationUrl();
        String tokenUrl = getOAuthTokenUrl();

        return new SecurityScheme()
                .type(Type.OAUTH2)
                .description(getSecurityDescription())
                .in(In.COOKIE)
                .name("JSESSIONID")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(authUrl)
                                .tokenUrl(tokenUrl)
                                .scopes(new Scopes()
                                        .addString("openid", "OpenID Connect scope")
                                        .addString("profile", "사용자 프로필 정보")
                                        .addString("email", "사용자 이메일 정보")
                                )
                        )
                );
    }

    private String getOAuthAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth";
    }

    private String getOAuthTokenUrl() {
        return "https://oauth2.googleapis.com/token";
    }

    private String getSecurityDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Google OAuth2를 통한 인증입니다. ");

        if (isProfileActive(PROD_PROFILE)) {
            desc.append("운영환경에서는 HTTPS를 통해서만 접근 가능합니다.");
        } else if (isProfileActive(DEV_PROFILE)) {
            desc.append("개발환경에서 테스트용으로 사용됩니다.");
        } else {
            desc.append("로컬환경에서는 HTTP로도 접근 가능합니다.");
        }

        desc.append("\n\n**사용법**:");
        desc.append("\n1. 'Authorize' 버튼을 클릭하여 Google 로그인을 진행합니다.");
        desc.append("\n2. 로그인 성공 후 JSESSIONID 쿠키가 자동으로 설정됩니다.");
        desc.append("\n3. 이후 모든 API 요청에서 자동으로 인증이 처리됩니다.");

        return desc.toString();
    }
}
