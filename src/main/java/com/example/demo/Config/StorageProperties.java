package com.example.demo.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Thư mục gốc để lưu file upload.
     */
    private String location = "uploads"; // mặc định thư mục uploads ngoài project

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
