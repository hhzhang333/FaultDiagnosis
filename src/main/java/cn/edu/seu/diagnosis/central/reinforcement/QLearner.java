package cn.edu.seu.diagnosis.central.reinforcement;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhzhang on 2019/5/5.
 */
@Getter
@Setter
public class QLearner {
    private StateSpace stateSpace;

    private double alpha;
    private double gamma;

    public QLearner(List<Action> actions, double alpha, double gamma) throws IOException {
        initStateSpace(actions);
        this.alpha = alpha;
        this.gamma = gamma;
    }

    public void updateHealth(State start, int action, double r) {
        double oldQ = start.getQvalue(action);
        double newValue = (1 - alpha) * oldQ + alpha * r;
        start.setQvalue(action, newValue);
    }

    public void update(State start, int action, State end, double r) {
        double oldQ = start.getQvalue(action);
        double max = end.getMaxQ();
        double newValue = oldQ + alpha * (r + gamma * max - oldQ);
        start.setQvalue(action, newValue);
    }

    public State getState(List<Double> sample) {
        return stateSpace.getState(sample);
    }

    public Action getAction(List<Double> samples) {
        return stateSpace.getAction(samples);
    }

    public Action getAction(String command) {
        return stateSpace.getActionByCommand(command);
    }

    public String getQTable() {
        List<State> stateList = stateSpace.getStates();
        StringBuilder stringBuilder = new StringBuilder();
        for (State state : stateList) {
            List<Double> qValues = state.getQValues();
            for (Double qvalue : qValues) {
                stringBuilder.append(qvalue);
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void initStateSpace(List<Action> actions) throws IOException {
        ArrayList<Node> nodes = new ArrayList<>();
        File trainFile = new ClassPathResource("/data/train.csv").getFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(trainFile));
        String line = bufferedReader.readLine();
        for (int i = 0; i < line.split(",").length - 1; i++) {
            nodes.add(new Node());
        }
        while ((line = bufferedReader.readLine()) != null) {
            String[] splits = line.split(",");
            if (splits[splits.length - 1].equals("health"))
                continue;
            for (int i = 0; i < splits.length - 1; i++) {
                Node node = nodes.get(i);
                double rv = Double.parseDouble(splits[i]);
                if (node.getStart() > rv) {
                    node.setStart(rv);
                }
                if (node.getEnd() < rv) {
                    node.setEnd(rv);
                }
            }
        }
        State state = new State(nodes, actions);
        stateSpace = new StateSpace(state);
    }
}
