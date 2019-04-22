package cn.edu.seu.diagnosis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hhzhang on 2018/12/10.
 */
@Component
public class DataCollectorConfig {
    @Value("${requestURL}")
    public String requestURL;

    @Value("#{'${statisticsElement}'.split(',')}")
    public List<String> statisticsElement;

    @Value("#{'${evaluateElement}'.split(',')}")
    public List<String> evaluateElement;

    @Value("${maxFile}")
    public int maxFile;

    @Value("${sensorDirectory}")
    public String sensorDirectory;

    @Value("${sensorModelDirectory}")
    public String sensorModelDirectory;

    @Value("${dispatchDirectory}")
    public String dispatchDirectory;

    @Value("${diagnosisModelDirectory}")
    public String diagnosisModelDirectory;

    @Value("${diagnosisModelName}")
    public String diagnosisModelName;

    @Value("${diagnosisDirectory}")
    public String diagnosisDirectory;

    @Value("${broswerAgent}")
    public String broswerAgent;

    @Value("${sensorName}")
    public String sensorName;

    @Value("${sensorModelName}")
    public String sensorModelName;

    @Value("${sensorDataUpload}")
    public String sensorDataUpload;

    @Value("${diagnosisName}")
    public String diagnosisName;

    @Value("${diagnosisDataUpload}")
    public String diagnosisDataUpload;

    @Value("#{'${evaluateValueRange}'.split(';')}")
    public List<String> evaluateValueRange;

    @Value("${ip}")
    public String ip;
}
