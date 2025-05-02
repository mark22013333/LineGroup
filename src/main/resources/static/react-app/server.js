const express = require('express');
const path = require('path');
const app = express();

// 設置靜態文件目錄
app.use(express.static('public'));

// 處理所有路由請求，返回 index.html
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// 啟動服務器
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
