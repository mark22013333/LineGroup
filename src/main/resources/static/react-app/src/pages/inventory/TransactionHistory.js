import React, { useState, useEffect } from 'react';
import {
    Table, Card, Button, Input, Select, Space, DatePicker, Tag,
    Row, Col, Statistic, Alert, Tooltip, Modal, Descriptions, message
} from 'antd';
import {
    HistoryOutlined, SearchOutlined, ReloadOutlined, EyeOutlined,
    RiseOutlined, FallOutlined, ExportOutlined
} from '@ant-design/icons';
import { inventoryAPI } from '../../services/inventoryAPI';
import dayjs from 'dayjs';

const { Option } = Select;
const { RangePicker } = DatePicker;

const TransactionHistory = () => {
    const [loading, setLoading] = useState(false);
    const [transactions, setTransactions] = useState([]);
    const [statistics, setStatistics] = useState(null);
    const [pagination, setPagination] = useState({
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total, range) => `第 ${range[0]}-${range[1]} 項，共 ${total} 項`
    });
    const [filters, setFilters] = useState({
        itemId: null,
        transactionType: null,
        referenceType: null,
        startDate: null,
        endDate: null,
        keyword: ''
    });
    const [detailModalVisible, setDetailModalVisible] = useState(false);
    const [selectedTransaction, setSelectedTransaction] = useState(null);

    useEffect(() => {
        fetchTransactions();
        fetchStatistics();
    }, []);

    const fetchTransactions = async (page = 1, size = 10) => {
        try {
            setLoading(true);
            const params = {
                ...filters,
                page,
                size,
                sortBy: 'createdAt',
                sortDir: 'desc'
            };

            const response = await inventoryAPI.getTransactions(params);
            if (response.success) {
                setTransactions(response.data.content);
                setPagination(prev => ({
                    ...prev,
                    current: page,
                    pageSize: size,
                    total: response.data.totalElements
                }));
            } else {
                message.error(response.message || '載入異動記錄失敗');
            }
        } catch (error) {
            message.error('載入異動記錄失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchStatistics = async () => {
        try {
            const params = {
                startDate: dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
                endDate: dayjs().format('YYYY-MM-DD')
            };
            const response = await inventoryAPI.getTransactionStatistics(params);
            if (response.success) {
                setStatistics(response.data);
            }
        } catch (error) {
            console.error('載入統計資料失敗:', error);
        }
    };

    const handleTableChange = (paginationConfig) => {
        fetchTransactions(paginationConfig.current, paginationConfig.pageSize);
    };

    const handleSearch = () => {
        fetchTransactions(1, pagination.pageSize);
    };

    const handleReset = () => {
        setFilters({
            itemId: null,
            transactionType: null,
            referenceType: null,
            startDate: null,
            endDate: null,
            keyword: ''
        });
        setTimeout(() => fetchTransactions(1, pagination.pageSize), 0);
    };

    const handleDateRangeChange = (dates) => {
        if (dates && dates.length === 2) {
            setFilters(prev => ({
                ...prev,
                startDate: dates[0].format('YYYY-MM-DD'),
                endDate: dates[1].format('YYYY-MM-DD')
            }));
        } else {
            setFilters(prev => ({
                ...prev,
                startDate: null,
                endDate: null
            }));
        }
    };

    const handleViewDetail = (record) => {
        setSelectedTransaction(record);
        setDetailModalVisible(true);
    };

    const getTransactionTypeColor = (type) => {
        switch (type) {
            case 'IN': return 'green';
            case 'OUT': return 'red';
            case 'ADJUST': return 'blue';
            case 'DAMAGED': return 'orange';
            case 'LOST': return 'purple';
            default: return 'default';
        }
    };

    const getTransactionTypeText = (type) => {
        switch (type) {
            case 'IN': return '入庫';
            case 'OUT': return '出庫';
            case 'ADJUST': return '調整';
            case 'DAMAGED': return '損壞';
            case 'LOST': return '遺失';
            default: return type;
        }
    };

    const getReferenceTypeText = (type) => {
        switch (type) {
            case 'PURCHASE': return '採購';
            case 'BORROW': return '借用';
            case 'RETURN': return '歸還';
            case 'ADJUSTMENT': return '盤點調整';
            case 'DAMAGE': return '損壞報廢';
            case 'LOSS': return '遺失';
            default: return type;
        }
    };

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            width: 80,
        },
        {
            title: '物品代碼',
            dataIndex: 'itemCode',
            key: 'itemCode',
            width: 120,
        },
        {
            title: '物品名稱',
            dataIndex: 'itemName',
            key: 'itemName',
            width: 150,
        },
        {
            title: '異動類型',
            dataIndex: 'transactionType',
            key: 'transactionType',
            width: 100,
            render: (type) => (
                <Tag color={getTransactionTypeColor(type)}>
                    {getTransactionTypeText(type)}
                </Tag>
            ),
        },
        {
            title: '參考類型',
            dataIndex: 'referenceType',
            key: 'referenceType',
            width: 100,
            render: (type) => getReferenceTypeText(type),
        },
        {
            title: '數量變化',
            dataIndex: 'quantityChange',
            key: 'quantityChange',
            width: 100,
            render: (quantity, record) => (
                <span style={{ 
                    color: quantity > 0 ? '#52c41a' : '#f5222d',
                    fontWeight: 'bold'
                }}>
                    {quantity > 0 ? '+' : ''}{quantity}
                </span>
            ),
        },
        {
            title: '異動前',
            dataIndex: 'beforeQuantity',
            key: 'beforeQuantity',
            width: 80,
        },
        {
            title: '異動後',
            dataIndex: 'afterQuantity',
            key: 'afterQuantity',
            width: 80,
        },
        {
            title: '參考單號',
            dataIndex: 'referenceNumber',
            key: 'referenceNumber',
            width: 120,
            render: (text) => text || '-',
        },
        {
            title: '處理人',
            dataIndex: 'processedByName',
            key: 'processedByName',
            width: 100,
            render: (text) => text || '-',
        },
        {
            title: '異動時間',
            dataIndex: 'createdAt',
            key: 'createdAt',
            width: 160,
            render: (text) => text ? new Date(text).toLocaleString('zh-TW') : '-',
        },
        {
            title: '操作',
            key: 'action',
            width: 100,
            fixed: 'right',
            render: (_, record) => (
                <Tooltip title="檢視詳情">
                    <Button
                        type="link"
                        icon={<EyeOutlined />}
                        onClick={() => handleViewDetail(record)}
                    />
                </Tooltip>
            ),
        },
    ];

    return (
        <div style={{ padding: '20px' }}>
            <h2>庫存異動記錄</h2>

            {/* 統計卡片 */}
            {statistics && (
                <Row gutter={[16, 16]} style={{ marginBottom: '20px' }}>
                    <Col xs={24} sm={6}>
                        <Card>
                            <Statistic
                                title="本月異動次數"
                                value={statistics.totalTransactions}
                                prefix={<HistoryOutlined />}
                                valueStyle={{ color: '#1890ff' }}
                            />
                        </Card>
                    </Col>
                    <Col xs={24} sm={6}>
                        <Card>
                            <Statistic
                                title="入庫次數"
                                value={statistics.inTransactions}
                                prefix={<RiseOutlined />}
                                valueStyle={{ color: '#52c41a' }}
                            />
                        </Card>
                    </Col>
                    <Col xs={24} sm={6}>
                        <Card>
                            <Statistic
                                title="出庫次數"
                                value={statistics.outTransactions}
                                prefix={<FallOutlined />}
                                valueStyle={{ color: '#f5222d' }}
                            />
                        </Card>
                    </Col>
                    <Col xs={24} sm={6}>
                        <Card>
                            <Statistic
                                title="調整次數"
                                value={statistics.adjustTransactions}
                                valueStyle={{ color: '#722ed1' }}
                            />
                        </Card>
                    </Col>
                </Row>
            )}

            <Card>
                {/* 搜尋區域 */}
                <div style={{ marginBottom: '16px' }}>
                    <Row gutter={[16, 16]}>
                        <Col xs={24} sm={6}>
                            <Input
                                placeholder="搜尋物品代碼或名稱"
                                value={filters.keyword}
                                onChange={(e) => setFilters(prev => ({ ...prev, keyword: e.target.value }))}
                                allowClear
                            />
                        </Col>
                        <Col xs={24} sm={4}>
                            <Select
                                placeholder="異動類型"
                                value={filters.transactionType}
                                onChange={(value) => setFilters(prev => ({ ...prev, transactionType: value }))}
                                allowClear
                                style={{ width: '100%' }}
                            >
                                <Option value="IN">入庫</Option>
                                <Option value="OUT">出庫</Option>
                                <Option value="ADJUST">調整</Option>
                                <Option value="DAMAGED">損壞</Option>
                                <Option value="LOST">遺失</Option>
                            </Select>
                        </Col>
                        <Col xs={24} sm={4}>
                            <Select
                                placeholder="參考類型"
                                value={filters.referenceType}
                                onChange={(value) => setFilters(prev => ({ ...prev, referenceType: value }))}
                                allowClear
                                style={{ width: '100%' }}
                            >
                                <Option value="PURCHASE">採購</Option>
                                <Option value="BORROW">借用</Option>
                                <Option value="RETURN">歸還</Option>
                                <Option value="ADJUSTMENT">盤點調整</Option>
                                <Option value="DAMAGE">損壞報廢</Option>
                                <Option value="LOSS">遺失</Option>
                            </Select>
                        </Col>
                        <Col xs={24} sm={10}>
                            <RangePicker
                                style={{ width: '100%' }}
                                onChange={handleDateRangeChange}
                                placeholder={['開始日期', '結束日期']}
                            />
                        </Col>
                    </Row>
                    <div style={{ marginTop: '16px' }}>
                        <Space>
                            <Button
                                type="primary"
                                icon={<SearchOutlined />}
                                onClick={handleSearch}
                            >
                                搜尋
                            </Button>
                            <Button onClick={handleReset}>重置</Button>
                            <Button
                                icon={<ReloadOutlined />}
                                onClick={() => {
                                    fetchTransactions(pagination.current, pagination.pageSize);
                                    fetchStatistics();
                                }}
                            >
                                重新整理
                            </Button>
                            <Button
                                icon={<ExportOutlined />}
                                onClick={() => {
                                    // 匯出功能
                                    message.info('匯出功能開發中');
                                }}
                            >
                                匯出
                            </Button>
                        </Space>
                    </div>
                </div>

                {/* 表格 */}
                <Table
                    columns={columns}
                    dataSource={transactions}
                    rowKey="id"
                    loading={loading}
                    pagination={pagination}
                    onChange={handleTableChange}
                    scroll={{ x: 1400 }}
                />
            </Card>

            {/* 詳情 Modal */}
            <Modal
                title="異動記錄詳情"
                open={detailModalVisible}
                onCancel={() => setDetailModalVisible(false)}
                footer={[
                    <Button key="close" onClick={() => setDetailModalVisible(false)}>
                        關閉
                    </Button>
                ]}
                width={700}
            >
                {selectedTransaction && (
                    <Descriptions column={2} bordered>
                        <Descriptions.Item label="異動ID" span={2}>
                            {selectedTransaction.id}
                        </Descriptions.Item>
                        <Descriptions.Item label="物品代碼">
                            {selectedTransaction.itemCode}
                        </Descriptions.Item>
                        <Descriptions.Item label="物品名稱">
                            {selectedTransaction.itemName}
                        </Descriptions.Item>
                        <Descriptions.Item label="異動類型">
                            <Tag color={getTransactionTypeColor(selectedTransaction.transactionType)}>
                                {getTransactionTypeText(selectedTransaction.transactionType)}
                            </Tag>
                        </Descriptions.Item>
                        <Descriptions.Item label="參考類型">
                            {getReferenceTypeText(selectedTransaction.referenceType)}
                        </Descriptions.Item>
                        <Descriptions.Item label="數量變化">
                            <span style={{ 
                                color: selectedTransaction.quantityChange > 0 ? '#52c41a' : '#f5222d',
                                fontWeight: 'bold'
                            }}>
                                {selectedTransaction.quantityChange > 0 ? '+' : ''}{selectedTransaction.quantityChange}
                            </span>
                        </Descriptions.Item>
                        <Descriptions.Item label="異動前數量">
                            {selectedTransaction.beforeQuantity}
                        </Descriptions.Item>
                        <Descriptions.Item label="異動後數量">
                            {selectedTransaction.afterQuantity}
                        </Descriptions.Item>
                        <Descriptions.Item label="參考單號">
                            {selectedTransaction.referenceNumber || '-'}
                        </Descriptions.Item>
                        <Descriptions.Item label="處理人">
                            {selectedTransaction.processedByName || '-'}
                        </Descriptions.Item>
                        <Descriptions.Item label="異動時間" span={2}>
                            {selectedTransaction.createdAt ? 
                                new Date(selectedTransaction.createdAt).toLocaleString('zh-TW') : '-'}
                        </Descriptions.Item>
                        <Descriptions.Item label="備註" span={2}>
                            {selectedTransaction.notes || '-'}
                        </Descriptions.Item>
                    </Descriptions>
                )}
            </Modal>
        </div>
    );
};

export default TransactionHistory;
