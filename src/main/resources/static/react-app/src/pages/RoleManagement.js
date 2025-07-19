import React, { useState, useEffect } from 'react';
import {
  Table, Card, Button, Space, Form, Input, Select,
  Row, Col, Tag, Modal, message, Tooltip, Popconfirm, Checkbox
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined,
  CheckCircleOutlined, CloseCircleOutlined, SettingOutlined
} from '@ant-design/icons';
import rolePermissionService from '../services/rolePermissionService';

const { Option } = Select;

/**
 * 角色管理頁面元件
 */
const RoleManagement = () => {
  // 狀態變數
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('新增角色');
  const [currentRole, setCurrentRole] = useState(null);
  const [permissionsModalVisible, setPermissionsModalVisible] = useState(false);
  const [permissions, setPermissions] = useState([]);
  const [selectedPermissions, setSelectedPermissions] = useState([]);
  const [form] = Form.useForm();
  const [searchForm] = Form.useForm();

  // 資料權限選項
  const dataScopeOptions = [
    { value: 0, label: '所有資料' },
    { value: 1, label: '部門及子部門資料' },
    { value: 2, label: '本部門資料' },
    { value: 3, label: '本人資料' },
  ];

  // 載入角色資料
  const fetchRoles = async (params = {}) => {
    try {
      setLoading(true);

      // 取得表單值
      const formValues = searchForm.getFieldsValue();

      // 轉換查詢參數
      const queryParams = {
        page: params.current ? params.current - 1 : 0,
        size: params.pageSize || 10,
        sortField: params.sortField || 'sort',
        sortDirection: params.sortOrder === 'ascend' ? 'asc' : 'desc',
        keyword: formValues.keyword || '',
        status: formValues.status,
        includeDeleted: false,
      };

      // 呼叫API
      const response = await rolePermissionService.getRoles(queryParams);
      const data = response.data;

      // 更新資料和分頁
      setRoles(data.content || []);
      setPagination({
        current: data.page + 1,
        pageSize: data.size,
        total: data.total,
      });

    } catch (error) {
      console.error('載入角色資料發生錯誤:', error);
      message.error('載入角色資料失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  // 載入權限樹
  const fetchPermissions = async () => {
    try {
      const response = await rolePermissionService.getPermissionTree();
      setPermissions(response.data || []);
    } catch (error) {
      console.error('載入權限資料發生錯誤:', error);
      message.error('載入權限資料失敗，請稍後再試');
    }
  };

  // 初始載入
  useEffect(() => {
    fetchRoles();
    fetchPermissions();
  }, []);

  // 處理表格變更（排序、分頁）
  const handleTableChange = (newPagination, filters, sorter) => {
    fetchRoles({
      current: newPagination.current,
      pageSize: newPagination.pageSize,
      sortField: sorter.field,
      sortOrder: sorter.order,
    });
  };

  // 處理表單搜尋
  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchRoles({ current: 1, pageSize: pagination.pageSize });
  };

  // 重設搜尋條件
  const handleReset = () => {
    searchForm.resetFields();
    setPagination({ ...pagination, current: 1 });
    fetchRoles({ current: 1, pageSize: pagination.pageSize });
  };

  // 開啟新增角色對話框
  const showAddModal = () => {
    setModalTitle('新增角色');
    setCurrentRole(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 開啟編輯角色對話框
  const showEditModal = (role) => {
    setModalTitle('編輯角色');
    setCurrentRole(role);
    form.setFieldsValue({
      name: role.name,
      code: role.code,
      dataScope: role.dataScope,
      status: role.status,
      sort: role.sort,
      description: role.description,
    });
    setModalVisible(true);
  };

  // 關閉角色對話框
  const handleCancel = () => {
    setModalVisible(false);
  };

  // 提交角色表單
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (currentRole) {
        // 編輯角色
        await rolePermissionService.updateRole(currentRole.id, values);
        message.success('角色更新成功');
      } else {
        // 新增角色
        await rolePermissionService.createRole(values);
        message.success('角色新增成功');
      }
      
      setModalVisible(false);
      fetchRoles();
    } catch (error) {
      console.error('儲存角色發生錯誤:', error);
      message.error('儲存角色失敗：' + (error.response?.data?.message || error.message));
    }
  };

  // 刪除角色
  const handleDelete = async (id) => {
    try {
      await rolePermissionService.deleteRole(id);
      message.success('角色刪除成功');
      fetchRoles();
    } catch (error) {
      console.error('刪除角色發生錯誤:', error);
      message.error('刪除角色失敗：' + (error.response?.data?.message || error.message));
    }
  };

  // 更新角色狀態
  const handleStatusChange = async (id, status) => {
    try {
      await rolePermissionService.updateRoleStatus(id, status);
      message.success('角色狀態更新成功');
      fetchRoles();
    } catch (error) {
      console.error('更新角色狀態發生錯誤:', error);
      message.error('更新角色狀態失敗：' + (error.response?.data?.message || error.message));
    }
  };

  // 開啟權限設定對話框
  const showPermissionsModal = async (role) => {
    setCurrentRole(role);
    try {
      const response = await rolePermissionService.getRolePermissions(role.id);
      setSelectedPermissions(response.data || []);
      setPermissionsModalVisible(true);
    } catch (error) {
      console.error('載入角色權限發生錯誤:', error);
      message.error('載入角色權限失敗，請稍後再試');
    }
  };

  // 處理權限變更
  const handlePermissionChange = (permissionIds) => {
    setSelectedPermissions(permissionIds);
  };

  // 儲存權限設定
  const handleSavePermissions = async () => {
    try {
      await rolePermissionService.assignPermissions(currentRole.id, selectedPermissions);
      message.success('權限設定儲存成功');
      setPermissionsModalVisible(false);
    } catch (error) {
      console.error('儲存權限設定發生錯誤:', error);
      message.error('儲存權限設定失敗：' + (error.response?.data?.message || error.message));
    }
  };

  // 定義表格欄位
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 60,
      sorter: true,
    },
    {
      title: '角色名稱',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
    },
    {
      title: '角色代碼',
      dataIndex: 'code',
      key: 'code',
      sorter: true,
    },
    {
      title: '資料權限',
      dataIndex: 'dataScope',
      key: 'dataScope',
      render: (dataScope) => {
        const option = dataScopeOptions.find(opt => opt.value === dataScope);
        return option ? option.label : '-';
      },
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      sorter: true,
      width: 80,
    },
    {
      title: '狀態',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => (
        status === 1 ?
          <Tag color="green">啟用</Tag> :
          <Tag color="red">停用</Tag>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="編輯">
            <Button 
              type="link" 
              icon={<EditOutlined />} 
              size="small" 
              onClick={() => showEditModal(record)} 
            />
          </Tooltip>
          <Tooltip title="設定權限">
            <Button 
              type="link" 
              icon={<SettingOutlined />} 
              size="small" 
              onClick={() => showPermissionsModal(record)} 
            />
          </Tooltip>
          {record.status === 1 ? (
            <Tooltip title="停用">
              <Button 
                type="link" 
                danger 
                icon={<CloseCircleOutlined />} 
                size="small" 
                onClick={() => handleStatusChange(record.id, 0)} 
              />
            </Tooltip>
          ) : (
            <Tooltip title="啟用">
              <Button 
                type="link" 
                icon={<CheckCircleOutlined />} 
                size="small" 
                style={{ color: 'green' }} 
                onClick={() => handleStatusChange(record.id, 1)} 
              />
            </Tooltip>
          )}
          <Tooltip title="刪除">
            <Popconfirm
              title="確定要刪除此角色嗎？"
              onConfirm={() => handleDelete(record.id)}
              okText="確定"
              cancelText="取消"
            >
              <Button 
                type="link" 
                danger 
                icon={<DeleteOutlined />} 
                size="small" 
              />
            </Popconfirm>
          </Tooltip>
        </Space>
      ),
    },
  ];

  return (
    <div className="role-management">
      <Card title="角色管理" bordered={false}>
        {/* 搜尋區域 */}
        <Form
          form={searchForm}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Row gutter={16} style={{ width: '100%' }}>
            <Col span={6}>
              <Form.Item name="keyword" label="關鍵字">
                <Input placeholder="角色名稱/代碼" allowClear />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="status" label="狀態">
                <Select placeholder="請選擇" allowClear>
                  <Option value={1}>啟用</Option>
                  <Option value={0}>停用</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12} style={{ textAlign: 'right' }}>
              <Space>
                <Button type="primary" htmlType="submit">
                  搜尋
                </Button>
                <Button onClick={handleReset}>
                  重設
                </Button>
                <Button type="primary" icon={<PlusOutlined />} onClick={showAddModal}>
                  新增角色
                </Button>
              </Space>
            </Col>
          </Row>
        </Form>

        {/* 表格區域 */}
        <Table
          rowKey="id"
          columns={columns}
          dataSource={roles}
          pagination={pagination}
          loading={loading}
          onChange={handleTableChange}
        />
      </Card>

      {/* 角色表單對話框 */}
      <Modal
        title={modalTitle}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={handleCancel}
        maskClosable={false}
        width={600}
      >
        <Form
          form={form}
          labelCol={{ span: 6 }}
          wrapperCol={{ span: 16 }}
          style={{ maxWidth: 600 }}
        >
          <Form.Item
            name="name"
            label="角色名稱"
            rules={[{ required: true, message: '請輸入角色名稱' }]}
          >
            <Input placeholder="請輸入角色名稱" />
          </Form.Item>

          <Form.Item
            name="code"
            label="角色代碼"
            rules={[{ required: true, message: '請輸入角色代碼' }]}
          >
            <Input placeholder="請輸入角色代碼" disabled={currentRole !== null} />
          </Form.Item>

          <Form.Item
            name="dataScope"
            label="資料權限"
            rules={[{ required: true, message: '請選擇資料權限' }]}
          >
            <Select placeholder="請選擇資料權限">
              {dataScopeOptions.map(option => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="status"
            label="狀態"
            initialValue={1}
          >
            <Select placeholder="請選擇狀態">
              <Option value={1}>啟用</Option>
              <Option value={0}>停用</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="sort"
            label="排序"
            initialValue={99}
          >
            <Input type="number" placeholder="請輸入排序值" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea rows={4} placeholder="請輸入角色描述" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 權限設定對話框 */}
      <Modal
        title={`設定權限 - ${currentRole?.name || ''}`}
        open={permissionsModalVisible}
        onOk={handleSavePermissions}
        onCancel={() => setPermissionsModalVisible(false)}
        width={800}
      >
        <div style={{ maxHeight: '500px', overflow: 'auto' }}>
          {/* 這裡實作權限樹，暫時使用簡化版，實際項目中可用 Tree 或 TreeSelect 元件 */}
          <div style={{ marginBottom: 16 }}>
            <h3>選擇權限</h3>
            <p style={{ color: '#999' }}>請選擇要分配給角色的權限</p>
          </div>
          {permissions.map(permission => (
            <div key={permission.id} style={{ marginBottom: 8 }}>
              <Checkbox
                checked={selectedPermissions.includes(permission.id)}
                onChange={(e) => {
                  if (e.target.checked) {
                    setSelectedPermissions([...selectedPermissions, permission.id]);
                  } else {
                    setSelectedPermissions(selectedPermissions.filter(id => id !== permission.id));
                  }
                }}
              >
                {permission.module} - {permission.feature} - {permission.description}
              </Checkbox>
            </div>
          ))}
        </div>
      </Modal>
    </div>
  );
};

export default RoleManagement;
