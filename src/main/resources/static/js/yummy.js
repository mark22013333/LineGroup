// --- Google Maps API 動態載入 ---

const API_KEY = 'REMOVED';


function loadGoogleMaps() {
    return new Promise((resolve, reject) => {
        // 移除任何已存在的Google Maps API腳本，防止重複載入
        const existingScripts = document.querySelectorAll('script[src*="maps.googleapis.com"]');
        existingScripts.forEach(script => {
            console.log("Removing existing Google Maps script:", script.src);
            script.remove();
        });
        // 清除可能殘留的回呼函式
        delete window.initMap;
        delete window.initMap_opera;

        // 檢測Opera瀏覽器 (雖然現代Opera基於Chromium，但保留此邏輯以防萬一)
        const isOpera = /OPR|Opera/.test(navigator.userAgent);

        const script = document.createElement('script');
        let callbackFunctionName = 'initMap'; // Default callback name

        // Opera需要特別處理，添加callback=initMap_opera參數
        // Note: Modern Opera might not need this anymore, but kept for compatibility.
        if (isOpera) {
            callbackFunctionName = 'initMap_opera';
            window[callbackFunctionName] = function () {
                console.log("Opera 專用地圖初始化函數已調用");
                initMap(); // 呼叫主要的初始化函式
            };
            script.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&libraries=places&callback=${callbackFunctionName}&v=quarterly&language=zh-TW`;
            console.log("Loading Maps API for Opera with callback:", callbackFunctionName);
        } else {
            // 將 initMap 設為全域函數供 callback 使用
            window[callbackFunctionName] = initMap;
            script.src = `https://maps.googleapis.com/maps/api/js?key=${API_KEY}&libraries=places&callback=${callbackFunctionName}&v=weekly&language=zh-TW`;
            console.log("Loading Maps API with callback:", callbackFunctionName);
        }

        script.async = true;
        script.defer = true; // defer 確保在 HTML 解析後執行

        // 添加超時處理，防止API載入無回應
        const timeoutDuration = isOpera ? 20000 : 10000; // Opera 給予更長時間
        const timeout = setTimeout(() => {
            if (!window.google || !window.google.maps) {
                console.error(`Google Maps API 載入超時 (${timeoutDuration}ms)`);
                script.remove(); // 清理超時的腳本標籤
                 // 確保全域回呼被清理
                delete window[callbackFunctionName];
                reject(new Error('Google Maps API 載入超時'));
            }
        }, timeoutDuration);

        script.onload = () => {
            clearTimeout(timeout);
            console.log("Google Maps API script loaded successfully.");
            // Resolve 表示腳本已載入，但不保證 initMap 已執行 (由 API 的 callback 觸發)
            resolve();
        };

        script.onerror = (error) => {
            clearTimeout(timeout);
            console.error('Google Maps API 腳本載入失敗:', error);
             // 確保全域回呼被清理
            delete window[callbackFunctionName];
            reject(new Error('Google Maps API 腳本載入失敗'));
        };

        document.head.appendChild(script);
        console.log("Appending Maps API script to head.");
    });
}

// --- 頁面和網路狀態監聽 ---

// 確保 DOM 載入完成後再嘗試載入地圖 API
document.addEventListener('DOMContentLoaded', () => {
    console.log("DOM fully loaded and parsed.");
    // 添加網路狀態檢測
    if (navigator.onLine) {
        console.log("Network online, attempting to load Google Maps.");
        loadGoogleMaps().catch(error => {
            console.error('Initial Google Maps API load failed:', error);
           showError(`地圖服務載入失敗: ${error.message}. 請檢查您的網路連線並重新整理頁面。`);
        });
    } else {
         console.warn("Network offline initially.");
        showError('您似乎處於離線狀態，請檢查網路連線後重試');
    }
});


// 監聽網路狀態變為線上
window.addEventListener('online', () => {
    console.log("Network status changed to online.");
    const errorElement = document.getElementById('error');
    // 如果之前有錯誤訊息，且 Google Maps 物件不存在，則嘗試重新載入
    if (errorElement && errorElement.style.display !== 'none' && (!window.google || !window.google.maps)) {
        console.log("Attempting to reload Google Maps after coming online.");
        errorElement.style.display = 'none'; // Hide previous error
        showStatus("偵測到網路連線，正在重新載入地圖服務...", "info"); // Show info message
        loadGoogleMaps().then(() => {
             hideStatus(); // Hide info on success
        }).catch(error => {
            console.error('Google Maps API reload failed:', error);
            showError(`重新載入地圖服務失敗: ${error.message}`);
        });
    }
});

// 監聽網路狀態變為離線
window.addEventListener('offline', () => {
     console.warn("Network status changed to offline.");
    showError('網路連線已中斷，部分功能可能無法使用');
});

// --- Helper Functions for Status/Error Display ---
function showStatus(message, type = 'info') { // type can be 'info', 'error', 'success'
    const statusElement = document.getElementById('statusMessage');
    if (!statusElement) return;
    statusElement.textContent = message;
    statusElement.className = `status-message ${type}`; // Reset classes
    statusElement.style.display = 'block';
}

function hideStatus() {
     const statusElement = document.getElementById('statusMessage');
     if (statusElement) statusElement.style.display = 'none';
}

function showError(message) {
    const errorElement = document.getElementById('error');
    if (!errorElement) {
        console.error("Error display element (#error) not found. Message:", message);
        return;
    }
    errorElement.textContent = message;
    errorElement.style.display = 'block';
    console.error("Displayed Error:", message);
    hideStatus(); // Hide any info messages when an error occurs
}

function hideError() {
     const errorElement = document.getElementById('error');
     if (errorElement) errorElement.style.display = 'none';
}

// --- End Helper Functions ---

// --- Global Variables (Initialized Later) ---
let map, userMarker, infowindow, placesService;
let userMarkerGlow, userInfoWindow; // Specific for user marker
let glowIntervalId = null;
let currentSearchLocation = null; // { lat: number, lng: number }
let confirmationInfoWindow = null; // For map click confirmation
let currentRestaurantMarkers = []; // Array to hold restaurant markers
let searchPagination = null; // To handle 'load more' results
let allResults = []; // 用於儲存所有分頁的結果
// --- End Global Variables ---

// --- Utility Functions ---

// 顯示權限指南
function showPermissionGuide() {
    const guide = document.getElementById('permissionGuide');
    if (guide) {
        guide.style.display = 'block';
    } else {
        console.warn("Permission guide element (#permissionGuide) not found.");
    }
}

// 清除地圖上的餐廳標記
function clearRestaurantMarkers() {
    console.log(`Clearing ${currentRestaurantMarkers.length} restaurant markers.`);
    currentRestaurantMarkers.forEach(marker => marker.setMap(null));
    currentRestaurantMarkers = [];
}

// 檢查瀏覽器地理位置相容性
function checkBrowserCompatibility() {
    const hasGeolocation = 'geolocation' in navigator;
    console.log("Geolocation support:", hasGeolocation);
    if (!hasGeolocation) {
         const browserCheck = document.getElementById('browserCheck');
         if(browserCheck) browserCheck.style.display = 'block';
         showError("您的瀏覽器不支援地理位置功能。請嘗試更新瀏覽器或使用其他瀏覽器。");
         return false;
    }
     // Hide the check if geolocation is supported
     const browserCheck = document.getElementById('browserCheck');
     if(browserCheck) browserCheck.style.display = 'none';
    return true;
}


// 呼吸燈動畫函數
const baseGlowScale = 12;
const maxGlowScale = 18;
const baseGlowOpacity = 0.2;
const maxGlowOpacity = 0.5;
const glowSpeed = 2000; // milliseconds

function animateGlow() {
    // Check if marker exists and is on map
    if (!userMarkerGlow || !userMarkerGlow.getMap()) {
        // If animation is running but marker removed, stop interval
        if (glowIntervalId) {
            clearInterval(glowIntervalId);
            glowIntervalId = null;
            // console.log("Stopped glow animation: marker removed.");
        }
        return;
    }

    const elapsed = Date.now() % glowSpeed;
    const fraction = elapsed / glowSpeed;
    // Use sin for smooth pulse
    const pulse = (Math.sin(fraction * 2 * Math.PI) + 1) / 2;

    const currentScale = baseGlowScale + (maxGlowScale - baseGlowScale) * pulse;
    const currentOpacity = baseGlowOpacity + (maxGlowOpacity - baseGlowOpacity) * pulse;

    // Use setIcon to update the icon properties dynamically
    userMarkerGlow.setIcon({
        path: google.maps.SymbolPath.CIRCLE,
        scale: currentScale,
        fillColor: '#FFD700',
        fillOpacity: currentOpacity,
        strokeWeight: 0 // No border for the glow
    });
}

// --- End Utility Functions ---

// ********** END OF PART 1 **********
// --- Map and Location Handling ---

// 初始化地圖
async function initMap() {
    console.log("Attempting to initialize map...");
    hideError(); // Hide any previous errors on init attempt
    showStatus("正在初始化地圖服務...", "info");

    const mapElement = document.getElementById("map");
    const loadingElement = document.getElementById('loading');

    if (!mapElement) {
        console.error("Map container element (#map) not found!");
        showError("無法找到地圖容器元素。");
        hideStatus();
        return;
    }

    try {
        // 再次確認 Google Maps 物件是否存在
        if (typeof google === 'undefined' || !google.maps) {
             console.error("Google Maps library not loaded when initMap called.");
             showError("地圖服務尚未就緒，請稍後再試。");
             hideStatus();
             // 可以考慮在這裡安排重試
             // setTimeout(initMap, 1000); // Example: Retry after 1 second
             return;
        }

        console.log("Google Maps library loaded. Initializing map...");

        // 初始化地圖實例
        map = new google.maps.Map(mapElement, {
            center: { lat: 25.0330, lng: 121.5654 }, // 預設中心點 (台北市)
            zoom: 15,
            gestureHandling: 'greedy', // 允許單指縮放/移動 (對行動裝置友好)
            mapTypeControl: false, // 隱藏地圖/衛星切換
            streetViewControl: false, // 隱藏街景小人
            fullscreenControl: false // 隱藏全螢幕按鈕 (可選)
        });

        // 初始化資訊視窗 (用於顯示餐廳詳情)
        infowindow = new google.maps.InfoWindow();

        // 初始化 Places Service (用於搜尋餐廳)
        // 再次確認 Places library 是否載入成功
        if (google.maps.places && google.maps.places.PlacesService) {
             placesService = new google.maps.places.PlacesService(map);
             console.log("PlacesService initialized successfully.");
        } else {
            console.error("Google Maps Places library failed to load!");
            showError("餐廳搜尋服務載入失敗，請重新整理頁面。");
            hideStatus();
            return; // 如果 Places 服務失敗，停止後續初始化
        }

        console.log("Map and PlacesService initialization successful.");
        hideStatus(); // 地圖初始化完成，隱藏狀態訊息

        // --- 地圖點擊事件監聽器 ---
        map.addListener('click', (event) => {
            const clickedLocation = event.latLng;
            console.log("Map clicked at:", clickedLocation.toJSON());

            // 如果之前有確認視窗，先關閉它
            if (confirmationInfoWindow) {
                confirmationInfoWindow.close();
            }

            // 建立確認視窗的內容
            const confirmationContent = `
                <div style="text-align: center; padding: 10px; font-family: sans-serif;">
                    <p style="margin-bottom: 15px; font-size: 14px;">要將搜尋中心設在此處嗎？</p>
                    <button id="confirmLocationBtn" style="margin-right: 10px; padding: 8px 15px; cursor:pointer; background-color: #4CAF50; color: white; border: none; border-radius: 4px;">確定</button>
                    <button id="cancelLocationBtn" style="padding: 8px 15px; cursor:pointer; background-color: #f44336; color: white; border: none; border-radius: 4px;">取消</button>
                </div>
            `;

            // 建立新的確認視窗
            confirmationInfoWindow = new google.maps.InfoWindow({
                content: confirmationContent,
                position: clickedLocation,
                maxWidth: 250 // 限制最大寬度
            });

            // 確保在 DOM 準備好後再綁定事件
            google.maps.event.addListenerOnce(confirmationInfoWindow, 'domready', () => {
                const confirmBtn = document.getElementById('confirmLocationBtn');
                const cancelBtn = document.getElementById('cancelLocationBtn');

                if (confirmBtn) {
                    confirmBtn.onclick = () => {
                        currentSearchLocation = clickedLocation.toJSON();
                        console.log("Search center set by map click:", currentSearchLocation);

                        // 更新使用者標記位置 (如果存在)
                        updateUserMarker(currentSearchLocation, "搜尋中心點 (點擊或拖曳設定)");

                        map.panTo(currentSearchLocation); // 平移地圖到新中心點
                        confirmationInfoWindow.close();
                        showStatus("搜尋中心點已更新", "success");
                        setTimeout(hideStatus, 2000); // 2秒後自動隱藏成功訊息
                    };
                } else {
                     console.error("Confirm button not found in InfoWindow DOM.");
                }

                if (cancelBtn) {
                    cancelBtn.onclick = () => confirmationInfoWindow.close();
                } else {
                    console.error("Cancel button not found in InfoWindow DOM.");
                }
            });

            confirmationInfoWindow.open(map);
        });
        // --- 結束地圖點擊事件 ---

        // --- 設定按鈕事件監聽器 ---
        const findButton = document.getElementById('findRestaurants');
        const findMeButton = document.getElementById('findMeButton');
        const loadMoreButton = document.getElementById('loadMoreButton');

        if (findButton) {
            findButton.addEventListener('click', () => {
                if (!currentSearchLocation) {
                    showError("請先定位您的位置或在地圖上點擊選擇搜尋中心。");
                    // 嘗試自動定位一次
                    getCurrentLocationAndUpdateMarker(true); // true 表示定位後置中地圖
                    return;
                }
                 if (!placesService) {
                     showError("餐廳搜尋服務尚未就緒，請稍後再試。");
                     return;
                 }
                findNearbyRestaurants(true); // true 表示這是初始搜尋
            });
        } else {
            console.error("Button with ID 'findRestaurants' not found!");
        }

        if (findMeButton) {
            findMeButton.addEventListener('click', () => {
                // 檢查瀏覽器相容性後再定位
                if (checkBrowserCompatibility()) {
                     getCurrentLocationAndUpdateMarker(true); // true 表示定位後置中地圖
                }
            });
        } else {
            console.error("Button with ID 'findMeButton' not found!");
        }

         if (loadMoreButton) {
            loadMoreButton.addEventListener('click', loadMoreResults);
            loadMoreButton.style.display = 'none'; // Initially hide 'Load More'
        } else {
             console.error("Button with ID 'loadMoreButton' not found!");
        }
        // --- 結束按鈕事件監聽器 ---

        // 地圖初始化完成後，檢查相容性並嘗試自動取得初始位置
        if (checkBrowserCompatibility()) {
             // 嘗試在不打擾使用者的情況下取得初始位置
             // false 表示如果定位成功，不要強制置中地圖，除非使用者點擊 "Find Me"
             getCurrentLocationAndUpdateMarker(false);
        } else {
            // 如果不支援地理定位，設定一個預設位置並告知使用者
            currentSearchLocation = { lat: 25.0330, lng: 121.5654 }; // 預設台北
            updateUserMarker(currentSearchLocation, "預設搜尋中心 (無法自動定位)");
            map.setCenter(currentSearchLocation);
             showError("無法自動定位，已顯示預設位置。");
         }

    } catch (error) {
        console.error("Critical error during map initialization:", error);
        if (loadingElement) loadingElement.style.display = 'none';
        showError("地圖初始化過程中發生嚴重錯誤: " + (error.message || error));
        hideStatus();
    }
}

// 取得目前位置並更新標記
async function getCurrentLocationAndUpdateMarker(centerMap = false) {
     console.log(`Attempting to get current location... (centerMap: ${centerMap})`);
     hideError(); // 清除舊錯誤
     showStatus("正在定位您的位置...", "info");
     const loadingElement = document.getElementById('loading');
     const findMeButton = document.getElementById('findMeButton');

     if(loadingElement) {
         loadingElement.style.display = 'block';
     }
      if(findMeButton) findMeButton.disabled = true; // 防止重複點擊

     try {
         const position = await new Promise((resolve, reject) => {
              // 設定更合理的超時和緩存時間
              const geoOptions = {
                  enableHighAccuracy: true, // 盡可能提高精度
                  timeout: 10000, // 10秒超時
                  maximumAge: 30000 // 接受30秒內的緩存位置
              };
              navigator.geolocation.getCurrentPosition(resolve, reject, geoOptions);
         });

         const userLocation = {
             lat: position.coords.latitude,
             lng: position.coords.longitude
         };
         console.log("Geolocation successful:", userLocation);
         currentSearchLocation = userLocation; // 將獲取到的位置設為搜尋中心

         updateUserMarker(userLocation, "您的目前位置 (可拖曳)"); // 更新標記

         if (centerMap && map) {
             console.log("Centering map to user location.");
             map.setCenter(userLocation);
             map.setZoom(16); // 定位成功後稍微放大
         }

         hideStatus(); // 隱藏定位中訊息
         showStatus("定位成功！", "success");
         setTimeout(hideStatus, 2000); // 短暫顯示成功訊息

     } catch (error) {
         console.error("Geolocation error:", error);
         hideStatus(); // 隱藏定位中訊息
         handleGeolocationError(error); // 使用統一的錯誤處理函數
         // 定位失敗，可以考慮保留上一次的位置或預設位置
         if (!currentSearchLocation) {
            currentSearchLocation = { lat: 25.0330, lng: 121.5654 }; // 預設台北
            updateUserMarker(currentSearchLocation, "預設搜尋中心 (定位失敗)");
            if(map && centerMap) map.setCenter(currentSearchLocation);
            showError("定位失敗，已顯示預設位置。");
         } else {
             showError("定位失敗，將使用上一次的位置或您設定的中心點。");
         }
     } finally {
          if(loadingElement) loadingElement.style.display = 'none';
          if(findMeButton) findMeButton.disabled = false; // 重新啟用按鈕
     }
}

// 更新或建立使用者位置標記及其效果
function updateUserMarker(location, title = "您的位置") {
    if (!map) {
        console.warn("Map not initialized yet, cannot update user marker.");
        return;
    }
     if (!google || !google.maps) {
         console.warn("Google Maps library not ready, cannot update user marker.");
         return;
     }

    const markerIcon = {
        path: google.maps.SymbolPath.CIRCLE, // 主要標記用圓形
        scale: 8,
        fillColor: '#4285F4', // Google 藍色
        fillOpacity: 1,
        strokeColor: 'white',
        strokeWeight: 2
    };

     const glowIcon = {
        path: google.maps.SymbolPath.CIRCLE, // 光暈也用圓形
        scale: baseGlowScale, // 初始大小
        fillColor: '#FFD700', // 黃色光暈
        fillOpacity: baseGlowOpacity, // 初始透明度
        strokeWeight: 0 // 光暈不需要邊框
    };

    // --- 更新或建立主要標記 ---
    if (userMarker) {
        console.log("Updating existing user marker position:", location);
        userMarker.setPosition(location);
        userMarker.setTitle(title); // 更新滑鼠懸停文字
    } else {
        console.log("Creating new user marker:", location);
        userMarker = new google.maps.Marker({
            position: location,
            map: map,
            icon: markerIcon,
            title: title, // 滑鼠懸停文字
            draggable: true, // 允許使用者拖曳標記來設定位置
            zIndex: 10 // 讓使用者標記在餐廳標記之上
        });

        // --- 為主要標記添加拖曳事件 ---
        userMarker.addListener('dragstart', () => {
             console.log("User marker drag start.");
             if (userInfoWindow) userInfoWindow.close(); // 拖曳時關閉資訊視窗
             if (glowIntervalId) clearInterval(glowIntervalId); // 拖曳時暫停動畫
             if (userMarkerGlow) userMarkerGlow.setVisible(false); // 隱藏光暈
        });

        userMarker.addListener('dragend', () => {
            const newPosition = userMarker.getPosition().toJSON();
            console.log("User marker dragged to:", newPosition);
            currentSearchLocation = newPosition; // 更新搜尋中心點
             if (userMarkerGlow) {
                 userMarkerGlow.setPosition(newPosition); // 同步光暈位置
                 userMarkerGlow.setVisible(true); // 重新顯示光暈
             }
             if (glowIntervalId) clearInterval(glowIntervalId); // 停止舊的動畫計時器
             glowIntervalId = setInterval(animateGlow, 50); // 重新啟動動畫

             // 更新資訊視窗內容
             updateUserInfoWindowContent("搜尋中心點 (點擊或拖曳設定)");
             if(userInfoWindow) userInfoWindow.open(map, userMarker); // 拖曳結束後重新打開

              showStatus("搜尋中心點已更新", "success");
              setTimeout(hideStatus, 2000);
        });

         // --- 為主要標記添加點擊事件 (顯示資訊視窗) ---
         userMarker.addListener('click', () => {
             if (!userInfoWindow) {
                 userInfoWindow = new google.maps.InfoWindow({
                    content: `<h3>${userMarker.getTitle()}</h3>`, // 初始內容
                     maxWidth: 200
                 });
             } else {
                  // 更新內容以防標題變化
                 userInfoWindow.setContent(`<h3>${userMarker.getTitle()}</h3>`);
             }
             userInfoWindow.open(map, userMarker);
         });
    }

    // --- 更新或建立光暈標記 ---
    if (userMarkerGlow) {
        userMarkerGlow.setPosition(location);
         if (!userMarkerGlow.getMap()) { // 如果之前被隱藏了，重新顯示
             userMarkerGlow.setMap(map);
         }
         userMarkerGlow.setVisible(true);
    } else {
        console.log("Creating user marker glow effect.");
        userMarkerGlow = new google.maps.Marker({
            position: location,
            map: map,
            icon: glowIcon,
            clickable: false, // 光暈不可點擊
            zIndex: 9 // 在主要標記之下
        });
    }

    // 更新 userInfoWindow (如果存在) 的內容
    updateUserInfoWindowContent(title);

    // --- 啟動/重置呼吸燈動畫 ---
    if (glowIntervalId) {
        clearInterval(glowIntervalId); // 清除舊的計時器
        console.log("Restarting glow animation interval.");
    } else {
        console.log("Starting glow animation interval.");
    }
    // 設定新的計時器，頻繁更新以獲得平滑效果
    glowIntervalId = setInterval(animateGlow, 50); // 每 50ms 更新一次動畫幀


}

// Helper to update user InfoWindow content
function updateUserInfoWindowContent(title) {
    if (userInfoWindow && userMarker) {
        userInfoWindow.setContent(`<h3>${title}</h3>`);
         // 如果視窗是打開的，確保它仍然附著在標記上
         if(userInfoWindow.getMap()) {
             userInfoWindow.open(map, userMarker);
         }
    }
}

// 處理地理位置錯誤
function handleGeolocationError(error) {
    hideStatus(); // 隱藏任何進行中的狀態訊息
    let message = "定位時發生未知錯誤。";
    switch (error.code) {
        case error.PERMISSION_DENIED:
            message = "您拒絕了地理位置權限請求。請檢查瀏覽器設定以啟用權限。";
            showPermissionGuide(); // 顯示權限設定指南
            break;
        case error.POSITION_UNAVAILABLE:
            message = "無法取得您目前的位置資訊。";
            break;
        case error.TIMEOUT:
            message = "定位請求超時。請稍後再試。";
            break;
        default:
            message = `定位時發生錯誤 (${error.code}): ${error.message}`;
            break;
    }
    console.error("Geolocation Error:", error.code, error.message);
    showError(message);
}

// --- End Map and Location Handling ---

// ********** END OF PART 2 **********
// --- Restaurant Search and Display ---

// 搜尋附近餐廳
// 搜尋附近餐廳 (修改後，會自動載入所有分頁)
function findNearbyRestaurants(isInitialSearch = true) {
    if (!map || !placesService || !currentSearchLocation) {
        console.error("Search prerequisites not met: map, placesService, or currentSearchLocation missing.");
        showError("無法搜尋餐廳，地圖服務或目前位置尚未準備好。");
        return;
    }

    hideError();
    showStatus("正在搜尋附近餐廳...", "info");
    const loadingElement = document.getElementById('loading');
    const resultsPanel = document.getElementById('results');
    const findButton = document.getElementById('findRestaurants');

    if (loadingElement) loadingElement.style.display = 'block';
    if (findButton) findButton.disabled = true; // 搜尋開始時禁用按鈕

    // 清除先前的結果 (如果是新的搜尋)
    if (isInitialSearch) {
        clearRestaurantMarkers();
        const restaurantList = document.getElementById('restaurantList');
        if (restaurantList) restaurantList.innerHTML = '';
        allResults = []; // 重置所有結果的儲存陣列
        searchPagination = null; // searchPagination 仍然可以用來檢查 hasNextPage
        console.log("Cleared previous results for new search.");
    }

    const searchRadius = 1000;
    const request = {
        location: new google.maps.LatLng(currentSearchLocation.lat, currentSearchLocation.lng),
        radius: searchRadius,
        // keyword: '餐廳', // 移除此行
        types: ['restaurant', 'cafe', 'bakery', 'meal_takeaway', 'food'], // 新增此行以包含多種類型
        language: 'zh-TW',
        rankBy: google.maps.places.RankBy.PROMINENCE, // 保留 PROMINENCE 或根據需要改為 DISTANCE
    };

    console.log("Performing nearbySearch with request:", request);

    // 定義遞迴處理分頁的回呼函式
    const processPage = (results, status, pagination) => {
         // 先把 searchPagination 保存起來，即使 status 不是 OK 也可能有分頁資訊
         if (pagination) {
             searchPagination = pagination;
         }

        if (status === google.maps.places.PlacesServiceStatus.OK && results) {
            console.log(`Received ${results.length} results for this page.`);
            allResults = allResults.concat(results); // 將當前頁結果添加到總結果中

            displayResults(results); // 顯示當前頁的結果
            createMarkersForPlaces(results); // 為當前頁建立標記

            // 使用保存的 searchPagination 檢查是否有下一頁
            if (searchPagination && searchPagination.hasNextPage) {
                console.log("More results available, fetching next page...");
                showStatus(`正在載入更多餐廳 (${allResults.length} 已載入)...`, "info");
                // *** 重要：加入延遲以避免 OVER_QUERY_LIMIT ***
                setTimeout(() => {
                    try {
                         searchPagination.nextPage(); // 請求下一頁，會再次觸發 processPage 回呼
                    } catch (e) {
                        console.error("Error calling nextPage():", e);
                        showError("載入更多結果時發生錯誤。");
                        if (loadingElement) loadingElement.style.display = 'none';
                        if (findButton) findButton.disabled = false; // 出錯時啟用按鈕
                        hideStatus();
                        // 即使 nextPage 出錯，還是可以嘗試調整現有結果的邊界
                        if (allResults.length > 0) {
                             adjustMapBounds(allResults);
                        }
                    }
                }, 1500); // 延遲 1.5 秒
            } else {
                // 沒有更多頁面了 (或者 pagination 物件不存在)
                console.log(`All pages loaded or no more pages. Total results: ${allResults.length}`);
                if (loadingElement) loadingElement.style.display = 'none';
                if (findButton) findButton.disabled = false; // 所有結果載入完成後啟用按鈕
                hideStatus(); // 隱藏載入訊息
                 showStatus(`共找到 ${allResults.length} 間餐廳`, "success");
                 setTimeout(hideStatus, 3000); // 短暫顯示最終結果數量

                // 在所有結果都載入後，調整地圖邊界
                if (allResults.length > 0) {
                    adjustMapBounds(allResults);
                }
            }
        } else if (status === google.maps.places.PlacesServiceStatus.ZERO_RESULTS) {
            console.log("No restaurants found for this request/page.");
             // 如果是第一頁就沒有結果
             if (allResults.length === 0) {
                 showError("在您指定的位置附近找不到餐廳。");
                 if (resultsPanel) resultsPanel.innerHTML = '<li>沒有找到符合條件的餐廳。</li>';
             } else {
                 // 如果是後續頁面沒有結果，表示已經載入完畢
                 showStatus(`共找到 ${allResults.length} 間餐廳`, "success");
                 setTimeout(hideStatus, 3000);
                  if (allResults.length > 0) {
                     adjustMapBounds(allResults); // 調整已載入結果的邊界
                 }
             }
            if (loadingElement) loadingElement.style.display = 'none';
            if (findButton) findButton.disabled = false; // 找不到結果也要啟用按鈕
            hideStatus();
        } else {
            // 其他錯誤狀態
            console.error("PlacesService nearbySearch failed:", status);
            showError(`搜尋餐廳時發生錯誤: ${status}. 請稍後再試。`);
            if (loadingElement) loadingElement.style.display = 'none';
            if (findButton) findButton.disabled = false; // 出錯時啟用按鈕
            hideStatus();
             // 即使出錯，也嘗試調整已載入結果的邊界
             if (allResults.length > 0) {
                 adjustMapBounds(allResults);
             }
        }
    };

    // 執行第一次搜尋，使用定義好的回呼函式
    placesService.nearbySearch(request, processPage);
}

// 為地點列表建立地圖標記
function createMarkersForPlaces(places) {
    if (!map) return;
    console.log(`Creating markers for ${places.length} places.`);

    places.forEach(place => {
        if (!place.geometry || !place.geometry.location) {
            console.warn("Place missing geometry:", place.name);
            return; // Skip places without location
        }
        createMarker(place);
    });
}

// 建立單一餐廳標記
function createMarker(place) {
    if (!map || !place.geometry || !place.geometry.location) return;

    // 建立較小的餐廳標記圖示
    const restaurantIcon = {
        path: google.maps.SymbolPath.CIRCLE,
        scale: 6, // 縮小標記大小
        fillColor: '#FF5722', // 橙色
        fillOpacity: 0.9,
        strokeWeight: 1,
        strokeColor: '#FFF'
    };

    const marker = new google.maps.Marker({
        map: map,
        position: place.geometry.location,
        title: place.name, // Hover text
        placeId: place.place_id, // 儲存 place_id 以便後續參考
        animation: google.maps.Animation.DROP, // 新增下降動畫
        icon: restaurantIcon // 使用較小的自訂圖示
    });

    // 為標記添加點擊事件，打開資訊視窗
    marker.addListener('click', () => {
        // 關閉可能已開啟的其他資訊視窗
        if (infowindow) infowindow.close();
        if (userInfoWindow) userInfoWindow.close();
        if (confirmationInfoWindow) confirmationInfoWindow.close();

        // 顯示載入中訊息
        infowindow.setContent(`<div><h4>${place.name}</h4><p>正在載入詳細資訊...</p></div>`);
        infowindow.open(map, marker);

        // 取得詳細資訊
        fetchPlaceDetails(place.place_id, marker);

        // 將地圖中心移至標記位置
        map.panTo(marker.getPosition());

        // 突出顯示對應的餐廳卡片
        highlightRestaurantCard(place.place_id);
    });

    currentRestaurantMarkers.push(marker); // 將標記添加到陣列中以便管理
}

// 突出顯示對應的餐廳卡片
function highlightRestaurantCard(placeId) {
    // 首先移除所有卡片的高亮顯示
    document.querySelectorAll('.restaurant-card').forEach(card => {
        card.classList.remove('border-primary', 'shadow');
    });

    // 尋找對應的卡片並高亮顯示
    const targetCard = document.querySelector(`.restaurant-card[data-place-id="${placeId}"]`);

    if (targetCard) {
        // 添加高亮樣式
        targetCard.classList.add('border-primary', 'shadow');

        // 滾動到卡片位置 (可選)
        targetCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

// 獲取地點詳細資訊 (需要額外 API 請求)
// 全域變數，用於保存詳細資訊
let currentPlaceDetails = null;

// 獲取地點詳細資訊 (需要額外 API 請求)
function fetchPlaceDetails(placeId, marker) {
    if (!placesService || !placeId) return;
    console.log("Fetching details for placeId:", placeId);

    const request = {
        placeId: placeId,
        fields: ['name', 'formatted_address', 'formatted_phone_number', 'website', 'rating', 'user_ratings_total', 'opening_hours', 'photos', 'reviews', 'geometry', 'url'] // 加入 reviews 欄位
    };

    placesService.getDetails(request, (placeDetails, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK && placeDetails) {
            console.log("Place details received:", placeDetails);
            // 保存詳細資訊以便在浮窗中使用
            currentPlaceDetails = placeDetails;
            
            // 建立地圖資訊視窗內容（較簡短的版本）
            let content = `<div class="info-window-content" style="max-width: 300px;">`;

            // 標題與基本資訊
            content += `<h4 style="margin-bottom: 8px;">${placeDetails.name}</h4>`;

            // 地址和聯絡資訊
            if (placeDetails.formatted_address) content += `<p style="margin: 5px 0;"><i class="fas fa-map-marker-alt"></i> ${placeDetails.formatted_address}</p>`;
            if (placeDetails.formatted_phone_number) content += `<p style="margin: 5px 0;"><i class="fas fa-phone"></i> <a href="tel:${placeDetails.formatted_phone_number}">${placeDetails.formatted_phone_number}</a></p>`;

            // 網站連結
            if (placeDetails.website) content += `<p style="margin: 5px 0;"><a href="${placeDetails.website}" target="_blank" style="color: #1a73e8;">訪問網站</a></p>`;

            // 評分和營業狀態
            let statusInfo = "";
            if (placeDetails.opening_hours) {
                statusInfo = placeDetails.opening_hours.isOpen() ?
                    '<span style="color: green; font-weight: bold;">營業中</span>' :
                    '<span style="color: red;">休息中</span>';
            }

            if (placeDetails.rating) {
                content += `<div style="margin-top: 10px; display: flex; align-items: center; justify-content: space-between;">
                    <div><strong>評分: ${placeDetails.rating}</strong> (${placeDetails.user_ratings_total || 0} 則評論)</div>
                    <div>${statusInfo}</div>
                </div>`;
            } else if (statusInfo) {
                content += `<div style="margin-top: 10px;">${statusInfo}</div>`;
            }

            // 照片區域
            if (placeDetails.photos && placeDetails.photos.length > 0) {
                const photoUrl = placeDetails.photos[0].getUrl({ maxWidth: 300, maxHeight: 200 });
                content += `<div style="margin-top: 15px; text-align: center;">
                    <img src="${photoUrl}" alt="${placeDetails.name}" style="max-width: 100%; max-height: 150px; object-fit: cover; border-radius: 4px;">
                </div>`;
            }

            // 查看評論按鈕
            if (placeDetails.reviews && placeDetails.reviews.length > 0) {
                content += `<div style="margin-top: 15px; text-align: center;">
                    <button id="viewReviewsBtn" style="background-color: #4CAF50; color: white; padding: 8px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 1em;">
                        查看 ${placeDetails.reviews.length} 則評論
                    </button>
                </div>`;
            }

            // 在 Google 地圖上查看連結
            if (placeDetails.url) {
                content += `<div style="margin-top: 15px; text-align: center;">
                    <a href="${placeDetails.url}" target="_blank" style="display: inline-block; background-color: #1a73e8; color: white; padding: 8px 12px; border-radius: 4px; text-decoration: none; font-size: 0.9em;">
                        在 Google 地圖上查看
                    </a>
                </div>`;
            }

            content += `</div>`;

            infowindow.setContent(content);
            // 確保視窗仍然附著在正確的標記上 (以防使用者在載入期間點擊其他標記)
            if (infowindow.getAnchor() === marker) {
                infowindow.open(map, marker);
                
                // 為查看評論按鈕添加事件監聽器
                google.maps.event.addListenerOnce(infowindow, 'domready', () => {
                    const viewReviewsBtn = document.getElementById('viewReviewsBtn');
                    if (viewReviewsBtn && placeDetails.reviews) {
                        viewReviewsBtn.addEventListener('click', () => {
                            showReviewsModal(placeDetails);
                        });
                    }
                });
            }

        } else {
            console.error("Place details request failed:", status);
            // 如果請求失敗，顯示基本資訊
            infowindow.setContent(`<div><h4>${marker.getTitle()}</h4><p>無法載入詳細資訊 (${status})</p></div>`);
            if (infowindow.getAnchor() === marker) {
                infowindow.open(map, marker);
            }
        }
    });
}

// 顯示評論浮窗
function showReviewsModal(placeDetails) {
    if (!placeDetails || !placeDetails.reviews) return;
    
    // 檢查是否已存在評論浮窗，如果存在則先移除
    const existingModal = document.getElementById('reviewsModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // 建立評論浮窗元素
    const modal = document.createElement('div');
    modal.id = 'reviewsModal';
    modal.classList.add('reviews-modal');
    
    // 建立評論浮窗內容
    let modalContent = `
        <div class="reviews-modal-content">
            <div class="reviews-modal-header">
                <h3 class="reviews-modal-title">${placeDetails.name} - 評論</h3>
                <button class="reviews-modal-close">&times;</button>
            </div>
            <div class="reviews-modal-body">`;
    
    // 評分和評論數量摘要
    if (placeDetails.rating) {
        modalContent += `
            <div class="reviews-summary">
                <div class="reviews-rating">
                    <span class="reviews-rating-value">${placeDetails.rating}</span>
                    <div class="reviews-rating-stars">
                        ${getStarsHTML(placeDetails.rating)}
                    </div>
                </div>
                <div class="reviews-count">${placeDetails.user_ratings_total || placeDetails.reviews.length} 則評論</div>
            </div>`;
    }
    
    // 評論列表
    modalContent += `<div class="reviews-list">`;
    placeDetails.reviews.forEach(review => {
        const reviewDate = new Date(review.time * 1000).toLocaleDateString();
        modalContent += `
            <div class="review-item">
                <div class="review-header">
                    <div class="review-author">${review.author_name}</div>
                    <div class="review-date">${reviewDate}</div>
                </div>
                <div class="review-rating">
                    ${getStarsHTML(review.rating)}
                    <span class="review-rating-value">${review.rating}</span>
                </div>
                <div class="review-text">${review.text}</div>
            </div>`;
    });
    
    modalContent += `</div>`; // 結束評論列表
    
    // 添加前往 Google Maps 查看更多評論的連結
    if (placeDetails.url) {
        modalContent += `
            <div class="reviews-more">
                <a href="${placeDetails.url}#reviews" target="_blank" class="reviews-more-link">
                    在 Google 地圖上查看更多評論
                </a>
            </div>`;
    }
    
    modalContent += `
            </div>
        </div>`;
    
    modal.innerHTML = modalContent;
    document.body.appendChild(modal);
    
    // 添加關閉按鈕事件
    const closeButton = modal.querySelector('.reviews-modal-close');
    if (closeButton) {
        closeButton.addEventListener('click', () => {
            modal.remove();
        });
    }
    
    // 點擊浮窗外部區域關閉浮窗
    modal.addEventListener('click', (event) => {
        if (event.target === modal) {
            modal.remove();
        }
    });
    
    // 添加鍵盤事件 (ESC 鍵關閉浮窗)
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && document.getElementById('reviewsModal')) {
            document.getElementById('reviewsModal').remove();
        }
    });
    
    // 浮窗顯示後滾動至頂部
    setTimeout(() => {
        modal.querySelector('.reviews-modal-body').scrollTop = 0;
    }, 100);
}

// 生成星星 HTML
function getStarsHTML(rating) {
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);
    
    let starsHTML = '';
    
    // 添加滿星
    for (let i = 0; i < fullStars; i++) {
        starsHTML += '<span class="star full-star">★</span>';
    }
    
    // 添加半星 (如果有)
    if (halfStar) {
        starsHTML += '<span class="star half-star">★</span>';
    }
    
    // 添加空星
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '<span class="star empty-star">☆</span>';
    }
    
    return starsHTML;
}


// 在結果面板中顯示餐廳列表（卡片式）
function displayResults(places) {
    const restaurantList = document.getElementById('restaurantList');
    if (!restaurantList) {
        console.error("Restaurant list element (#restaurantList) not found.");
        return;
    }

    places.forEach((place) => {
        const li = document.createElement('li');
        // 添加 col-md-4 讓一行顯示三張卡片
        li.classList.add('col-md-4', 'col-sm-6', 'mb-4');

        const cardDiv = document.createElement('div');
        cardDiv.classList.add('card', 'h-100', 'restaurant-card');
        cardDiv.setAttribute('data-place-id', place.place_id); // 儲存 place_id 方便後續操作

        // --- 建立卡片內容 ---
        let cardHTML = `<div class="card-body">`;

        // 1. 卡片頂部 - 照片區域
        if (place.photos && place.photos.length > 0) {
            const photoUrl = place.photos[0].getUrl({ maxWidth: 500 });
            cardHTML += `<div class="card-img-container mb-3">
                <img src="${photoUrl}" class="card-img-top rounded" alt="${place.name || '餐廳照片'}" style="max-height: 180px; object-fit: cover; width: 100%;">
            </div>`;
        }

        // 2. 餐廳名稱
        cardHTML += `<h5 class="card-title">${place.name || '未命名餐廳'}</h5>`;

        // 3. 地址
        const address = place.vicinity || place.formatted_address || '無地址資訊';
        cardHTML += `<p class="card-text text-muted mb-2">${address}</p>`;

        // 4. 評分與評論數 (可點擊查看評論)
        if (place.rating) {
            cardHTML += `<div class="mb-2">
                <div class="d-flex align-items-center mb-1">
                    <span class="me-2">評分:</span>
                    <span class="badge bg-warning text-dark me-1">${place.rating}</span>
                </div>
                <button class="btn btn-sm btn-outline-secondary comments-btn w-100" data-place-id="${place.place_id}">
                    <i class="fas fa-comments me-1"></i>查看 ${place.user_ratings_total || 0} 則評論
                </button>
            </div>`;
        }

        // 5. 營業狀態
        if (place.opening_hours) {
            cardHTML += `<div class="mb-2">
                <span class="badge ${place.opening_hours.open_now ? 'bg-success' : 'bg-danger'}">
                    ${place.opening_hours.open_now ? '營業中' : '休息中'}
                </span>
            </div>`;
        }

        // 6. 查看詳情按鈕
        cardHTML += `<div class="mt-3">
            <button class="btn btn-sm btn-outline-primary view-details-btn" data-place-id="${place.place_id}">查看詳情</button>
        </div>`;

        cardHTML += `</div>`; // 結束 card-body

        li.innerHTML = cardHTML;

        // 添加查看詳情按鈕點擊事件
        li.querySelector('.view-details-btn').addEventListener('click', (e) => {
            e.stopPropagation(); // 防止事件冒泡到卡片

            // 尋找對應的標記並觸發點擊
            const marker = currentRestaurantMarkers.find(m => m.placeId === place.place_id);
            if (marker) {
                google.maps.event.trigger(marker, 'click');
                // 滾動到地圖頂部
                document.getElementById('map').scrollIntoView({ behavior: 'smooth' });
            } else {
                console.warn("Marker not found for place:", place.name);
            }
        });

        // 添加查看評論按鈕事件
        const commentsBtn = li.querySelector('.comments-btn');
        if (commentsBtn) {
            commentsBtn.addEventListener('click', (e) => {
                e.stopPropagation(); // 防止事件冒泡到卡片
                
                const placeId = commentsBtn.getAttribute('data-place-id');
                if (!placeId) return;
                
                // 如果已存在詳細信息，直接顯示評論浮窗
                if (currentPlaceDetails && currentPlaceDetails.place_id === placeId && currentPlaceDetails.reviews) {
                    showReviewsModal(currentPlaceDetails);
                    return;
                }
                
                // 否則先獲取詳細信息
                showStatus("正在載入評論資訊...", "info");
                const request = {
                    placeId: placeId,
                    fields: ['name', 'rating', 'user_ratings_total', 'reviews', 'url', 'photos', 'place_id']
                };
                
                placesService.getDetails(request, (placeDetails, status) => {
                    hideStatus();
                    if (status === google.maps.places.PlacesServiceStatus.OK && placeDetails) {
                        currentPlaceDetails = placeDetails; // 保存供後續使用
                        showReviewsModal(placeDetails);
                    } else {
                        console.error("Failed to fetch place details for reviews:", status);
                        showError("無法載入評論資訊，請稍後再試");
                        setTimeout(hideError, 3000);
                    }
                });
            });
        }

        // 整個卡片的點擊事件 (可選，如果你希望整個卡片都可點擊)
        li.addEventListener('click', () => {
            li.querySelector('.view-details-btn').click();
        });

        restaurantList.appendChild(li);
    });

    console.log(`Added ${places.length} restaurant cards. Total: ${restaurantList.childElementCount}`);
}

// 載入更多結果
function loadMoreResults() {
    const loadMoreButton = document.getElementById('loadMoreButton');
    if (!searchPagination || !searchPagination.hasNextPage) {
        console.log("No more results to load or pagination object missing.");
        if (loadMoreButton) loadMoreButton.style.display = 'none';
        return;
    }

    console.log("Loading next page of results...");
    showStatus("正在載入更多結果...", "info");
    if (loadMoreButton) loadMoreButton.disabled = true; // Disable button during loading

    searchPagination.nextPage(); // Google Maps API 會自動處理回呼
    // 回呼函式 (在 nearbySearch 中定義) 會處理新載入的結果
    // 我們只需要確保回呼函式能正確附加結果而不是替換它們
    // 且回呼函式會更新 'Load More' 按鈕的狀態

    // nearbySearch 的回呼會處理狀態更新和按鈕重新啟用
     // Add a timeout to re-enable the button just in case the callback fails silently
     setTimeout(() => {
          if (loadMoreButton && loadMoreButton.disabled) {
               console.warn("Re-enabling 'Load More' button after timeout.");
               loadMoreButton.disabled = false;
               hideStatus();
          }
     }, 8000); // 8 second timeout
}

// 調整地圖邊界以包含所有結果和使用者位置
function adjustMapBounds(places) {
     if (!map || (!userMarker && places.length === 0)) return; // Need map and at least one point

     const bounds = new google.maps.LatLngBounds();

     // 包含使用者位置
     if (userMarker) {
          bounds.extend(userMarker.getPosition());
     } else if (currentSearchLocation) {
          bounds.extend(new google.maps.LatLng(currentSearchLocation.lat, currentSearchLocation.lng));
     }

     // 包含所有餐廳位置
     places.forEach(place => {
          if (place.geometry && place.geometry.location) {
               bounds.extend(place.geometry.location);
          }
     });

     // 如果只有一個點 (例如只有使用者位置)，避免過度縮放
     if (bounds.getNorthEast().equals(bounds.getSouthWest())) {
          console.log("Adjusting bounds for single point.");
          map.setCenter(bounds.getCenter());
          map.setZoom(16); // Set a reasonable zoom level for a single point
     } else {
          console.log("Fitting map to bounds:", bounds.toJSON());
          map.fitBounds(bounds, 100); // Add padding (e.g., 100px)
     }
}


// --- End Restaurant Search and Display ---

// ********** END OF PART 3 **********