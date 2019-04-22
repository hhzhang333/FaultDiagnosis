package cn.edu.seu.diagnosis.client.service;

import cn.edu.seu.diagnosis.common.DataCollectorUtils;
import cn.edu.seu.diagnosis.config.DataCollectorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by hhzhang on 2018/12/7.
 */
@Service
public class FileStorageService {

    @Autowired
    private DataCollectorConfig dataCollectorConfig;

    @Autowired
    private DataCollectorUtils dataCollectorUtils;

    @Autowired
    private ZipUtils zipUtils;

    public String storeFile(MultipartFile file) throws Exception {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new Exception("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = dataCollectorUtils.resolveFileUrl(
                    dataCollectorConfig.dispatchDirectory,
                    dataCollectorConfig.sensorModelName
            );
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new Exception("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) throws Exception {
        try {
//            zipUtils.zipDirectory(dataCollectorConfig.sensorDirectory, fileName);
            Path path = dataCollectorUtils.resolveFileUrl(dataCollectorConfig.sensorDirectory, fileName);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new Exception("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new Exception("File not found " + fileName, ex);
        }
    }
}
