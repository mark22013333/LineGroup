import React from 'react';
import {Button, Result} from 'antd';
import {useNavigate} from 'react-router-dom';

const NotFound = () => {
    const navigate = useNavigate();

    return (
        <Result
            status="404"
            title="404"
            subTitle="抱歉，您訪問的頁面不存在。"
            extra={
                <Button type="primary" onClick={() => navigate('/')}>
                    返回首頁
                </Button>
            }
        />
    );
};

export default NotFound;
