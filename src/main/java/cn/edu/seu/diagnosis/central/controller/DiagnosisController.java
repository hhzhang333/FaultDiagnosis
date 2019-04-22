package cn.edu.seu.diagnosis.central.controller;

import cn.edu.seu.diagnosis.central.service.DiagnosisService;
import cn.edu.seu.diagnosis.common.DataCollectorService;
import cn.edu.seu.diagnosis.common.DataCollectorUtils;
import cn.edu.seu.diagnosis.common.DiagnosisData;
import cn.edu.seu.diagnosis.config.CommunicationConfig;
import cn.edu.seu.diagnosis.config.DataCollectorConfig;
import cn.edu.seu.diagnosis.model.CommonModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import weka.classifiers.Classifier;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by hhzhang on 2018/12/16.
 */
@Slf4j
@Controller
public class DiagnosisController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private CommunicationConfig communicationConfig;

    @Autowired
    private DataCollectorUtils dataCollectorUtils;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private CommonModelService commonModelService;

    @Autowired
    private DataCollectorConfig dataCollectorConfig;

    private ScheduledFuture<?> diagnosisDataCollectorTaskMonitor;

    @RequestMapping(value = "${startDiagnosisUrl}", method = RequestMethod.POST)
    @ResponseBody
    public void startDiagnosis(HttpServletRequest request,
                               @RequestBody DiagnosisData data) {
        try {
            String requestIp = CommunicationConfig.getIpAddress(request);
            String executeCommand = diagnosisService.startDiagnose(data);
            System.out.println(requestIp + " command: " + executeCommand);
            data.setCurrentCommand(executeCommand);
            data.setClientIpAddr(requestIp);
            restTemplate.postForEntity(
                    CommunicationConfig.generateUrl(requestIp, communicationConfig.diagnosisTask),
                    data,
                    Void.class
            );
        } catch (Exception ex) {
            log.error("Exception in startDiagnosis, ex: " + ex);
        }
    }

    @RequestMapping(value = "${diagnosisProcessForController}", method = RequestMethod.POST)
    public void diagnosisProcess(HttpServletRequest request,
                                 DiagnosisData diagnosisData) {
        try {
            if (diagnosisData.isHealth()) {
                diagnosisService.diagnose(diagnosisData);
                return;
            }
            String executeCommand = diagnosisService.diagnose(diagnosisData);

            DiagnosisData current = diagnosisData.newStage();
            current.setCurrentCommand(executeCommand);

            restTemplate.postForEntity(
                    CommunicationConfig.generateUrl(diagnosisData.getClientIpAddr(), communicationConfig.diagnosisTask),
                    current,
                    Void.class
            );
        } catch (Exception ex) {
            log.error("Exception in diagnosisProcess: ex: ", ex);
        }
    }

    @RequestMapping(value = "diagnosisDataStart/{time}", method = RequestMethod.POST)
    @ResponseBody
    public boolean collectDiagnosisData(@PathVariable int time,
                                        @RequestParam String src,
                                        @RequestParam String dest,
                                        @RequestParam String faultLabel) {
        try {
            CronTrigger trigger = dataCollectorUtils.generateCronTrigger(time);

            if (diagnosisDataCollectorTaskMonitor != null || !diagnosisDataCollectorTaskMonitor.isDone())
                return false;
            diagnosisDataCollectorTaskMonitor = taskScheduler.schedule(
                    diagnosisDataCollector(src, dest, faultLabel),
                    trigger
            );
            return true;
        } catch (Exception ex) {
            log.error("Exception in collectDiagnosisData, ex: " + ex);
            return false;
        }
    }

    @RequestMapping(value = "diagnosisDataStop", method = RequestMethod.GET)
    @ResponseBody
    public boolean diagnosisDataStop() {

        if (diagnosisDataCollectorTaskMonitor == null)
            return true;
        diagnosisDataCollectorTaskMonitor.cancel(false);
        return true;
    }

    @RequestMapping(value = "trainMLDiagnosisModel", method = RequestMethod.GET)
    @ResponseBody
    public boolean trainMLDiagnosisModel() {
        try {
            Classifier model = commonModelService.trainModel(
                    "weka.classifiers.functions.MultilayerPerceptron",
                    dataCollectorConfig.diagnosisDirectory,
                    false
            );
            commonModelService.saveModel(
                    model,
                    dataCollectorConfig.diagnosisModelDirectory,
                    dataCollectorConfig.diagnosisName
            );
            return true;
        } catch (Exception ex) {
            log.error("Exception in trainMLDiagnosisModel, ex: " + ex);
            return false;
        }
    }

    @ResponseBody
    @RequestMapping(value = "getQtable", method = RequestMethod.GET)
    public String showQtable() {
        try {
            return diagnosisService.getQTable();
        } catch (Exception ex) {
            log.error("Exception in showQtable ex " + ex);
            return "error";
        }
    }

    private Runnable diagnosisDataCollector(String src, String dest, String label) {
        return () -> {
            try {
                dataCollectorService.collectDiagnosisData(src, dest, label);
            } catch (Exception ex) {
                log.error("Exception in monitor, ex: " + ex);
            }
        };
    }
}
