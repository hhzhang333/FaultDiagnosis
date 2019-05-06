package cn.edu.seu.diagnosis.common;

import cn.edu.seu.diagnosis.config.CommunicationConfig;
import cn.edu.seu.diagnosis.config.DataCollectorConfig;
import cn.edu.seu.diagnosis.model.CommonModelService;
import cn.edu.seu.diagnosis.model.domain.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhzhang on 2018/12/10.
 */
@Service
public class DataCollectorService {
    @Autowired
    private DataCollectorConfig dataCollectorConfig;

    @Autowired
    private DataCollectorUtils dataCollectorUtils;

    @Autowired
    private CommonModelService commonModelService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommunicationConfig communicationConfig;
    @Autowired
    private Elements elements;

    //client节点本地采集感知数据集
    public void collectSensorData() throws Exception {
        for (String ip : communicationConfig.clients) {
            String realip = ip.replace("/", "-");
            String sensorName = realip + "_" + dataCollectorConfig.sensorName;
            File file = dataCollectorUtils.resolveFileUrl(
                    dataCollectorConfig.sensorDirectory,
                    sensorName
            ).toFile();

            String data;
            if (file.length() == 0) {
                data = this.collectData(sensorName, ip, true);
            } else {
                data = this.collectData(sensorName, ip, false);
            }

            dataCollectorUtils.writeToFile(file, data);
//            dataCollectorUtils.writeToFile(file, collectRaw(ip));
        }
    }

    public String collectRaw(String ip) throws IOException {
        return dataCollectorUtils.getRemoteSystemStatus(
                dataCollectorConfig.requestURL.replace("IP", ip),
                dataCollectorConfig.broswerAgent
        );
    }

    public void monitorSystem() throws Exception {
        List<Double> data = getDiagnosisData();
        if (!isHealth(data)) {
            this.startDiagnosisProcess(data);
        }
    }

    public List<Double> getDiagnosisData() throws IOException {
        String ip = dataCollectorConfig.ip;

        String content = dataCollectorUtils.getRemoteSystemStatus(
                dataCollectorConfig.requestURL.replace("IP", ip),
                dataCollectorConfig.broswerAgent
        );

        List<Double> statisticsProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.statisticsElement,
                content
        );
        List<Double> evaluateProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.evaluateElement,
                content
        );

        statisticsProperties.addAll(evaluateProperties);
        return statisticsProperties;
    }

    private void startDiagnosisProcess(List<Double> params) {
        String diagnosisUrl = CommunicationConfig.generateUrl(
                "127.0.0.1",
                communicationConfig.startDiagnosisProcess
        );

        DiagnosisData data = new DiagnosisData();
        data.setCurrentContent(listToCSV(params));
        data.setClientIpAddr(communicationConfig.ip);
        data.setHealth(false);

        //开始诊断，暂停监控系统，诊断结束，重新开始监控
//        String stopUrl = CommunicationConfig.generateUrl(communicationConfig.ip, communicationConfig.stopMonitor);
        String stopUrl = CommunicationConfig.generateUrl("127.0.0.1", communicationConfig.stopMonitor);
        System.out.println(stopUrl);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.getForEntity(stopUrl, Void.class);
            System.out.println("start-diagnosis");
            if (responseEntity.getStatusCodeValue() == 200) {
                System.out.println(diagnosisUrl);
                restTemplate.postForObject(diagnosisUrl, data, Void.class);
                System.out.println(diagnosisUrl);
            }
        } catch (Exception ex) {
            String startUrl = CommunicationConfig.generateUrl(communicationConfig.ip, communicationConfig.monitor + "/" + 1);
            restTemplate.getForEntity(startUrl, Void.class);
        }

    }

    public boolean isHealth(List<Double> values) throws Exception {
        elements.newInstances();
        elements.addInstance(values);
        File modelFile = new ClassPathResource("/model/sensor.model").getFile();

        String classifiedLabel = commonModelService.classify(modelFile, elements.getDataInstances());
        System.out.println(classifiedLabel);
        return classifiedLabel.equals("health");
    }

    public String collectRawSensorData() throws IOException {
        String sensorName = dataCollectorConfig.sensorName;

        for (String ip : communicationConfig.clients) {
            String content = dataCollectorUtils.getRemoteSystemStatus(
                    dataCollectorConfig.requestURL.replace("IP", ip),
                    dataCollectorConfig.broswerAgent
            );

            File file = dataCollectorUtils.resolveFileUrl(
                    dataCollectorConfig.sensorDirectory,
                    ip.replace("/", "-") + sensorName
            ).toFile();

            if (!dataCollectorUtils.isInit(file)) {
                String labels = dataCollectorUtils.parseContentLabel(content);
                dataCollectorUtils.writeToFile(file, labels);
            } else {
                String values = dataCollectorUtils.parseContentValue(content);
                dataCollectorUtils.writeToFile(file, values);
            }
        }
        return "ok";

//        String content = dataCollectorUtils.getRemoteSystemStatus(
//                dataCollectorConfig.requestURL.replace("IP", dataCollectorConfig.ip),
//                dataCollectorConfig.broswerAgent
//        );

//        File file = dataCollectorUtils.resolveFileUrl(
//                dataCollectorConfig.sensorDirectory,
//                sensorName
//        ).toFile();
//
//        if (!dataCollectorUtils.isInit(file)) {
//            String labels = dataCollectorUtils.parseContentLabel(content);
//            dataCollectorUtils.writeToFile(file, labels);
//        } else {
//            String values = dataCollectorUtils.parseContentValue(content);
//            dataCollectorUtils.writeToFile(file, values);
//        }
//        return content;
    }

    public String collectData(String sensorName, String ip, boolean isLabel) throws Exception {

        if (isLabel) {

            List<String> labels = new ArrayList<>(dataCollectorConfig.statisticsElement);

            for (int i = 0; i < 7; i++) {
                labels.add(dataCollectorConfig.evaluateElement.get(i).split("\\.")[1]);
            }
            return listToCSVString(labels);
        }

        String systemContent = dataCollectorUtils.getRemoteSystemStatus(
                dataCollectorConfig.requestURL.replace("IP", ip),
                dataCollectorConfig.broswerAgent
        );

        List<Double> statisticsProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.statisticsElement,
                systemContent
        );
        List<Double> evaluateProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.evaluateElement,
                systemContent
        );


//        String inputLabel = constructProperties(statisticsProperties, false);
//        String outputClass = constructProperties(evaluateProperties, true);

        statisticsProperties.addAll(evaluateProperties);
        return listToCSV(statisticsProperties);
    }

    public String listToCSVString(List<String> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String object : values) {
            stringBuilder.append(object).append(",");
        }
        stringBuilder.append("class");
        return stringBuilder.toString();
    }

    public String listToCSV(List<Double> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Double object : values) {
            stringBuilder.append(object).append(",");
        }
        stringBuilder.append("error");
        return stringBuilder.toString();
    }

    //控制节点统一采集诊断数据
    public void collectDiagnosisData(String src, String dest, String faultLabel) throws IOException {
        File file = dataCollectorUtils.resolveFileUrl(
                dataCollectorConfig.diagnosisDirectory,
                dataCollectorConfig.diagnosisName
        ).toFile();

        dataCollectorUtils.fileCheck(file, dataCollectorConfig.maxFile * 2);
//        dataCollectorUtils.initArffFile(
//                dataCollectorUtils.resolveFileUrl(dataCollectorConfig.diagnosisDirectory, dataCollectorConfig.diagnosisName),
//                "@relation fault.diagnosis.DiagnosisModel.training",
//                dataCollectorConfig.statisticsElement,
//                dataCollectorConfig.evaluateElement,
//                false
//        );

        String srcStatus = dataCollectorUtils.getRemoteSystemStatus(
                dataCollectorConfig.requestURL.replace("IP", src),
                dataCollectorConfig.broswerAgent
        );
        String destStatus = dataCollectorUtils.getRemoteSystemStatus(
                dataCollectorConfig.requestURL.replace("IP", dest),
                dataCollectorConfig.broswerAgent
        );

        List<Double> srcStatisticsProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.statisticsElement,
                srcStatus
        );

        List<Double> srcEvaluateProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.evaluateElement,
                srcStatus
        );

        List<Double> destStatisticsProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.statisticsElement,
                destStatus
        );

        List<Double> destEvaluateProperties = dataCollectorUtils.parseProperties(
                dataCollectorConfig.evaluateElement,
                destStatus
        );

        String srcLabel = this.constructProperties(srcStatisticsProperties, false) +
                this.constructProperties(srcEvaluateProperties, false);

        String dstLabel = this.constructProperties(destStatisticsProperties, false) +
                this.constructProperties(destEvaluateProperties, false);

        String diagnosisContent = srcLabel + dstLabel + "," + faultLabel;

        File fileSrc = dataCollectorUtils.resolveFileUrl(
                dataCollectorConfig.dispatchDirectory,
                src + dataCollectorConfig.diagnosisName).toFile();

        dataCollectorUtils.writeToFile(fileSrc, srcLabel + "," + faultLabel);

        File fileDest = dataCollectorUtils.resolveFileUrl(
                dataCollectorConfig.dispatchDirectory,
                dest + dataCollectorConfig.diagnosisName).toFile();

        dataCollectorUtils.writeToFile(fileDest, dstLabel + "," + faultLabel);

        File fileContent = dataCollectorUtils.resolveFileUrl(
                dataCollectorConfig.diagnosisDirectory,
                dataCollectorConfig.diagnosisName
        ).toFile();

        dataCollectorUtils.writeToFile(fileContent, diagnosisContent);

    }

    public String constructProperties(List<Double> properties, boolean isSensorClass) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < properties.size(); i++) {
            //连续区间划分
            if (isSensorClass) {
                int range = Integer.parseInt(dataCollectorConfig.evaluateValueRange.get(i).split(",")[2]);
                stringBuilder.append(dataCollectorUtils.normalizeValue(properties.get(i), range))
                        .append("_");
            } else
                stringBuilder.append(properties.get(i)).append(",");
        }

        //删除class label后多余的"-"
        if (isSensorClass) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        return stringBuilder.toString();
    }
}
