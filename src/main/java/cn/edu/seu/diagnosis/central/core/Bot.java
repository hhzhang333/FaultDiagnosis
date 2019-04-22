package cn.edu.seu.diagnosis.central.core;

import cn.edu.seu.diagnosis.common.DataCollectorUtils;
import cn.edu.seu.diagnosis.common.DiagnosisData;
import cn.edu.seu.diagnosis.config.CommandListConfig;
import cn.edu.seu.diagnosis.config.DataCollectorConfig;
import com.github.chen0040.rl.learning.qlearn.QLearner;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Created by hhzhang on 2019/1/2.
 */
@Getter
@Setter
@Component
public abstract class Bot {

    private Map<String, List<Road>> commands = new HashMap<>();

    @Autowired
    private CommandListConfig commandList;

    private List<String> classLabel = new ArrayList<>();

    protected QLearner agent;

    private int reward = 100;

    @Autowired
    private DataCollectorUtils dataCollectorUtils;
    @Autowired
    private DataCollectorConfig dataCollectorConfig;

    private List<ArrayList<Double>> spaceSplits = null;

    public void init() throws IOException {
        List<String> features = new ArrayList<>();
        features.addAll(dataCollectorConfig.statisticsElement);
        features.addAll(dataCollectorConfig.evaluateElement);

//        List<List<Double>> samples = dataCollectorUtils.initQtable(features, "/data/sensor.arff");
//        List<ArrayList<Double>> spaceSlits = dataCollectorUtils.initSpaceSlice(samples);
//        samples.clear();

        spaceSplits = dataCollectorUtils.initTable("/data/train.csv");
        ArrayList<String> label = dataCollectorUtils.initClassificatedClass(spaceSplits);
        classLabel.addAll(label);
    }


    public abstract String selectionAction(DiagnosisData date);

    public abstract void updateStrategy(int state, int action, int newState, double reward);

    public String act(DiagnosisData diagnosisData) throws Exception {
        if (!diagnosisData.isHealth()) {
            if (commands.containsKey(diagnosisData.getClientIpAddr())) {
                Road road = new Road();
                int currentStateId = getStatId(diagnosisData.getCurrentContent());
                road.setCurrentState(currentStateId);
                int preStateId = getStatId(diagnosisData.getPreContent());
                road.setPreState(preStateId);
                road.setActionId(commandList.getCommandId(diagnosisData.getPreCommand()));
                road.setReward(0.0);
                commands.get(diagnosisData.getClientIpAddr()).add(road);
            } else {
                Road road = new Road();
                int currentStateId = getStatId(diagnosisData.getCurrentContent());
                road.setCurrentState(currentStateId);
                ArrayList<Road> roads = new ArrayList<>();
                roads.add(road);
                commands.put(diagnosisData.getClientIpAddr(), roads);
            }
            return selectionAction(diagnosisData);
        } else {
            List<Road> roads = commands.get(diagnosisData.getClientIpAddr());
            int stateId = roads.get(roads.size() - 1).getCurrentState();
            int actionId = commandList.getCommandId(diagnosisData.getPreCommand());
            int healthId = classLabel.size() - 1;
            this.updateStrategy(stateId, actionId, healthId, reward);
            int eveReward = reward / roads.size();
            for (int i = roads.size(); i >= 1; i--) {
                Road road = roads.get(i);
                this.updateStrategy(road.getPreState(), road.getActionId(), road.getCurrentState(), eveReward);
            }
            commands.remove(diagnosisData.getClientIpAddr());
            return roads.toString();
        }
    }

    public int getStatId(String content) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < spaceSplits.size(); i++) {
            String[] values = content.split(",");
            ArrayList<Double> innerList = spaceSplits.get(i);
            for (int j = 0; j < innerList.size(); j++) {
                if (Double.parseDouble(values[i]) <= innerList.get(j))
                    stringBuilder.append(innerList.get(j)).append("_");
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        for (int i = 0; i < classLabel.size(); i++) {
            if (classLabel.get(i).equals(stringBuilder.toString()))
                return i;
        }
        return 0;
    }
}
