package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.InventoryDTO;
import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.entity.inventory.Inventory;
import com.cheng.linegroup.entity.inventory.InventoryTransaction;
import com.cheng.linegroup.entity.inventory.Item;
import com.cheng.linegroup.enums.inventory.ReferenceTypeEnum;
import com.cheng.linegroup.enums.inventory.TransactionTypeEnum;
import com.cheng.linegroup.repository.UserRepository;
import com.cheng.linegroup.repository.inventory.InventoryRepository;
import com.cheng.linegroup.repository.inventory.InventoryTransactionRepository;
import com.cheng.linegroup.repository.inventory.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 庫存服務層
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * 庫存調整
     */
    @Transactional
    public InventoryDTO adjustInventory(InventoryDTO.AdjustRequest request) {
        log.info("庫存調整，物品ID: {}, 調整數量: {}", request.getItemId(), request.getQuantity());

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        Inventory inventory = item.getInventory();
        if (inventory == null) {
            throw new IllegalArgumentException("物品庫存記錄不存在");
        }

        // 檢查庫存調整是否合理
        int newTotalQuantity = inventory.getTotalQuantity() + request.getQuantity();
        if (newTotalQuantity < 0) {
            throw new IllegalArgumentException("調整後庫存數量不能為負數");
        }

        // 檢查可用庫存是否足夠（減少庫存時）
        if (request.getQuantity() < 0) {
            int newAvailableQuantity = inventory.getAvailableQuantity() + request.getQuantity();
            if (newAvailableQuantity < 0) {
                throw new IllegalArgumentException("可用庫存不足，無法調整");
            }
        }

        // 取得當前使用者
        User currentUser = getCurrentUser();

        // 記錄庫存異動前的數量
        int beforeQuantity = inventory.getTotalQuantity();

        // 調整庫存
        inventory.adjustStock(request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        // 建立庫存異動記錄
        InventoryTransaction transaction = InventoryTransaction.createAdjustment(
                item,
                request.getQuantity(),
                beforeQuantity,
                inventory.getTotalQuantity(),
                request.getReason(),
                request.getNotes(),
                currentUser
        );
        transactionRepository.save(transaction);

        log.info("庫存調整成功，物品ID: {}, 調整後總數量: {}", request.getItemId(), inventory.getTotalQuantity());
        return convertToDTO(inventory);
    }

    /**
     * 設定庫存警告
     */
    @Transactional
    public InventoryDTO setStockLevel(InventoryDTO.SettingRequest request) {
        log.info("設定庫存警告，物品ID: {}", request.getItemId());

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        Inventory inventory = item.getInventory();
        if (inventory == null) {
            throw new IllegalArgumentException("物品庫存記錄不存在");
        }

        // 驗證庫存警告設定
        if (request.getMinStockLevel() != null && request.getMaxStockLevel() != null) {
            if (request.getMinStockLevel() >= request.getMaxStockLevel()) {
                throw new IllegalArgumentException("最低庫存警告必須小於最高庫存警告");
            }
        }

        // 更新庫存警告設定
        if (request.getMinStockLevel() != null) {
            inventory.setMinStockLevel(request.getMinStockLevel());
        }
        if (request.getMaxStockLevel() != null) {
            inventory.setMaxStockLevel(request.getMaxStockLevel());
        }

        inventory = inventoryRepository.save(inventory);
        log.info("庫存警告設定成功，物品ID: {}", request.getItemId());

        return convertToDTO(inventory);
    }

    /**
     * 根據ID查詢庫存
     */
    public InventoryDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("庫存記錄不存在"));
        return convertToDTO(inventory);
    }

    /**
     * 根據物品ID查詢庫存
     */
    public InventoryDTO getInventoryByItemId(Long itemId) {
        Inventory inventory = inventoryRepository.findByItemId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("物品庫存記錄不存在"));
        return convertToDTO(inventory);
    }

    /**
     * 分頁查詢庫存
     */
    public PageResponse<InventoryDTO> getInventories(InventoryDTO.QueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        Page<Inventory> inventoryPage = inventoryRepository.findInventoriesByConditions(
                request.getCategoryId(),
                request.getLowStock() != null ? request.getLowStock() : false,
                request.getOutOfStock() != null ? request.getOutOfStock() : false,
                request.getInStock() != null ? request.getInStock() : false,
                pageable
        );

        List<InventoryDTO> inventoryDTOs = inventoryPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(inventoryDTOs, inventoryPage.getTotalElements(), 
                              request.getPage() - 1, request.getSize());
    }

    /**
     * 取得低庫存警告列表
     */
    public List<InventoryDTO.Alert> getLowStockAlerts() {
        List<Inventory> lowStockInventories = inventoryRepository.findLowStockInventories();
        return lowStockInventories.stream()
                .map(this::convertToAlert)
                .collect(Collectors.toList());
    }

    /**
     * 取得缺貨警告列表
     */
    public List<InventoryDTO.Alert> getOutOfStockAlerts() {
        List<Inventory> outOfStockInventories = inventoryRepository.findOutOfStockInventories();
        return outOfStockInventories.stream()
                .map(this::convertToAlert)
                .collect(Collectors.toList());
    }

    /**
     * 取得庫存統計資訊
     */
    public InventoryDTO.Statistics getInventoryStatistics() {
        long totalItemTypes = inventoryRepository.count();
        long totalQuantity = inventoryRepository.getTotalQuantity();
        long totalAvailableQuantity = inventoryRepository.getTotalAvailableQuantity();
        long totalBorrowedQuantity = inventoryRepository.getTotalBorrowedQuantity();
        long totalDamagedQuantity = inventoryRepository.getTotalDamagedQuantity();
        long lowStockItemCount = inventoryRepository.countLowStockItems();
        long outOfStockItemCount = inventoryRepository.countOutOfStockItems();

        return InventoryDTO.Statistics.builder()
                .totalItemTypes(totalItemTypes)
                .totalQuantity(totalQuantity)
                .totalAvailableQuantity(totalAvailableQuantity)
                .totalBorrowedQuantity(totalBorrowedQuantity)
                .totalDamagedQuantity(totalDamagedQuantity)
                .lowStockItemCount(lowStockItemCount)
                .outOfStockItemCount(outOfStockItemCount)
                .turnoverRate(calculateTurnoverRate())
                .statisticsTime(LocalDateTime.now())
                .build();
    }

    /**
     * 批次庫存調整
     */
    @Transactional
    public List<InventoryDTO> batchAdjustInventory(List<InventoryDTO.AdjustRequest> requests) {
        log.info("批次庫存調整，調整項目數: {}", requests.size());

        return requests.stream()
                .map(this::adjustInventory)
                .collect(Collectors.toList());
    }

    /**
     * 庫存盤點
     */
    @Transactional
    public List<InventoryDTO> stockTaking(List<InventoryDTO.AdjustRequest> adjustments) {
        log.info("庫存盤點，調整項目數: {}", adjustments.size());

        User currentUser = getCurrentUser();
        
        return adjustments.stream()
                .map(adjustment -> {
                    adjustment.setReason("庫存盤點");
                    return adjustInventory(adjustment);
                })
                .collect(Collectors.toList());
    }

    /**
     * 計算庫存週轉率
     */
    private Double calculateTurnoverRate() {
        // 簡化計算：出庫總量 / 平均庫存
        // 實際應用中可能需要更複雜的計算邏輯
        // 計算所有物品的總出庫量（這裡需要重新設計邏輯）
        long totalOutbound = inventoryRepository.getTotalBorrowedQuantity();
        long averageStock = inventoryRepository.getTotalQuantity();
        
        return averageStock > 0 ? (double) totalOutbound / averageStock : 0.0;
    }

    /**
     * 取得當前使用者
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return userRepository.findByUsername(authentication.getName())
                    .orElse(null);
        }
        return null;
    }

    /**
     * 轉換為 DTO
     */
    private InventoryDTO convertToDTO(Inventory inventory) {
        Item item = inventory.getItem();
        
        return InventoryDTO.builder()
                .id(inventory.getId())
                .itemId(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getName())
                .categoryName(item.getCategory().getName())
                .totalQuantity(inventory.getTotalQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .borrowedQuantity(inventory.getBorrowedQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .damagedQuantity(inventory.getDamagedQuantity())
                .minStockLevel(inventory.getMinStockLevel())
                .maxStockLevel(inventory.getMaxStockLevel())
                .inStock(inventory.isInStock())
                .lowStock(inventory.isLowStock())
                .outOfStock(inventory.isOutOfStock())
                .stockStatus(getStockStatus(inventory))
                .lastTransactionDate(inventory.getLastTransactionDate())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    /**
     * 轉換為警告 DTO
     */
    private InventoryDTO.Alert convertToAlert(Inventory inventory) {
        Item item = inventory.getItem();
        String alertType = inventory.isOutOfStock() ? "OUT_OF_STOCK" : "LOW_STOCK";
        String alertMessage = inventory.isOutOfStock() ? "庫存已用完，請及時補貨" : "庫存不足，請及時補貨";
        
        return InventoryDTO.Alert.builder()
                .itemId(item.getId())
                .itemCode(item.getItemCode())
                .itemName(item.getName())
                .categoryName(item.getCategory().getName())
                .currentQuantity(inventory.getAvailableQuantity())
                .minStockLevel(inventory.getMinStockLevel())
                .alertType(alertType)
                .alertMessage(alertMessage)
                .alertTime(LocalDateTime.now())
                .build();
    }

    /**
     * 取得庫存狀態描述
     */
    private String getStockStatus(Inventory inventory) {
        if (inventory.isOutOfStock()) {
            return "缺貨";
        } else if (inventory.isLowStock()) {
            return "低庫存";
        } else {
            return "正常";
        }
    }
}
