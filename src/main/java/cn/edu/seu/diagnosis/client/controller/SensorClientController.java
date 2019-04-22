package cn.edu.seu.diagnosis.client.controller;

import cn.edu.seu.diagnosis.common.DataCollectorService;
import cn.edu.seu.diagnosis.common.DataCollectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by hhzhang on 2018/12/5.
 */
@Slf4j
@Controller
public class SensorClientController {

    @Autowired
    TaskScheduler taskScheduler;

    private ScheduledFuture<?> sensorCollectorScheduler;
    private ScheduledFuture<?> monitorScheduler;

    @Autowired
    private DataCollectorService dataCollectorService;
    @Autowired
    private DataCollectorUtils dataCollectorUtils;

    @RequestMapping(value = "${sensorDataStartUrl}/{time}", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<Void> sensorDataCollect(@PathVariable int time) throws IOException {
        try {
            CronTrigger trigger = dataCollectorUtils.generateCronTrigger(time);

            if (sensorCollectorScheduler != null)
                if (!sensorCollectorScheduler.isDone())
                    return new ResponseEntity<>(HttpStatus.CONFLICT);


            sensorCollectorScheduler = taskScheduler.schedule(executeCollectorDataTask(), trigger);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Exception in sensorDataCollect, ex: ", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("${sensorDataStopUrl}")
    @ResponseBody
    ResponseEntity<Void> sensorDataStop() {
        if (sensorCollectorScheduler == null)
            return new ResponseEntity<Void>(HttpStatus.OK);
        sensorCollectorScheduler.cancel(true);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping("${startMonitor}/{range}")
    ResponseEntity<Void> monitorSystem(@PathVariable int range) {
        try {
            CronTrigger trigger = dataCollectorUtils.generateCronTrigger(range);
            if (monitorScheduler != null)
                if (!monitorScheduler.isDone())
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
            monitorScheduler = taskScheduler.schedule(monitor(), trigger);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Exception in monitorSystem, ex: " + ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("${stopMonitor}")
    ResponseEntity<Void> stopMonitor() {
        if (monitorScheduler == null)
            return new ResponseEntity<>(HttpStatus.OK);
        monitorScheduler.cancel(false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Runnable executeCollectorDataTask() {
        return () -> {
            try {
                dataCollectorService.collectSensorData();
            } catch (Exception e) {
                log.error("Exception in executeCollectorDataTask, ex: " + e);
            }
        };
    }

    private Runnable monitor() {
        return () -> {
            try {
                dataCollectorService.monitorSystem();
            } catch (Exception ex) {
                log.error("Exception in monitor, ex: " + ex);
            }
        };
    }

}
