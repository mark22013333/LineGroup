import React, { useState, useEffect } from 'react';
import { Layout, Menu, Avatar, Dropdown, theme } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  DashboardOutlined,
  TeamOutlined,
  SettingOutlined,
  LogoutOutlined,
  ShopOutlined,
  ScanOutlined,
  InboxOutlined,
  SwapOutlined,
  BarChartOutlined
} from '@ant-design/icons';
import { useAuth } from '../utils/AuthContext';
import MobileNavigation from '../components/MobileNavigation';

const { Header, Sider, Content } = Layout;

const DashboardLayout = () => {
  const { user, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  // 檢測螢幕尺寸變化
  useEffect(() => {
    const checkScreenSize = () => {
      setIsMobile(window.innerWidth <= 768);
      if (window.innerWidth <= 768) {
        setCollapsed(true); // 手機端預設收合側邊欄
      }
    };

    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

  const handleMenuClick = ({ key }) => {
    navigate(key);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // 使用者選單項
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

  // 手機端使用不同的佈局
  if (isMobile) {
    return (
      <>
        <MobileNavigation />
        <Layout style={{ minHeight: '100vh' }}>
          <Content
            style={{
              padding: '16px',
              background: colorBgContainer,
              minHeight: '100vh',
            }}
          >
            <Outlet />
          </Content>
        </Layout>
      </>
    );
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed} 
        theme="dark"
        breakpoint="lg"
        collapsedWidth={isMobile ? 0 : 80}
      >
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
              label: '使用者管理',
            },
            {
              key: 'inventory',
              icon: <ShopOutlined />,
              label: '庫存管理',
              children: [
                {
                  key: '/inventory/dashboard',
                  icon: <DashboardOutlined />,
                  label: '庫存儀表板',
                },
                {
                  key: '/inventory/barcode-scanner',
                  icon: <ScanOutlined />,
                  label: '條碼掃描',
                },
                {
                  key: '/inventory/mobile-scanner',
                  icon: <ScanOutlined />,
                  label: '手機掃描',
                },
                {
                  key: '/inventory/items',
                  icon: <InboxOutlined />,
                  label: '物品管理',
                },
                {
                  key: '/inventory/borrow-return',
                  icon: <SwapOutlined />,
                  label: '借還管理',
                },
                {
                  key: '/inventory/reports',
                  icon: <BarChartOutlined />,
                  label: '報表管理',
                },
              ],
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
