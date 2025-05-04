import React, { useState, useEffect } from 'react';
import { 
  Table, Input, Button, Space, Card, 
  Row, Col, Form, Select, DatePicker, 
  Tag, Tooltip, message
} from 'antd';
import { 
  SearchOutlined, ReloadOutlined, 
  UserOutlined, LockOutlined, UnlockOutlined 
} from '@ant-design/icons';
import userService from '../services/userService';
import moment from 'moment';

const { RangePicker } = DatePicker;
const { Option } = Select;

/**
 * 使用者管理頁面元件
 */
const UserManagement = () => {
  // 狀態變數
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [roles, setRoles] = useState([]);
  const [form] = Form.useForm();
  
  // 載入使用者資料
  const fetchUsers = async (params = {}) => {
    try {
      setLoading(true);
      
      // 取得表單值
      const formValues = form.getFieldsValue();
      
      // 轉換查詢參數
      const queryParams = {
        page: params.current ? params.current - 1 : 0, // 轉換為後端的頁碼（從0開始）
        size: params.pageSize || 10,
        sortField: params.sortField || 'createTime',
        sortDirection: params.sortOrder === 'ascend' ? 'asc' : 'desc',
        keyword: formValues.keyword || '',
        roleId: formValues.roleId,
        status: formValues.status,
        startDate: formValues.dateRange?.[0]?.format('YYYY-MM-DD'),
        endDate: formValues.dateRange?.[1]?.format('YYYY-MM-DD'),
      };
      
      // 呼叫API
      const data = await userService.getUsers(queryParams);
      
      // 更新資料和分頁
      setUsers(data.content || []);
      setPagination({
        current: data.page + 1, // 轉換為前端的頁碼（從1開始）
        pageSize: data.size,
        total: data.total,
      });
      
    } catch (error) {
      console.error('載入使用者資料發生錯誤:', error);
      message.error('載入使用者資料失敗，請稍後再試');
    } finally {
      setLoading(false);
    }
  };
  
  // 載入角色選項
  const fetchRoles = async () => {
    try {
      const data = await userService.getRoles();
      setRoles(data || []);
    } catch (error) {
      console.error('載入角色資料發生錯誤:', error);
    }
  };
  
  // 初始載入
  useEffect(() => {
    fetchUsers();
    fetchRoles();
  }, []);
  
  // 處理表格變更（排序、分頁）
  const handleTableChange = (newPagination, filters, sorter) => {
    fetchUsers({
      current: newPagination.current,
      pageSize: newPagination.pageSize,
      sortField: sorter.field,
      sortOrder: sorter.order,
    });
  };
  
  // 處理表單搜尋
  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 }); // 重設到第一頁
    fetchUsers({ current: 1, pageSize: pagination.pageSize });
  };
  
  // 重設搜尋條件
  const handleReset = () => {
    form.resetFields();
    setPagination({ ...pagination, current: 1 });
    fetchUsers({ current: 1, pageSize: pagination.pageSize });
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
      title: '使用者名稱',
      dataIndex: 'username',
      key: 'username',
      sorter: true,
    },
    {
      title: '暱稱',
      dataIndex: 'nickname',
      key: 'nickname',
    },
    {
      title: '電子郵件',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '手機號碼',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: '角色',
      dataIndex: 'roles',
      key: 'roles',
      render: (roles) => (
        <>
          {(roles || []).map((role) => (
            <Tag color="blue" key={role.id}>
              {role.name}
            </Tag>
          ))}
        </>
      ),
    },
    {
      title: '狀態',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        status === 1 ? 
          <Tag color="green">啟用</Tag> : 
          <Tag color="red">停用</Tag>
      ),
    },
    {
      title: '最後登入時間',
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      sorter: true,
      render: (time) => time ? moment(time).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '建立時間',
      dataIndex: 'createTime',
      key: 'createTime',
      sorter: true,
      render: (time) => moment(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="查看詳情">
            <Button type="link" icon={<UserOutlined />} size="small" />
          </Tooltip>
          {record.status === 1 ? (
            <Tooltip title="停用">
              <Button type="link" danger icon={<LockOutlined />} size="small" />
            </Tooltip>
          ) : (
            <Tooltip title="啟用">
              <Button type="link" icon={<UnlockOutlined />} size="small" style={{ color: 'green' }} />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];
  
  return (
    <div className="user-management">
      <Card title="使用者管理" bordered={false}>
        {/* 搜尋區塊 */}
        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 24 }}
        >
          <Row gutter={[16, 16]} style={{ width: '100%' }}>
            <Col span={6}>
              <Form.Item name="keyword">
                <Input 
                  placeholder="搜尋使用者名稱/暱稱/Email" 
                  prefix={<SearchOutlined />} 
                  allowClear
                />
              </Form.Item>
            </Col>
            
            <Col span={4}>
              <Form.Item name="roleId">
                <Select placeholder="選擇角色" allowClear>
                  {roles.map(role => (
                    <Option key={role.id} value={role.id}>{role.name}</Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            
            <Col span={4}>
              <Form.Item name="status">
                <Select placeholder="選擇狀態" allowClear>
                  <Option value={1}>啟用</Option>
                  <Option value={0}>停用</Option>
                </Select>
              </Form.Item>
            </Col>
            
            <Col span={6}>
              <Form.Item name="dateRange">
                <RangePicker placeholder={['開始日期', '結束日期']} />
              </Form.Item>
            </Col>
            
            <Col span={4}>
              <Space>
                <Button type="primary" htmlType="submit">
                  搜尋
                </Button>
                <Button icon={<ReloadOutlined />} onClick={handleReset}>
                  重設
                </Button>
              </Space>
            </Col>
          </Row>
        </Form>
        
        {/* 表格區塊 */}
        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          pagination={pagination}
          loading={loading}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
        />
      </Card>
    </div>
  );
};

export default UserManagement;
