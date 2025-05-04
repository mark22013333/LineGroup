import React from 'react';
import { Row, Col, Card, Statistic, Table, Typography } from 'antd';
import { UserOutlined, TeamOutlined, DesktopOutlined, FileOutlined } from '@ant-design/icons';

const { Title } = Typography;

const Dashboard = () => {
  // 這些數據通常會從後端 API 取得
  const stats = [
    {
      title: '總使用者數',
      value: 1024,
      icon: <UserOutlined />,
      color: '#1890ff'
    },
    {
      title: '今日活躍',
      value: 86,
      icon: <TeamOutlined />,
      color: '#52c41a'
    },
    {
      title: '系統服務',
      value: 8,
      icon: <DesktopOutlined />,
      color: '#722ed1'
    },
    {
      title: '訊息紀錄',
      value: 2048,
      icon: <FileOutlined />,
      color: '#fa8c16'
    },
  ];

  // 範例表格數據
  const dataSource = [
    {
      key: '1',
      username: 'admin',
      loginTime: '2025-04-30 21:30:45',
      action: '登入系統',
      ip: '192.168.1.100',
    },
    {
      key: '2',
      username: 'user1',
      loginTime: '2025-04-30 20:15:32',
      action: '查詢使用者資料',
      ip: '192.168.1.101',
    },
    {
      key: '3',
      username: 'manager',
      loginTime: '2025-04-30 19:45:18',
      action: '更新系統設置',
      ip: '192.168.1.102',
    },
    {
      key: '4',
      username: 'guest',
      loginTime: '2025-04-30 18:22:05',
      action: '查看儀表板',
      ip: '192.168.1.103',
    },
  ];

  const columns = [
    {
      title: '使用者名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '時間',
      dataIndex: 'loginTime',
      key: 'loginTime',
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
    },
    {
      title: 'IP地址',
      dataIndex: 'ip',
      key: 'ip',
    },
  ];

  return (
    <div>
      <Title level={2}>系統儀表板</Title>
      
      <Row gutter={[16, 16]}>
        {stats.map((stat, index) => (
          <Col xs={24} sm={12} md={6} key={index}>
            <Card>
              <Statistic
                title={stat.title}
                value={stat.value}
                valueStyle={{ color: stat.color }}
                prefix={stat.icon}
              />
            </Card>
          </Col>
        ))}
      </Row>
      
      <div style={{ marginTop: 24 }}>
        <Title level={4}>最近活動</Title>
        <Table dataSource={dataSource} columns={columns} pagination={false} />
      </div>
    </div>
  );
};

export default Dashboard;
