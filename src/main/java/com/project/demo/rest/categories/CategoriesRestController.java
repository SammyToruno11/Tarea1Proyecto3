package com.project.demo.rest.categories;

import com.project.demo.logic.entity.categories.Categories;
import com.project.demo.logic.entity.categories.CategoriesRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoriesRestController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Categories> categoriesPage = categoriesRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoriesPage.getTotalPages());
        meta.setTotalElements(categoriesPage.getTotalElements());
        meta.setPageNumber(categoriesPage.getNumber() + 1);
        meta.setPageSize(categoriesPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Categories retrieved successfully",
                categoriesPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> createCategory(@RequestBody Categories categories,
                                            HttpServletRequest request) {
        Categories savedCategories = categoriesRepository.save(categories);
        return new GlobalResponseHandler().handleResponse(
                "Category created successfully",
                savedCategories,
                HttpStatus.CREATED,
                request
        );
    }

    @PutMapping("/{categoriesId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoriesId,
                                            @RequestBody Categories categories,
                                            HttpServletRequest request) {
        Optional<Categories> foundCategories = categoriesRepository.findById(categoriesId);

        if (foundCategories.isPresent()) {
            Categories existingCategory = foundCategories.get();

            existingCategory.setName(categories.getName());
            existingCategory.setDescription(categories.getDescription());

            Categories updatedCategory = categoriesRepository.save(existingCategory);

            return new GlobalResponseHandler().handleResponse(
                    "Category updated successfully",
                    updatedCategory,
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Category id " + categoriesId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }
    
    @DeleteMapping("/{categoriesId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and isAuthenticated()")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoriesId,
                                            HttpServletRequest request) {
        Optional<Categories> foundCategories = categoriesRepository.findById(categoriesId);

        if (foundCategories.isPresent()) {
            categoriesRepository.deleteById(categoriesId);
            return new GlobalResponseHandler().handleResponse(
                    "Category deleted successfully",
                    foundCategories.get(),
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Category id " + categoriesId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }
}
