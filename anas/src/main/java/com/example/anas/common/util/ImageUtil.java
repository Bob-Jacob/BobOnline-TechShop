package com.example.anas.common.util;

import java.util.Arrays;
import java.util.List;

public class ImageUtil {
    private static final List<String> PLACEHOLDER_IMAGES = Arrays.asList(
            "https://via.placeholder.com/400x300/6366f1/ffffff?text=Digital+Product",
            "https://via.placeholder.com/400x300/8b5cf6/ffffff?text=Product",
            "https://via.placeholder.com/400x300/10b981/ffffff?text=Item"
    );

    public static String getPlaceholderImage() {
        return PLACEHOLDER_IMAGES.get(0);
    }

    public static String getPlaceholderImage(int index) {
        return PLACEHOLDER_IMAGES.get(index % PLACEHOLDER_IMAGES.size());
    }

    public static boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        return lowerUrl.startsWith("http://") || 
               lowerUrl.startsWith("https://") || 
               lowerUrl.startsWith("/");
    }
}
