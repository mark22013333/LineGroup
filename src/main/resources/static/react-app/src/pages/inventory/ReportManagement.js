import React, { useState } from 'react';
import { 
    Card, 
    Button, 
    Form, 
    Select, 
    DatePicker, 
    message, 
    Space, 
    Row,
    Col,
    Divider,
    Alert,
    Spin
} from 'antd';
import { 
    FileExcelOutlined, 
    FilePdfOutlined,
    DownloadOutlined,
    BarChartOutlined
} from '@ant-design/icons';
import { inventoryAPI } from '../../services/inventoryAPI';
import dayjs from 'dayjs';

const { Option } = Select;
const { RangePicker } = DatePicker;

const ReportManagement = () => {
    const [inventoryForm] = Form.useForm();
    const [borrowForm] = Form.useForm();
    const [loading, setLoading] = useState(false);

    const handleGenerateInventoryReport = async () => {
        try {
            const values = await inventoryForm.validateFields();
            setLoading(true);

            const requestData = {
                categoryIds: values.categoryIds,
                itemCodes: values.itemCodes,
                includeOutOfStock: values.includeOutOfStock || false,
                includeLowStock: values.includeLowStock || false,
                format: 'EXCEL'
            };

            const response = await inventoryAPI.generateInventoryReport(requestData);
            
            if (response.success) {
                message.success('庫存報表產生成功');
                // 這裡應該處理檔案下載
                console.log('報表資訊:', response.data);
            } else {
                message.error(response.message || '報表產生失敗');
            }
        } catch (error) {
            message.error('報表產生失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateBorrowReport = async () => {
        try {
            const values = await borrowForm.validateFields();
            setLoading(true);

            const requestData = {
                startDate: values.dateRange ? values.dateRange[0].format('YYYY-MM-DD') : undefined,
                endDate: values.dateRange ? values.dateRange[1].format('YYYY-MM-DD') : undefined,
                status: values.status,
                borrowerDepartment: values.borrowerDepartment,
                includeOverdue: values.includeOverdue || false,
                format: 'EXCEL'
            };

            const response = await inventoryAPI.generateBorrowReport(requestData);
            
            if (response.success) {
                message.success('借還報表產生成功');
                // 這裡應該處理檔案下載
                console.log('報表資訊:', response.data);
            } else {
                message.error(response.message || '報表產生失敗');
            }
        } catch (error) {
            message.error('報表產生失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px' }}>
            <Row gutter={[16, 16]}>
                {/* 庫存報表 */}
                <Col xs={24} lg={12}>
                    <Card 
                        title={
                            <Space>
                                <BarChartOutlined />
                                庫存狀況報表
                            </Space>
                        }
                        style={{ height: '100%' }}
                    >
                        <Alert
                            message="庫存報表說明"
                            description="產生當前庫存狀況報表，包含物品資訊、庫存數量、價值統計等"
                            type="info"
                            showIcon
                            style={{ marginBottom: '20px' }}
                        />

                        <Form
                            form={inventoryForm}
                            layout="vertical"
                            initialValues={{
                                includeOutOfStock: true,
                                includeLowStock: true
                            }}
                        >
                            <Form.Item
                                label="分類篩選"
                                name="categoryIds"
                            >
                                <Select
                                    mode="multiple"
                                    placeholder="選擇分類（不選則包含所有分類）"
                                    allowClear
                                >
                                    <Option value={1}>電子設備</Option>
                                    <Option value={2}>辦公用品</Option>
                                    <Option value={3}>工具設備</Option>
                                    <Option value={4}>家具用品</Option>
                                </Select>
                            </Form.Item>

                            <Form.Item
                                label="物品代碼"
                                name="itemCodes"
                            >
                                <Select
                                    mode="tags"
                                    placeholder="輸入物品代碼（可多選）"
                                    allowClear
                                />
                            </Form.Item>

                            <Form.Item name="includeOutOfStock" valuePropName="checked">
                                <Space>
                                    <input type="checkbox" />
                                    包含缺貨物品
                                </Space>
                            </Form.Item>

                            <Form.Item name="includeLowStock" valuePropName="checked">
                                <Space>
                                    <input type="checkbox" />
                                    包含低庫存物品
                                </Space>
                            </Form.Item>

                            <Divider />

                            <Space style={{ width: '100%', justifyContent: 'center' }}>
                                <Button 
                                    type="primary" 
                                    icon={<FileExcelOutlined />}
                                    onClick={handleGenerateInventoryReport}
                                    loading={loading}
                                >
                                    產生 Excel 報表
                                </Button>
                                <Button 
                                    icon={<FilePdfOutlined />}
                                    disabled
                                >
                                    產生 PDF 報表
                                </Button>
                            </Space>
                        </Form>
                    </Card>
                </Col>

                {/* 借還記錄報表 */}
                <Col xs={24} lg={12}>
                    <Card 
                        title={
                            <Space>
                                <DownloadOutlined />
                                借還記錄報表
                            </Space>
                        }
                        style={{ height: '100%' }}
                    >
                        <Alert
                            message="借還報表說明"
                            description="產生指定時間範圍內的借還記錄報表，包含借用人、物品、時間等資訊"
                            type="info"
                            showIcon
                            style={{ marginBottom: '20px' }}
                        />

                        <Form
                            form={borrowForm}
                            layout="vertical"
                            initialValues={{
                                dateRange: [dayjs().subtract(30, 'day'), dayjs()],
                                includeOverdue: true
                            }}
                        >
                            <Form.Item
                                label="時間範圍"
                                name="dateRange"
                                rules={[{ required: true, message: '請選擇時間範圍' }]}
                            >
                                <RangePicker
                                    style={{ width: '100%' }}
                                    placeholder={['開始日期', '結束日期']}
                                />
                            </Form.Item>

                            <Form.Item
                                label="借還狀態"
                                name="status"
                            >
                                <Select
                                    placeholder="選擇狀態（不選則包含所有狀態）"
                                    allowClear
                                >
                                    <Option value="BORROWED">已借出</Option>
                                    <Option value="RETURNED">已歸還</Option>
                                    <Option value="PARTIAL_RETURNED">部分歸還</Option>
                                    <Option value="OVERDUE">逾期</Option>
                                    <Option value="CANCELLED">已取消</Option>
                                </Select>
                            </Form.Item>

                            <Form.Item
                                label="借用人部門"
                                name="borrowerDepartment"
                            >
                                <Select
                                    placeholder="選擇部門（不選則包含所有部門）"
                                    allowClear
                                >
                                    <Option value="IT部門">IT部門</Option>
                                    <Option value="行銷部門">行銷部門</Option>
                                    <Option value="工程部門">工程部門</Option>
                                    <Option value="財務部門">財務部門</Option>
                                    <Option value="人事部門">人事部門</Option>
                                </Select>
                            </Form.Item>

                            <Form.Item name="includeOverdue" valuePropName="checked">
                                <Space>
                                    <input type="checkbox" />
                                    包含逾期記錄
                                </Space>
                            </Form.Item>

                            <Divider />

                            <Space style={{ width: '100%', justifyContent: 'center' }}>
                                <Button 
                                    type="primary" 
                                    icon={<FileExcelOutlined />}
                                    onClick={handleGenerateBorrowReport}
                                    loading={loading}
                                >
                                    產生 Excel 報表
                                </Button>
                                <Button 
                                    icon={<FilePdfOutlined />}
                                    disabled
                                >
                                    產生 PDF 報表
                                </Button>
                            </Space>
                        </Form>
                    </Card>
                </Col>
            </Row>

            {/* 快速報表區域 */}
            <Row style={{ marginTop: '20px' }}>
                <Col span={24}>
                    <Card title="快速報表">
                        <Row gutter={[16, 16]}>
                            <Col xs={24} sm={12} md={6}>
                                <Button 
                                    block 
                                    size="large"
                                    onClick={() => {
                                        inventoryForm.setFieldsValue({
                                            includeLowStock: true,
                                            includeOutOfStock: true
                                        });
                                        handleGenerateInventoryReport();
                                    }}
                                >
                                    低庫存警告報表
                                </Button>
                            </Col>
                            <Col xs={24} sm={12} md={6}>
                                <Button 
                                    block 
                                    size="large"
                                    onClick={() => {
                                        borrowForm.setFieldsValue({
                                            status: 'OVERDUE',
                                            dateRange: [dayjs().subtract(90, 'day'), dayjs()]
                                        });
                                        handleGenerateBorrowReport();
                                    }}
                                >
                                    逾期借用報表
                                </Button>
                            </Col>
                            <Col xs={24} sm={12} md={6}>
                                <Button 
                                    block 
                                    size="large"
                                    onClick={() => {
                                        borrowForm.setFieldsValue({
                                            dateRange: [dayjs().startOf('month'), dayjs().endOf('month')]
                                        });
                                        handleGenerateBorrowReport();
                                    }}
                                >
                                    本月借還報表
                                </Button>
                            </Col>
                            <Col xs={24} sm={12} md={6}>
                                <Button 
                                    block 
                                    size="large"
                                    onClick={() => {
                                        inventoryForm.resetFields();
                                        handleGenerateInventoryReport();
                                    }}
                                >
                                    完整庫存報表
                                </Button>
                            </Col>
                        </Row>
                    </Card>
                </Col>
            </Row>

            {loading && (
                <div style={{ 
                    position: 'fixed', 
                    top: 0, 
                    left: 0, 
                    right: 0, 
                    bottom: 0, 
                    backgroundColor: 'rgba(0,0,0,0.3)', 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center',
                    zIndex: 9999
                }}>
                    <Card style={{ textAlign: 'center', padding: '20px' }}>
                        <Spin size="large" />
                        <p style={{ marginTop: '10px' }}>正在產生報表，請稍候...</p>
                    </Card>
                </div>
            )}
        </div>
    );
};

export default ReportManagement;
