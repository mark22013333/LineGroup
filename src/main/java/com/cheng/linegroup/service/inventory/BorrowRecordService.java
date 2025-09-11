package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.BorrowRecordDTO;
import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.entity.inventory.BorrowRecord;
import com.cheng.linegroup.entity.inventory.Inventory;
import com.cheng.linegroup.entity.inventory.InventoryTransaction;
import com.cheng.linegroup.entity.inventory.Item;
import com.cheng.linegroup.enums.inventory.BorrowStatusEnum;
import com.cheng.linegroup.enums.inventory.ReferenceTypeEnum;
import com.cheng.linegroup.enums.inventory.ReturnConditionEnum;
import com.cheng.linegroup.enums.inventory.TransactionTypeEnum;
import com.cheng.linegroup.repository.UserRepository;
import com.cheng.linegroup.repository.inventory.BorrowRecordRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 借還記錄服務層
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;

    /**
     * 建立借用記錄
     */
    @Transactional
    public BorrowRecordDTO createBorrowRecord(BorrowRecordDTO.CreateRequest request) {
        log.info("建立借用記錄，物品ID: {}, 借用人ID: {}, 數量: {}", 
                request.getItemId(), request.getBorrowerId(), request.getQuantity());

        // 驗證物品
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        if (!item.getEnabled()) {
            throw new IllegalArgumentException("物品已停用，無法借用");
        }

        if (!item.getBorrowable()) {
            throw new IllegalArgumentException("物品不可借用");
        }

        // 驗證借用人
        User borrower = userRepository.findById(request.getBorrowerId())
                .orElseThrow(() -> new IllegalArgumentException("借用人不存在"));

        if (!borrower.getEnabled()) {
            throw new IllegalArgumentException("借用人帳號已停用");
        }

        // 檢查借用人是否有逾期記錄
        if (borrowRecordRepository.hasOverdueRecords(borrower)) {
            throw new IllegalArgumentException("借用人有逾期記錄，無法借用新物品");
        }

        // 檢查庫存
        Inventory inventory = item.getInventory();
        if (inventory == null || inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("庫存不足，無法借用");
        }

        // 檢查借用期限
        if (item.getMaxBorrowDays() != null) {
            long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), request.getExpectedReturnDate());
            if (daysBetween > item.getMaxBorrowDays()) {
                throw new IllegalArgumentException("借用期限超過物品最大借用天數: " + item.getMaxBorrowDays() + " 天");
            }
        }

        // 取得當前處理人
        User processor = getCurrentUser();

        // 產生借還單號
        String recordNumber = generateRecordNumber();

        // 建立借用記錄
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .recordNumber(recordNumber)
                .item(item)
                .borrower(borrower)
                .quantity(request.getQuantity())
                .borrowDate(LocalDateTime.now())
                .expectedReturnDate(request.getExpectedReturnDate())
                .status(BorrowStatusEnum.BORROWED)
                .borrowPurpose(request.getPurpose())
                .borrowNotes(request.getBorrowNotes())
                .processedBy(processor)
                .build();

        borrowRecord = borrowRecordRepository.save(borrowRecord);

        // 更新庫存
        inventory.borrowItems(request.getQuantity());
        inventoryRepository.save(inventory);

        // 建立庫存異動記錄
        InventoryTransaction transaction = InventoryTransaction.createBorrow(
                item,
                request.getQuantity(),
                inventory.getTotalQuantity() + request.getQuantity(),
                inventory.getTotalQuantity(),
                ReferenceTypeEnum.BORROW,
                borrowRecord.getId(),
                borrowRecord.getRecordNumber(),
                processor
        );
        transactionRepository.save(transaction);

        log.info("借用記錄建立成功，借還單號: {}", recordNumber);
        return convertToDTO(borrowRecord);
    }

    /**
     * 歸還物品
     */
    @Transactional
    public BorrowRecordDTO returnItem(Long borrowRecordId, BorrowRecordDTO.ReturnRequest request) {
        log.info("歸還物品，借用記錄ID: {}, 歸還數量: {}", borrowRecordId, request.getReturnQuantity());

        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new IllegalArgumentException("借用記錄不存在"));

        if (borrowRecord.getStatus() == BorrowStatusEnum.RETURNED) {
            throw new IllegalArgumentException("該記錄已完全歸還");
        }

        // 檢查歸還數量
        int remainingQuantity = borrowRecord.getQuantity() - 
                (borrowRecord.getReturnQuantity() != null ? borrowRecord.getReturnQuantity() : 0);
        
        if (request.getReturnQuantity() > remainingQuantity) {
            throw new IllegalArgumentException("歸還數量超過剩餘未歸還數量");
        }

        // 取得當前處理人
        User processor = getCurrentUser();

        // 更新借用記錄
        int newReturnQuantity = (borrowRecord.getReturnQuantity() != null ? borrowRecord.getReturnQuantity() : 0) 
                + request.getReturnQuantity();
        
        borrowRecord.setReturnQuantity(newReturnQuantity);
        borrowRecord.setActualReturnDate(LocalDateTime.now());
        borrowRecord.setReturnCondition(request.getReturnCondition());
        borrowRecord.setReturnNotes(request.getReturnNotes());
        borrowRecord.setReturnProcessedBy(processor);

        // 計算罰款
        if (request.getPenaltyAmount() != null && request.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0) {
            borrowRecord.setPenaltyAmount(request.getPenaltyAmount());
            borrowRecord.setPenaltyPaid(false);
        }

        // 更新狀態
        if (newReturnQuantity == borrowRecord.getQuantity()) {
            borrowRecord.setStatus(BorrowStatusEnum.RETURNED);
        } else {
            borrowRecord.setStatus(BorrowStatusEnum.PARTIAL_RETURNED);
        }

        borrowRecord = borrowRecordRepository.save(borrowRecord);

        // 更新庫存
        Inventory inventory = borrowRecord.getItem().getInventory();
        
        if (request.getReturnCondition() == ReturnConditionEnum.GOOD) {
            // 狀況良好，歸還到可用庫存
            inventory.returnItems(request.getReturnQuantity());
        } else if (request.getReturnCondition() == ReturnConditionEnum.DAMAGED) {
            // 損壞，歸還到損壞庫存
            inventory.returnDamagedItems(request.getReturnQuantity());
        } else if (request.getReturnCondition() == ReturnConditionEnum.LOST) {
            // 遺失，不歸還庫存，直接減少借出數量
            inventory.setBorrowedQuantity(inventory.getBorrowedQuantity() - request.getReturnQuantity());
        }

        inventoryRepository.save(inventory);

        // 建立庫存異動記錄
        TransactionTypeEnum transactionType = request.getReturnCondition() == ReturnConditionEnum.GOOD 
                ? TransactionTypeEnum.IN : TransactionTypeEnum.DAMAGED;
        
        InventoryTransaction transaction = InventoryTransaction.createReturn(
                borrowRecord.getItem(),
                request.getReturnQuantity(),
                inventory.getTotalQuantity() - request.getReturnQuantity(),
                inventory.getTotalQuantity(),
                ReferenceTypeEnum.RETURN,
                borrowRecord.getId(),
                borrowRecord.getRecordNumber(),
                processor
        );
        transactionRepository.save(transaction);

        log.info("物品歸還成功，借還單號: {}", borrowRecord.getRecordNumber());
        return convertToDTO(borrowRecord);
    }

    /**
     * 條碼掃描借用
     */
    @Transactional
    public BorrowRecordDTO barcodeBorrow(BorrowRecordDTO.BarcodeBorrowRequest request) {
        log.info("條碼掃描借用，條碼: {}", request.getBarcode());

        // 根據條碼查詢物品
        Item item = itemRepository.findByBarcode(request.getBarcode())
                .orElseThrow(() -> new IllegalArgumentException("條碼對應的物品不存在"));

        // 轉換為一般借用請求
        BorrowRecordDTO.CreateRequest createRequest = BorrowRecordDTO.CreateRequest.builder()
                .itemId(item.getId())
                .borrowerId(request.getBorrowerId())
                .quantity(request.getQuantity())
                .expectedReturnDate(request.getExpectedReturnDate())
                .purpose(request.getPurpose())
                .borrowNotes(request.getBorrowNotes())
                .build();

        return createBorrowRecord(createRequest);
    }

    /**
     * 條碼掃描歸還
     */
    @Transactional
    public BorrowRecordDTO barcodeReturn(BorrowRecordDTO.BarcodeReturnRequest request) {
        log.info("條碼掃描歸還，條碼: {}", request.getBarcode());

        // 轉換為一般歸還請求
        BorrowRecordDTO.ReturnRequest returnRequest = BorrowRecordDTO.ReturnRequest.builder()
                .returnQuantity(request.getReturnQuantity())
                .returnCondition(request.getReturnCondition())
                .returnNotes(request.getReturnNotes())
                .build();

        return returnItem(request.getBorrowRecordId(), returnRequest);
    }

    /**
     * 根據ID查詢借還記錄
     */
    public BorrowRecordDTO getBorrowRecordById(Long id) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("借還記錄不存在"));
        return convertToDTO(borrowRecord);
    }

    /**
     * 根據借還單號查詢記錄
     */
    public Optional<BorrowRecordDTO> getBorrowRecordByNumber(String recordNumber) {
        return borrowRecordRepository.findByRecordNumber(recordNumber)
                .map(this::convertToDTO);
    }

    /**
     * 分頁查詢借還記錄
     */
    public PageResponse<BorrowRecordDTO> getBorrowRecords(BorrowRecordDTO.QueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        LocalDateTime borrowStartDate = request.getBorrowStartDate() != null 
                ? request.getBorrowStartDate().atStartOfDay() : null;
        LocalDateTime borrowEndDate = request.getBorrowEndDate() != null 
                ? request.getBorrowEndDate().atTime(23, 59, 59) : null;
        LocalDateTime returnStartDate = request.getReturnStartDate() != null 
                ? request.getReturnStartDate().atStartOfDay() : null;
        LocalDateTime returnEndDate = request.getReturnEndDate() != null 
                ? request.getReturnEndDate().atTime(23, 59, 59) : null;

        Page<BorrowRecord> recordPage = borrowRecordRepository.findRecordsByConditions(
                request.getBorrowerId(),
                request.getItemId(),
                request.getStatus(),
                borrowStartDate,
                borrowEndDate,
                pageable
        );

        List<BorrowRecordDTO> recordDTOs = recordPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(recordDTOs, recordPage.getTotalElements(), 
                              request.getPage() - 1, request.getSize());
    }

    /**
     * 取得使用者當前借用記錄
     */
    public List<BorrowRecordDTO> getCurrentBorrowsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

        List<BorrowRecord> records = borrowRecordRepository.findCurrentBorrowsByUser(user);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得逾期記錄
     */
    public List<BorrowRecordDTO> getOverdueRecords() {
        List<BorrowRecord> records = borrowRecordRepository.findOverdueRecords(LocalDate.now());
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得即將到期記錄
     */
    public List<BorrowRecordDTO> getRecordsDueSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(days);
        
        List<BorrowRecord> records = borrowRecordRepository.findRecordsDueSoon(startDate, endDate);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得借還統計資訊
     */
    public BorrowRecordDTO.Statistics getBorrowStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        long totalBorrowCount = borrowRecordRepository.countByBorrowDateBetween(startDateTime, endDateTime);
        long totalReturnCount = borrowRecordRepository.countReturnedByDateRange(startDateTime, endDateTime);
        long currentBorrowedCount = borrowRecordRepository.countCurrentBorrowed();
        long overdueCount = borrowRecordRepository.countOverdue();

        return BorrowRecordDTO.Statistics.builder()
                .totalBorrowCount(totalBorrowCount)
                .totalReturnCount(totalReturnCount)
                .currentBorrowedCount(currentBorrowedCount)
                .overdueCount(overdueCount)
                .statisticsTime(LocalDateTime.now())
                .build();
    }

    /**
     * 產生借還單號
     */
    private String generateRecordNumber() {
        String prefix = "BR";
        String date = LocalDate.now().toString().replace("-", "");
        long count = borrowRecordRepository.countByRecordNumberStartingWith(prefix + date) + 1;
        return String.format("%s%s%04d", prefix, date, count);
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
    private BorrowRecordDTO convertToDTO(BorrowRecord record) {
        return BorrowRecordDTO.builder()
                .id(record.getId())
                .recordNumber(record.getRecordNumber())
                .itemId(record.getItem().getId())
                .itemCode(record.getItem().getItemCode())
                .itemName(record.getItem().getName())
                .categoryName(record.getItem().getCategory().getName())
                .borrowerId(record.getBorrower().getId())
                .borrowerName(record.getBorrower().getUsername())
                .borrowerDepartment(record.getBorrower().getDepartment())
                .quantity(record.getQuantity())
                .borrowDate(record.getBorrowDate().toLocalDate())
                .expectedReturnDate(record.getExpectedReturnDate())
                .actualReturnDate(record.getActualReturnDate() != null ? record.getActualReturnDate().toLocalDate() : null)
                .returnQuantity(record.getReturnQuantity())
                .returnCondition(record.getReturnCondition())
                .status(record.getStatus())
                .purpose(record.getPurpose())
                .borrowNotes(record.getBorrowNotes())
                .returnNotes(record.getReturnNotes())
                .overdue(record.isOverdue())
                .overdueDays((int) record.getOverdueDays())
                .penaltyAmount(record.getPenaltyAmount())
                .penaltyPaid(record.getPenaltyPaid())
                .processedById(record.getProcessedBy() != null ? record.getProcessedBy().getId() : null)
                .processedByName(record.getProcessedBy() != null ? record.getProcessedBy().getUsername() : null)
                .returnProcessedById(record.getReturnProcessedBy() != null ? record.getReturnProcessedBy().getId() : null)
                .returnProcessedByName(record.getReturnProcessedBy() != null ? record.getReturnProcessedBy().getUsername() : null)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
