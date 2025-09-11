import React, { useState } from 'react';
import { Layout, Menu, Breadcrumb, theme } from 'antd';
import {
    DashboardOutlined,
    InboxOutlined,
    ScanOutlined,
    SwapOutlined,
    FileTextOutlined,
    UserOutlined,
    TagsOutlined,
    HistoryOutlined
} from '@ant-design/icons';
import {
    Dashboard,
    ItemManagement,
    BarcodeScanner,
    BorrowRecordManagement,
    ReportManagement,
    UserManagement,
    CategoryManagement,
    TransactionHistory
} from './index';

const { Header, Content, Sider } = Layout;

const InventoryMain = () => {
    const [selectedKey, setSelectedKey] = useState('dashboard');
    const [collapsed, setCollapsed] = useState(false);
    const {
        token: { colorBgContainer, borderRadiusLG },
    } = theme.useToken();

    const menuItems = [
        {
            key: 'dashboard',
            icon: <DashboardOutlined />,
            label: '儀表板',
            component: <Dashboard />
        },
        {
            key: 'items',
            icon: <InboxOutlined />,
            label: '物品管理',
            component: <ItemManagement />
        },
        {
            key: 'categories',
            icon: <TagsOutlined />,
            label: '分類管理',
            component: <CategoryManagement />
        },
        {
            key: 'scanner',
            icon: <ScanOutlined />,
            label: '條碼掃描',
            component: <BarcodeScanner />
        },
        {
            key: 'borrow',
            icon: <SwapOutlined />,
            label: '借還管理',
            component: <BorrowRecordManagement />
        },
        {
            key: 'transactions',
            icon: <HistoryOutlined />,
            label: '異動記錄',
            component: <TransactionHistory />
        },
        {
            key: 'users',
            icon: <UserOutlined />,
            label: '使用者管理',
            component: <UserManagement />
        },
        {
            key: 'reports',
            icon: <FileTextOutlined />,
            label: '報表管理',
            component: <ReportManagement />
        }
    ];

    const getCurrentComponent = () => {
        const currentItem = menuItems.find(item => item.key === selectedKey);
        return currentItem ? currentItem.component : <Dashboard />;
    };

    const getCurrentBreadcrumb = () => {
        const currentItem = menuItems.find(item => item.key === selectedKey);
        return currentItem ? currentItem.label : '儀表板';
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider 
                collapsible 
                collapsed={collapsed} 
                onCollapse={setCollapsed}
                theme="light"
                width={250}
            >
                <div style={{ 
                    height: 32, 
                    margin: 16, 
                    background: 'rgba(255, 255, 255, 0.3)',
                    borderRadius: borderRadiusLG,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 'bold',
                    color: '#1890ff'
                }}>
                    {collapsed ? 'IMS' : '庫存管理系統'}
                </div>
                <Menu
                    mode="inline"
                    selectedKeys={[selectedKey]}
                    items={menuItems.map(item => ({
                        key: item.key,
                        icon: item.icon,
                        label: item.label
                    }))}
                    onClick={({ key }) => setSelectedKey(key)}
                />
            </Sider>
            <Layout>
                <Header
                    style={{
                        padding: '0 24px',
                        background: colorBgContainer,
                        display: 'flex',
                        alignItems: 'center',
                        borderBottom: '1px solid #f0f0f0'
                    }}
                >
                    <Breadcrumb
                        items={[
                            {
                                title: '庫存管理系統',
                            },
                            {
                                title: getCurrentBreadcrumb(),
                            },
                        ]}
                    />
                </Header>
                <Content
                    style={{
                        margin: 0,
                        background: colorBgContainer,
                        borderRadius: collapsed ? 0 : borderRadiusLG,
                        overflow: 'auto'
                    }}
                >
                    {getCurrentComponent()}
                </Content>
            </Layout>
        </Layout>
    );
};

export default InventoryMain;
