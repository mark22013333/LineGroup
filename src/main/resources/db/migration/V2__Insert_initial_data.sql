-- 庫存管理系統初始資料腳本
-- 建立時間: 2024-09-07

-- 1. 插入系統設定
INSERT INTO system_settings (setting_key, setting_value, setting_type, description, is_system) VALUES
('inventory.default_borrow_days', '7', 'INTEGER', '預設借用天數', TRUE),
('inventory.max_borrow_days', '30', 'INTEGER', '最大借用天數', TRUE),
('inventory.auto_generate_barcode', 'true', 'BOOLEAN', '是否自動產生條碼', TRUE),
('inventory.barcode_prefix', 'INV', 'STRING', '條碼前綴', TRUE),
('inventory.low_stock_alert_enabled', 'true', 'BOOLEAN', '是否啟用低庫存警告', TRUE),
('inventory.overdue_alert_enabled', 'true', 'BOOLEAN', '是否啟用逾期警告', TRUE),
('inventory.email_notification_enabled', 'false', 'BOOLEAN', '是否啟用郵件通知', TRUE),
('inventory.report_retention_days', '90', 'INTEGER', '報表保留天數', TRUE);

-- 2. 插入預設分類
INSERT INTO inventory_categories (id, name, code, description, parent_id, level, sort_order, enabled) VALUES
(1, '電子設備', 'ELECTRONICS', '各類電子設備與器材', NULL, 1, 1, TRUE),
(2, '辦公用品', 'OFFICE', '辦公室日常用品', NULL, 1, 2, TRUE),
(3, '工具設備', 'TOOLS', '各類工具與設備', NULL, 1, 3, TRUE),
(4, '家具用品', 'FURNITURE', '辦公家具與用品', NULL, 1, 4, TRUE),
(5, '清潔用品', 'CLEANING', '清潔維護用品', NULL, 1, 5, TRUE),

-- 電子設備子分類
(11, '電腦設備', 'COMPUTER', '桌上型電腦、筆記型電腦等', 1, 2, 1, TRUE),
(12, '網路設備', 'NETWORK', '路由器、交換器、網路線等', 1, 2, 2, TRUE),
(13, '影音設備', 'AUDIO_VIDEO', '投影機、音響、攝影機等', 1, 2, 3, TRUE),
(14, '手機平板', 'MOBILE', '手機、平板電腦等行動裝置', 1, 2, 4, TRUE),
(15, '週邊設備', 'PERIPHERALS', '鍵盤、滑鼠、螢幕等週邊', 1, 2, 5, TRUE),

-- 辦公用品子分類
(21, '文具用品', 'STATIONERY', '筆、紙張、文件夾等', 2, 2, 1, TRUE),
(22, '列印耗材', 'PRINTING', '墨水匣、碳粉匣、紙張等', 2, 2, 2, TRUE),
(23, '辦公機器', 'OFFICE_MACHINE', '印表機、影印機、碎紙機等', 2, 2, 3, TRUE),

-- 工具設備子分類
(31, '手工具', 'HAND_TOOLS', '螺絲起子、扳手、鉗子等', 3, 2, 1, TRUE),
(32, '電動工具', 'POWER_TOOLS', '電鑽、切割機等電動工具', 3, 2, 2, TRUE),
(33, '測量工具', 'MEASURING', '尺規、游標卡尺、測量儀等', 3, 2, 3, TRUE),

-- 家具用品子分類
(41, '桌椅', 'DESK_CHAIR', '辦公桌、辦公椅等', 4, 2, 1, TRUE),
(42, '收納用品', 'STORAGE', '櫃子、抽屜、收納盒等', 4, 2, 2, TRUE),
(43, '裝飾用品', 'DECORATION', '植物、畫框、裝飾品等', 4, 2, 3, TRUE);

-- 3. 插入範例物品
INSERT INTO inventory_items (name, code, barcode, description, category_id, unit, unit_price, brand, model, location, status) VALUES
-- 電腦設備
('Dell OptiPlex 7090 桌上型電腦', 'COMP001', 'INV001001', 'Intel i7-11700, 16GB RAM, 512GB SSD', 11, '台', 35000.00, 'Dell', 'OptiPlex 7090', 'IT室-A01', 'ACTIVE'),
('MacBook Pro 13吋', 'COMP002', 'INV001002', 'Apple M2 晶片, 8GB RAM, 256GB SSD', 11, '台', 45000.00, 'Apple', 'MacBook Pro 13', 'IT室-A02', 'ACTIVE'),
('HP EliteBook 840 G8', 'COMP003', 'INV001003', 'Intel i5-1135G7, 8GB RAM, 256GB SSD', 11, '台', 28000.00, 'HP', 'EliteBook 840 G8', 'IT室-A03', 'ACTIVE'),

-- 網路設備
('TP-Link Archer AX73 路由器', 'NET001', 'INV002001', 'AX5400 雙頻 Wi-Fi 6 路由器', 12, '台', 3500.00, 'TP-Link', 'Archer AX73', 'IT室-B01', 'ACTIVE'),
('Netgear GS108 交換器', 'NET002', 'INV002002', '8埠 Gigabit 交換器', 12, '台', 1200.00, 'Netgear', 'GS108', 'IT室-B02', 'ACTIVE'),

-- 影音設備
('Epson EB-2247U 投影機', 'AV001', 'INV003001', '4200流明 WUXGA 投影機', 13, '台', 25000.00, 'Epson', 'EB-2247U', '會議室-C01', 'ACTIVE'),
('Logitech C920 網路攝影機', 'AV002', 'INV003002', '1080p HD 網路攝影機', 13, '台', 2500.00, 'Logitech', 'C920', 'IT室-C01', 'ACTIVE'),

-- 辦公用品
('A4 影印紙', 'PAPER001', 'INV004001', '80gsm A4 白色影印紙', 22, '包', 150.00, 'Double A', 'A4-80gsm', '倉庫-D01', 'ACTIVE'),
('原子筆', 'PEN001', 'INV004002', '藍色原子筆 0.7mm', 21, '支', 15.00, 'Pilot', 'BPS-GP', '辦公室-E01', 'ACTIVE'),

-- 工具設備
('十字螺絲起子', 'TOOL001', 'INV005001', 'PH2 十字螺絲起子', 31, '支', 120.00, 'Stanley', 'STHT0-60210', '工具室-F01', 'ACTIVE'),
('數位游標卡尺', 'TOOL002', 'INV005002', '150mm 數位顯示游標卡尺', 33, '支', 800.00, 'Mitutoyo', '500-196-30', '工具室-F02', 'ACTIVE');

-- 4. 插入庫存資料
INSERT INTO inventories (item_id, current_quantity, min_stock_level, max_stock_level, reorder_point, reorder_quantity, location) VALUES
(1, 5, 2, 10, 3, 5, 'IT室-A01'),
(2, 3, 1, 5, 2, 3, 'IT室-A02'),
(3, 8, 2, 12, 3, 5, 'IT室-A03'),
(4, 10, 3, 15, 5, 8, 'IT室-B01'),
(5, 15, 5, 25, 8, 10, 'IT室-B02'),
(6, 2, 1, 3, 1, 2, '會議室-C01'),
(7, 12, 3, 20, 5, 10, 'IT室-C01'),
(8, 500, 100, 1000, 150, 500, '倉庫-D01'),
(9, 200, 50, 500, 80, 200, '辦公室-E01'),
(10, 25, 5, 50, 10, 20, '工具室-F01'),
(11, 8, 2, 15, 3, 10, '工具室-F02');

-- 5. 插入庫存警告設定
INSERT INTO inventory_alerts (item_id, alert_type, threshold_value, is_enabled) VALUES
-- 低庫存警告
(1, 'LOW_STOCK', 2, TRUE),
(2, 'LOW_STOCK', 1, TRUE),
(3, 'LOW_STOCK', 2, TRUE),
(4, 'LOW_STOCK', 3, TRUE),
(5, 'LOW_STOCK', 5, TRUE),
(6, 'LOW_STOCK', 1, TRUE),
(7, 'LOW_STOCK', 3, TRUE),
(8, 'LOW_STOCK', 100, TRUE),
(9, 'LOW_STOCK', 50, TRUE),
(10, 'LOW_STOCK', 5, TRUE),
(11, 'LOW_STOCK', 2, TRUE),

-- 缺貨警告
(1, 'OUT_OF_STOCK', 0, TRUE),
(2, 'OUT_OF_STOCK', 0, TRUE),
(3, 'OUT_OF_STOCK', 0, TRUE),
(4, 'OUT_OF_STOCK', 0, TRUE),
(5, 'OUT_OF_STOCK', 0, TRUE),
(6, 'OUT_OF_STOCK', 0, TRUE),
(7, 'OUT_OF_STOCK', 0, TRUE),
(8, 'OUT_OF_STOCK', 0, TRUE),
(9, 'OUT_OF_STOCK', 0, TRUE),
(10, 'OUT_OF_STOCK', 0, TRUE),
(11, 'OUT_OF_STOCK', 0, TRUE);

-- 6. 插入範例借還記錄
INSERT INTO borrow_records (record_number, item_id, borrower_id, borrower_name, borrower_department, quantity, borrow_date, expected_return_date, status, purpose, processed_by) VALUES
('BR202409070001', 2, 1, '張小明', 'IT部門', 1, '2024-09-01 09:00:00', '2024-09-08 17:00:00', 'BORROWED', '出差使用', 1),
('BR202409070002', 7, 2, '李小華', '行銷部門', 2, '2024-09-03 10:30:00', '2024-09-10 17:00:00', 'BORROWED', '會議錄影', 1),
('BR202409070003', 10, 3, '王小美', '工程部門', 5, '2024-09-05 14:00:00', '2024-09-12 17:00:00', 'BORROWED', '設備維修', 1);

-- 7. 插入範例異動記錄
INSERT INTO inventory_transactions (transaction_number, item_id, transaction_type, reference_type, quantity_before, quantity_change, quantity_after, reason, transaction_date, processed_by) VALUES
('TXN202409070001', 1, 'IN', 'PURCHASE', 3, 2, 5, '新購入設備', '2024-09-01 08:00:00', 1),
('TXN202409070002', 2, 'OUT', 'BORROW', 4, -1, 3, '借出給張小明', '2024-09-01 09:00:00', 1),
('TXN202409070003', 7, 'OUT', 'BORROW', 14, -2, 12, '借出給李小華', '2024-09-03 10:30:00', 1),
('TXN202409070004', 8, 'IN', 'PURCHASE', 300, 200, 500, '補充辦公用紙', '2024-09-04 15:00:00', 1),
('TXN202409070005', 10, 'OUT', 'BORROW', 30, -5, 25, '借出給王小美', '2024-09-05 14:00:00', 1);
