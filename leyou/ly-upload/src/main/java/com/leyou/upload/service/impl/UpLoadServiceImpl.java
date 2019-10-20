package com.leyou.upload.service.impl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UpLoadProperties;
import com.leyou.upload.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
@EnableConfigurationProperties(UpLoadProperties.class)
public class UpLoadServiceImpl implements UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UpLoadProperties prop;

    @Override
    public String upLoad(MultipartFile file) {
        try {
            //校验文件类型
            String contentType = file.getContentType();
            if (!prop.getAllowTypes().contains(contentType)) {
                //文件类型不匹配
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件内容
            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read == null) {
                //文件类型不匹配
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            /*
            //准备目标路径
            File dust = new File("D:\\IdeaProjects2\\upload_leyou",file.getOriginalFilename());
            //保存文件到本地
            file.transferTo(dust);
             */
            //上传到FastDFS
            //获取扩展名后缀
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            //返回路径
            return prop.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            //上传失败记录日志
            log.error("文件上传失败",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
