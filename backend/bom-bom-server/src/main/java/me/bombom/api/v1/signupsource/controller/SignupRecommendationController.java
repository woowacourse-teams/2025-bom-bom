package me.bombom.api.v1.signupsource.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.signupsource.dto.response.SignupRecommendationsResponse;
import me.bombom.api.v1.signupsource.service.SignupRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/newsletters/signup-recommendations")
public class SignupRecommendationController implements SignupRecommendationControllerApi {

    private final SignupRecommendationService signupRecommendationService;

    @Override
    @GetMapping
    public SignupRecommendationsResponse getSignupRecommendations() {
        return signupRecommendationService.getRecommendations();
    }
}
