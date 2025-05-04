import React, {createContext, useState, useContext, useEffect} from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({children}) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // 檢查本機儲存中是否有儲存的認證訊息
        const token = localStorage.getItem('token');
        const userData = localStorage.getItem('user');

        if (token && userData) {
            setUser(JSON.parse(userData));
            console.log('AuthContext: 已從本機儲存中恢復認證狀態');
        }
        setLoading(false);
    }, []);

    // 登入 - 注意：authService.login已經處理了token儲存
    // 這個方法現在主要負責更新狀態而非儲存令牌
    const login = (userData) => {
        // 確認令牌是否已儲存
        const token = localStorage.getItem('token');
        if (!token) {
            console.error('AuthContext: 登入失敗，未找到認證令牌');
            return false;
        }

        // 儲存使用者訊息（如果尚未儲存）
        if (!localStorage.getItem('user')) {
            localStorage.setItem('user', JSON.stringify(userData));
        }

        setUser(userData);
        return true;
    };

    // 登出
    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
    };

    // 取得儲存的 token
    const getToken = () => {
        return localStorage.getItem('token');
    };

    const value = {
        user,
        loading,
        login,
        logout,
        getToken,
        isAuthenticated: !!user
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    return useContext(AuthContext);
};
