package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.CategoryCreateVsUpdateRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.CategoryResponse;
import tasc.bookstore.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryCreateVsUpdateRequest request) {
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully added category");
        apiResponse.setResult(categoryService.createCategory(request));
        return apiResponse;
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody CategoryCreateVsUpdateRequest request) {
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully updated category");
        apiResponse.setResult(categoryService.updateCategory(id, request));
        return apiResponse;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully deleted category");
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        ApiResponse<List<CategoryResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.getAllCategories());
        return apiResponse;
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable Long id) {
        ApiResponse<CategoryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(categoryService.getCategoryById(id));
        return apiResponse;
    }
}
