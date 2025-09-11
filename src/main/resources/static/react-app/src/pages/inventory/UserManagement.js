import React, { useState, useEffect } from 'react';
import {
    Table, Card, Button, Input, Select, Space, Modal, Form, message,
    Popconfirm, Tag, Tooltip, Row, Col, Statistic, Alert, Spin
} from 'antd';
import {
    PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined,
    UserOutlined, TeamOutlined, ExclamationCircleOutlined,
    ReloadOutlined, EyeOutlined
} from '@ant-design/icons';
import { inventoryAPI } from '../../services/inventoryAPI';

const { Option } = Select;
const { Search } = Input;

const UserManagement = () => {
    const [loading, setLoading] = useState(false);
    const [users, setUsers] = useState([]);
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
        username: '',
        nickname: '',
        email: '',
        deptId: null,
        status: null
    });
    const [modalVisible, setModalVisible] = useState(false);
    const [modalType, setModalType] = useState('create'); // create, edit, view
    const [selectedUser, setSelectedUser] = useState(null);
    const [form] = Form.useForm();

    useEffect(() => {
        fetchUsers();
        fetchStatistics();
    }, []);

    const fetchUsers = async (page = 1, size = 10) => {
        try {
            setLoading(true);
            const params = {
                ...filters,
                page,
                size,
                sortBy: 'createdAt',
                sortDir: 'desc'
            };

            const response = await inventoryAPI.getUsers(params);
            if (response.success) {
                setUsers(response.data.content);
                setPagination(prev => ({
                    ...prev,
                    current: page,
                    pageSize: size,
                    total: response.data.totalElements
                }));
            } else {
                message.error(response.message || '載入使用者資料失敗');
            }
        } catch (error) {
            message.error('載入使用者資料失敗: ' + error.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchStatistics = async () => {
        try {
            const response = await inventoryAPI.getUserStatistics();
            if (response.success) {
                setStatistics(response.data);
            }
        } catch (error) {
            console.error('載入統計資料失敗:', error);
        }
    };

    const handleTableChange = (paginationConfig) => {
        fetchUsers(paginationConfig.current, paginationConfig.pageSize);
    };

    const handleSearch = () => {
        fetchUsers(1, pagination.pageSize);
    };

    const handleReset = () => {
        setFilters({
            username: '',
            nickname: '',
            email: '',
            deptId: null,
            status: null
        });
        setTimeout(() => fetchUsers(1, pagination.pageSize), 0);
    };

    const handleCreate = () => {
        setModalType('create');
        setSelectedUser(null);
        form.resetFields();
        setModalVisible(true);
    };

    const handleEdit = (record) => {
        setModalType('edit');
        setSelectedUser(record);
        form.setFieldsValue({
            username: record.username,
            nickname: record.nickname,
            email: record.email,
            phone: record.phone,
            deptId: record.deptId,
            status: record.status
        });
        setModalVisible(true);
    };

    const handleView = (record) => {
        setModalType('view');
        setSelectedUser(record);
        form.setFieldsValue({
            username: record.username,
            nickname: record.nickname,
            email: record.email,
            phone: record.phone,
            deptId: record.deptId,
            status: record.status
        });
        setModalVisible(true);
    };

    const handleSubmit = async (values) => {
        try {
            setLoading(true);
            let response;

            if (modalType === 'create') {
                response = await inventoryAPI.createUser(values);
                message.success('使用者建立成功');
            } else if (modalType === 'edit') {
                response = await inventoryAPI.updateUser(selectedUser.id, values);
                message.success('使用者更新成功');
            }

            if (response.success) {
                setModalVisible(false);
                fetchUsers(pagination.current, pagination.pageSize);
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

    const handleToggleStatus = async (record) => {
        try {
            const response = await inventoryAPI.toggleUserStatus(record.id);
            if (response.success) {
                message.success('使用者狀態切換成功');
                fetchUsers(pagination.current, pagination.pageSize);
                fetchStatistics();
            } else {
                message.error(response.message || '狀態切換失敗');
            }
        } catch (error) {
            message.error('狀態切換失敗: ' + error.message);
        }
    };

    const handleDelete = async (record) => {
        try {
            const response = await inventoryAPI.deleteUser(record.id);
            if (response.success) {
                message.success('使用者刪除成功');
                fetchUsers(pagination.current, pagination.pageSize);
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
            title: '使用者名稱',
            dataIndex: 'username',
            key: 'username',
            width: 120,
        },
        {
            title: '暱稱',
            dataIndex: 'nickname',
            key: 'nickname',
            width: 120,
            render: (text) => text || '-',
        },
        {
            title: '電子郵件',
            dataIndex: 'email',
            key: 'email',
            width: 180,
            render: (text) => text || '-',
        },
        {
            title: '手機號碼',
            dataIndex: 'phone',
            key: 'phone',
            width: 120,
            render: (text) => text || '-',
        },
        {
            title: '部門',
            dataIndex: 'department',
            key: 'department',
            width: 120,
            render: (text) => text || '-',
        },
        {
            title: '狀態',
            dataIndex: 'status',
            key: 'status',
            width: 80,
            render: (status) => (
                <Tag color={status === 1 ? 'green' : 'red'}>
                    {status === 1 ? '啟用' : '停用'}
                </Tag>
            ),
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
            width: 200,
            fixed: 'right',
            render: (_, record) => (
                <Space size="small">
                    <Tooltip title="檢視">
                        <Button
                            type="link"
                            icon={<EyeOutlined />}
                            onClick={() => handleView(record)}
                        />
                    </Tooltip>
                    <Tooltip title="編輯">
                        <Button
                            type="link"
                            icon={<EditOutlined />}
                            onClick={() => handleEdit(record)}
                        />
                    </Tooltip>
                    <Tooltip title={record.status === 1 ? '停用' : '啟用'}>
                        <Button
                            type="link"
                            onClick={() => handleToggleStatus(record)}
                        >
                            {record.status === 1 ? '停用' : '啟用'}
                        </Button>
                    </Tooltip>
                    <Popconfirm
                        title="確定要刪除這個使用者嗎？"
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
            <h2>使用者管理</h2>

            {/* 統計卡片 */}
            {statistics && (
                <Row gutter={[16, 16]} style={{ marginBottom: '20px' }}>
                    <Col xs={24} sm={8}>
                        <Card>
                            <Statistic
                                title="總使用者數"
                                value={statistics.totalUsers}
                                prefix={<TeamOutlined />}
                                valueStyle={{ color: '#1890ff' }}
                            />
                        </Card>
                    </Col>
                    <Col xs={24} sm={8}>
                        <Card>
                            <Statistic
                                title="啟用使用者"
                                value={statistics.enabledUsers}
                                prefix={<UserOutlined />}
                                valueStyle={{ color: '#52c41a' }}
                            />
                        </Card>
                    </Col>
                    <Col xs={24} sm={8}>
                        <Card>
                            <Statistic
                                title="停用使用者"
                                value={statistics.disabledUsers}
                                prefix={<ExclamationCircleOutlined />}
                                valueStyle={{ color: '#f5222d' }}
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
                                placeholder="使用者名稱"
                                value={filters.username}
                                onChange={(e) => setFilters(prev => ({ ...prev, username: e.target.value }))}
                                allowClear
                            />
                        </Col>
                        <Col xs={24} sm={6}>
                            <Input
                                placeholder="暱稱"
                                value={filters.nickname}
                                onChange={(e) => setFilters(prev => ({ ...prev, nickname: e.target.value }))}
                                allowClear
                            />
                        </Col>
                        <Col xs={24} sm={6}>
                            <Input
                                placeholder="電子郵件"
                                value={filters.email}
                                onChange={(e) => setFilters(prev => ({ ...prev, email: e.target.value }))}
                                allowClear
                            />
                        </Col>
                        <Col xs={24} sm={6}>
                            <Select
                                placeholder="狀態"
                                value={filters.status}
                                onChange={(value) => setFilters(prev => ({ ...prev, status: value }))}
                                allowClear
                                style={{ width: '100%' }}
                            >
                                <Option value={1}>啟用</Option>
                                <Option value={0}>停用</Option>
                            </Select>
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
                                新增使用者
                            </Button>
                            <Button
                                icon={<ReloadOutlined />}
                                onClick={() => fetchUsers(pagination.current, pagination.pageSize)}
                            >
                                重新整理
                            </Button>
                        </Space>
                    </div>
                </div>

                {/* 表格 */}
                <Table
                    columns={columns}
                    dataSource={users}
                    rowKey="id"
                    loading={loading}
                    pagination={pagination}
                    onChange={handleTableChange}
                    scroll={{ x: 1200 }}
                />
            </Card>

            {/* 使用者表單 Modal */}
            <Modal
                title={
                    modalType === 'create' ? '新增使用者' :
                    modalType === 'edit' ? '編輯使用者' : '檢視使用者'
                }
                open={modalVisible}
                onCancel={() => setModalVisible(false)}
                footer={modalType === 'view' ? [
                    <Button key="close" onClick={() => setModalVisible(false)}>
                        關閉
                    </Button>
                ] : [
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
                    disabled={modalType === 'view'}
                >
                    <Form.Item
                        label="使用者名稱"
                        name="username"
                        rules={[
                            { required: true, message: '請輸入使用者名稱' },
                            { min: 3, message: '使用者名稱至少需要3個字元' },
                            { max: 50, message: '使用者名稱不能超過50個字元' }
                        ]}
                    >
                        <Input placeholder="請輸入使用者名稱" />
                    </Form.Item>

                    <Form.Item
                        label="暱稱"
                        name="nickname"
                        rules={[
                            { max: 20, message: '暱稱不能超過20個字元' }
                        ]}
                    >
                        <Input placeholder="請輸入暱稱" />
                    </Form.Item>

                    <Form.Item
                        label="電子郵件"
                        name="email"
                        rules={[
                            { type: 'email', message: '請輸入有效的電子郵件格式' },
                            { max: 100, message: '電子郵件不能超過100個字元' }
                        ]}
                    >
                        <Input placeholder="請輸入電子郵件" />
                    </Form.Item>

                    <Form.Item
                        label="手機號碼"
                        name="phone"
                        rules={[
                            { pattern: /^09\d{8}$/, message: '請輸入有效的手機號碼格式 (09xxxxxxxx)' }
                        ]}
                    >
                        <Input placeholder="請輸入手機號碼" />
                    </Form.Item>

                    <Form.Item
                        label="部門ID"
                        name="deptId"
                    >
                        <Input type="number" placeholder="請輸入部門ID" />
                    </Form.Item>

                    {modalType === 'edit' && (
                        <Form.Item
                            label="狀態"
                            name="status"
                            rules={[{ required: true, message: '請選擇狀態' }]}
                        >
                            <Select placeholder="請選擇狀態">
                                <Option value={1}>啟用</Option>
                                <Option value={0}>停用</Option>
                            </Select>
                        </Form.Item>
                    )}
                </Form>
            </Modal>
        </div>
    );
};

export default UserManagement;
