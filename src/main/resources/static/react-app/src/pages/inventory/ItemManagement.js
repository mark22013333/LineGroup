import React, { useState, useEffect } from 'react';
import { 
    Table, 
    Button, 
    Modal, 
    Form, 
    Input, 
    Select, 
    InputNumber, 
    Upload, 
    message, 
    Space, 
    Tag, 
    Popconfirm,
    Row,
    Col,
    Card,
    Divider
} from 'antd';
import { 
    PlusOutlined, 
    EditOutlined, 
    DeleteOutlined, 
    SearchOutlined,
    QrcodeOutlined,
    UploadOutlined,
    ExportOutlined
} from '@ant-design/icons';
import { inventoryAPI } from '../../services/inventoryAPI';

const { Option } = Select;
const { Search } = Input;

const ItemManagement = () => {
    const [items, setItems] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [modalVisible, setModalVisible] = useState(false);
    const [editingItem, setEditingItem] = useState(null);
    const [form] = Form.useForm();
    const [searchParams, setSearchParams] = useState({
        page: 1,
        size: 10,
        sortBy: 'createdAt',
        sortDir: 'DESC'
    });
    const [pagination, setPagination] = useState({
        current: 1,
        pageSize: 10,
        total: 0
    });

    useEffect(() => {
        fetchItems();
        fetchCategories();
    }, [searchParams]);

    const fetchItems = async () => {
        try {
            setLoading(true);
            const response = await inventoryAPI.getItems(searchParams);
            if (response.success) {
                setItems(response.data.content || []);
                setPagination({
                    current: response.data.currentPage || 1,
                    pageSize: response.data.pageSize || 10,
                    total: response.data.totalElements || 0
                });
            } else {
                message.error(response.message || '載入物品列表失敗');
            }
        } catch (error) {
            message.error('載入物品列表失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchCategories = async () => {
        try {
            const response = await inventoryAPI.getCategoryTree();
            if (response.success) {
                setCategories(response.data || []);
            }
        } catch (error) {
            console.error('載入分類失敗:', error);
        }
    };

    const handleTableChange = (pagination, filters, sorter) => {
        setSearchParams({
            ...searchParams,
            page: pagination.current,
            size: pagination.pageSize,
            sortBy: sorter.field || 'createdAt',
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

    const handleAdd = () => {
        setEditingItem(null);
        form.resetFields();
        setModalVisible(true);
    };

    const handleEdit = (record) => {
        setEditingItem(record);
        form.setFieldsValue({
            ...record,
            categoryId: record.category?.id
        });
        setModalVisible(true);
    };

    const handleDelete = async (id) => {
        try {
            const response = await inventoryAPI.deleteItem(id);
            if (response.success) {
                message.success('刪除成功');
                fetchItems();
            } else {
                message.error(response.message || '刪除失敗');
            }
        } catch (error) {
            message.error('刪除失敗: ' + error.message);
        }
    };

    const handleModalOk = async () => {
        try {
            const values = await form.validateFields();
            
            if (editingItem) {
                // 更新物品
                const response = await inventoryAPI.updateItem(editingItem.id, values);
                if (response.success) {
                    message.success('更新成功');
                    setModalVisible(false);
                    fetchItems();
                } else {
                    message.error(response.message || '更新失敗');
                }
            } else {
                // 新增物品
                const response = await inventoryAPI.createItem(values);
                if (response.success) {
                    message.success('新增成功');
                    setModalVisible(false);
                    fetchItems();
                } else {
                    message.error(response.message || '新增失敗');
                }
            }
        } catch (error) {
            console.error('表單驗證失敗:', error);
        }
    };

    const handleGenerateBarcode = async (record) => {
        try {
            const response = await inventoryAPI.batchGenerateItemBarcodes({
                itemIds: [record.id]
            });
            if (response.success) {
                message.success('條碼產生成功');
                fetchItems();
            } else {
                message.error(response.message || '條碼產生失敗');
            }
        } catch (error) {
            message.error('條碼產生失敗: ' + error.message);
        }
    };

    const renderCategoryOptions = (categories, level = 0) => {
        return categories.map(category => (
            <React.Fragment key={category.id}>
                <Option value={category.id}>
                    {'　'.repeat(level)}{category.name}
                </Option>
                {category.children && renderCategoryOptions(category.children, level + 1)}
            </React.Fragment>
        ));
    };

    const columns = [
        {
            title: '物品代碼',
            dataIndex: 'code',
            key: 'code',
            sorter: true,
            render: (text) => <strong>{text}</strong>
        },
        {
            title: '物品名稱',
            dataIndex: 'name',
            key: 'name',
            sorter: true,
        },
        {
            title: '分類',
            dataIndex: ['category', 'name'],
            key: 'categoryName',
        },
        {
            title: '品牌',
            dataIndex: 'brand',
            key: 'brand',
        },
        {
            title: '型號',
            dataIndex: 'model',
            key: 'model',
        },
        {
            title: '單價',
            dataIndex: 'unitPrice',
            key: 'unitPrice',
            render: (price) => price ? `NT$ ${price.toLocaleString()}` : '-'
        },
        {
            title: '庫存',
            key: 'inventory',
            render: (_, record) => {
                const inventory = record.inventory;
                if (!inventory) return '-';
                
                const current = inventory.currentQuantity || 0;
                const available = inventory.availableQuantity || 0;
                const min = inventory.minStockLevel || 0;
                
                let color = 'green';
                if (current === 0) color = 'red';
                else if (current <= min) color = 'orange';
                
                return (
                    <Space direction="vertical" size="small">
                        <Tag color={color}>當前: {current}</Tag>
                        <Tag>可用: {available}</Tag>
                    </Space>
                );
            }
        },
        {
            title: '條碼',
            dataIndex: 'barcode',
            key: 'barcode',
            render: (barcode, record) => (
                <Space>
                    {barcode ? (
                        <Tag color="blue">{barcode}</Tag>
                    ) : (
                        <Button 
                            size="small" 
                            icon={<QrcodeOutlined />}
                            onClick={() => handleGenerateBarcode(record)}
                        >
                            產生
                        </Button>
                    )}
                </Space>
            )
        },
        {
            title: '狀態',
            dataIndex: 'status',
            key: 'status',
            render: (status) => {
                const statusMap = {
                    'ACTIVE': { color: 'green', text: '啟用' },
                    'INACTIVE': { color: 'orange', text: '停用' },
                    'DISCONTINUED': { color: 'red', text: '停產' }
                };
                const config = statusMap[status] || { color: 'default', text: status };
                return <Tag color={config.color}>{config.text}</Tag>;
            }
        },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Space>
                    <Button 
                        type="link" 
                        icon={<EditOutlined />} 
                        onClick={() => handleEdit(record)}
                    >
                        編輯
                    </Button>
                    <Popconfirm
                        title="確定要刪除這個物品嗎？"
                        onConfirm={() => handleDelete(record.id)}
                        okText="確定"
                        cancelText="取消"
                    >
                        <Button 
                            type="link" 
                            danger 
                            icon={<DeleteOutlined />}
                        >
                            刪除
                        </Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '20px' }}>
            <Card>
                <Row justify="space-between" align="middle" style={{ marginBottom: '16px' }}>
                    <Col>
                        <h2>物品管理</h2>
                    </Col>
                    <Col>
                        <Space>
                            <Button 
                                type="primary" 
                                icon={<PlusOutlined />} 
                                onClick={handleAdd}
                            >
                                新增物品
                            </Button>
                            <Button icon={<ExportOutlined />}>
                                匯出
                            </Button>
                        </Space>
                    </Col>
                </Row>

                <Row gutter={[16, 16]} style={{ marginBottom: '16px' }}>
                    <Col xs={24} sm={12} md={8}>
                        <Search
                            placeholder="搜尋物品名稱、代碼、品牌..."
                            allowClear
                            enterButton={<SearchOutlined />}
                            onSearch={handleSearch}
                        />
                    </Col>
                </Row>

                <Table
                    columns={columns}
                    dataSource={items}
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

            <Modal
                title={editingItem ? '編輯物品' : '新增物品'}
                open={modalVisible}
                onOk={handleModalOk}
                onCancel={() => setModalVisible(false)}
                width={800}
                okText="確定"
                cancelText="取消"
            >
                <Form
                    form={form}
                    layout="vertical"
                    initialValues={{
                        status: 'ACTIVE',
                        unit: '個'
                    }}
                >
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                label="物品代碼"
                                name="code"
                                rules={[{ required: true, message: '請輸入物品代碼' }]}
                            >
                                <Input placeholder="請輸入物品代碼" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                label="物品名稱"
                                name="name"
                                rules={[{ required: true, message: '請輸入物品名稱' }]}
                            >
                                <Input placeholder="請輸入物品名稱" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                label="分類"
                                name="categoryId"
                                rules={[{ required: true, message: '請選擇分類' }]}
                            >
                                <Select placeholder="請選擇分類">
                                    {renderCategoryOptions(categories)}
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                label="條碼"
                                name="barcode"
                            >
                                <Input placeholder="請輸入條碼（可選）" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        label="物品描述"
                        name="description"
                    >
                        <Input.TextArea rows={3} placeholder="請輸入物品描述" />
                    </Form.Item>

                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item
                                label="品牌"
                                name="brand"
                            >
                                <Input placeholder="請輸入品牌" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                label="型號"
                                name="model"
                            >
                                <Input placeholder="請輸入型號" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                label="供應商"
                                name="supplier"
                            >
                                <Input placeholder="請輸入供應商" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item
                                label="單位"
                                name="unit"
                            >
                                <Input placeholder="請輸入單位" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                label="單價"
                                name="unitPrice"
                            >
                                <InputNumber
                                    style={{ width: '100%' }}
                                    placeholder="請輸入單價"
                                    min={0}
                                    precision={2}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                label="狀態"
                                name="status"
                            >
                                <Select>
                                    <Option value="ACTIVE">啟用</Option>
                                    <Option value="INACTIVE">停用</Option>
                                    <Option value="DISCONTINUED">停產</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        label="存放位置"
                        name="location"
                    >
                        <Input placeholder="請輸入存放位置" />
                    </Form.Item>

                    <Form.Item
                        label="規格說明"
                        name="specifications"
                    >
                        <Input.TextArea rows={2} placeholder="請輸入規格說明" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default ItemManagement;
