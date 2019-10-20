package com.leyou.upload.web;


import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UpLoadController {


    @Autowired
    private UploadService uploadService;
    /**
     * 上传图片
     * @param file
     * @return
     */
    @RequestMapping("/image")
    public ResponseEntity<String> upLoad(@RequestParam("file")MultipartFile file) {
        return ResponseEntity.ok(uploadService.upLoad(file));
    }
}