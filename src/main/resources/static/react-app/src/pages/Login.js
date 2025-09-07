import React, {useState} from 'react';
import {Form, Input, Button, Checkbox, message, Typography} from 'antd';
import {UserOutlined, LockOutlined} from '@ant-design/icons';
import {useNavigate, useLocation} from 'react-router-dom';
import {useAuth} from '../utils/AuthContext';
import authService from '../services/authService';

const {Title} = Typography;

const Login = () => {
    const [loading, setLoading] = useState(false);
    const {login} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // 取得跳轉前頁面的路徑，預設為儀表板
    const from = location.state?.from?.pathname || '/dashboard';

    const onFinish = async (values) => {
        try {
            setLoading(true);

            // 呼叫authService進行登入（它會負責儲存token）
            await authService.login(values.username, values.password);

            // 取得使用者訊息
            const userInfo = authService.getCurrentUser() || {
                username: values.username
            };

            // 使用AuthContext更新認證狀態（不再傳入token參數）
            const loginSuccess = login(userInfo);

            if (loginSuccess) {
                message.success('登入成功！');
                navigate(from, {replace: true});
            } else {
                message.error('登入失敗: 無法取得認證令牌');
            }
        } catch (error) {
            console.error('登入失敗:', error);
            message.error('使用者名或密碼錯誤');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-form">
                <div className="login-form-title">
                    <Title level={2}>Line Group 後台管理系統</Title>
                </div>
                <Form
                    name="login_form"
                    initialValues={{remember: true}}
                    onFinish={onFinish}
                >
                    <Form.Item
                        name="username"
                        rules={[{required: true, message: '請輸入您的使用者名！'}]}
                    >
                        <Input
                            prefix={<UserOutlined/>}
                            placeholder="使用者名"
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[{required: true, message: '請輸入您的密碼！'}]}
                    >
                        <Input.Password
                            prefix={<LockOutlined/>}
                            placeholder="密碼"
                            size="large"
                        />
                    </Form.Item>

                    <Form.Item>
                        <Form.Item name="remember" valuePropName="checked" noStyle>
                            <Checkbox>記住我</Checkbox>
                        </Form.Item>
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            htmlType="submit"
                            className="login-form-button"
                            loading={loading}
                            size="large"
                        >
                            登入
                        </Button>
                    </Form.Item>
                </Form>
            </div>
        </div>
    );
};

export default Login;
