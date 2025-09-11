package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.inventory.BarcodeDTO;
import com.cheng.linegroup.entity.inventory.Item;
import com.cheng.linegroup.repository.inventory.ItemRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 條碼服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarcodeService {

    private final ItemRepository itemRepository;

    /**
     * 產生 QR Code
     */
    public BarcodeDTO.GenerateResponse generateQRCode(BarcodeDTO.GenerateRequest request) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    request.getContent(),
                    BarcodeFormat.QR_CODE,
                    request.getWidth() != null ? request.getWidth() : 200,
                    request.getHeight() != null ? request.getHeight() : 200,
                    hints
            );

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", outputStream);
            byte[] imageData = outputStream.toByteArray();
            outputStream.close();

            return BarcodeDTO.GenerateResponse.builder()
                    .content(request.getContent())
                    .format("QR_CODE")
                    .width(bitMatrix.getWidth())
                    .height(bitMatrix.getHeight())
                    .imageData(imageData)
                    .contentType("image/png")
                    .fileSize((long) imageData.length)
                    .build();

        } catch (WriterException | IOException e) {
            log.error("產生 QR Code 失敗", e);
            throw new RuntimeException("產生 QR Code 失敗: " + e.getMessage());
        }
    }

    /**
     * 產生 Bar Code (Code128)
     */
    public BarcodeDTO.GenerateResponse generateBarCode(BarcodeDTO.GenerateRequest request) {
        try {
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(
                    request.getContent(),
                    BarcodeFormat.CODE_128,
                    request.getWidth() != null ? request.getWidth() : 300,
                    request.getHeight() != null ? request.getHeight() : 100
            );

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", outputStream);
            byte[] imageData = outputStream.toByteArray();
            outputStream.close();

            return BarcodeDTO.GenerateResponse.builder()
                    .content(request.getContent())
                    .format("CODE_128")
                    .width(bitMatrix.getWidth())
                    .height(bitMatrix.getHeight())
                    .imageData(imageData)
                    .contentType("image/png")
                    .fileSize((long) imageData.length)
                    .build();

        } catch (IOException e) {
            log.error("產生 Bar Code 失敗", e);
            throw new RuntimeException("產生 Bar Code 失敗: " + e.getMessage());
        }
    }

    /**
     * 根據條碼查詢物品
     */
    public BarcodeDTO.ScanResponse scanBarcode(BarcodeDTO.ScanRequest request) {
        try {
            String barcode = request.getBarcode().trim();
            
            // 先嘗試用條碼查詢
            Optional<Item> itemByBarcode = itemRepository.findByBarcode(barcode);
            if (itemByBarcode.isPresent()) {
                return createScanResponse(itemByBarcode.get(), "BARCODE");
            }

            // 再嘗試用物品代碼查詢
            Optional<Item> itemByCode = itemRepository.findByCode(barcode);
            if (itemByCode.isPresent()) {
                return createScanResponse(itemByCode.get(), "ITEM_CODE");
            }

            // 都找不到
            return BarcodeDTO.ScanResponse.builder()
                    .barcode(barcode)
                    .found(false)
                    .message("找不到對應的物品")
                    .build();

        } catch (Exception e) {
            log.error("掃描條碼失敗", e);
            throw new RuntimeException("掃描條碼失敗: " + e.getMessage());
        }
    }

    /**
     * 批次產生物品條碼
     */
    @Transactional
    public BarcodeDTO.BatchGenerateResponse batchGenerateItemBarcodes(BarcodeDTO.BatchGenerateRequest request) {
        try {
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            for (Long itemId : request.getItemIds()) {
                try {
                    Optional<Item> itemOpt = itemRepository.findById(itemId);
                    if (itemOpt.isPresent()) {
                        Item item = itemOpt.get();
                        
                        // 如果物品還沒有條碼，自動產生一個
                        if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
                            String barcode = generateItemBarcode(item);
                            item.setBarcode(barcode);
                            itemRepository.save(item);
                        }
                        
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.append("物品ID ").append(itemId).append(" 不存在; ");
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.append("物品ID ").append(itemId).append(" 處理失敗: ").append(e.getMessage()).append("; ");
                }
            }

            return BarcodeDTO.BatchGenerateResponse.builder()
                    .totalCount(request.getItemIds().size())
                    .successCount(successCount)
                    .failCount(failCount)
                    .errorMessages(errorMessages.toString())
                    .build();

        } catch (Exception e) {
            log.error("批次產生物品條碼失敗", e);
            throw new RuntimeException("批次產生物品條碼失敗: " + e.getMessage());
        }
    }

    /**
     * 驗證條碼格式
     */
    public BarcodeDTO.ValidationResponse validateBarcode(BarcodeDTO.ValidationRequest request) {
        try {
            String barcode = request.getBarcode().trim();
            boolean isValid = true;
            String message = "條碼格式正確";

            // 基本格式檢查
            if (barcode.isEmpty()) {
                isValid = false;
                message = "條碼不能為空";
            } else if (barcode.length() < 3) {
                isValid = false;
                message = "條碼長度太短";
            } else if (barcode.length() > 50) {
                isValid = false;
                message = "條碼長度太長";
            } else if (!barcode.matches("^[A-Za-z0-9\\-_]+$")) {
                isValid = false;
                message = "條碼只能包含字母、數字、連字號和底線";
            }

            // 檢查是否已存在
            boolean exists = false;
            if (isValid) {
                exists = itemRepository.existsByBarcode(barcode);
                if (exists && request.getExcludeItemId() != null) {
                    // 排除指定物品ID的檢查
                    Optional<Item> existingItem = itemRepository.findByBarcode(barcode);
                    if (existingItem.isPresent() && 
                        existingItem.get().getId().equals(request.getExcludeItemId())) {
                        exists = false;
                    }
                }
            }

            return BarcodeDTO.ValidationResponse.builder()
                    .barcode(barcode)
                    .isValid(isValid)
                    .exists(exists)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("驗證條碼失敗", e);
            throw new RuntimeException("驗證條碼失敗: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private BarcodeDTO.ScanResponse createScanResponse(Item item, String matchType) {
        return BarcodeDTO.ScanResponse.builder()
                .barcode(item.getBarcode())
                .found(true)
                .matchType(matchType)
                .itemId(item.getId())
                .itemCode(item.getCode())
                .itemName(item.getName())
                .categoryName(item.getCategory().getName())
                .currentStock(item.getInventory() != null ? item.getInventory().getCurrentQuantity() : 0)
                .availableStock(item.getInventory() != null ? item.getInventory().getAvailableQuantity() : 0)
                .location(item.getLocation())
                .status(item.getStatus().name())
                .message("找到對應物品")
                .build();
    }

    private String generateItemBarcode(Item item) {
        // 產生格式: INV + 分類代碼前3位 + 物品ID補零到6位
        String categoryPrefix = item.getCategory().getCode().length() >= 3 ? 
                item.getCategory().getCode().substring(0, 3) : 
                item.getCategory().getCode();
        
        return String.format("INV%s%06d", categoryPrefix.toUpperCase(), item.getId());
    }
}
