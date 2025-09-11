import React from 'react';
import {Routes, Route, Navigate} from 'react-router-dom';
import Login from './pages/Login';
import DashboardLayout from './layouts/DashboardLayout';
import Dashboard from './pages/Dashboard';
import UserManagement from './pages/UserManagement';
import RoleManagement from './pages/RoleManagement';
import PermissionManagement from './pages/PermissionManagement';
import NotFound from './pages/NotFound';
import {AuthProvider} from './utils/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import { 
    Dashboard as InventoryDashboard,
    ItemManagement,
    BarcodeScanner,
    MobileBarcodeScanner,
    BorrowRecordManagement,
    ReportManagement
} from './pages/inventory';

function App() {
    return (
        <AuthProvider>
            <Routes>
                <Route path="/login" element={<Login/>}/>
                <Route path="/" element={<Navigate to="/dashboard" replace/>}/>

                <Route path="/" element={<PrivateRoute><DashboardLayout/></PrivateRoute>}>
                    <Route path="dashboard" element={<Dashboard/>}/>
                    <Route path="users" element={<UserManagement/>}/>
                    <Route path="settings/roles" element={<RoleManagement/>}/>
                    <Route path="settings/permissions" element={<PermissionManagement/>}/>
                    <Route path="inventory/dashboard" element={<InventoryDashboard/>}/>
                    <Route path="inventory/barcode-scanner" element={<BarcodeScanner/>}/>
                    <Route path="inventory/mobile-scanner" element={<MobileBarcodeScanner/>}/>
                    <Route path="inventory/items" element={<ItemManagement/>}/>
                    <Route path="inventory/borrow-return" element={<BorrowRecordManagement/>}/>
                    <Route path="inventory/reports" element={<ReportManagement/>}/>
                    {/* 其他需要認證的路由在這裡新增 */}
                </Route>

                <Route path="*" element={<NotFound/>}/>
            </Routes>
        </AuthProvider>
    );
}

export default App;
