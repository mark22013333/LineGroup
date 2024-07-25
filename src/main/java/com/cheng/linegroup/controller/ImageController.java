package com.cheng.linegroup.controller;

import jakarta.servlet.http.HttpServletResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Cheng
 * @since 2024/7/14 13:16
 **/
@RestController
@RequestMapping("img")
public class ImageController {

    @Value("${image.base-path}")
    private String basePath;

    private final ResourceLoader resourceLoader;

    public ImageController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
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
}
