package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.inventory.BarcodeDTO;
import com.cheng.linegroup.service.inventory.BarcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 條碼管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/barcode")
@RequiredArgsConstructor
@Validated
@Tag(name = "條碼管理", description = "條碼產生、掃描相關 API")
public class BarcodeController {

    private final BarcodeService barcodeService;

    /**
     * 產生 QR Code
     */
    @PostMapping("/qrcode/generate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "產生 QR Code", description = "產生 QR Code 圖片")
    public ResponseEntity<byte[]> generateQRCode(@Valid @RequestBody BarcodeDTO.GenerateRequest request) {
        try {
            BarcodeDTO.GenerateResponse response = barcodeService.generateQRCode(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(response.getFileSize());
            headers.set("Content-Disposition", "inline; filename=\"qrcode.png\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response.getImageData());

        } catch (IllegalArgumentException e) {
            log.error("產生 QR Code 參數錯誤", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("產生 QR Code 失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 產生 Bar Code
     */
    @PostMapping("/barcode/generate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "產生 Bar Code", description = "產生 Bar Code 圖片")
    public ResponseEntity<byte[]> generateBarCode(@Valid @RequestBody BarcodeDTO.GenerateRequest request) {
        try {
            BarcodeDTO.GenerateResponse response = barcodeService.generateBarCode(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(response.getFileSize());
            headers.set("Content-Disposition", "inline; filename=\"barcode.png\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response.getImageData());

        } catch (IllegalArgumentException e) {
            log.error("產生 Bar Code 參數錯誤", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("產生 Bar Code 失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 掃描條碼
     */
    @PostMapping("/scan")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "掃描條碼", description = "根據條碼查詢對應物品資訊")
    public ApiResponse<BarcodeDTO.ScanResponse> scanBarcode(@Valid @RequestBody BarcodeDTO.ScanRequest request) {
        try {
            BarcodeDTO.ScanResponse response = barcodeService.scanBarcode(request);
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("掃描條碼失敗", e);
            return ApiResponse.error("掃描條碼失敗: " + e.getMessage());
        }
    }

    /**
     * 批次產生物品條碼
     */
    @PostMapping("/batch/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "批次產生物品條碼", description = "為多個物品批次產生條碼")
    public ApiResponse<BarcodeDTO.BatchGenerateResponse> batchGenerateItemBarcodes(@Valid @RequestBody BarcodeDTO.BatchGenerateRequest request) {
        try {
            BarcodeDTO.BatchGenerateResponse response = barcodeService.batchGenerateItemBarcodes(request);
            return ApiResponse.success("批次產生條碼完成", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("批次產生物品條碼失敗", e);
            return ApiResponse.error("批次產生物品條碼失敗: " + e.getMessage());
        }
    }

    /**
     * 驗證條碼
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "驗證條碼", description = "驗證條碼格式和唯一性")
    public ApiResponse<BarcodeDTO.ValidationResponse> validateBarcode(@Valid @RequestBody BarcodeDTO.ValidationRequest request) {
        try {
            BarcodeDTO.ValidationResponse response = barcodeService.validateBarcode(request);
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("驗證條碼失敗", e);
            return ApiResponse.error("驗證條碼失敗: " + e.getMessage());
        }
    }
}
