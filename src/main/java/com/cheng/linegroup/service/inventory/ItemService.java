package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.ItemDTO;
import com.cheng.linegroup.entity.inventory.Category;
import com.cheng.linegroup.entity.inventory.Inventory;
import com.cheng.linegroup.entity.inventory.Item;
import com.cheng.linegroup.enums.StatusEnum;
import com.cheng.linegroup.repository.inventory.CategoryRepository;
import com.cheng.linegroup.repository.inventory.InventoryRepository;
import com.cheng.linegroup.repository.inventory.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 物品服務層
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * 建立物品
     */
    @Transactional
    public ItemDTO createItem(ItemDTO.CreateRequest request) {
        log.info("建立物品: {}", request.getName());

        // 檢查物品代碼是否重複
        if (itemRepository.existsByItemCode(request.getItemCode())) {
            throw new IllegalArgumentException("物品代碼已存在: " + request.getItemCode());
        }

        // 檢查條碼是否重複
        if (request.getBarcode() != null && itemRepository.existsByBarcode(request.getBarcode())) {
            throw new IllegalArgumentException("條碼已存在: " + request.getBarcode());
        }

        // 檢查分類是否存在
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));

        if (!category.getEnabled()) {
            throw new IllegalArgumentException("分類已停用，無法新增物品");
        }

        // 計算總價
        BigDecimal totalPrice = request.getUnitPrice() != null && request.getInitialQuantity() != null
                ? request.getUnitPrice().multiply(BigDecimal.valueOf(request.getInitialQuantity()))
                : null;

        // 建立物品實體
        Item item = Item.builder()
                .itemCode(request.getItemCode())
                .name(request.getName())
                .description(request.getDescription())
                .barcode(request.getBarcode())
                .category(category)
                .brand(request.getBrand())
                .model(request.getModel())
                .specifications(request.getSpecifications())
                .unit(request.getUnit())
                .unitPrice(request.getUnitPrice())
                .totalPrice(totalPrice)
                .supplier(request.getSupplier())
                .purchaseDate(request.getPurchaseDate())
                .warrantyExpiry(request.getWarrantyExpiry())
                .location(request.getLocation())
                .enabled(request.getEnabled())
                .borrowable(request.getBorrowable())
                .maxBorrowDays(request.getMaxBorrowDays())
                .notes(request.getNotes())
                .build();

        item = itemRepository.save(item);

        // 建立對應的庫存記錄
        Inventory inventory = Inventory.builder()
                .item(item)
                .totalQuantity(request.getInitialQuantity())
                .availableQuantity(request.getInitialQuantity())
                .borrowedQuantity(0)
                .reservedQuantity(0)
                .damagedQuantity(0)
                .minStockLevel(5) // 預設最低庫存警告
                .maxStockLevel(100) // 預設最高庫存警告
                .build();

        inventoryRepository.save(inventory);

        log.info("物品建立成功，ID: {}", item.getId());
        return convertToDTO(item);
    }

    /**
     * 更新物品
     */
    @Transactional
    public ItemDTO updateItem(Long id, ItemDTO.UpdateRequest request) {
        log.info("更新物品，ID: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        // 檢查條碼是否重複（排除自己）
        if (request.getBarcode() != null && !request.getBarcode().equals(item.getBarcode()) 
            && itemRepository.existsByBarcode(request.getBarcode())) {
            throw new IllegalArgumentException("條碼已存在: " + request.getBarcode());
        }

        // 檢查分類是否存在
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));

        if (!category.getEnabled()) {
            throw new IllegalArgumentException("分類已停用，無法更新物品");
        }

        // 計算總價
        BigDecimal totalPrice = request.getUnitPrice() != null && item.getInventory() != null
                ? request.getUnitPrice().multiply(BigDecimal.valueOf(item.getInventory().getTotalQuantity()))
                : item.getTotalPrice();

        // 更新物品資訊
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setBarcode(request.getBarcode());
        item.setCategory(category);
        item.setBrand(request.getBrand());
        item.setModel(request.getModel());
        item.setSpecifications(request.getSpecifications());
        item.setUnit(request.getUnit());
        item.setUnitPrice(request.getUnitPrice());
        item.setTotalPrice(totalPrice);
        item.setSupplier(request.getSupplier());
        item.setPurchaseDate(request.getPurchaseDate());
        item.setWarrantyExpiry(request.getWarrantyExpiry());
        item.setLocation(request.getLocation());
        item.setEnabled(request.getEnabled());
        item.setBorrowable(request.getBorrowable());
        item.setMaxBorrowDays(request.getMaxBorrowDays());
        item.setNotes(request.getNotes());

        item = itemRepository.save(item);
        log.info("物品更新成功，ID: {}", item.getId());

        return convertToDTO(item);
    }

    /**
     * 刪除物品
     */
    @Transactional
    public void deleteItem(Long id) {
        log.info("刪除物品，ID: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

        // 檢查是否有借用記錄
        if (item.hasActiveBorrowRecords()) {
            throw new IllegalArgumentException("該物品還有未歸還的借用記錄，無法刪除");
        }

        itemRepository.delete(item);
        log.info("物品刪除成功，ID: {}", id);
    }

    /**
     * 根據ID查詢物品
     */
    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("物品不存在"));
        return convertToDTO(item);
    }

    /**
     * 根據條碼查詢物品
     */
    public Optional<ItemDTO> getItemByBarcode(String barcode) {
        return itemRepository.findByBarcode(barcode)
                .map(this::convertToDTO);
    }

    /**
     * 根據物品代碼查詢物品
     */
    public Optional<ItemDTO> getItemByCode(String itemCode) {
        return itemRepository.findByItemCode(itemCode)
                .map(this::convertToDTO);
    }

    /**
     * 分頁查詢物品
     */
    public PageResponse<ItemDTO> getItems(ItemDTO.QueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        Page<Item> itemPage = itemRepository.findItemsByConditions(
                StatusEnum.ACTIVE,
                request.getCategoryId(),
                request.getKeyword(),
                request.getBrand(),
                null, // location
                null, // isConsumable
                pageable
        );

        List<ItemDTO> itemDTOs = itemPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(itemDTOs, itemPage.getTotalElements(), 
                              request.getPage() - 1, request.getSize());
    }

    /**
     * 取得可借用的物品列表
     */
    public List<ItemDTO.SimpleItem> getBorrowableItems() {
        List<Item> items = itemRepository.findBorrowableItemsWithStock();
        return items.stream()
                .map(this::convertToSimpleItem)
                .collect(Collectors.toList());
    }

    /**
     * 根據分類查詢物品
     */
    public List<ItemDTO> getItemsByCategory(Long categoryId, Boolean includeSubCategories) {
        List<Item> items;
        if (includeSubCategories != null && includeSubCategories) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("分類不存在"));
            items = itemRepository.findByCategoryAndSubCategories(category);
        } else {
            items = itemRepository.findByCategoryIdAndEnabledTrue(categoryId);
        }

        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 搜尋物品（全文搜尋）
     */
    public PageResponse<ItemDTO> searchItems(String keyword, Pageable pageable) {
        Page<Item> itemPage = itemRepository.searchItems(keyword, StatusEnum.ACTIVE, pageable);
        
        List<ItemDTO> itemDTOs = itemPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(itemDTOs, itemPage.getTotalElements(), 
                              pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * 取得低庫存物品
     */
    public List<ItemDTO> getLowStockItems() {
        List<Item> items = itemRepository.findLowStockItems(StatusEnum.ACTIVE);
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得缺貨物品
     */
    public List<ItemDTO> getOutOfStockItems() {
        List<Item> items = itemRepository.findOutOfStockItems(StatusEnum.ACTIVE);
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得物品統計資訊
     */
    public ItemDTO.Statistics getItemStatistics() {
        long totalItems = itemRepository.count();
        long enabledItems = itemRepository.countByEnabledTrue();
        long borrowableItems = itemRepository.countByBorrowableTrue();
        long lowStockItems = itemRepository.countLowStockItems();
        long outOfStockItems = itemRepository.countOutOfStockItems();
        
        return ItemDTO.Statistics.builder()
                .totalItems(totalItems)
                .enabledItems(enabledItems)
                .borrowableItems(borrowableItems)
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStockItems)
                .build();
    }

    /**
     * 轉換為 DTO
     */
    private ItemDTO convertToDTO(Item item) {
        Inventory inventory = item.getInventory();
        
        return ItemDTO.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .name(item.getName())
                .description(item.getDescription())
                .barcode(item.getBarcode())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .brand(item.getBrand())
                .model(item.getModel())
                .specifications(item.getSpecifications())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .supplier(item.getSupplier())
                .purchaseDate(item.getPurchaseDate())
                .warrantyExpiry(item.getWarrantyExpiry())
                .location(item.getLocation())
                .enabled(item.getEnabled())
                .borrowable(item.getBorrowable())
                .maxBorrowDays(item.getMaxBorrowDays())
                .notes(item.getNotes())
                .totalQuantity(inventory != null ? inventory.getTotalQuantity() : 0)
                .availableQuantity(inventory != null ? inventory.getAvailableQuantity() : 0)
                .borrowedQuantity(inventory != null ? inventory.getBorrowedQuantity() : 0)
                .reservedQuantity(inventory != null ? inventory.getReservedQuantity() : 0)
                .damagedQuantity(inventory != null ? inventory.getDamagedQuantity() : 0)
                .inStock(inventory != null ? inventory.isInStock() : false)
                .lowStock(inventory != null ? inventory.isLowStock() : false)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .createdBy(item.getCreatedBy() != null ? item.getCreatedBy().getUsername() : null)
                .updatedBy(item.getUpdatedBy() != null ? item.getUpdatedBy().getUsername() : null)
                .build();
    }

    /**
     * 轉換為簡化版 DTO
     */
    private ItemDTO.SimpleItem convertToSimpleItem(Item item) {
        Inventory inventory = item.getInventory();
        
        return ItemDTO.SimpleItem.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .name(item.getName())
                .barcode(item.getBarcode())
                .categoryName(item.getCategory().getName())
                .availableQuantity(inventory != null ? inventory.getAvailableQuantity() : 0)
                .borrowable(item.getBorrowable())
                .build();
    }
}
