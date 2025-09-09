package tasc.bookstore.service;

import tasc.bookstore.dto.request.CategoryCreateVsUpdateRequest;
import tasc.bookstore.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateVsUpdateRequest request);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryCreateVsUpdateRequest request);
    void deleteCategory(Long id);
    List<CategoryResponse> getAllCategories();
}
