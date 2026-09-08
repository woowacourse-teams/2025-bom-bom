package me.bombom.api.v1.inquiry.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.dto.InquiryCategoryResponse;
import me.bombom.api.v1.inquiry.service.InquiryCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries/categories")
public class InquiryCategoryController implements InquiryCategoryControllerApi {

    private final InquiryCategoryService inquiryCategoryService;

    @Override
    @GetMapping
    public List<InquiryCategoryResponse> getCategories() {
        return inquiryCategoryService.getCategories();
    }
}
