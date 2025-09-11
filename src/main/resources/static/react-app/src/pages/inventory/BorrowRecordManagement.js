import React, { useState, useEffect } from 'react';
import { 
    Table, 
    Button, 
    Modal, 
    Form, 
    Input, 
    Select, 
    InputNumber, 
    DatePicker, 
    message, 
    Space, 
    Tag, 
    Row,
    Col,
    Card,
    Descriptions,
    Alert
} from 'antd';
import { 
    PlusOutlined, 
    EyeOutlined, 
    UndoOutlined,
    SearchOutlined,
    ExportOutlined,
    ExclamationCircleOutlined
} from '@ant-design/icons';
import { inventoryAPI } from '../../services/inventoryAPI';
import dayjs from 'dayjs';

const { Option } = Select;
const { Search } = Input;
const { RangePicker } = DatePicker;

const BorrowRecordManagement = () => {
    const [records, setRecords] = useState([]);
    const [loading, setLoading] = useState(false);
    const [detailModalVisible, setDetailModalVisible] = useState(false);
    const [returnModalVisible, setReturnModalVisible] = useState(false);
    const [selectedRecord, setSelectedRecord] = useState(null);
    const [returnForm] = Form.useForm();
    const [searchParams, setSearchParams] = useState({
        page: 1,
        size: 10,
        sortBy: 'borrowDate',
        sortDir: 'DESC'
    });
    const [pagination, setPagination] = useState({
        current: 1,
        pageSize: 10,
        total: 0
    });

    useEffect(() => {
        fetchRecords();
    }, [searchParams]);

    const fetchRecords = async () => {
        try {
            setLoading(true);
            const response = await inventoryAPI.getBorrowRecords(searchParams);
            if (response.success) {
                setRecords(response.data.content || []);
                setPagination({
                    current: response.data.currentPage || 1,
                    pageSize: response.data.pageSize || 10,
                    total: response.data.totalElements || 0
                });
            } else {
                message.error(response.message || '載入借還記錄失敗');
            }
        } catch (error) {
            message.error('載入借還記錄失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleTableChange = (pagination, filters, sorter) => {
        setSearchParams({
            ...searchParams,
            page: pagination.current,
            size: pagination.pageSize,
            sortBy: sorter.field || 'borrowDate',
            sortDir: sorter.order === 'ascend' ? 'ASC' : 'DESC'
        });
    };

    const handleSearch = (value) => {
        setSearchParams({
            ...searchParams,
            keyword: value,
            page: 1
        });
    };

    const handleStatusFilter = (status) => {
        setSearchParams({
            ...searchParams,
            status: status,
            page: 1
        });
    };

    const handleDateRangeFilter = (dates) => {
        if (dates && dates.length === 2) {
            setSearchParams({
                ...searchParams,
                borrowStartDate: dates[0].format('YYYY-MM-DD'),
                borrowEndDate: dates[1].format('YYYY-MM-DD'),
                page: 1
            });
        } else {
            setSearchParams({
                ...searchParams,
                borrowStartDate: undefined,
                borrowEndDate: undefined,
                page: 1
            });
        }
    };

    const handleViewDetail = (record) => {
        setSelectedRecord(record);
        setDetailModalVisible(true);
    };

    const handleReturn = (record) => {
        setSelectedRecord(record);
        returnForm.resetFields();
        returnForm.setFieldsValue({
            returnQuantity: record.quantity - record.returnedQuantity
        });
        setReturnModalVisible(true);
    };

    const handleReturnSubmit = async () => {
        try {
            const values = await returnForm.validateFields();
            
            const response = await inventoryAPI.returnItem(selectedRecord.id, {
                returnQuantity: values.returnQuantity,
                condition: values.condition,
                notes: values.notes
            });

            if (response.success) {
                message.success('歸還成功');
                setReturnModalVisible(false);
                fetchRecords();
            } else {
                message.error(response.message || '歸還失敗');
            }
        } catch (error) {
            message.error('歸還失敗: ' + error.message);
        }
    };

    const getStatusTag = (status) => {
        const statusMap = {
            'BORROWED': { color: 'blue', text: '已借出' },
            'RETURNED': { color: 'green', text: '已歸還' },
            'PARTIAL_RETURNED': { color: 'orange', text: '部分歸還' },
            'OVERDUE': { color: 'red', text: '逾期' },
            'CANCELLED': { color: 'default', text: '已取消' }
        };
        const config = statusMap[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
    };

    const isOverdue = (record) => {
        if (record.status === 'RETURNED' || record.status === 'CANCELLED') {
            return false;
        }
        return dayjs().isAfter(dayjs(record.expectedReturnDate));
    };

    const columns = [
        {
            title: '借還單號',
            dataIndex: 'recordNumber',
            key: 'recordNumber',
            render: (text) => <strong>{text}</strong>
        },
        {
            title: '物品資訊',
            key: 'item',
            render: (_, record) => (
                <div>
                    <div><strong>{record.item?.name}</strong></div>
                    <div style={{ fontSize: '12px', color: '#666' }}>
                        {record.item?.code}
                    </div>
                </div>
            )
        },
        {
            title: '借用人',
            key: 'borrower',
            render: (_, record) => (
                <div>
                    <div>{record.borrowerName}</div>
                    <div style={{ fontSize: '12px', color: '#666' }}>
                        {record.borrowerDepartment}
                    </div>
                </div>
            )
        },
        {
            title: '數量',
            key: 'quantity',
            render: (_, record) => (
                <div>
                    <div>借用: {record.quantity}</div>
                    {record.returnedQuantity > 0 && (
                        <div style={{ fontSize: '12px', color: '#52c41a' }}>
                            已還: {record.returnedQuantity}
                        </div>
                    )}
                </div>
            )
        },
        {
            title: '借用時間',
            dataIndex: 'borrowDate',
            key: 'borrowDate',
            sorter: true,
            render: (date) => dayjs(date).format('YYYY-MM-DD HH:mm')
        },
        {
            title: '預計歸還',
            dataIndex: 'expectedReturnDate',
            key: 'expectedReturnDate',
            render: (date, record) => {
                const isLate = isOverdue(record);
                return (
                    <div style={{ color: isLate ? '#f5222d' : 'inherit' }}>
                        {dayjs(date).format('YYYY-MM-DD HH:mm')}
                        {isLate && <ExclamationCircleOutlined style={{ marginLeft: '4px' }} />}
                    </div>
                );
            }
        },
        {
            title: '實際歸還',
            dataIndex: 'actualReturnDate',
            key: 'actualReturnDate',
            render: (date) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
        },
        {
            title: '狀態',
            dataIndex: 'status',
            key: 'status',
            render: (status, record) => (
                <Space direction="vertical" size="small">
                    {getStatusTag(status)}
                    {isOverdue(record) && status !== 'RETURNED' && (
                        <Tag color="red" size="small">逾期</Tag>
                    )}
                </Space>
            )
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space>
                    <Button 
                        type="link" 
                        icon={<EyeOutlined />} 
                        onClick={() => handleViewDetail(record)}
                    >
                        詳情
                    </Button>
                    {(record.status === 'BORROWED' || record.status === 'PARTIAL_RETURNED') && (
                        <Button 
                            type="link" 
                            icon={<UndoOutlined />}
                            onClick={() => handleReturn(record)}
                        >
                            歸還
                        </Button>
                    )}
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '20px' }}>
            <Card>
                <Row justify="space-between" align="middle" style={{ marginBottom: '16px' }}>
                    <Col>
                        <h2>借還記錄管理</h2>
                    </Col>
                    <Col>
                        <Space>
                            <Button icon={<ExportOutlined />}>
                                匯出
                            </Button>
                        </Space>
                    </Col>
                </Row>

                <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
                    <Col xs={24} sm={8}>
                        <Search
                            placeholder="搜尋借還單號、物品、借用人..."
                            allowClear
                            enterButton={<SearchOutlined />}
                            onSearch={handleSearch}
                        />
                    </Col>
                    <Col xs={24} sm={6}>
                        <Select
                            placeholder="選擇狀態"
                            allowClear
                            style={{ width: '100%' }}
                            onChange={handleStatusFilter}
                        >
                            <Option value="BORROWED">已借出</Option>
                            <Option value="RETURNED">已歸還</Option>
                            <Option value="PARTIAL_RETURNED">部分歸還</Option>
                            <Option value="OVERDUE">逾期</Option>
                            <Option value="CANCELLED">已取消</Option>
                        </Select>
                    </Col>
                    <Col xs={24} sm={10}>
                        <RangePicker
                            placeholder={['借用開始日期', '借用結束日期']}
                            style={{ width: '100%' }}
                            onChange={handleDateRangeFilter}
                        />
                    </Col>
                </Row>

                <Table
                    columns={columns}
                    dataSource={records}
                    rowKey="id"
                    loading={loading}
                    pagination={{
                        ...pagination,
                        showSizeChanger: true,
                        showQuickJumper: true,
                        showTotal: (total, range) => 
                            `第 ${range[0]}-${range[1]} 項，共 ${total} 項`
                    }}
                    onChange={handleTableChange}
                    scroll={{ x: 1200 }}
                />
            </Card>

            {/* 詳情Modal */}
            <Modal
                title="借還記錄詳情"
                open={detailModalVisible}
                onCancel={() => setDetailModalVisible(false)}
                footer={[
                    <Button key="close" onClick={() => setDetailModalVisible(false)}>
                        關閉
                    </Button>
                ]}
                width={800}
            >
                {selectedRecord && (
                    <div>
                        {isOverdue(selectedRecord) && selectedRecord.status !== 'RETURNED' && (
                            <Alert
                                message="逾期警告"
                                description="此借用記錄已逾期，請盡快處理歸還"
                                type="warning"
                                showIcon
                                style={{ marginBottom: '20px' }}
                            />
                        )}
                        
                        <Descriptions column={2} bordered>
                            <Descriptions.Item label="借還單號" span={2}>
                                <strong>{selectedRecord.recordNumber}</strong>
                            </Descriptions.Item>
                            <Descriptions.Item label="物品代碼">
                                {selectedRecord.item?.code}
                            </Descriptions.Item>
                            <Descriptions.Item label="物品名稱">
                                {selectedRecord.item?.name}
                            </Descriptions.Item>
                            <Descriptions.Item label="借用人">
                                {selectedRecord.borrowerName}
                            </Descriptions.Item>
                            <Descriptions.Item label="部門">
                                {selectedRecord.borrowerDepartment || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="聯絡方式">
                                {selectedRecord.borrowerContact || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="借用數量">
                                {selectedRecord.quantity}
                            </Descriptions.Item>
                            <Descriptions.Item label="已歸還數量">
                                {selectedRecord.returnedQuantity}
                            </Descriptions.Item>
                            <Descriptions.Item label="借用時間">
                                {dayjs(selectedRecord.borrowDate).format('YYYY-MM-DD HH:mm:ss')}
                            </Descriptions.Item>
                            <Descriptions.Item label="預計歸還時間">
                                {dayjs(selectedRecord.expectedReturnDate).format('YYYY-MM-DD HH:mm:ss')}
                            </Descriptions.Item>
                            <Descriptions.Item label="實際歸還時間">
                                {selectedRecord.actualReturnDate ? 
                                    dayjs(selectedRecord.actualReturnDate).format('YYYY-MM-DD HH:mm:ss') : 
                                    '-'
                                }
                            </Descriptions.Item>
                            <Descriptions.Item label="狀態">
                                {getStatusTag(selectedRecord.status)}
                            </Descriptions.Item>
                            <Descriptions.Item label="借用目的" span={2}>
                                {selectedRecord.purpose || '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="備註" span={2}>
                                {selectedRecord.notes || '-'}
                            </Descriptions.Item>
                        </Descriptions>
                    </div>
                )}
            </Modal>

            {/* 歸還Modal */}
            <Modal
                title="物品歸還"
                open={returnModalVisible}
                onOk={handleReturnSubmit}
                onCancel={() => setReturnModalVisible(false)}
                okText="確定歸還"
                cancelText="取消"
            >
                <Form form={returnForm} layout="vertical">
                    <Alert
                        message={`歸還物品: ${selectedRecord?.item?.name}`}
                        type="info"
                        style={{ marginBottom: '16px' }}
                    />
                    
                    <Form.Item
                        label="歸還數量"
                        name="returnQuantity"
                        rules={[{ required: true, message: '請輸入歸還數量' }]}
                    >
                        <InputNumber
                            min={1}
                            max={selectedRecord ? selectedRecord.quantity - selectedRecord.returnedQuantity : 1}
                            style={{ width: '100%' }}
                        />
                    </Form.Item>

                    <Form.Item
                        label="物品狀況"
                        name="condition"
                        initialValue="GOOD"
                        rules={[{ required: true, message: '請選擇物品狀況' }]}
                    >
                        <Select>
                            <Option value="GOOD">良好</Option>
                            <Option value="DAMAGED">損壞</Option>
                            <Option value="LOST">遺失</Option>
                        </Select>
                    </Form.Item>

                    <Form.Item
                        label="歸還備註"
                        name="notes"
                    >
                        <Input.TextArea rows={3} placeholder="請輸入歸還備註" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default BorrowRecordManagement;
