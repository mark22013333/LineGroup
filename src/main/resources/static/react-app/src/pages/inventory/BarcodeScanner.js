import React, { useState, useRef, useEffect } from 'react';
import { 
    Card, 
    Button, 
    Input, 
    Space, 
    Alert, 
    Modal, 
    Form, 
    InputNumber, 
    Select, 
    message,
    Row,
    Col,
    Descriptions,
    Tag,
    Divider
} from 'antd';
import { 
    ScanOutlined, 
    CameraOutlined, 
    SearchOutlined,
    ShoppingCartOutlined,
    UndoOutlined
} from '@ant-design/icons';
import BarcodeScannerLib from 'react-qr-barcode-scanner';
import { inventoryAPI } from '../../services/inventoryAPI';

const { Option } = Select;

const BarcodeScanner = () => {
    const [scanning, setScanning] = useState(false);
    const [manualInput, setManualInput] = useState('');
    const [scannedItem, setScannedItem] = useState(null);
    const [borrowModalVisible, setBorrowModalVisible] = useState(false);
    const [returnModalVisible, setReturnModalVisible] = useState(false);
    const [borrowForm] = Form.useForm();
    const [returnForm] = Form.useForm();
    const videoRef = useRef(null);
    const canvasRef = useRef(null);

    // 攝影機掃描功能
    const startScanning = () => {
        setScanning(true);
        message.info('正在啟動攝影機，請允許瀏覽器存取攝影機權限');
    };

    const stopScanning = () => {
        setScanning(false);
        message.info('已停止掃描');
    };

    // 處理掃描成功
    const handleScanSuccess = async (result) => {
        console.log('[BarcodeScanner] 掃描回調被觸發:', result);
        
        if (result && result.text) {
            console.log('[BarcodeScanner] 掃描成功，條碼內容:', result.text);
            message.info(`掃描到內容: ${result.text}`);
            
            try {
                console.log('[BarcodeScanner] 正在查詢後端 API...');
                const response = await inventoryAPI.scanBarcode({
                    barcode: result.text
                });

                console.log('[BarcodeScanner] 後端 API 回應:', response);

                if (response.success && response.data.found) {
                    setScannedItem(response.data);
                    message.success(`掃描成功！找到物品: ${response.data.itemName}`);
                    stopScanning(); // 掃描成功後自動停止
                } else {
                    message.warning(`掃描到條碼 "${result.text}"，但在庫存中找不到對應物品`);
                    setScannedItem(null);
                }
            } catch (error) {
                console.error('[BarcodeScanner] 掃描查詢失敗:', error);
                message.error('掃描查詢失敗: ' + error.message);
                setScannedItem(null);
            }
        } else {
            console.log('[BarcodeScanner] 掃描結果無效:', result);
        }
    };

    // 處理掃描錯誤
    const handleScanError = (error) => {
        // 過濾掉正常的「找不到條碼」錯誤，這是掃描過程中的正常現象
        if (error.name === 'NotFoundException' || 
            error.message?.includes('No MultiFormat Readers were able to detect the code')) {
            // 這是正常的掃描過程，不需要顯示錯誤訊息
            return;
        }
        
        console.error('[BarcodeScanner] 掃描錯誤:', error);
        
        if (error.name === 'NotAllowedError') {
            message.error('攝影機權限被拒絕，請允許瀏覽器存取攝影機');
        } else if (error.name === 'NotFoundError') {
            message.error('找不到攝影機設備');
        } else if (error.name === 'NotReadableError') {
            message.error('攝影機被其他應用程式佔用');
        } else if (error.name === 'OverconstrainedError') {
            message.error('攝影機不支援所請求的設定');
        } else {
            // 只記錄到控制台，不顯示給用戶
            console.warn('[BarcodeScanner] 其他掃描錯誤:', error.message);
        }
    };

    const handleManualScan = async () => {
        if (!manualInput.trim()) {
            message.warning('請輸入條碼');
            return;
        }

        try {
            const response = await inventoryAPI.scanBarcode({
                barcode: manualInput.trim()
            });

            if (response.success && response.data.found) {
                setScannedItem(response.data);
                message.success('找到對應物品');
            } else {
                message.error(response.data?.message || '找不到對應物品');
                setScannedItem(null);
            }
        } catch (error) {
            message.error('掃描失敗: ' + error.message);
            setScannedItem(null);
        }
    };

    const handleBorrow = () => {
        if (!scannedItem) {
            message.warning('請先掃描物品');
            return;
        }
        borrowForm.resetFields();
        borrowForm.setFieldsValue({
            itemId: scannedItem.itemId,
            quantity: 1
        });
        setBorrowModalVisible(true);
    };

    const handleReturn = () => {
        if (!scannedItem) {
            message.warning('請先掃描物品');
            return;
        }
        returnForm.resetFields();
        returnForm.setFieldsValue({
            barcode: scannedItem.barcode
        });
        setReturnModalVisible(true);
    };

    const handleBorrowSubmit = async () => {
        try {
            const values = await borrowForm.validateFields();
            
            const response = await inventoryAPI.barcodeBorrow({
                barcode: scannedItem.barcode,
                borrowerName: values.borrowerName,
                borrowerDepartment: values.borrowerDepartment,
                borrowerContact: values.borrowerContact,
                quantity: values.quantity,
                expectedReturnDays: values.expectedReturnDays,
                purpose: values.purpose
            });

            if (response.success) {
                message.success('借用成功');
                setBorrowModalVisible(false);
                setScannedItem(null);
                setManualInput('');
            } else {
                message.error(response.message || '借用失敗');
            }
        } catch (error) {
            message.error('借用失敗: ' + error.message);
        }
    };

    const handleReturnSubmit = async () => {
        try {
            const values = await returnForm.validateFields();
            
            const response = await inventoryAPI.barcodeReturn({
                barcode: scannedItem.barcode,
                returnQuantity: values.returnQuantity,
                condition: values.condition,
                notes: values.notes
            });

            if (response.success) {
                message.success('歸還成功');
                setReturnModalVisible(false);
                setScannedItem(null);
                setManualInput('');
            } else {
                message.error(response.message || '歸還失敗');
            }
        } catch (error) {
            message.error('歸還失敗: ' + error.message);
        }
    };

    const handleClear = () => {
        setScannedItem(null);
        setManualInput('');
        stopScanning();
    };

    return (
        <div style={{ padding: '20px' }}>
            <Row gutter={[16, 16]}>
                {/* 掃描區域 */}
                <Col xs={24} lg={12}>
                    <Card title="條碼掃描" style={{ height: '100%' }}>
                        {/* 攝影機掃描區域 */}
                        <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                            <div 
                                style={{ 
                                    width: '100%', 
                                    height: '300px', 
                                    border: '2px dashed #d9d9d9',
                                    borderRadius: '8px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    backgroundColor: scanning ? '#000' : '#fafafa',
                                    overflow: 'hidden'
                                }}
                            >
                                {scanning ? (
                                    <BarcodeScannerLib
                                        width={300}
                                        height={300}
                                        onUpdate={(err, result) => {
                                            console.log('[BarcodeScanner] onUpdate 回調:', { err, result });
                                            if (result) {
                                                handleScanSuccess(result);
                                            } else if (err) {
                                                handleScanError(err);
                                            }
                                        }}
                                        facingMode="environment" // 使用後置攝影機
                                        constraints={{
                                            video: {
                                                facingMode: "environment",
                                                width: { ideal: 1280 },
                                                height: { ideal: 720 },
                                                focusMode: "continuous",
                                                advanced: [{
                                                    focusMode: "continuous"
                                                }, {
                                                    focusDistance: { min: 0.1, max: Infinity }
                                                }]
                                            }
                                        }}
                                    />
                                ) : (
                                    <div>
                                        <CameraOutlined style={{ fontSize: '48px', color: '#d9d9d9' }} />
                                        <p style={{ color: '#999', marginTop: '10px' }}>攝影機掃描區域</p>
                                        <p style={{ color: '#999', fontSize: '12px' }}>點擊「開始掃描」啟動攝影機</p>
                                        <p style={{ color: '#999', fontSize: '10px', marginTop: '5px' }}>
                                            支援 QR Code、條碼等多種格式
                                        </p>
                                    </div>
                                )}
                            </div>
                            
                            {/* 掃描提示 */}
                            {scanning && (
                                <Alert
                                    message="掃描提示"
                                    description={
                                        <div>
                                            <p>• 將 QR Code 或條碼對準攝影機中央</p>
                                            <p>• 保持適當距離（10-30 公分）</p>
                                            <p>• 確保光線充足，避免反光</p>
                                            <p>• 手機會自動對焦，請稍等片刻</p>
                                        </div>
                                    }
                                    type="info"
                                    showIcon
                                    style={{ marginTop: '10px', fontSize: '12px' }}
                                />
                            )}
                            
                            <Space style={{ marginTop: '10px' }}>
                                {!scanning ? (
                                    <Button 
                                        type="primary" 
                                        icon={<ScanOutlined />}
                                        onClick={startScanning}
                                        size="large"
                                    >
                                        開始掃描
                                    </Button>
                                ) : (
                                    <Button 
                                        danger
                                        onClick={stopScanning}
                                        size="large"
                                    >
                                        停止掃描
                                    </Button>
                                )}
                            </Space>
                        </div>

                        <Divider>或</Divider>

                        {/* 手動輸入區域 */}
                        <div>
                            <Space.Compact style={{ width: '100%' }}>
                                <Input
                                    placeholder="手動輸入條碼或物品代碼"
                                    value={manualInput}
                                    onChange={(e) => setManualInput(e.target.value)}
                                    onPressEnter={handleManualScan}
                                />
                                <Button 
                                    type="primary" 
                                    icon={<SearchOutlined />}
                                    onClick={handleManualScan}
                                >
                                    搜尋
                                </Button>
                            </Space.Compact>
                        </div>

                        <div style={{ marginTop: '20px', textAlign: 'center' }}>
                            <Button 
                                icon={<UndoOutlined />}
                                onClick={handleClear}
                            >
                                清除
                            </Button>
                        </div>
                    </Card>
                </Col>

                {/* 物品資訊區域 */}
                <Col xs={24} lg={12}>
                    <Card title="物品資訊" style={{ height: '100%' }}>
                        {scannedItem ? (
                            <div>
                                <Alert
                                    message="掃描成功"
                                    description={`找到物品: ${scannedItem.itemName}`}
                                    type="success"
                                    showIcon
                                    style={{ marginBottom: '20px' }}
                                />

                                <Descriptions column={1} size="small">
                                    <Descriptions.Item label="物品代碼">
                                        <strong>{scannedItem.itemCode}</strong>
                                    </Descriptions.Item>
                                    <Descriptions.Item label="物品名稱">
                                        {scannedItem.itemName}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="分類">
                                        {scannedItem.categoryName}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="條碼">
                                        <Tag color="blue">{scannedItem.barcode}</Tag>
                                    </Descriptions.Item>
                                    <Descriptions.Item label="庫存狀況">
                                        <Space>
                                            <Tag color={scannedItem.currentStock > 0 ? 'green' : 'red'}>
                                                當前: {scannedItem.currentStock}
                                            </Tag>
                                            <Tag color={scannedItem.availableStock > 0 ? 'green' : 'orange'}>
                                                可用: {scannedItem.availableStock}
                                            </Tag>
                                        </Space>
                                    </Descriptions.Item>
                                    <Descriptions.Item label="存放位置">
                                        {scannedItem.location || '-'}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="狀態">
                                        <Tag color={scannedItem.status === 'ACTIVE' ? 'green' : 'orange'}>
                                            {scannedItem.status === 'ACTIVE' ? '啟用' : '停用'}
                                        </Tag>
                                    </Descriptions.Item>
                                </Descriptions>

                                <div style={{ marginTop: '20px', textAlign: 'center' }}>
                                    <Space>
                                        <Button 
                                            type="primary" 
                                            icon={<ShoppingCartOutlined />}
                                            onClick={handleBorrow}
                                            disabled={scannedItem.availableStock <= 0}
                                        >
                                            借用
                                        </Button>
                                        <Button 
                                            icon={<UndoOutlined />}
                                            onClick={handleReturn}
                                        >
                                            歸還
                                        </Button>
                                    </Space>
                                </div>
                            </div>
                        ) : (
                            <div style={{ textAlign: 'center', color: '#999', padding: '50px 0' }}>
                                <ScanOutlined style={{ fontSize: '48px', marginBottom: '10px' }} />
                                <p>請掃描條碼或輸入物品代碼</p>
                            </div>
                        )}
                    </Card>
                </Col>
            </Row>

            {/* 借用Modal */}
            <Modal
                title="快速借用"
                open={borrowModalVisible}
                onOk={handleBorrowSubmit}
                onCancel={() => setBorrowModalVisible(false)}
                okText="確定借用"
                cancelText="取消"
            >
                <Form form={borrowForm} layout="vertical">
                    <Form.Item
                        label="借用人姓名"
                        name="borrowerName"
                        rules={[{ required: true, message: '請輸入借用人姓名' }]}
                    >
                        <Input placeholder="請輸入借用人姓名" />
                    </Form.Item>

                    <Form.Item
                        label="部門"
                        name="borrowerDepartment"
                    >
                        <Input placeholder="請輸入部門" />
                    </Form.Item>

                    <Form.Item
                        label="聯絡方式"
                        name="borrowerContact"
                    >
                        <Input placeholder="請輸入聯絡方式" />
                    </Form.Item>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                label="借用數量"
                                name="quantity"
                                rules={[{ required: true, message: '請輸入借用數量' }]}
                            >
                                <InputNumber
                                    min={1}
                                    max={scannedItem?.availableStock || 1}
                                    style={{ width: '100%' }}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                label="預計借用天數"
                                name="expectedReturnDays"
                                initialValue={7}
                            >
                                <InputNumber
                                    min={1}
                                    max={30}
                                    style={{ width: '100%' }}
                                />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        label="借用目的"
                        name="purpose"
                    >
                        <Input.TextArea rows={3} placeholder="請輸入借用目的" />
                    </Form.Item>
                </Form>
            </Modal>

            {/* 歸還Modal */}
            <Modal
                title="快速歸還"
                open={returnModalVisible}
                onOk={handleReturnSubmit}
                onCancel={() => setReturnModalVisible(false)}
                okText="確定歸還"
                cancelText="取消"
            >
                <Form form={returnForm} layout="vertical">
                    <Form.Item
                        label="歸還數量"
                        name="returnQuantity"
                        rules={[{ required: true, message: '請輸入歸還數量' }]}
                        initialValue={1}
                    >
                        <InputNumber
                            min={1}
                            style={{ width: '100%' }}
                        />
                    </Form.Item>

                    <Form.Item
                        label="物品狀況"
                        name="condition"
                        initialValue="GOOD"
                    >
                        <Select>
                            <Option value="GOOD">良好</Option>
                            <Option value="DAMAGED">損壞</Option>
                            <Option value="LOST">遺失</Option>
                        </Select>
                    </Form.Item>

                    <Form.Item
                        label="備註"
                        name="notes"
                    >
                        <Input.TextArea rows={3} placeholder="請輸入備註" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default BarcodeScanner;
