import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Progress, Table, Alert, Spin } from 'antd';
import { 
    ShoppingCartOutlined, 
    InboxOutlined, 
    WarningOutlined,
    RiseOutlined,
    UserOutlined,
    CalendarOutlined 
} from '@ant-design/icons';
import { inventoryAPI } from '../../services/inventoryAPI';

const Dashboard = () => {
    const [loading, setLoading] = useState(true);
    const [statistics, setStatistics] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            console.log('[Dashboard] 開始載入儀表板資料...');
            setLoading(true);
            
            console.log('[Dashboard] 呼叫 inventoryAPI.getDashboardStatistics()');
            const response = await inventoryAPI.getDashboardStatistics();
            
            console.log('[Dashboard] API 回應:', response);
            if (response.success) {
                setStatistics(response.data);
                console.log('[Dashboard] 儀表板資料載入成功');
            } else {
                console.error('[Dashboard] API 回應失敗:', response.message);
                setError(response.message || '載入儀表板資料失敗');
            }
        } catch (err) {
            console.error('[Dashboard] 載入儀表板資料發生錯誤:', err);
            setError('載入儀表板資料失敗: ' + err.message);
        } finally {
            setLoading(false);
            console.log('[Dashboard] 儀表板資料載入完成');
        }
    };

    if (loading) {
        return (
            <div style={{ textAlign: 'center', padding: '50px' }}>
                <Spin size="large" />
                <p>載入中...</p>
            </div>
        );
    }

    if (error) {
        return (
            <Alert
                message="載入失敗"
                description={error}
                type="error"
                showIcon
                style={{ margin: '20px' }}
            />
        );
    }

    const lowStockPercentage = statistics?.totalItemTypes > 0 
        ? (statistics.lowStockItemCount / statistics.totalItemTypes * 100).toFixed(1)
        : 0;

    const outOfStockPercentage = statistics?.totalItemTypes > 0 
        ? (statistics.outOfStockItemCount / statistics.totalItemTypes * 100).toFixed(1)
        : 0;

    return (
        <div style={{ padding: '20px' }}>
            <h2>庫存管理儀表板</h2>
            
            {/* 統計卡片 */}
            <Row gutter={[16, 16]} style={{ marginBottom: '20px' }}>
                <Col xs={24} sm={12} md={6}>
                    <Card>
                        <Statistic
                            title="物品總類"
                            value={statistics?.totalItemTypes || 0}
                            prefix={<InboxOutlined />}
                            valueStyle={{ color: '#1890ff' }}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} md={6}>
                    <Card>
                        <Statistic
                            title="總庫存數量"
                            value={statistics?.totalQuantity || 0}
                            prefix={<ShoppingCartOutlined />}
                            valueStyle={{ color: '#52c41a' }}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} md={6}>
                    <Card>
                        <Statistic
                            title="低庫存物品"
                            value={statistics?.lowStockItemCount || 0}
                            prefix={<WarningOutlined />}
                            valueStyle={{ color: '#faad14' }}
                            suffix={`(${lowStockPercentage}%)`}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} md={6}>
                    <Card>
                        <Statistic
                            title="缺貨物品"
                            value={statistics?.outOfStockItemCount || 0}
                            prefix={<WarningOutlined />}
                            valueStyle={{ color: '#f5222d' }}
                            suffix={`(${outOfStockPercentage}%)`}
                        />
                    </Card>
                </Col>
            </Row>

            {/* 庫存價值和借還統計 */}
            <Row gutter={[16, 16]} style={{ marginBottom: '20px' }}>
                <Col xs={24} sm={12} md={8}>
                    <Card title="庫存總價值">
                        <Statistic
                            value={statistics?.totalStockValue || 0}
                            precision={0}
                            valueStyle={{ color: '#722ed1' }}
                            prefix="NT$"
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} md={8}>
                    <Card title="當前借用數量">
                        <Statistic
                            value={statistics?.currentBorrowedCount || 0}
                            prefix={<UserOutlined />}
                            valueStyle={{ color: '#13c2c2' }}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} md={8}>
                    <Card title="借用趨勢">
                        <Statistic
                            value={statistics?.borrowTrend || 0}
                            precision={1}
                            valueStyle={{ 
                                color: (statistics?.borrowTrend || 0) >= 0 ? '#52c41a' : '#f5222d' 
                            }}
                            prefix={<RiseOutlined />}
                            suffix="%"
                        />
                        <div style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>
                            本月 {statistics?.monthlyBorrowCount || 0} vs 上月 {statistics?.lastMonthBorrowCount || 0}
                        </div>
                    </Card>
                </Col>
            </Row>

            {/* 今日統計 */}
            <Row gutter={[16, 16]} style={{ marginBottom: '20px' }}>
                <Col xs={24} sm={8}>
                    <Card>
                        <Statistic
                            title="今日借用"
                            value={statistics?.todayBorrowCount || 0}
                            prefix={<CalendarOutlined />}
                            valueStyle={{ color: '#1890ff' }}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={8}>
                    <Card>
                        <Statistic
                            title="今日歸還"
                            value={statistics?.todayReturnCount || 0}
                            prefix={<CalendarOutlined />}
                            valueStyle={{ color: '#52c41a' }}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={8}>
                    <Card>
                        <Statistic
                            title="逾期未還"
                            value={statistics?.overdueCount || 0}
                            prefix={<WarningOutlined />}
                            valueStyle={{ color: '#f5222d' }}
                        />
                    </Card>
                </Col>
            </Row>

            {/* 庫存狀況進度條 */}
            <Row gutter={[16, 16]}>
                <Col xs={24} md={12}>
                    <Card title="庫存狀況分析">
                        <div style={{ marginBottom: '15px' }}>
                            <div>正常庫存</div>
                            <Progress 
                                percent={100 - parseFloat(lowStockPercentage) - parseFloat(outOfStockPercentage)} 
                                status="success" 
                                showInfo={false}
                            />
                        </div>
                        <div style={{ marginBottom: '15px' }}>
                            <div>低庫存警告</div>
                            <Progress 
                                percent={parseFloat(lowStockPercentage)} 
                                status="warning" 
                                showInfo={false}
                            />
                        </div>
                        <div>
                            <div>缺貨</div>
                            <Progress 
                                percent={parseFloat(outOfStockPercentage)} 
                                status="exception" 
                                showInfo={false}
                            />
                        </div>
                    </Card>
                </Col>
                <Col xs={24} md={12}>
                    <Card title="即將到期提醒">
                        <Statistic
                            title="即將到期借用"
                            value={statistics?.dueSoonCount || 0}
                            valueStyle={{ color: '#faad14' }}
                            prefix={<WarningOutlined />}
                        />
                        <div style={{ fontSize: '12px', color: '#666', marginTop: '10px' }}>
                            3天內需要歸還的物品數量
                        </div>
                    </Card>
                </Col>
            </Row>

            {/* 更新時間 */}
            <div style={{ textAlign: 'center', marginTop: '20px', color: '#666' }}>
                最後更新時間: {statistics?.statisticsTime ? 
                    new Date(statistics.statisticsTime).toLocaleString('zh-TW') : 
                    '未知'
                }
            </div>
        </div>
    );
};

export default Dashboard;
