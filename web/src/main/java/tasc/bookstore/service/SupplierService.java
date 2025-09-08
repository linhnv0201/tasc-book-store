package tasc.bookstore.service;

import tasc.bookstore.dto.request.SupplierCreationRequest;
import tasc.bookstore.dto.request.SupplierUpdateRequest;
import tasc.bookstore.dto.request.UserCreationRequest;
import tasc.bookstore.dto.request.UserUpdateRequest;
import tasc.bookstore.dto.response.SupplierResponse;
import tasc.bookstore.dto.response.UserResponse;

import java.util.List;

public interface SupplierService {
    SupplierResponse createSupplier(SupplierCreationRequest request);
    SupplierResponse updateSupplier(Long id ,SupplierUpdateRequest request);
    SupplierResponse getSupplier(Long id);
    List<SupplierResponse> getAllSuppliers();
    void deleteSupplier(Long id);
}
