package tasc.bookstore.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.SupplierCreationRequest;
import tasc.bookstore.dto.request.SupplierUpdateRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.SupplierResponse;
import tasc.bookstore.service.SupplierService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupplierController {

    SupplierService supplierService;

    // Create supplier
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupplierResponse> createSupplier(@RequestBody SupplierCreationRequest request) {
        ApiResponse<SupplierResponse> response = new ApiResponse<>();
        response.setResult(supplierService.createSupplier(request));
        return response;
    }

    // Update supplier
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierUpdateRequest request) {
        ApiResponse<SupplierResponse> response = new ApiResponse<>();
        response.setResult(supplierService.updateSupplier(id, request));
        return response;
    }

    // Get supplier by id
    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> getSupplier(@PathVariable Long id) {
        ApiResponse<SupplierResponse> response = new ApiResponse<>();
        response.setResult(supplierService.getSupplier(id));
        return response;
    }

    // Get all suppliers
    @GetMapping
    public ApiResponse<List<SupplierResponse>> getAllSuppliers() {
        ApiResponse<List<SupplierResponse>> response = new ApiResponse<>();
        response.setResult(supplierService.getAllSuppliers());
        return response;
    }

    // Delete supplier
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage("Supplier deleted successfully");
        return response;
    }
}
