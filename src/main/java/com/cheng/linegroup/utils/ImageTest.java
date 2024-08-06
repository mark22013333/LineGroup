package com.cheng.linegroup.utils;

import com.jhlabs.image.GaussianFilter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


import java.io.IOException;

/**
 * @author Cheng
 * @since 2024/7/28 11:02
 **/
public class ImageTest {
    public static BufferedImage gaussianFilter(BufferedImage srcImage) {
        GaussianFilter filter = new GaussianFilter();
        // 數字越大越模糊
        filter.setRadius(40.0F);
        BufferedImage dstImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_USHORT_555_RGB);
        filter.filter(srcImage, dstImage);
        return dstImage;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File("/Users/cheng/Downloads/pic/史努比耀西遊台灣.png"));
        BufferedImage gaussianFilter = gaussianFilter(originalImage);
        ImageIO.write(gaussianFilter, "jpg", new File("/Users/cheng/Downloads/pic/output_blurred_image.jpg"));
    }
}
