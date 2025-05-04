import React from 'react';
import {Routes, Route, Navigate} from 'react-router-dom';
import Login from './pages/Login';
import DashboardLayout from './layouts/DashboardLayout';
import Dashboard from './pages/Dashboard';
import UserManagement from './pages/UserManagement';
import NotFound from './pages/NotFound';
import {AuthProvider} from './utils/AuthContext';
import PrivateRoute from './components/PrivateRoute';

function App() {
    return (
        <AuthProvider>
            <Routes>
                <Route path="/login" element={<Login/>}/>
                <Route path="/" element={<Navigate to="/dashboard" replace/>}/>

                <Route path="/" element={<PrivateRoute><DashboardLayout/></PrivateRoute>}>
                    <Route path="dashboard" element={<Dashboard/>}/>
                    <Route path="users" element={<UserManagement/>}/>
                    {/* 其他需要認證的路由將在這裡新增 */}
                </Route>

                <Route path="*" element={<NotFound/>}/>
            </Routes>
        </AuthProvider>
    );
}

export default App;
