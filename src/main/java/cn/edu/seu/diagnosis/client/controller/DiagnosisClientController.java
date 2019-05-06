package cn.edu.seu.diagnosis.client.controller;

import cn.edu.seu.diagnosis.client.service.CommandExecutorService;
import cn.edu.seu.diagnosis.common.DataCollectorService;
import cn.edu.seu.diagnosis.common.DiagnosisData;
import cn.edu.seu.diagnosis.config.CommunicationConfig;
import cn.edu.seu.diagnosis.config.DataCollectorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by hhzhang on 2018/12/14.
 */
@Slf4j
@Controller
public class DiagnosisClientController {
    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private DataCollectorConfig dataCollectorConfig;

    @Autowired
    private CommandExecutorService commandExecutor;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommunicationConfig communicationConfig;

    @RequestMapping(value = "${diagnosisTask}")
    @ResponseBody
    public void executeCommandAndReportReward(@RequestBody DiagnosisData data) {
        try {

            System.out.println("accept diagnosis, command: " + commandExecutor);

            commandExecutor.execute(data.getCurrentCommand(), 10000);
//            Thread.sleep(5000);

            List<Double> doubleList = dataCollectorService.getDiagnosisData();

            boolean health = dataCollectorService.isHealth(doubleList);

            //诊断结束，重新开始监控系统
            if (health) {
                String monitorUrl = CommunicationConfig.generateUrl(communicationConfig.ip, communicationConfig.monitor + "/" + 1);
                restTemplate.getForEntity(monitorUrl, Void.class);
            }
            data.setCurrentContent(dataCollectorService.listToCSV(doubleList));

            data.setHealth(health);
            restTemplate.postForEntity(
                    CommunicationConfig.generateUrl(
                            communicationConfig.master,
                            communicationConfig.diagnosisTaskForController
                    ),
                    data,
                    Void.class
            );
        } catch (Exception ex) {
            log.error("Exception in executeCommandAndReportReward, ex: ", ex);
        }
    }
}
