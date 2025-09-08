package tasc.bookstore.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tasc.bookstore.dto.request.SupplierCreationRequest;
import tasc.bookstore.dto.request.SupplierUpdateRequest;
import tasc.bookstore.dto.response.SupplierResponse;
import tasc.bookstore.entity.Supplier;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.mapper.SupplierMapper;
import tasc.bookstore.repository.SupplierRepository;
import tasc.bookstore.service.SupplierService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupplierServiceImpl implements SupplierService {

    SupplierRepository supplierRepository;
    SupplierMapper supplierMapper;

    @Override
    public SupplierResponse createSupplier(SupplierCreationRequest request) {
        if (supplierRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.SUPPLIER_EXISTED);
        }
        Supplier supplier = supplierMapper.toCreateSupplier(request);
        supplier = supplierRepository.save(supplier);

        return supplierMapper.toSupplierResponse(supplier);
    }

    @Override
    public SupplierResponse updateSupplier(Long id, SupplierUpdateRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        if (!supplier.getName().equals(request.getName()) &&
                supplierRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.SUPPLIER_EXISTED);
        }

        supplierMapper.toUpdateSupplier(supplier, request);
        supplier = supplierRepository.save(supplier);
        return supplierMapper.toSupplierResponse(supplier);
    }

    @Override
    public SupplierResponse getSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));
        return supplierMapper.toSupplierResponse(supplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));
        supplierRepository.delete(supplier);
    }

    @Override
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toSupplierResponse)
                .toList();
    }
}
