import React, { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, theme } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  DashboardOutlined,
  TeamOutlined,
  SettingOutlined,
  LogoutOutlined
} from '@ant-design/icons';
import { useAuth } from '../utils/AuthContext';

const { Header, Sider, Content } = Layout;

const DashboardLayout = () => {
  const { user, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const handleMenuClick = ({ key }) => {
    navigate(key);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // 用戶選單項
  const userMenuItems = [
    {
      key: '1',
      label: '個人中心',
      icon: <UserOutlined />,
      onClick: () => navigate('/profile')
    },
    {
      key: '2',
      label: '系統設置',
      icon: <SettingOutlined />,
      onClick: () => navigate('/settings')
    },
    {
      key: '3',
      type: 'divider',
    },
    {
      key: '4',
      label: '退出登入',
      icon: <LogoutOutlined />,
      onClick: handleLogout
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark">
        <div className="demo-logo-vertical" style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6 }} />
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={[location.pathname]}
          onClick={handleMenuClick}
          items={[
            {
              key: '/dashboard',
              icon: <DashboardOutlined />,
              label: '儀表板',
            },
            {
              key: '/users',
              icon: <TeamOutlined />,
              label: '用戶管理',
            },
            {
              key: 'settings',
              icon: <SettingOutlined />,
              label: '系統設置',
              children: [
                {
                  key: '/settings/roles',
                  label: '角色管理',
                },
                {
                  key: '/settings/permissions',
                  label: '權限配置',
                },
              ],
            },
          ]}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: 0, background: colorBgContainer }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingLeft: 16, paddingRight: 24 }}>
            {React.createElement(collapsed ? MenuUnfoldOutlined : MenuFoldOutlined, {
              className: 'trigger',
              onClick: () => setCollapsed(!collapsed),
              style: { fontSize: '18px' }
            })}
            
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
                <span style={{ marginRight: 8 }}>{user?.username || 'Admin'}</span>
                <Avatar icon={<UserOutlined />} />
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            minHeight: 280,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default DashboardLayout;
