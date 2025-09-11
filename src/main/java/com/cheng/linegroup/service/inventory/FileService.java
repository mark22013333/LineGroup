package com.cheng.linegroup.service.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 檔案服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${inventory.upload.upload-path:/tmp/inventory/uploads}")
    private String uploadPath;

    @Value("${inventory.report.export-path:/tmp/inventory/reports}")
    private String reportPath;

    @Value("${inventory.upload.max-file-size:5242880}")
    private long maxFileSize;

    @Value("${inventory.upload.allowed-types:jpg,jpeg,png,gif,pdf,xlsx,xls}")
    private String allowedTypes;

    // 暫存檔案資訊（實際專案中應使用資料庫或 Redis）
    private final ConcurrentHashMap<String, FileInfo> tempFiles = new ConcurrentHashMap<>();

    /**
     * 上傳檔案
     */
    public String uploadFile(MultipartFile file, String category) throws IOException {
        validateFile(file);
        
        // 建立目錄
        Path uploadDir = Paths.get(uploadPath, category);
        Files.createDirectories(uploadDir);
        
        // 產生檔案名稱
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = generateFilename() + "." + fileExtension;
        
        // 儲存檔案
        Path targetPath = uploadDir.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 記錄檔案資訊
        String fileId = UUID.randomUUID().toString();
        FileInfo fileInfo = FileInfo.builder()
                .fileId(fileId)
                .originalName(originalFilename)
                .fileName(newFilename)
                .filePath(targetPath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .category(category)
                .uploadTime(LocalDateTime.now())
                .build();
        
        tempFiles.put(fileId, fileInfo);
        
        log.info("檔案上傳成功: {}", fileInfo);
        return fileId;
    }

    /**
     * 儲存報表檔案
     */
    public String saveReportFile(byte[] data, String fileName, String contentType) throws IOException {
        // 建立目錄
        Path reportDir = Paths.get(reportPath);
        Files.createDirectories(reportDir);
        
        // 產生檔案名稱
        String newFilename = generateFilename() + "_" + fileName;
        Path targetPath = reportDir.resolve(newFilename);
        
        // 儲存檔案
        Files.write(targetPath, data);
        
        // 記錄檔案資訊
        String fileId = UUID.randomUUID().toString();
        FileInfo fileInfo = FileInfo.builder()
                .fileId(fileId)
                .originalName(fileName)
                .fileName(newFilename)
                .filePath(targetPath.toString())
                .fileSize((long) data.length)
                .contentType(contentType)
                .category("report")
                .uploadTime(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusHours(24)) // 報表檔案24小時後過期
                .build();
        
        tempFiles.put(fileId, fileInfo);
        
        log.info("報表檔案儲存成功: {}", fileInfo);
        return fileId;
    }

    /**
     * 下載檔案
     */
    public Resource downloadFile(String fileId) throws IOException {
        FileInfo fileInfo = tempFiles.get(fileId);
        if (fileInfo == null) {
            throw new IllegalArgumentException("檔案不存在: " + fileId);
        }
        
        // 檢查檔案是否過期
        if (fileInfo.getExpiryTime() != null && LocalDateTime.now().isAfter(fileInfo.getExpiryTime())) {
            tempFiles.remove(fileId);
            deletePhysicalFile(fileInfo.getFilePath());
            throw new IllegalArgumentException("檔案已過期: " + fileId);
        }
        
        Path filePath = Paths.get(fileInfo.getFilePath());
        if (!Files.exists(filePath)) {
            tempFiles.remove(fileId);
            throw new IllegalArgumentException("實體檔案不存在: " + fileId);
        }
        
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("檔案無法讀取: " + fileId);
            }
        } catch (MalformedURLException e) {
            throw new IOException("檔案路徑錯誤: " + fileId, e);
        }
    }

    /**
     * 取得檔案資訊
     */
    public FileInfo getFileInfo(String fileId) {
        return tempFiles.get(fileId);
    }

    /**
     * 刪除檔案
     */
    public boolean deleteFile(String fileId) {
        FileInfo fileInfo = tempFiles.remove(fileId);
        if (fileInfo != null) {
            return deletePhysicalFile(fileInfo.getFilePath());
        }
        return false;
    }

    /**
     * 清理過期檔案
     */
    public void cleanupExpiredFiles() {
        LocalDateTime now = LocalDateTime.now();
        tempFiles.entrySet().removeIf(entry -> {
            FileInfo fileInfo = entry.getValue();
            if (fileInfo.getExpiryTime() != null && now.isAfter(fileInfo.getExpiryTime())) {
                deletePhysicalFile(fileInfo.getFilePath());
                log.info("清理過期檔案: {}", fileInfo.getFileName());
                return true;
            }
            return false;
        });
    }

    // ==================== 私有方法 ====================

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("檔案不能為空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("檔案大小超過限制: " + maxFileSize + " bytes");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("檔案名稱不能為空");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("不支援的檔案類型: " + extension);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String generateFilename() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
               "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean deletePhysicalFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("刪除實體檔案失敗: {}", filePath, e);
            return false;
        }
    }

    /**
     * 檔案資訊類別
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FileInfo {
        private String fileId;
        private String originalName;
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String contentType;
        private String category;
        private LocalDateTime uploadTime;
        private LocalDateTime expiryTime;
    }
}
