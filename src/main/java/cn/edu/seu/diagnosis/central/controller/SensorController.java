package cn.edu.seu.diagnosis.central.controller;

import cn.edu.seu.diagnosis.common.DataCollectorUtils;
import cn.edu.seu.diagnosis.config.CommunicationConfig;
import cn.edu.seu.diagnosis.config.DataCollectorConfig;
import cn.edu.seu.diagnosis.model.CommonModelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import weka.classifiers.Classifier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hhzhang on 2018/12/6.
 */
@Slf4j
@Controller
public class SensorController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommunicationConfig communicationConfig;

    @Autowired
    private DataCollectorUtils dataCollectorUtils;

    @Autowired
    private DataCollectorConfig dataCollectorConfig;

    @Autowired
    private CommonModelService commonModelService;

    @RequestMapping(value = "${startCollectSensorDataUrl}/{range}", method = RequestMethod.GET)
    @ResponseBody
    public Boolean startCollectSensorData(@PathVariable int range) {
        try {
            List<String> clients = communicationConfig.clients;
            for (String client: clients) {
                String sensorUrl = CommunicationConfig.generateUrl(client, communicationConfig.sensorDataStartUrl + "/" + range);
                ResponseEntity<Void> responseEntity = restTemplate.getForEntity(sensorUrl, Void.class);
                if (responseEntity.getStatusCodeValue() != 200)
                    throw new Exception("client task: collect sensor data failed");
            }
            return true;
        } catch (Exception ex) {
            log.error("Exception in startCollectNodeData, ex: ", ex);
            return false;
        }
    }

    @RequestMapping(value = "${stopCollectSensorDataUrl}", method = RequestMethod.GET)
    @ResponseBody
    public boolean stopCollectSensorData() {
        try {
            List<String> clients = communicationConfig.clients;
            for (String client: clients) {
                String sensorUrl = CommunicationConfig.generateUrl(client, communicationConfig.sensorDataStopUrl);
                ResponseEntity<Void> responseEntity = restTemplate.getForEntity(sensorUrl, Void.class);
                if (responseEntity.getStatusCodeValue() != 200)
                    throw new Exception("client task: stop collect sensor data failed");
            }
            return true;
        } catch (Exception ex) {
            log.error("Exception in stopCollectSensorData, ex: ", ex);
            return false;
        }
    }

    @RequestMapping(value = "${startMonitorDCUrl}/{range}", method = RequestMethod.GET)
    @ResponseBody
    public boolean startMonitorSystem(@PathVariable int range) {
        try {
            List<String> clients = communicationConfig.clients;
            for (String client : clients) {
                String monitorUrl = CommunicationConfig.generateUrl(client, communicationConfig.monitor + "/" + range);
                restTemplate.getForEntity(monitorUrl, Void.class);
            }

            System.out.println("start monitor");

            return true;
        } catch (Exception ex) {
            log.error("Exception in startMonitorSystem, ex: ", ex);
            return false;
        }
    }

    @RequestMapping(value = "${stopMonitorDCUrl}", method = RequestMethod.GET)
    @ResponseBody
    public boolean stopMonitorSystem() {
        try {
            List<String> clients = communicationConfig.clients;
            for (String client : clients) {
//                String stopUrl = CommunicationConfig.generateUrl(client, communicationConfig.stopMonitor);
                String stopUrl = CommunicationConfig.generateUrl("127.0.0.1", communicationConfig.stopMonitor);
                ResponseEntity<Void> responseEntity = restTemplate.getForEntity(stopUrl, Void.class);
                if (responseEntity.getStatusCodeValue() != 200)
                    throw new Exception("stop client: " + client + "monitor system failed");
            }
            return true;
        } catch (Exception ex) {
            log.error("Exception in stopMonitorSystem, ex: ", ex);
            return false;
        }
    }

    @RequestMapping(value = "${collectDataUrl}", method = RequestMethod.GET)
    @ResponseBody
    public boolean downloadData() {
        try {
            List<String> clients = communicationConfig.clients;
            File file = new File(dataCollectorConfig.sensorDataUpload);
            if (file.exists()) {
                FileUtils.cleanDirectory(new File(dataCollectorConfig.sensorDataUpload));
            }
            for (String client: clients) {
                String downloadUrl = CommunicationConfig.generateUrl(client, communicationConfig.downloadUrl + "/sensor.txt");
                HttpEntity<Resource> httpEntity = restTemplate.getForEntity(downloadUrl, Resource.class);
                Path path = dataCollectorUtils.resolveFileUrl(
                            dataCollectorConfig.sensorDataUpload,
                        client + "_" + httpEntity.getBody().getFilename()
                );
                Files.copy(httpEntity.getBody().getInputStream(), path);
            }
            return true;
        } catch (Exception ex) {
            log.error("Exception in downloadSensorData, ex: ", ex);
            return false;
        }
    }

    @RequestMapping(value = "${dispatchModelUrl}", method = RequestMethod.GET)
    @ResponseBody
    public boolean dispatchModel() {
        try {
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            Path path = dataCollectorUtils.resolveFileUrl(
                    dataCollectorConfig.sensorModelDirectory,
                    dataCollectorConfig.sensorModelName
            );
            FileSystemResource fileSystemResource = new FileSystemResource(path.toFile());
            map.add("model", fileSystemResource);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, httpHeaders);

            List<String> clients = communicationConfig.clients;
            for (String client: clients) {
                String dispatchUrl = CommunicationConfig.generateUrl(client, communicationConfig.sensorModelDispatch);
                restTemplate.exchange(dispatchUrl, HttpMethod.POST, requestEntity, String.class);
            }
            return true;
        } catch (Exception ex) {
            log.error("Exception in dispatchModel, ex: ", ex);
            return false;
        }
    }

    @RequestMapping(value = "trainSensorModel", method = RequestMethod.GET)
    @ResponseBody
    public void trainSensorModel() {
        try {
            Classifier model = commonModelService.trainModel(
                    "weka.classifiers.trees.RandomTree",
                    dataCollectorConfig.sensorDataUpload,
                    true
            );
            commonModelService.saveModel(
                    model,
                    dataCollectorConfig.sensorModelDirectory,
                    dataCollectorConfig.sensorModelName
            );
        } catch (Exception ex) {
            log.error("Exception in trainSensorModel, ex: ", ex);
        }
    }

    @RequestMapping(value = "reformatData", method = RequestMethod.GET)
    @ResponseBody
    public void formatData() {
        try {

            File files = new File(dataCollectorConfig.sensorDirectory);

            for (File file : files.listFiles()) {
                commonModelService.formataData(file);
            }
        } catch (Exception ex) {
            log.error("Exception in formatData: ex: ", ex);
        }
    }
}
