package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.contants.Sign;
import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.services.LineService;
import com.cheng.linegroup.services.dto.LineMessage;
import com.cheng.linegroup.utils.JasyptUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Cheng
 * @since 2024/7/14 13:16
 **/
@Slf4j
@RestController
@RequestMapping("img")
@RequiredArgsConstructor
public class ImageController {

    @Value("${image.base-path}")
    private String basePath;
    @Value("${image.domain}")
    private String domain;

    @Getter
    private static final List<String> IMAGES = new ArrayList<>();

    private final Line line;
    private final ResourceLoader resourceLoader;
    private final LineService lineService;

    @PostConstruct
    public void init() {
        execScript();
        log.info("IMAGES SIZE: {}", IMAGES);
    }

    @GetMapping("resetImages/{key}")
    public ResponseEntity<?> resetImages(@PathVariable(value = "key") String key) {
        try {
            if (JasyptUtils.KEY.equals(key)) {
                execScript();
            } else {
                throw BizException.error("密碼錯誤");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("IMAGES 重置成功");
    }

    @GetMapping("send/myself")
    public ResponseEntity<?> getImages(HttpServletResponse response) {
        if (IMAGES.isEmpty()) {
            log.warn("IMAGES 列表為空，無法選擇隨機圖片");
            return ResponseEntity.badRequest().body("IMAGES 列表為空，無法選擇隨機圖片");
        }

        String selfUid = line.getMessage().getSelfUid();
        String imgName = IMAGES.get(ThreadLocalRandom.current().nextInt(IMAGES.size()));
        imgName = domain.concat(imgName);
        log.info("imgName: {}", imgName);

        LineMessage lineMessage = LineMessage.builder()
                .uid(selfUid)
                .messages(Collections.singletonList(
                        LineMessage.Message.builder()
                                .type(MessageType.image)
                                .originalContentUrl(imgName)
                                .previewImageUrl(imgName)
                                .build()
                ))
                .build();

        lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_PUSH);
        return ResponseEntity.ok(imgName);
    }

    @GetMapping("/{imgName}/{width}/{height}")
    public void getImage(HttpServletResponse response,
                         @PathVariable String imgName,
                         @PathVariable int width,
                         @PathVariable int height) throws IOException {

        Resource resource = resourceLoader.getResource("file:" + basePath + "/" + imgName + ".jpg");
        BufferedImage img = ImageIO.read(resource.getInputStream());

        BufferedImage outputImage = Thumbnails.of(img)
                .size(width, height)
                .asBufferedImage();
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        ImageIO.write(outputImage, "jpeg", response.getOutputStream());
    }

    private static void execScript() {
        String scriptPath = "/images/getImagePath.sh";
        ProcessBuilder processBuilder = new ProcessBuilder(scriptPath);

        try {
            // 執行 shell 腳本
            Process process = processBuilder.start();
            IMAGES.clear();

            // 讀取腳本執行結果
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    IMAGES.add(URLEncoder.encode(line, StandardCharsets.UTF_8)
                            .replace(Sign.ENCODED_SLASH, Sign.SLASH)
                            .replace(Sign.PLUS, Sign.ENCODED_SPACE));
                }
            }

            // 讀取錯誤輸出
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("Script error output: {}", errorLine);
                }
            }

            // 確認腳本執行成功
            int exitCode = process.waitFor();
            log.info("Exited with error code : {}", exitCode);

        } catch (IOException e) {
            log.error("IO Exception occurred: {}", ExceptionUtils.getStackTrace(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Script execution interrupted: {}", ExceptionUtils.getStackTrace(e));
        }

    }

}
