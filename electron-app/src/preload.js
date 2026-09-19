const { contextBridge } = require('electron');
const fs = require('fs');
const path = require('path');

const config = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'config.json'), 'utf8'));
const apiUrl = String(config.apiUrl || '').replace(/\/$/, '');

contextBridge.exposeInMainWorld('avai', {
  apiUrl
});
