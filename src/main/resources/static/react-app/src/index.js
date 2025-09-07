import React from 'react';
import {createRoot} from 'react-dom/client';
import {BrowserRouter} from 'react-router-dom';
import App from './App';
import {AuthProvider} from './utils/AuthContext';
import './index.css';

// 在開發模式下不使用 basename，生產環境使用 /admin
const basename = process.env.NODE_ENV === 'production' ? '/admin' : '/';

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
