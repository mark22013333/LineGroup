import React from 'react';
import {createRoot} from 'react-dom/client';
import {BrowserRouter} from 'react-router-dom';
import App from './App';
import {AuthProvider} from './utils/AuthContext';
import './index.css';

// 統一使用根路徑，不使用 basename
const basename = '/';

const root = createRoot(document.getElementById('root'));

root.render(
    <React.StrictMode>
        <BrowserRouter basename={basename}>
            <AuthProvider>
                <App/>
            </AuthProvider>
        </BrowserRouter>
    </React.StrictMode>
);
