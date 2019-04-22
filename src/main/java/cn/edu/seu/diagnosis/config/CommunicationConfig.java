package cn.edu.seu.diagnosis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by hhzhang on 2018/12/13.
 */
@Component
public class CommunicationConfig {
    //master
    @Value("#{'${clients}'.split(',')}")
    public List<String> clients;

    @Value("${ip}")
    public String ip;

    //client
    @Value("${master}")
    public String master;

    //sensor
    @Value("${sensorDataStartUrl}")
    public String sensorDataStartUrl;
    @Value("${sensorDataStopUrl}")
    public String sensorDataStopUrl;
    @Value("${sensorDataDownload}")
    public String sensorDataDownload;
    @Value("${sensorModelDispatch}")
    public String sensorModelDispatch;

    @Value("${startMonitor}")
    public String monitor;
    @Value("${stopMonitor}")
    public String stopMonitor;

    //diagnosis
    @Value("${diagnosisDataStartUrl}")
    public String diagnosisDataStartUrl;
    @Value("${diagnosisDataStopUrl}")
    public String diagnosisDataStopUrl;
    @Value("${diagnosisDataDownload}")
    public String diagnosisDataDownload;

    @Value("${downloadUrl}")
    public String downloadUrl;

    @Value("${startDiagnosisUrl}")
    public String startDiagnosisProcess;

    @Value("${diagnosisTask}")
    public String diagnosisTask;

    @Value("${diagnosisProcessForController}")
    public String diagnosisTaskForController;

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String generateUrl(String ip, String requestUrl) {
        return "http://" + ip + ":8080/FaultDiagnosis_war/" + requestUrl;
    }

}
