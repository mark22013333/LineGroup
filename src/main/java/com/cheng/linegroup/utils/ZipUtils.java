
package com.cheng.linegroup.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 壓縮檔案工具類
 * 提供將檔案壓縮為 ZIP 的功能
 *
 * @author Cheng
 * @since 2025-06-29 15:58
 */
@Slf4j
public class ZipUtils {

    /**
     * 預設的檔案大小限制 (10GB)
     */
    private static final long DEFAULT_SIZE_LIMIT = 10L * 1024 * 1024 * 1024;

    /**
     * 將單一檔案壓縮為 ZIP
     *
     * @param sourceFilePath 來源檔案路徑
     * @param zipFilePath    目標 ZIP 檔案路徑
     * @throws IOException 若壓縮過程發生錯誤
     */
    public static void compressToZip(String sourceFilePath, String zipFilePath) throws IOException {
        Path sourcePath = Paths.get(sourceFilePath);
        String fileName = sourcePath.getFileName().toString();

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);

            Files.copy(sourcePath, zos);
            zos.closeEntry();
        }

        log.info("ZIP 檔案已建立: {}", zipFilePath);
    }

    /**
     * 將多個檔案壓縮為 ZIP
     *
     * @param sourceFilePaths 來源檔案路徑列表
     * @param zipFilePath     目標 ZIP 檔案路徑
     * @throws IOException 若壓縮過程發生錯誤
     */
    public static void compressFilesToZip(List<String> sourceFilePaths, String zipFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : sourceFilePaths) {
                Path sourcePath = Paths.get(filePath);
                String fileName = sourcePath.getFileName().toString();

                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);

                Files.copy(sourcePath, zos);
                zos.closeEntry();
            }
        }

        log.info("多檔案 ZIP 壓縮完成: {}", zipFilePath);
    }

    /**
     * 將多個檔案壓縮為 ZIP，支援檔案大小限制和自動分割
     *
     * @param sourceFilePaths 來源檔案路徑列表
     * @param baseZipFilePath ZIP 檔案路徑
     * @param sizeLimitBytes  檔案大小限制(位元組)
     * @return 建立的 ZIP 檔案路徑列表
     * @throws IOException 若壓縮過程發生錯誤
     */
    public static List<String> compressFilesToZipWithSizeLimit(List<String> sourceFilePaths,
                                                               String baseZipFilePath,
                                                               long sizeLimitBytes) throws IOException {
        List<String> zipFilePaths = new ArrayList<>();

        // 檢查每個檔案的大小
        List<FileInfo> fileInfos = new ArrayList<>();
        for (String filePath : sourceFilePaths) {
            File file = new File(filePath);
            if (file.exists()) {
                fileInfos.add(new FileInfo(filePath, file.length()));
            }
        }

        // 如果沒有檔案，返回空列表
        if (fileInfos.isEmpty()) {
            log.warn("沒有找到任何檔案進行壓縮");
            return zipFilePaths;
        }

        // 按檔案大小進行分組
        List<List<FileInfo>> fileGroups = groupFilesBySize(fileInfos, sizeLimitBytes);

        // 為每個組建立 ZIP 檔案
        for (int i = 0; i < fileGroups.size(); i++) {
            String zipFilePath = generateZipFilePath(baseZipFilePath, i, fileGroups.size());
            List<FileInfo> group = fileGroups.get(i);

            try (FileOutputStream fos = new FileOutputStream(zipFilePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                for (FileInfo fileInfo : group) {
                    Path sourcePath = Paths.get(fileInfo.filePath());
                    String fileName = sourcePath.getFileName().toString();

                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);

                    Files.copy(sourcePath, zos);
                    zos.closeEntry();
                }
            }

            zipFilePaths.add(zipFilePath);
            log.info("建立 ZIP 檔案 ({}/{}): {}, 包含 {} 個檔案",
                    i + 1, fileGroups.size(), zipFilePath, group.size());
        }

        return zipFilePaths;
    }

    /**
     * 使用預設大小限制壓縮檔案
     */
    public static List<String> compressFilesToZipWithSizeLimit(List<String> sourceFilePaths,
                                                               String baseZipFilePath) throws IOException {
        return compressFilesToZipWithSizeLimit(sourceFilePaths, baseZipFilePath, DEFAULT_SIZE_LIMIT);
    }

    /**
     * 將檔案按大小分組
     */
    private static List<List<FileInfo>> groupFilesBySize(List<FileInfo> fileInfos, long sizeLimitBytes) {
        List<List<FileInfo>> groups = new ArrayList<>();
        List<FileInfo> currentGroup = new ArrayList<>();
        long currentGroupSize = 0;

        for (FileInfo fileInfo : fileInfos) {
            // 如果單個檔案就超過限制，單獨放一組
            if (fileInfo.size() > sizeLimitBytes) {
                // 如果當前組不為空，先加入結果
                if (!currentGroup.isEmpty()) {
                    groups.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                    currentGroupSize = 0;
                }

                // 單獨建立一個組
                List<FileInfo> singleFileGroup = new ArrayList<>();
                singleFileGroup.add(fileInfo);
                groups.add(singleFileGroup);

                log.warn("檔案 {} 大小 {} 超過限制 {}，將單獨壓縮",
                        fileInfo.filePath(),
                        formatFileSize(fileInfo.size()),
                        formatFileSize(sizeLimitBytes));
                continue;
            }

            // 如果加入當前檔案會超過限制，則開始新組
            if (currentGroupSize + fileInfo.size() > sizeLimitBytes && !currentGroup.isEmpty()) {
                groups.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroupSize = 0;
            }

            currentGroup.add(fileInfo);
            currentGroupSize += fileInfo.size();
        }

        // 加入最後一組
        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }

        return groups;
    }

    /**
     * 產生 ZIP 檔案路徑
     */
    private static String generateZipFilePath(String baseZipFilePath, int index, int totalCount) {
        if (totalCount == 1) {
            return baseZipFilePath;
        }

        // 取檔案名稱和副檔名
        String fileName = baseZipFilePath.substring(0, baseZipFilePath.lastIndexOf('.'));
        String extension = baseZipFilePath.substring(baseZipFilePath.lastIndexOf('.'));

        return String.format("%s_part%02d%s", fileName, index + 1, extension);
    }

    /**
     * 格式化檔案大小顯示
     */
    private static String formatFileSize(long size) {
        if (size >= 1024 * 1024 * 1024) {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        } else if (size >= 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else if (size >= 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return size + " bytes";
        }
    }

    /**
     * 將整個目錄壓縮為 ZIP
     *
     * @param sourceDir   來源目錄
     * @param zipFilePath 目標 ZIP 檔案路徑
     * @throws IOException 若壓縮過程發生錯誤
     */
    public static void compressDirectoryToZip(String sourceDir, String zipFilePath) throws IOException {
        Path sourceDirPath = Paths.get(sourceDir);

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos);
             var pathStream = Files.walk(sourceDirPath)) {

            pathStream
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        String entryName = sourceDirPath.relativize(path).toString();
                        // 在 Windows 系統上統一使用 '/' 作為分隔符
                        entryName = entryName.replace('\\', '/');

                        ZipEntry zipEntry = new ZipEntry(entryName);
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            log.error("壓縮檔案時發生錯誤: {}", path, e);
                            throw new RuntimeException("壓縮檔案失敗: " + path, e);
                        }
                    });
        }

        log.info("目錄壓縮完成: {}", zipFilePath);
    }

    /**
     * 檔案資訊類別
     */
    private record FileInfo(String filePath, long size) {

    }

    /**
     * 測試用主方法
     */
    public static void main(String[] args) {
        System.out.println("=== ZipUtils 測試開始 ===");

        try {
            // 建立測試目錄
            String testDir = "test_zip_utils";
            File testDirFile = new File(testDir);
            if (!testDirFile.exists()) {
                testDirFile.mkdirs();
            }

            // 測試1: 建立測試檔案
            System.out.println("\n1. 建立測試檔案...");
            List<String> testFilePaths = createTestFiles(testDir);
            System.out.println("建立了 " + testFilePaths.size() + " 個測試檔案");

            // 測試2: 單一檔案壓縮
            System.out.println("\n2. 測試單一檔案壓縮...");
            String singleZipPath = testDir + "/single_file_test.zip";
            compressToZip(testFilePaths.get(0), singleZipPath);
            System.out.println("單一檔案壓縮完成: " + singleZipPath);

            // 測試3: 多檔案壓縮
            System.out.println("\n3. 測試多檔案壓縮...");
            String multiZipPath = testDir + "/multi_file_test.zip";
            compressFilesToZip(testFilePaths, multiZipPath);
            System.out.println("多檔案壓縮完成: " + multiZipPath);

            // 測試4: 帶大小限制的壓縮（使用較小的限制進行測試）
            System.out.println("\n4. 測試帶大小限制的壓縮（限制 5KB）...");
            String limitedZipBasePath = testDir + "/limited_test.zip";
            long testSizeLimit = 5 * 1024; // 5KB 限制用於測試
            List<String> limitedZipPaths = compressFilesToZipWithSizeLimit(testFilePaths, limitedZipBasePath, testSizeLimit);
            System.out.println("建立了 " + limitedZipPaths.size() + " 個分割 ZIP 檔案:");
            for (String zipPath : limitedZipPaths) {
                File zipFile = new File(zipPath);
                System.out.println("  - " + zipPath + " (大小: " + formatFileSize(zipFile.length()) + ")");
            }

            // 測試5: 預設大小限制的壓縮
            System.out.println("\n5. 測試預設大小限制的壓縮（10GB 限制）...");
            String defaultLimitZipPath = testDir + "/default_limit_test.zip";
            List<String> defaultLimitZipPaths = compressFilesToZipWithSizeLimit(testFilePaths, defaultLimitZipPath);
            System.out.println("建立了 " + defaultLimitZipPaths.size() + " 個 ZIP 檔案 (預設限制)");

            // 測試6: 目錄壓縮
            System.out.println("\n6. 測試目錄壓縮...");
            String dirZipPath = testDir + "_directory.zip";
            compressDirectoryToZip(testDir, dirZipPath);
            System.out.println("目錄壓縮完成: " + dirZipPath);

            // 測試7: 驗證檔案分組邏輯
            System.out.println("\n7. 測試檔案分組邏輯...");
            testFileGrouping();

            // 測試8: 驗證檔案路徑產生邏輯
            System.out.println("\n8. 測試檔案路徑產生邏輯...");
            testZipFilePathGeneration();

            // 測試9: 驗證檔案大小格式化
            System.out.println("\n9. 測試檔案大小格式化...");
            testFileSizeFormatting();

            System.out.println("\n=== 所有測試完成 ===");
            System.out.println("注意: 請手動檢查產生的 ZIP 檔案是否正確");

        } catch (Exception e) {
            System.err.println("測試過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 建立測試檔案
     */
    private static List<String> createTestFiles(String testDir) throws IOException {
        List<String> filePaths = new ArrayList<>();

        // 建立不同大小的測試檔案
        String[] testContents = {
                "小檔案內容 - 這是一個小檔案",
                "中等檔案內容 - ".repeat(100) + "這是一個中等大小的檔案",
                "大檔案內容 - ".repeat(500) + "這是一個較大的檔案",
                "超大檔案內容 - ".repeat(1000) + "這是一個很大的檔案"
        };

        for (int i = 0; i < testContents.length; i++) {
            String fileName = String.format("test_file_%d.txt", i + 1);
            String filePath = testDir + "/" + fileName;

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(testContents[i]);
            }

            filePaths.add(filePath);
            File file = new File(filePath);
            System.out.println("  建立檔案: " + fileName + " (大小: " + formatFileSize(file.length()) + ")");
        }

        return filePaths;
    }

    /**
     * 測試檔案分組邏輯
     */
    private static void testFileGrouping() {
        System.out.println("測試檔案分組邏輯:");

        List<FileInfo> testFiles = List.of(
                new FileInfo("file1.txt", 1024),      // 1KB
                new FileInfo("file2.txt", 2048),      // 2KB
                new FileInfo("file3.txt", 3072),      // 3KB
                new FileInfo("file4.txt", 6144),      // 6KB (超過 5KB 限制)
                new FileInfo("file5.txt", 1024)       // 1KB
        );

        long testLimit = 5 * 1024; // 5KB 限制
        List<List<FileInfo>> groups = groupFilesBySize(testFiles, testLimit);

        System.out.println("  檔案分組結果 (限制: " + formatFileSize(testLimit) + "):");
        for (int i = 0; i < groups.size(); i++) {
            List<FileInfo> group = groups.get(i);
            long groupSize = group.stream().mapToLong(FileInfo::size).sum();
            System.out.println("    組 " + (i + 1) + ": " + group.size() + " 個檔案, 總大小: " + formatFileSize(groupSize));
            for (FileInfo file : group) {
                System.out.println("      - " + file.filePath() + " (" + formatFileSize(file.size()) + ")");
            }
        }
    }

    /**
     * 測試 ZIP 檔案路徑產生
     */
    private static void testZipFilePathGeneration() {
        System.out.println("測試 ZIP 檔案路徑產生:");

        String baseZipPath = "/test/data_export.zip";

        // 測試單一檔案
        String singleFile = generateZipFilePath(baseZipPath, 0, 1);
        System.out.println("  單一檔案: " + singleFile);

        // 測試多檔案
        for (int i = 0; i < 3; i++) {
            String multiFile = generateZipFilePath(baseZipPath, i, 3);
            System.out.println("  多檔案 " + (i + 1) + ": " + multiFile);
        }
    }

    /**
     * 測試檔案大小格式化
     */
    private static void testFileSizeFormatting() {
        System.out.println("測試檔案大小格式化:");

        long[] testSizes = {
                100,                          // bytes
                1024,                         // 1KB
                1024 * 1024,                  // 1MB
                1024 * 1024 * 1024,           // 1GB
                10L * 1024 * 1024 * 1024      // 10GB
        };

        for (long size : testSizes) {
            System.out.println("  " + size + " bytes = " + formatFileSize(size));
        }
    }
}