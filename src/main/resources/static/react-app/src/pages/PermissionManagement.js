import React, { useState, useEffect } from 'react';
import {
  Table, Card, Button, Space, Form, Input, Select,
  Row, Col, Modal, message, Tooltip, Popconfirm
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined
} from '@ant-design/icons';
import rolePermissionService from '../services/rolePermissionService';

const { Option } = Select;

/**
 * 權限管理頁面元件
 */
const PermissionManagement = () => {
  // 狀態變數
  const [permissions, setPermissions] = useState([]);
  const [modules, setModules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('新增權限');
  const [currentPermission, setCurrentPermission] = useState(null);
  const [form] = Form.useForm();
  const [searchForm] = Form.useForm();

  // 載入權限資料
  const fetchPermissions = async (params = {}) => {
    try {
      setLoading(true);

      // 取得表單值
      const formValues = searchForm.getFieldsValue();

      // 轉換查詢參數
      const queryParams = {
        page: params.current ? params.current - 1 : 0,
        size: params.pageSize || 10,
        sortField: params.sortField || 'module',
        sortDirection: params.sortOrder === 'ascend' ? 'asc' : 'desc',
        keyword: formValues.keyword || '',
        module: formValues.module,
        feature: formValues.feature,
      };

      // 呼叫API
      const response = await rolePermissionService.getPermissions(queryParams);
      const data = response.data;

      // 更新資料和分頁
      setPermissions(data.content || []);
      setPagination({
        current: data.page + 1,
        pageSize: data.size,
        total: data.total,
      });

    } catch (error) {
      console.error('載入權限資料發生錯誤:', error);
      message.error('載入權限資料失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };

  // 載入模組選項
  const fetchModules = async () => {
    try {
      const response = await rolePermissionService.getAllModules();
      setModules(response.data || []);
    } catch (error) {
      console.error('載入模組資料發生錯誤:', error);
    }
  };

  // 初始載入
  useEffect(() => {
    fetchPermissions();
    fetchModules();
  }, []);

  // 處理表格變更（排序、分頁）
  const handleTableChange = (newPagination, filters, sorter) => {
    fetchPermissions({
      current: newPagination.current,
      pageSize: newPagination.pageSize,
      sortField: sorter.field,
      sortOrder: sorter.order,
    });
  };

  // 處理表單搜尋
  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchPermissions({ current: 1, pageSize: pagination.pageSize });
  };

  // 重設搜尋條件
  const handleReset = () => {
    searchForm.resetFields();
    setPagination({ ...pagination, current: 1 });
    fetchPermissions({ current: 1, pageSize: pagination.pageSize });
  };

  // 開啟新增權限對話框
  const showAddModal = () => {
    setModalTitle('新增權限');
    setCurrentPermission(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 開啟編輯權限對話框
  const showEditModal = (permission) => {
    setModalTitle('編輯權限');
    setCurrentPermission(permission);
    form.setFieldsValue({
      module: permission.module,
      feature: permission.feature,
      actionKey: permission.actionKey,
      description: permission.description,
      sort: permission.sort,
    });
    setModalVisible(true);
  };

  // 關閉權限對話框
  const handleCancel = () => {
    setModalVisible(false);
  };

  // 提交權限表單
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      if (currentPermission) {
        // 編輯權限
        await rolePermissionService.updatePermission(currentPermission.id, values);
        message.success('權限更新成功');
      } else {
        // 新增權限
        await rolePermissionService.createPermission(values);
        message.success('權限新增成功');
      }
      
      setModalVisible(false);
      fetchPermissions();
    } catch (error) {
      console.error('儲存權限發生錯誤:', error);
      message.error('儲存權限失敗：' + (error.response?.data?.message || error.message));
    }
  };

  // 刪除權限
  const handleDelete = async (id) => {
    try {
      await rolePermissionService.deletePermission(id);
      message.success('權限刪除成功');
      fetchPermissions();
    } catch (error) {
      console.error('刪除權限發生錯誤:', error);
      message.error('刪除權限失敗：' + (error.response?.data?.message || error.message));
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
      title: '模組名稱',
      dataIndex: 'module',
      key: 'module',
      sorter: true,
    },
    {
      title: '功能名稱',
      dataIndex: 'feature',
      key: 'feature',
      sorter: true,
    },
    {
      title: '權限代碼',
      dataIndex: 'actionKey',
      key: 'actionKey',
      sorter: true,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      sorter: true,
      width: 80,
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
      width: 120,
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
          <Tooltip title="刪除">
            <Popconfirm
              title="確定要刪除此權限嗎？"
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
    <div className="permission-management">
      <Card title="權限管理" bordered={false}>
        {/* 搜尋區域 */}
        <Form
          form={searchForm}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Row gutter={16} style={{ width: '100%' }}>
            <Col span={6}>
              <Form.Item name="module" label="模組">
                <Select placeholder="請選擇" allowClear>
                  {modules.map(module => (
                    <Option key={module} value={module}>{module}</Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="feature" label="功能">
                <Input placeholder="功能名稱" allowClear />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="keyword" label="關鍵字">
                <Input placeholder="權限代碼/描述" allowClear />
              </Form.Item>
            </Col>
            <Col span={6} style={{ textAlign: 'right' }}>
              <Space>
                <Button type="primary" htmlType="submit">
                  搜尋
                </Button>
                <Button onClick={handleReset}>
                  重設
                </Button>
                <Button type="primary" icon={<PlusOutlined />} onClick={showAddModal}>
                  新增權限
                </Button>
              </Space>
            </Col>
          </Row>
        </Form>

        {/* 表格區域 */}
        <Table
          rowKey="id"
          columns={columns}
          dataSource={permissions}
          pagination={pagination}
          loading={loading}
          onChange={handleTableChange}
        />
      </Card>

      {/* 權限表單對話框 */}
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
            name="module"
            label="模組名稱"
            rules={[{ required: true, message: '請輸入模組名稱' }]}
          >
            <Input placeholder="請輸入模組名稱" />
          </Form.Item>

          <Form.Item
            name="feature"
            label="功能名稱"
            rules={[{ required: true, message: '請輸入功能名稱' }]}
          >
            <Input placeholder="請輸入功能名稱" />
          </Form.Item>

          <Form.Item
            name="actionKey"
            label="權限代碼"
            rules={[{ required: true, message: '請輸入權限代碼' }]}
          >
            <Input placeholder="請輸入權限代碼，例如：user:add" />
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
            rules={[{ required: true, message: '請輸入權限描述' }]}
          >
            <Input.TextArea rows={4} placeholder="請輸入權限描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PermissionManagement;
