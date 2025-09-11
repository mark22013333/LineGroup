package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.InventoryTransactionDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 庫存異動記錄服務層
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    /**
     * 建立庫存異動記錄
     */
    @Transactional
    public InventoryTransactionDTO createTransaction(InventoryTransactionDTO.CreateRequest request) {
        log.info("建立庫存異動記錄，物品ID: {}, 異動類型: {}, 數量: {}", 
                request.getItemId(), request.getTransactionType(), request.getQuantity());

        // 驗證物品
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        // 取得庫存記錄
        Inventory inventory = item.getInventory();
        if (inventory == null) {
            throw new IllegalArgumentException("物品庫存記錄不存在");
        }

        // 取得當前使用者
        User currentUser = getCurrentUser();

        // 記錄異動前數量
        int beforeQuantity = inventory.getTotalQuantity();

        // 驗證異動數量
        validateTransactionQuantity(request.getTransactionType(), request.getQuantity(), inventory);

        // 產生異動單號
        String transactionNumber = generateTransactionNumber(request.getTransactionType());

        // 建立異動記錄
        InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionNumber(transactionNumber)
                .item(item)
                .transactionType(request.getTransactionType())
                .quantity(request.getQuantity())
                .beforeQuantity(beforeQuantity)
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .referenceNumber(request.getReferenceNumber())
                .reason(request.getReason())
                .notes(request.getNotes())
                .processedBy(currentUser)
                .transactionDate(LocalDateTime.now())
                .build();

        // 更新庫存
        updateInventoryByTransaction(inventory, request.getTransactionType(), request.getQuantity());
        
        // 設定異動後數量
        transaction.setAfterQuantity(inventory.getTotalQuantity());

        transaction = transactionRepository.save(transaction);
        inventoryRepository.save(inventory);

        log.info("庫存異動記錄建立成功，異動單號: {}", transactionNumber);
        return convertToDTO(transaction);
    }

    /**
     * 批次建立庫存異動記錄
     */
    @Transactional
    public List<InventoryTransactionDTO> createBatchTransactions(InventoryTransactionDTO.BatchRequest request) {
        log.info("批次建立庫存異動記錄，異動項目數: {}", request.getTransactions().size());

        return request.getTransactions().stream()
                .map(transactionRequest -> {
                    // 為批次異動添加統一的原因和備註
                    if (request.getBatchReason() != null) {
                        transactionRequest.setReason(request.getBatchReason());
                    }
                    if (request.getBatchNotes() != null) {
                        transactionRequest.setNotes(request.getBatchNotes());
                    }
                    return createTransaction(transactionRequest);
                })
                .collect(Collectors.toList());
    }

    /**
     * 審核異動記錄
     */
    @Transactional
    public InventoryTransactionDTO approveTransaction(Long transactionId, InventoryTransactionDTO.ApprovalRequest request) {
        log.info("審核庫存異動記錄，ID: {}, 審核結果: {}", transactionId, request.getApproved());

        InventoryTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("異動記錄不存在"));

        if (transaction.getApprovedBy() != null) {
            throw new IllegalArgumentException("該異動記錄已審核");
        }

        User currentUser = getCurrentUser();
        
        if (request.getApproved()) {
            transaction.setApprovedBy(currentUser);
            transaction.setApprovedDate(LocalDateTime.now());
            log.info("異動記錄審核通過，ID: {}", transactionId);
        } else {
            // 如果審核不通過，需要回滾庫存變更
            rollbackInventoryTransaction(transaction);
            transaction.setApprovedBy(currentUser);
            transaction.setApprovedDate(LocalDateTime.now());
            transaction.setNotes(transaction.getNotes() + " [審核不通過: " + request.getApprovalNotes() + "]");
            log.info("異動記錄審核不通過，已回滾庫存，ID: {}", transactionId);
        }

        transaction = transactionRepository.save(transaction);
        return convertToDTO(transaction);
    }

    /**
     * 根據ID查詢異動記錄
     */
    public InventoryTransactionDTO getTransactionById(Long id) {
        InventoryTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("異動記錄不存在"));
        return convertToDTO(transaction);
    }

    /**
     * 分頁查詢異動記錄
     */
    public PageResponse<InventoryTransactionDTO> getTransactions(InventoryTransactionDTO.QueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        Page<InventoryTransaction> transactionPage = transactionRepository.findTransactionsByConditions(
                request.getItemId(),
                request.getTransactionType(),
                request.getReferenceType(),
                request.getProcessedById(),
                request.getTransactionStartDate(),
                request.getTransactionEndDate(),
                pageable
        );

        List<InventoryTransactionDTO> transactionDTOs = transactionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(transactionDTOs, transactionPage.getTotalElements(), 
                              request.getPage() - 1, request.getSize());
    }

    /**
     * 取得物品的異動歷史
     */
    public List<InventoryTransactionDTO> getItemTransactionHistory(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        List<InventoryTransaction> transactions = transactionRepository.findTop10ByItemOrderByTransactionDateDesc(item);
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得待審核的異動記錄
     */
    public List<InventoryTransactionDTO> getPendingApprovalTransactions() {
        List<InventoryTransaction> transactions = transactionRepository.findPendingApprovalTransactions();
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得異動統計資訊
     */
    public InventoryTransactionDTO.Statistics getTransactionStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> typeStats = transactionRepository.getTransactionTypeStatistics(startDateTime, endDateTime);
        
        long totalTransactionCount = 0;
        long inboundCount = 0;
        long outboundCount = 0;
        long adjustmentCount = 0;
        long damagedCount = 0;
        long lostCount = 0;

        for (Object[] stat : typeStats) {
            TransactionTypeEnum type = (TransactionTypeEnum) stat[0];
            Long count = (Long) stat[1];
            totalTransactionCount += count;

            switch (type) {
                case IN:
                    inboundCount = count;
                    break;
                case OUT:
                    outboundCount = count;
                    break;
                case ADJUST:
                    adjustmentCount = count;
                    break;
                case DAMAGED:
                    damagedCount = count;
                    break;
                case LOST:
                    lostCount = count;
                    break;
            }
        }

        return InventoryTransactionDTO.Statistics.builder()
                .totalTransactionCount(totalTransactionCount)
                .inboundCount(inboundCount)
                .outboundCount(outboundCount)
                .adjustmentCount(adjustmentCount)
                .damagedCount(damagedCount)
                .lostCount(lostCount)
                .statisticsTime(LocalDateTime.now())
                .build();
    }

    /**
     * 取得異動趨勢
     */
    public List<InventoryTransactionDTO.Trend> getTransactionTrends(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> trends = transactionRepository.getTransactionTrends(startDateTime, endDateTime);
        
        return trends.stream()
                .map(trend -> InventoryTransactionDTO.Trend.builder()
                        .date((String) trend[0])
                        .transactionType((TransactionTypeEnum) trend[1])
                        .transactionCount((Long) trend[2])
                        .totalQuantity((Long) trend[3])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 驗證異動數量
     */
    private void validateTransactionQuantity(TransactionTypeEnum transactionType, Integer quantity, Inventory inventory) {
        switch (transactionType) {
            case OUT:
                if (quantity > 0) {
                    throw new IllegalArgumentException("出庫數量應為負數");
                }
                if (Math.abs(quantity) > inventory.getAvailableQuantity()) {
                    throw new IllegalArgumentException("出庫數量超過可用庫存");
                }
                break;
            case IN:
                if (quantity <= 0) {
                    throw new IllegalArgumentException("入庫數量應為正數");
                }
                break;
            case ADJUST:
                int newQuantity = inventory.getTotalQuantity() + quantity;
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("調整後庫存數量不能為負數");
                }
                break;
            case DAMAGED:
            case LOST:
                if (quantity > 0) {
                    throw new IllegalArgumentException("損壞/遺失數量應為負數");
                }
                if (Math.abs(quantity) > inventory.getAvailableQuantity()) {
                    throw new IllegalArgumentException("損壞/遺失數量超過可用庫存");
                }
                break;
        }
    }

    /**
     * 根據異動類型更新庫存
     */
    private void updateInventoryByTransaction(Inventory inventory, TransactionTypeEnum transactionType, Integer quantity) {
        switch (transactionType) {
            case IN:
                inventory.addStock(quantity);
                break;
            case OUT:
                inventory.removeStock(Math.abs(quantity));
                break;
            case ADJUST:
                inventory.adjustStock(quantity);
                break;
            case DAMAGED:
                inventory.markAsDamaged(Math.abs(quantity));
                break;
            case LOST:
                inventory.markAsLost(Math.abs(quantity));
                break;
        }
        inventory.setLastTransactionDate(LocalDateTime.now());
    }

    /**
     * 回滾庫存異動
     */
    private void rollbackInventoryTransaction(InventoryTransaction transaction) {
        Inventory inventory = transaction.getItem().getInventory();
        TransactionTypeEnum transactionType = transaction.getTransactionType();
        Integer quantity = transaction.getQuantity();

        // 執行相反的操作來回滾
        switch (transactionType) {
            case IN:
                inventory.removeStock(quantity);
                break;
            case OUT:
                inventory.addStock(Math.abs(quantity));
                break;
            case ADJUST:
                inventory.adjustStock(-quantity);
                break;
            case DAMAGED:
                // 從損壞庫存回復到可用庫存
                inventory.setDamagedQuantity(inventory.getDamagedQuantity() - Math.abs(quantity));
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + Math.abs(quantity));
                break;
            case LOST:
                // 遺失無法回滾，記錄日誌
                log.warn("無法回滾遺失異動，異動ID: {}", transaction.getId());
                break;
        }

        inventoryRepository.save(inventory);
    }

    /**
     * 產生異動單號
     */
    private String generateTransactionNumber(TransactionTypeEnum transactionType) {
        String prefix = "IT";
        String date = LocalDate.now().toString().replace("-", "");
        long count = transactionRepository.count() + 1;
        return String.format("%s%s%06d", prefix, date, count);
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
    private InventoryTransactionDTO convertToDTO(InventoryTransaction transaction) {
        return InventoryTransactionDTO.builder()
                .id(transaction.getId())
                .transactionNumber(transaction.getTransactionNumber())
                .itemId(transaction.getItem().getId())
                .itemCode(transaction.getItem().getItemCode())
                .itemName(transaction.getItem().getName())
                .categoryName(transaction.getItem().getCategory().getName())
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .beforeQuantity(transaction.getBeforeQuantity())
                .afterQuantity(transaction.getAfterQuantity())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .referenceNumber(transaction.getReferenceNumber())
                .reason(transaction.getReason())
                .notes(transaction.getNotes())
                .processedById(transaction.getProcessedBy() != null ? transaction.getProcessedBy().getId() : null)
                .processedByName(transaction.getProcessedBy() != null ? transaction.getProcessedBy().getUsername() : null)
                .approvedById(transaction.getApprovedBy() != null ? transaction.getApprovedBy().getId() : null)
                .approvedByName(transaction.getApprovedBy() != null ? transaction.getApprovedBy().getUsername() : null)
                .transactionDate(transaction.getTransactionDate())
                .approvedDate(transaction.getApprovedDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
