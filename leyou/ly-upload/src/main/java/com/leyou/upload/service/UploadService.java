package com.leyou.upload.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    String upLoad(MultipartFile file);
}
