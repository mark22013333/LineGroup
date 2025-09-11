import React, { useState, useEffect } from 'react';
import { 
    Button, 
    Drawer, 
    List, 
    Space,
    Badge,
    FloatButton
} from 'antd';
import { 
    MenuOutlined,
    ScanOutlined,
    DashboardOutlined,
    InboxOutlined,
    FileTextOutlined,
    HistoryOutlined,
    UserOutlined,
    SettingOutlined,
    LogoutOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';

const MobileNavigation = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { logout } = useAuth();
    const [drawerVisible, setDrawerVisible] = useState(false);

    const menuItems = [
        {
            key: '/inventory/dashboard',
            icon: <DashboardOutlined />,
            title: '庫存儀表板',
            description: '查看庫存統計和概況'
        },
        {
            key: '/inventory/barcode-scanner',
            icon: <ScanOutlined />,
            title: '條碼掃描',
            description: '掃描條碼進行借還操作'
        },
        {
            key: '/inventory/mobile-scanner',
            icon: <ScanOutlined />,
            title: '手機掃描',
            description: '全螢幕掃描體驗',
            badge: 'NEW'
        },
        {
            key: '/inventory/items',
            icon: <InboxOutlined />,
            title: '物品管理',
            description: '管理庫存物品'
        },
        {
            key: '/inventory/borrow-return',
            icon: <HistoryOutlined />,
            title: '借還記錄',
            description: '查看借還歷史記錄'
        },
        {
            key: '/inventory/reports',
            icon: <FileTextOutlined />,
            title: '報表管理',
            description: '產生和查看各種報表'
        }
    ];

    const systemMenuItems = [
        {
            key: '/dashboard',
            icon: <DashboardOutlined />,
            title: '系統儀表板',
            description: '系統總覽'
        },
        {
            key: '/users',
            icon: <UserOutlined />,
            title: '使用者管理',
            description: '管理系統使用者'
        },
        {
            key: '/settings/roles',
            icon: <SettingOutlined />,
            title: '角色管理',
            description: '管理使用者角色'
        }
    ];

    const handleMenuClick = (path) => {
        navigate(path);
        setDrawerVisible(false);
    };

    const handleLogout = () => {
        logout();
        setDrawerVisible(false);
    };

    const renderMenuItem = (item) => (
        <List.Item
            key={item.key}
            onClick={() => handleMenuClick(item.key)}
            style={{
                cursor: 'pointer',
                backgroundColor: location.pathname === item.key ? '#f0f0f0' : 'transparent',
                borderRadius: '8px',
                margin: '4px 0',
                padding: '12px 16px'
            }}
        >
            <List.Item.Meta
                avatar={
                    <Badge dot={item.badge === 'NEW'} color="red">
                        <div style={{ 
                            fontSize: '20px', 
                            color: location.pathname === item.key ? '#1890ff' : '#666' 
                        }}>
                            {item.icon}
                        </div>
                    </Badge>
                }
                title={
                    <div style={{ 
                        color: location.pathname === item.key ? '#1890ff' : '#333',
                        fontWeight: location.pathname === item.key ? 'bold' : 'normal'
                    }}>
                        {item.title}
                        {item.badge && (
                            <Badge 
                                count={item.badge} 
                                style={{ 
                                    backgroundColor: '#52c41a',
                                    marginLeft: '8px',
                                    fontSize: '10px'
                                }} 
                            />
                        )}
                    </div>
                }
                description={item.description}
            />
        </List.Item>
    );

    // 檢查是否在手機端掃描頁面
    const isMobileScannerPage = location.pathname === '/inventory/mobile-scanner';
    const [isMobile, setIsMobile] = useState(false);

    // 檢測螢幕尺寸
    useEffect(() => {
        const checkScreenSize = () => {
            setIsMobile(window.innerWidth <= 768);
        };

        checkScreenSize();
        window.addEventListener('resize', checkScreenSize);
        return () => window.removeEventListener('resize', checkScreenSize);
    }, []);

    // 如果在手機端掃描頁面，不顯示導航
    if (isMobileScannerPage) {
        return null;
    }

    return (
        <>
            {/* 手機端導航按鈕 - 只在手機端顯示 */}
            {isMobile && (
                <div style={{
                    position: 'fixed',
                    top: '20px',
                    left: '20px',
                    zIndex: 1000
                }}>
                    <Button
                        type="primary"
                        icon={<MenuOutlined />}
                        onClick={() => setDrawerVisible(true)}
                        style={{
                            borderRadius: '50%',
                            width: '48px',
                            height: '48px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
                        }}
                    />
                </div>
            )}

            {/* 快速掃描按鈕 - 只在手機端顯示 */}
            {isMobile && (
                <FloatButton
                    icon={<ScanOutlined />}
                    type="primary"
                    style={{
                        right: 24,
                        bottom: 80,
                        width: 60,
                        height: 60
                    }}
                    onClick={() => navigate('/inventory/mobile-scanner')}
                    tooltip="快速掃描"
                />
            )}

            {/* 側邊導航抽屜 */}
            <Drawer
                title="導航選單"
                placement="left"
                width="85%"
                open={drawerVisible}
                onClose={() => setDrawerVisible(false)}
                bodyStyle={{ padding: '16px' }}
            >
                <div>
                    {/* 庫存管理區塊 */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ 
                            color: '#1890ff', 
                            borderBottom: '2px solid #1890ff',
                            paddingBottom: '8px',
                            marginBottom: '16px'
                        }}>
                            庫存管理
                        </h3>
                        <List
                            dataSource={menuItems}
                            renderItem={renderMenuItem}
                            split={false}
                        />
                    </div>

                    {/* 系統管理區塊 */}
                    <div style={{ marginBottom: '24px' }}>
                        <h3 style={{ 
                            color: '#52c41a', 
                            borderBottom: '2px solid #52c41a',
                            paddingBottom: '8px',
                            marginBottom: '16px'
                        }}>
                            系統管理
                        </h3>
                        <List
                            dataSource={systemMenuItems}
                            renderItem={renderMenuItem}
                            split={false}
                        />
                    </div>

                    {/* 登出按鈕 */}
                    <div style={{ 
                        position: 'absolute',
                        bottom: '20px',
                        left: '16px',
                        right: '16px'
                    }}>
                        <Button
                            danger
                            icon={<LogoutOutlined />}
                            onClick={handleLogout}
                            block
                            size="large"
                        >
                            登出
                        </Button>
                    </div>
                </div>
            </Drawer>

            {/* CSS 樣式 */}
            <style jsx>{`
                @media (max-width: 768px) {
                    .mobile-nav-trigger {
                        display: block !important;
                    }
                }
                @media (min-width: 769px) {
                    .mobile-nav-trigger {
                        display: none !important;
                    }
                }
            `}</style>
        </>
    );
};

export default MobileNavigation;
