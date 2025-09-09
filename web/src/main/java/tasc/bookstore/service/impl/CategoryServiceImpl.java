package tasc.bookstore.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tasc.bookstore.dto.request.CategoryCreateVsUpdateRequest;
import tasc.bookstore.dto.response.CategoryResponse;
import tasc.bookstore.entity.Category;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.repository.CategoryRepository;
import tasc.bookstore.service.CategoryService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryCreateVsUpdateRequest request) {
        if(categoryRepository.existsByName(request.getCategoryName())){
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        Category category = new Category();
        category.setName(request.getCategoryName());
        categoryRepository.save(category);

        CategoryResponse categoryResponse = new CategoryResponse();
        category.setName(category.getName());
        return categoryResponse;
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryName(category.getName());
        return categoryResponse;
    }


    @Override
    public CategoryResponse updateCategory(Long id, CategoryCreateVsUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if(!category.getName().equals(request.getCategoryName()) &&
                categoryRepository.existsByName(request.getCategoryName())){
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        category.setName(request.getCategoryName());
        categoryRepository.save(category);

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryName(category.getName());
        return categoryResponse;
    }

    @Override
    public void deleteCategory(Long id) {
        if(categoryRepository.existsById(id)){
            categoryRepository.deleteById(id);
        } else {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponse> categoriesResponse = new ArrayList<>();

        for (Category category : categories) {
            CategoryResponse response = new CategoryResponse();
            response.setCategoryName(category.getName());
            categoriesResponse.add(response);
        }
        return categoriesResponse;
    }

}
