package cn.edu.seu.diagnosis.client.controller;

import cn.edu.seu.diagnosis.client.service.FileStorageService;
import cn.edu.seu.diagnosis.client.service.UploadFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by hhzhang on 2018/12/7.
 */
@Slf4j
@RestController
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("${sensorModelDispatch}")
    public UploadFileResponse uploadFile(@RequestParam("model") MultipartFile model) {
        try {
            String fileName = fileStorageService.storeFile(model);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/downloadFile/")
                    .path(fileName)
                    .toUriString();

            return new UploadFileResponse(fileName, fileDownloadUri, model.getContentType(), model.getSize());
        } catch (Exception ex) {
            log.error("Exception in uploadFileResponse, ex is: ", ex);
            return new UploadFileResponse(null, null, null, 0);
        }
    }

    @GetMapping("${downloadUrl}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        try {
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            // Fallback to the default content type if type could not be determined
            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            log.error("Exception in downloadFile, ex: ", ex);
            return ResponseEntity.notFound().build();
        }

    }
}
