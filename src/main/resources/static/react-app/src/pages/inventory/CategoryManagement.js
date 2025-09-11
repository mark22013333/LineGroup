import React, { useState, useEffect } from 'react';
import {
    Table, Card, Button, Input, Modal, Form, message,
    Space, Popconfirm, Tooltip, Row, Col, Statistic, Tree
} from 'antd';
import {
    PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined,
    ReloadOutlined, FolderOutlined, TagsOutlined
} from '@ant-design/icons';
import { inventoryAPI } from '../../services/inventoryAPI';

const CategoryManagement = () => {
    const [loading, setLoading] = useState(false);
    const [categories, setCategories] = useState([]);
    const [categoryTree, setCategoryTree] = useState([]);
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
        name: '',
        parentId: null
    });
    const [modalVisible, setModalVisible] = useState(false);
    const [modalType, setModalType] = useState('create'); // create, edit
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [form] = Form.useForm();

    useEffect(() => {
        fetchCategories();
        fetchCategoryTree();
        fetchStatistics();
    }, []);

    const fetchCategories = async (page = 1, size = 10) => {
        try {
            setLoading(true);
            const params = {
                ...filters,
                page,
                size,
                sortBy: 'createdAt',
                sortDir: 'desc'
            };

            const response = await inventoryAPI.getCategories(params);
            if (response.success) {
                setCategories(response.data.content);
                setPagination(prev => ({
                    ...prev,
                    current: page,
                    pageSize: size,
                    total: response.data.totalElements
                }));
            } else {
                message.error(response.message || '載入分類資料失敗');
            }
        } catch (error) {
            message.error('載入分類資料失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchCategoryTree = async () => {
        try {
            const response = await inventoryAPI.getCategoryTree();
            if (response.success) {
                setCategoryTree(response.data);
            }
        } catch (error) {
            console.error('載入分類樹狀結構失敗:', error);
        }
    };

    const fetchStatistics = async () => {
        try {
            const response = await inventoryAPI.getCategoryStatistics();
            if (response.success) {
                setStatistics(response.data);
            }
        } catch (error) {
            console.error('載入統計資料失敗:', error);
        }
    };

    const handleTableChange = (paginationConfig) => {
        fetchCategories(paginationConfig.current, paginationConfig.pageSize);
    };

    const handleSearch = () => {
        fetchCategories(1, pagination.pageSize);
    };

    const handleReset = () => {
        setFilters({
            name: '',
            parentId: null
        });
        setTimeout(() => fetchCategories(1, pagination.pageSize), 0);
    };

    const handleCreate = () => {
        setModalType('create');
        setSelectedCategory(null);
        form.resetFields();
        setModalVisible(true);
    };

    const handleEdit = (record) => {
        setModalType('edit');
        setSelectedCategory(record);
        form.setFieldsValue({
            name: record.name,
            description: record.description,
            parentId: record.parentId,
            sortOrder: record.sortOrder
        });
        setModalVisible(true);
    };

    const handleSubmit = async (values) => {
        try {
            setLoading(true);
            let response;

            if (modalType === 'create') {
                response = await inventoryAPI.createCategory(values);
                message.success('分類建立成功');
            } else if (modalType === 'edit') {
                response = await inventoryAPI.updateCategory(selectedCategory.id, values);
                message.success('分類更新成功');
            }

            if (response.success) {
                setModalVisible(false);
                fetchCategories(pagination.current, pagination.pageSize);
                fetchCategoryTree();
                fetchStatistics();
            } else {
                message.error(response.message || '操作失敗');
            }
        } catch (error) {
            message.error('操作失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (record) => {
        try {
            const response = await inventoryAPI.deleteCategory(record.id);
            if (response.success) {
                message.success('分類刪除成功');
                fetchCategories(pagination.current, pagination.pageSize);
                fetchCategoryTree();
                fetchStatistics();
            } else {
                message.error(response.message || '刪除失敗');
            }
        } catch (error) {
            message.error('刪除失敗: ' + error.message);
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
            title: '分類名稱',
            dataIndex: 'name',
            key: 'name',
            width: 150,
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            width: 200,
            render: (text) => text || '-',
        },
        {
            title: '上級分類',
            dataIndex: 'parentName',
            key: 'parentName',
            width: 120,
            render: (text) => text || '根分類',
        },
        {
            title: '物品數量',
            dataIndex: 'itemCount',
            key: 'itemCount',
            width: 100,
            render: (count) => count || 0,
        },
        {
            title: '排序',
            dataIndex: 'sortOrder',
            key: 'sortOrder',
            width: 80,
            render: (order) => order || 0,
        },
        {
            title: '建立時間',
            dataIndex: 'createdAt',
            key: 'createdAt',
            width: 160,
            render: (text) => text ? new Date(text).toLocaleString('zh-TW') : '-',
        },
        {
            title: '操作',
            key: 'action',
            width: 150,
            fixed: 'right',
            render: (_, record) => (
                <Space size="small">
                    <Tooltip title="編輯">
                        <Button
                            type="link"
                            icon={<EditOutlined />}
                            onClick={() => handleEdit(record)}
                        />
                    </Tooltip>
                    <Popconfirm
                        title="確定要刪除這個分類嗎？"
                        description="刪除分類可能會影響相關物品"
                        onConfirm={() => handleDelete(record)}
                        okText="確定"
                        cancelText="取消"
                    >
                        <Tooltip title="刪除">
                            <Button
                                type="link"
                                danger
                                icon={<DeleteOutlined />}
                            />
                        </Tooltip>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '20px' }}>
            <h2>分類管理</h2>

            {/* 統計卡片 */}
            {statistics && (
                <Row gutter={[16, 16]} style={{ marginBottom: '20px' }}>
                    <Col xs={24} sm={8}>
                        <Card>
                            <Statistic
                                title="總分類數"
                                value={statistics.totalCategories}
                                prefix={<TagsOutlined />}
                                valueStyle={{ color: '#1890ff' }}
                            />
                        </Card>
                    </Col>
                    <Col xs={24} sm={8}>
                        <Card>
                            <Statistic
                                title="根分類數"
                                value={statistics.rootCategories}
                                prefix={<FolderOutlined />}
                                valueStyle={{ color: '#52c41a' }}
                            />
                        </Card>
                    </Col>
                    <Col xs={24} sm={8}>
                        <Card>
                            <Statistic
                                title="最大層級"
                                value={statistics.maxLevel}
                                valueStyle={{ color: '#722ed1' }}
                            />
                        </Card>
                    </Col>
                </Row>
            )}

            <Row gutter={[16, 16]}>
                {/* 分類樹狀結構 */}
                <Col xs={24} lg={8}>
                    <Card title="分類結構" style={{ height: '600px', overflow: 'auto' }}>
                        {categoryTree.length > 0 ? (
                            <Tree
                                treeData={categoryTree}
                                defaultExpandAll
                                showIcon
                                icon={<FolderOutlined />}
                            />
                        ) : (
                            <div style={{ textAlign: 'center', color: '#999', padding: '50px 0' }}>
                                暫無分類資料
                            </div>
                        )}
                    </Card>
                </Col>

                {/* 分類列表 */}
                <Col xs={24} lg={16}>
                    <Card>
                        {/* 搜尋區域 */}
                        <div style={{ marginBottom: '16px' }}>
                            <Row gutter={[16, 16]}>
                                <Col xs={24} sm={12}>
                                    <Input
                                        placeholder="分類名稱"
                                        value={filters.name}
                                        onChange={(e) => setFilters(prev => ({ ...prev, name: e.target.value }))}
                                        allowClear
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
                                        type="primary"
                                        icon={<PlusOutlined />}
                                        onClick={handleCreate}
                                    >
                                        新增分類
                                    </Button>
                                    <Button
                                        icon={<ReloadOutlined />}
                                        onClick={() => {
                                            fetchCategories(pagination.current, pagination.pageSize);
                                            fetchCategoryTree();
                                        }}
                                    >
                                        重新整理
                                    </Button>
                                </Space>
                            </div>
                        </div>

                        {/* 表格 */}
                        <Table
                            columns={columns}
                            dataSource={categories}
                            rowKey="id"
                            loading={loading}
                            pagination={pagination}
                            onChange={handleTableChange}
                            scroll={{ x: 1000 }}
                        />
                    </Card>
                </Col>
            </Row>

            {/* 分類表單 Modal */}
            <Modal
                title={modalType === 'create' ? '新增分類' : '編輯分類'}
                open={modalVisible}
                onCancel={() => setModalVisible(false)}
                footer={[
                    <Button key="cancel" onClick={() => setModalVisible(false)}>
                        取消
                    </Button>,
                    <Button
                        key="submit"
                        type="primary"
                        loading={loading}
                        onClick={() => form.submit()}
                    >
                        {modalType === 'create' ? '建立' : '更新'}
                    </Button>
                ]}
                width={600}
            >
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                >
                    <Form.Item
                        label="分類名稱"
                        name="name"
                        rules={[
                            { required: true, message: '請輸入分類名稱' },
                            { max: 50, message: '分類名稱不能超過50個字元' }
                        ]}
                    >
                        <Input placeholder="請輸入分類名稱" />
                    </Form.Item>

                    <Form.Item
                        label="描述"
                        name="description"
                        rules={[
                            { max: 200, message: '描述不能超過200個字元' }
                        ]}
                    >
                        <Input.TextArea rows={3} placeholder="請輸入分類描述" />
                    </Form.Item>

                    <Form.Item
                        label="上級分類ID"
                        name="parentId"
                    >
                        <Input type="number" placeholder="請輸入上級分類ID（留空為根分類）" />
                    </Form.Item>

                    <Form.Item
                        label="排序"
                        name="sortOrder"
                        initialValue={0}
                    >
                        <Input type="number" placeholder="請輸入排序數值" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default CategoryManagement;
