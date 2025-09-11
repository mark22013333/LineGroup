-- 庫存管理系統資料表建立腳本
-- 建立時間: 2024-09-07

-- 1. 分類表
CREATE TABLE inventory_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '分類名稱',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '分類代碼',
    description TEXT COMMENT '分類描述',
    parent_id BIGINT COMMENT '父分類ID',
    level INT DEFAULT 1 COMMENT '分類層級',
    sort_order INT DEFAULT 0 COMMENT '排序順序',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    created_by BIGINT COMMENT '建立者ID',
    updated_by BIGINT COMMENT '更新者ID',
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code),
    INDEX idx_enabled (enabled),
    FOREIGN KEY (parent_id) REFERENCES inventory_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存分類表';

-- 2. 物品表
CREATE TABLE inventory_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '物品名稱',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '物品代碼',
    barcode VARCHAR(200) COMMENT '條碼',
    description TEXT COMMENT '物品描述',
    category_id BIGINT NOT NULL COMMENT '分類ID',
    unit VARCHAR(20) DEFAULT '個' COMMENT '單位',
    unit_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '單價',
    specifications TEXT COMMENT '規格說明',
    brand VARCHAR(100) COMMENT '品牌',
    model VARCHAR(100) COMMENT '型號',
    supplier VARCHAR(200) COMMENT '供應商',
    purchase_date DATE COMMENT '採購日期',
    warranty_period INT COMMENT '保固期(月)',
    location VARCHAR(200) COMMENT '存放位置',
    image_url VARCHAR(500) COMMENT '圖片URL',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '狀態: ACTIVE, INACTIVE, DISCONTINUED',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否啟用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    created_by BIGINT COMMENT '建立者ID',
    updated_by BIGINT COMMENT '更新者ID',
    INDEX idx_category_id (category_id),
    INDEX idx_code (code),
    INDEX idx_barcode (barcode),
    INDEX idx_status (status),
    INDEX idx_enabled (enabled),
    FULLTEXT idx_search (name, description, brand, model),
    FOREIGN KEY (category_id) REFERENCES inventory_categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存物品表';

-- 3. 庫存表
CREATE TABLE inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    current_quantity INT DEFAULT 0 COMMENT '當前庫存數量',
    reserved_quantity INT DEFAULT 0 COMMENT '預留數量',
    available_quantity INT GENERATED ALWAYS AS (current_quantity - reserved_quantity) STORED COMMENT '可用數量',
    min_stock_level INT DEFAULT 0 COMMENT '最低庫存警告值',
    max_stock_level INT DEFAULT 0 COMMENT '最高庫存警告值',
    reorder_point INT DEFAULT 0 COMMENT '再訂購點',
    reorder_quantity INT DEFAULT 0 COMMENT '再訂購數量',
    last_stock_take_date DATE COMMENT '最後盤點日期',
    last_stock_take_quantity INT COMMENT '最後盤點數量',
    location VARCHAR(200) COMMENT '存放位置',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    updated_by BIGINT COMMENT '更新者ID',
    UNIQUE KEY uk_item_id (item_id),
    INDEX idx_current_quantity (current_quantity),
    INDEX idx_available_quantity (available_quantity),
    FOREIGN KEY (item_id) REFERENCES inventory_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存表';

-- 4. 借還記錄表
CREATE TABLE borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_number VARCHAR(50) NOT NULL UNIQUE COMMENT '借還單號',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    borrower_id BIGINT NOT NULL COMMENT '借用人ID',
    borrower_name VARCHAR(100) NOT NULL COMMENT '借用人姓名',
    borrower_department VARCHAR(100) COMMENT '借用人部門',
    borrower_contact VARCHAR(100) COMMENT '借用人聯絡方式',
    quantity INT NOT NULL COMMENT '借用數量',
    borrow_date TIMESTAMP NOT NULL COMMENT '借用時間',
    expected_return_date TIMESTAMP COMMENT '預計歸還時間',
    actual_return_date TIMESTAMP COMMENT '實際歸還時間',
    returned_quantity INT DEFAULT 0 COMMENT '已歸還數量',
    status VARCHAR(20) DEFAULT 'BORROWED' COMMENT '狀態: BORROWED, RETURNED, PARTIAL_RETURNED, OVERDUE, CANCELLED',
    purpose TEXT COMMENT '借用目的',
    notes TEXT COMMENT '備註',
    processed_by BIGINT COMMENT '處理人ID',
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '處理時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_record_number (record_number),
    INDEX idx_item_id (item_id),
    INDEX idx_borrower_id (borrower_id),
    INDEX idx_status (status),
    INDEX idx_borrow_date (borrow_date),
    INDEX idx_expected_return_date (expected_return_date),
    FOREIGN KEY (item_id) REFERENCES inventory_items(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借還記錄表';

-- 5. 庫存異動記錄表
CREATE TABLE inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_number VARCHAR(50) NOT NULL UNIQUE COMMENT '異動單號',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    transaction_type VARCHAR(30) NOT NULL COMMENT '異動類型: IN, OUT, ADJUST, TRANSFER, BORROW, RETURN, STOCK_TAKE',
    reference_type VARCHAR(30) COMMENT '參考類型: PURCHASE, SALE, BORROW, RETURN, ADJUSTMENT, TRANSFER, STOCK_TAKE',
    reference_id BIGINT COMMENT '參考ID',
    quantity_before INT NOT NULL COMMENT '異動前數量',
    quantity_change INT NOT NULL COMMENT '異動數量(正負值)',
    quantity_after INT NOT NULL COMMENT '異動後數量',
    unit_cost DECIMAL(10,2) COMMENT '單位成本',
    total_cost DECIMAL(12,2) COMMENT '總成本',
    reason TEXT COMMENT '異動原因',
    notes TEXT COMMENT '備註',
    transaction_date TIMESTAMP NOT NULL COMMENT '異動時間',
    processed_by BIGINT NOT NULL COMMENT '處理人ID',
    approved_by BIGINT COMMENT '審核人ID',
    approved_at TIMESTAMP COMMENT '審核時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    INDEX idx_transaction_number (transaction_number),
    INDEX idx_item_id (item_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_reference_type (reference_type),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_processed_by (processed_by),
    FOREIGN KEY (item_id) REFERENCES inventory_items(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存異動記錄表';

-- 6. 庫存警告設定表
CREATE TABLE inventory_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '物品ID',
    alert_type VARCHAR(20) NOT NULL COMMENT '警告類型: LOW_STOCK, OUT_OF_STOCK, OVERSTOCK, EXPIRY',
    threshold_value INT COMMENT '警告閾值',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否啟用',
    last_alert_time TIMESTAMP COMMENT '最後警告時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    UNIQUE KEY uk_item_alert (item_id, alert_type),
    INDEX idx_alert_type (alert_type),
    INDEX idx_is_enabled (is_enabled),
    FOREIGN KEY (item_id) REFERENCES inventory_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='庫存警告設定表';

-- 7. 系統設定表
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '設定鍵',
    setting_value TEXT COMMENT '設定值',
    setting_type VARCHAR(20) DEFAULT 'STRING' COMMENT '設定類型: STRING, INTEGER, BOOLEAN, JSON',
    description TEXT COMMENT '設定描述',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否為系統設定',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_setting_key (setting_key),
    INDEX idx_is_system (is_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系統設定表';
