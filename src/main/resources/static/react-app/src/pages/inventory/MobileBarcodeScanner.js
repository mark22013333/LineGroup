import React, { useState, useEffect } from 'react';
import { 
    Button, 
    Input, 
    Space, 
    Alert, 
    Modal, 
    Form, 
    InputNumber, 
    Select, 
    message,
    Descriptions,
    Tag,
    Card,
    Row,
    Col,
    Drawer,
    FloatButton
} from 'antd';
import { 
    ScanOutlined, 
    CameraOutlined, 
    SearchOutlined,
    ShoppingCartOutlined,
    UndoOutlined,
    ArrowLeftOutlined,
    InfoCircleOutlined,
    CloseOutlined
} from '@ant-design/icons';
import BarcodeScannerLib from 'react-qr-barcode-scanner';
import { inventoryAPI } from '../../services/inventoryAPI';
import { useNavigate } from 'react-router-dom';

const { Option } = Select;

const MobileBarcodeScanner = () => {
    const navigate = useNavigate();
    const [scanning, setScanning] = useState(false);
    const [manualInput, setManualInput] = useState('');
    const [scannedItem, setScannedItem] = useState(null);
    const [borrowModalVisible, setBorrowModalVisible] = useState(false);
    const [returnModalVisible, setReturnModalVisible] = useState(false);
    const [itemInfoVisible, setItemInfoVisible] = useState(false);
    const [borrowForm] = Form.useForm();
    const [returnForm] = Form.useForm();

    // 自動啟動掃描
    useEffect(() => {
        startScanning();
        return () => {
            setScanning(false);
        };
    }, []);

    const startScanning = () => {
        setScanning(true);
        message.info('正在啟動攝影機，請允許瀏覽器存取攝影機權限');
    };

    const stopScanning = () => {
        setScanning(false);
        message.info('已停止掃描');
    };

    const handleScanSuccess = async (result) => {
        console.log('[MobileBarcodeScanner] 掃描成功:', result);
        
        if (result && result.text) {
            // 震動回饋（如果支援）
            if (navigator.vibrate) {
                navigator.vibrate(200);
            }

            // 先顯示掃描到的內容
            message.success(`掃描到內容: ${result.text}`, 3);
            
            // 暫停掃描以避免重複掃描
            stopScanning();
            
            // 建立一個簡單的掃描結果物件來顯示
            const scannedResult = {
                barcode: result.text,
                itemCode: result.text,
                itemName: `掃描內容: ${result.text}`,
                categoryName: '未分類',
                currentStock: 0,
                availableStock: 0,
                location: '未知',
                status: 'SCANNED',
                found: false
            };
            
            setScannedItem(scannedResult);
            setItemInfoVisible(true); // 立即顯示物品資訊抽屜
            
            try {
                console.log('[MobileBarcodeScanner] 正在查詢後端 API...');
                const response = await inventoryAPI.scanBarcode({
                    barcode: result.text
                });

                console.log('[MobileBarcodeScanner] 後端 API 回應:', response);

                if (response.success && response.data.found) {
                    // 更新為真實的物品資訊
                    setScannedItem({...response.data, found: true});
                    message.success(`找到物品: ${response.data.itemName}`, 2);
                } else {
                    // 保持顯示掃描內容，但標記為未找到
                    setScannedItem({...scannedResult, found: false});
                    message.warning(`條碼 "${result.text}" 在庫存中找不到對應物品`, 2);
                }
            } catch (error) {
                console.error('[MobileBarcodeScanner] 掃描查詢失敗:', error);
                // 仍然顯示掃描內容，但標記為查詢失敗
                setScannedItem({...scannedResult, found: false, error: error.message});
                message.error('查詢失敗，但已掃描到內容: ' + result.text, 3);
            }
        }
    };

    const handleScanError = (error) => {
        if (error.name === 'NotFoundException' || 
            error.message?.includes('No MultiFormat Readers were able to detect the code')) {
            return;
        }
        
        console.error('[MobileBarcodeScanner] 掃描錯誤:', error);
        
        if (error.name === 'NotAllowedError') {
            message.error('攝影機權限被拒絕，請允許瀏覽器存取攝影機');
        } else if (error.name === 'NotFoundError') {
            message.error('找不到攝影機設備');
        } else if (error.name === 'NotReadableError') {
            message.error('攝影機被其他應用程式佔用');
        } else if (error.name === 'OverconstrainedError') {
            message.error('攝影機不支援所請求的設定');
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
                setItemInfoVisible(true);
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
        setItemInfoVisible(false);
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
        setItemInfoVisible(false);
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
                startScanning(); // 重新開始掃描
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
                startScanning(); // 重新開始掃描
            } else {
                message.error(response.message || '歸還失敗');
            }
        } catch (error) {
            message.error('歸還失敗: ' + error.message);
        }
    };

    const handleContinueScanning = () => {
        setScannedItem(null);
        setItemInfoVisible(false);
        startScanning();
    };

    return (
        <div style={{ 
            height: '100vh', 
            width: '100vw', 
            position: 'relative',
            backgroundColor: '#000',
            overflow: 'hidden'
        }}>
            {/* 頂部導航欄 */}
            <div style={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                zIndex: 1000,
                background: 'rgba(0, 0, 0, 0.7)',
                padding: '10px 15px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Button 
                    type="text" 
                    icon={<ArrowLeftOutlined />}
                    onClick={() => navigate(-1)}
                    style={{ color: 'white', fontSize: '18px' }}
                >
                    返回
                </Button>
                <span style={{ color: 'white', fontSize: '16px', fontWeight: 'bold' }}>
                    條碼掃描
                </span>
                <Button 
                    type="text" 
                    icon={scanning ? <CloseOutlined /> : <ScanOutlined />}
                    onClick={scanning ? stopScanning : startScanning}
                    style={{ color: 'white', fontSize: '18px' }}
                />
            </div>

            {/* 掃描區域 */}
            <div style={{ 
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}>
                {scanning ? (
                    <BarcodeScannerLib
                        width="100%"
                        height="100%"
                        onUpdate={(err, result) => {
                            if (result) {
                                handleScanSuccess(result);
                            } else if (err) {
                                handleScanError(err);
                            }
                        }}
                        facingMode="environment"
                        constraints={{
                            video: {
                                facingMode: "environment",
                                width: { ideal: 1920 },
                                height: { ideal: 1080 },
                                focusMode: "continuous"
                            }
                        }}
                    />
                ) : (
                    <div style={{ 
                        textAlign: 'center', 
                        color: 'white',
                        padding: '20px'
                    }}>
                        <CameraOutlined style={{ fontSize: '64px', marginBottom: '20px' }} />
                        <h2>攝影機已停止</h2>
                        <p>點擊右上角按鈕重新開始掃描</p>
                    </div>
                )}
            </div>

            {/* 掃描框指示器 */}
            {scanning && (
                <div style={{
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    transform: 'translate(-50%, -50%)',
                    width: '250px',
                    height: '250px',
                    border: '2px solid #1890ff',
                    borderRadius: '12px',
                    pointerEvents: 'none',
                    zIndex: 100
                }}>
                    {/* 四個角落的裝飾 */}
                    <div style={{
                        position: 'absolute',
                        top: '-2px',
                        left: '-2px',
                        width: '30px',
                        height: '30px',
                        borderTop: '4px solid #52c41a',
                        borderLeft: '4px solid #52c41a',
                        borderRadius: '12px 0 0 0'
                    }} />
                    <div style={{
                        position: 'absolute',
                        top: '-2px',
                        right: '-2px',
                        width: '30px',
                        height: '30px',
                        borderTop: '4px solid #52c41a',
                        borderRight: '4px solid #52c41a',
                        borderRadius: '0 12px 0 0'
                    }} />
                    <div style={{
                        position: 'absolute',
                        bottom: '-2px',
                        left: '-2px',
                        width: '30px',
                        height: '30px',
                        borderBottom: '4px solid #52c41a',
                        borderLeft: '4px solid #52c41a',
                        borderRadius: '0 0 0 12px'
                    }} />
                    <div style={{
                        position: 'absolute',
                        bottom: '-2px',
                        right: '-2px',
                        width: '30px',
                        height: '30px',
                        borderBottom: '4px solid #52c41a',
                        borderRight: '4px solid #52c41a',
                        borderRadius: '0 0 12px 0'
                    }} />
                </div>
            )}

            {/* 底部操作區域 */}
            <div style={{
                position: 'absolute',
                bottom: 0,
                left: 0,
                right: 0,
                background: 'rgba(0, 0, 0, 0.8)',
                padding: '20px',
                zIndex: 1000
            }}>
                <Space.Compact style={{ width: '100%', marginBottom: '15px' }}>
                    <Input
                        placeholder="手動輸入條碼"
                        value={manualInput}
                        onChange={(e) => setManualInput(e.target.value)}
                        onPressEnter={handleManualScan}
                        style={{ fontSize: '16px', height: '45px' }}
                    />
                    <Button 
                        type="primary" 
                        icon={<SearchOutlined />}
                        onClick={handleManualScan}
                        style={{ height: '45px', minWidth: '60px' }}
                    />
                </Space.Compact>

                {scanning && (
                    <Alert
                        message="掃描提示"
                        description="將條碼對準中央框線內，保持穩定並確保光線充足"
                        type="info"
                        showIcon
                        style={{ 
                            backgroundColor: 'rgba(255, 255, 255, 0.9)',
                            border: 'none',
                            borderRadius: '8px'
                        }}
                    />
                )}
            </div>

            {/* 物品資訊抽屜 */}
            <Drawer
                title="物品資訊"
                placement="bottom"
                height="60%"
                open={itemInfoVisible}
                onClose={() => setItemInfoVisible(false)}
                extra={
                    <Button 
                        type="primary" 
                        onClick={handleContinueScanning}
                        icon={<ScanOutlined />}
                    >
                        繼續掃描
                    </Button>
                }
            >
                {scannedItem && (
                    <div>
                        <Alert
                            message={scannedItem.found ? "掃描成功" : "掃描到內容"}
                            description={
                                scannedItem.found 
                                    ? `找到物品: ${scannedItem.itemName}`
                                    : `掃描到條碼: ${scannedItem.barcode}${scannedItem.error ? ' (查詢失敗)' : ' (庫存中未找到)'}`
                            }
                            type={scannedItem.found ? "success" : "warning"}
                            showIcon
                            style={{ marginBottom: '20px' }}
                        />

                        <Descriptions column={1} size="middle">
                            <Descriptions.Item label="條碼內容">
                                <Tag color="blue" style={{ fontSize: '14px', padding: '4px 8px' }}>
                                    {scannedItem.barcode}
                                </Tag>
                            </Descriptions.Item>
                            {scannedItem.found && (
                                <>
                                    <Descriptions.Item label="物品代碼">
                                        <strong>{scannedItem.itemCode}</strong>
                                    </Descriptions.Item>
                                    <Descriptions.Item label="物品名稱">
                                        {scannedItem.itemName}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="分類">
                                        {scannedItem.categoryName}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="庫存狀況">
                                        <Space direction="vertical" size="small">
                                            <Tag color={scannedItem.currentStock > 0 ? 'green' : 'red'}>
                                                當前庫存: {scannedItem.currentStock}
                                            </Tag>
                                            <Tag color={scannedItem.availableStock > 0 ? 'green' : 'orange'}>
                                                可借用: {scannedItem.availableStock}
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
                                </>
                            )}
                            {!scannedItem.found && (
                                <Descriptions.Item label="狀態">
                                    <Tag color="orange">未在庫存中找到</Tag>
                                </Descriptions.Item>
                            )}
                            {scannedItem.error && (
                                <Descriptions.Item label="錯誤訊息">
                                    <Tag color="red">{scannedItem.error}</Tag>
                                </Descriptions.Item>
                            )}
                        </Descriptions>

                        <div style={{ marginTop: '30px' }}>
                            <Row gutter={[16, 16]}>
                                {scannedItem.found ? (
                                    <>
                                        <Col span={12}>
                                            <Button 
                                                type="primary" 
                                                icon={<ShoppingCartOutlined />}
                                                onClick={handleBorrow}
                                                disabled={scannedItem.availableStock <= 0}
                                                block
                                                size="large"
                                            >
                                                借用
                                            </Button>
                                        </Col>
                                        <Col span={12}>
                                            <Button 
                                                icon={<UndoOutlined />}
                                                onClick={handleReturn}
                                                block
                                                size="large"
                                            >
                                                歸還
                                            </Button>
                                        </Col>
                                    </>
                                ) : (
                                    <Col span={24}>
                                        <Button 
                                            type="primary" 
                                            icon={<ScanOutlined />}
                                            onClick={handleContinueScanning}
                                            block
                                            size="large"
                                        >
                                            重新掃描
                                        </Button>
                                    </Col>
                                )}
                            </Row>
                        </div>
                    </div>
                )}
            </Drawer>

            {/* 借用Modal */}
            <Modal
                title="快速借用"
                open={borrowModalVisible}
                onOk={handleBorrowSubmit}
                onCancel={() => setBorrowModalVisible(false)}
                okText="確定借用"
                cancelText="取消"
                width="90%"
                style={{ top: 20 }}
            >
                <Form form={borrowForm} layout="vertical">
                    <Form.Item
                        label="借用人姓名"
                        name="borrowerName"
                        rules={[{ required: true, message: '請輸入借用人姓名' }]}
                    >
                        <Input placeholder="請輸入借用人姓名" size="large" />
                    </Form.Item>

                    <Form.Item
                        label="部門"
                        name="borrowerDepartment"
                    >
                        <Input placeholder="請輸入部門" size="large" />
                    </Form.Item>

                    <Form.Item
                        label="聯絡方式"
                        name="borrowerContact"
                    >
                        <Input placeholder="請輸入聯絡方式" size="large" />
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
                                    size="large"
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
                                    size="large"
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
                width="90%"
                style={{ top: 20 }}
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
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item
                        label="物品狀況"
                        name="condition"
                        initialValue="GOOD"
                    >
                        <Select size="large">
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

export default MobileBarcodeScanner;
