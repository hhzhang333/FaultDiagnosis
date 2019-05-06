package cn.edu.seu.diagnosis.central.service;

import cn.edu.seu.diagnosis.central.reinforcement.Action;
import cn.edu.seu.diagnosis.central.reinforcement.Progress;
import cn.edu.seu.diagnosis.central.reinforcement.QLearner;
import cn.edu.seu.diagnosis.common.DiagnosisData;
import cn.edu.seu.diagnosis.config.CommandListConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hhzhang on 2018/12/16.
 */
@Service
@Getter
@Setter
public class DiagnosisService {

    @Autowired
    private CommandListConfig commandListConfig;

    HashMap<String, List<Progress>> diagnosis;

    private double alpha = 0.5;
    private double gamma = 0.4;
    private double reward = 100;
    private QLearner qLearner;

    @PostConstruct
    public void init() throws IOException {
        List<Action> actions = new ArrayList<>();
        int id = 0;
        for (String command : commandListConfig.getCommands()) {
            Action action = new Action();
            action.setActionId(id);
            action.setCommand(command);
            actions.add(action);
            id++;
        }

        qLearner = new QLearner(actions, alpha, gamma);
        diagnosis = new HashMap<>();
    }

    public Action diagnose(DiagnosisData data) {
        this.addToProgress(data);
        List<Double> samples = this.extractSamplesFromString(data.getCurrentContent());
        return qLearner.getAction(samples);
    }

    private List<Double> extractSamplesFromString(String sample) {
        List<Double> doubleList = new ArrayList<>();
        String[] values = sample.split(",");
        for (int i = 0; i < values.length - 1; i++) {
            doubleList.add(Double.parseDouble(values[i]));
        }
        return doubleList;
    }

    private void addToProgress(DiagnosisData diagnosisData) {
        if (diagnosis.containsKey(diagnosisData.getClientIpAddr())) {
            Progress progress = new Progress();
            progress.setAction(qLearner.getAction(diagnosisData.getPreCommand()));
            progress.setCurrentState(qLearner.getState(this.extractSamplesFromString(diagnosisData.getCurrentContent())));
            progress.setPreState(qLearner.getState(this.extractSamplesFromString(diagnosisData.getPreCommand())));
            if (diagnosisData.isHealth())
                progress.setReward(reward);
            else
                progress.setReward(0.0);
        } else {
            List<Progress> progresses = new ArrayList<>();
            Progress progress = new Progress();
            progress.setCurrentState(qLearner.getState(this.extractSamplesFromString(diagnosisData.getCurrentContent())));
            progress.setPreState(null);
            progresses.add(progress);
            diagnosis.put(diagnosisData.getClientIpAddr(), progresses);
        }
    }

    public String getQTable() {
        return qLearner.getQTable();
    }

//    public List<String> commandRollBack(String ip) {
//        List<String> commands = new ArrayList<>();
//        Map<String, List<Road>> roadMap =  qBot.getCommands();
//        if (roadMap.containsKey(ip)) {
//            List<Road> roads = roadMap.get(ip);
//            for (Road road: roads) {
//                commands.add(String.valueOf(road.getActionId()));
//            }
//            return commands;
//        }
//        return null;
//    }
}

