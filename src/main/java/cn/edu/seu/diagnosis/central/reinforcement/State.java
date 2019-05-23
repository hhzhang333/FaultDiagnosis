package cn.edu.seu.diagnosis.central.reinforcement;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Created by hhzhang on 2019/4/27.
 */
@Getter
@Setter
public class State {
    private HashMap<Action, List<List<Double>>> faultSamples = new HashMap<>();
    private ArrayList<Node> stateRange;
    private List<Action> actions;
    private List<Double> qValues = new ArrayList<>();

    public State(ArrayList<Node> stateRange, List<Action> actions) {
        this.actions = actions;
        this.stateRange = stateRange;
        initQ();
        for (Action action : actions) {
            faultSamples.put(action, new ArrayList<>());
        }
    }

    private void initQ() {
        for (int i = 0; i < actions.size(); i++)
            qValues.add(0.0);
    }

    public double getMaxQ() {
        double value = -100;
        for (Double q : qValues) {
            if (q > value)
                value = q;
        }
        return value;
    }

    private Action getMaxQAction() {
        int action = 0;
        double value = -100;
        for (int i = 0; i < qValues.size(); i++) {
            if (qValues.get(i) > value) {
                value = qValues.get(i);
                action = i;
            }
        }
        return actions.get(action);
    }

    public Action getAction(double greedy) {
        Random random = new Random();
        double gd = random.nextDouble();
        if (gd < greedy)
            return getMaxQAction();
        else {
            int aID = random.nextInt(actions.size());
            return actions.get(aID);
        }
    }

    public boolean belongs(List<Double> sample) {
        for (int i = 0; i < stateRange.size(); i++) {
            Node node = stateRange.get(i);
            if (node.getStart() <= sample.get(i) && node.getEnd() >= sample.get(i)) {
                continue;
            } else
                return false;
        }
        return true;
    }

    public double getQvalue(int id) {
        return qValues.get(id);
    }

    public void setQvalue(int id, double value) {
        qValues.set(id, value);
    }

    public boolean needSplit(double splitP) {
        int maxCount = 0;
        for (Action action : actions) {
            maxCount += faultSamples.get(action).size();
        }

        for (Action action : actions) {
            if (faultSamples.get(action).size() / maxCount >= splitP)
                return false;
        }
        return true;
    }

    public List<State> split() {
        double maxEnt = -1000;
        int maxDim = -1;
        double maxSplit = -1000;

        HashMap<Action, Integer> rootEnt = new HashMap<>();
        for (Action action : faultSamples.keySet()) {
            rootEnt.put(action, faultSamples.get(action).size());
        }
        double root = entD(rootEnt);

        for (int i = 0; i < stateRange.size(); i++) {

            ArrayList<Double> values = new ArrayList<>();
            for (Action fault : faultSamples.keySet()) {
                List<List<Double>> samples = faultSamples.get(fault);
                for (int j = 0; j < samples.size(); j++) {
                    values.add(samples.get(j).get(i));
                }
            }
            Collections.sort(values);

            List<Double> avaiSplits = getAvaiSplits(values);

            HashMap<Action, Integer> minus = new HashMap<>();
            HashMap<Action, Integer> plus = new HashMap<>();

            int plusCount = 0;
            int minusCount = 0;

            for (Double split : avaiSplits) {

                for (Action fault : faultSamples.keySet()) {
                    List<List<Double>> samples = faultSamples.get(fault);
                    for (List<Double> sample : samples) {
                        if (sample.get(i) < split) {
                            minusCount = getEntCount(minus, minusCount, fault);
                        } else {
                            plusCount = getEntCount(plus, plusCount, fault);
                        }
                    }
                }
                double minusEnt = entD(minus);
                double plusEnt = entD(plus);
                double result = root + minusCount / (minusCount + plusCount) * minusEnt + plusCount / (plusCount + minusCount) * plusEnt;
                if (result > maxEnt) {
                    maxDim = i;
                    maxSplit = split;
                }
                minus.clear();
                plus.clear();
            }
        }

        List<State> states = new ArrayList<>();

        ArrayList<Node> s1Range = new ArrayList<>();
        ArrayList<Node> s2Range = new ArrayList<>();

        for (int i = 0; i < stateRange.size(); i++) {
            if (i == maxDim) {
                Node node1 = new Node();
                node1.setStart(stateRange.get(i).getStart());
                node1.setEnd(maxSplit);
                Node node2 = new Node();
                node2.setStart(maxSplit);
                node2.setEnd(stateRange.get(i).getEnd());
                s1Range.add(node1);
                s2Range.add(node2);
            } else {
                s1Range.add(stateRange.get(i));
                s2Range.add(stateRange.get(i));
            }
        }
        State s1 = new State(s1Range, this.getActions());
        State s2 = new State(s2Range, this.getActions());
        states.add(s1);
        states.add(s2);
        return states;
    }

    private int getEntCount(HashMap<Action, Integer> minus, int minusCount, Action fault) {
        if (minus.containsKey(fault)) {
            int count = minus.get(fault);
            minus.put(fault, count + 1);
        } else {
            minus.put(fault, 1);
        }
        minusCount++;
        return minusCount;
    }

    public List<Double> getAvaiSplits(ArrayList<Double> samples) {
        List<Double> splits = new ArrayList<>();
        for (int i = 0; i < samples.size() - 1; i++) {
            splits.add((samples.get(i) + samples.get(i + 1)) / 2);
        }
        return splits;
    }

    public double entD(HashMap<Action, Integer> actions) {
        double value = 0.0;
        int totalCount = 0;
        for (Action fault : actions.keySet()) {
            totalCount += actions.get(fault);
        }

        for (Action fault : actions.keySet()) {
            if (actions.get(fault) == 0)
                continue;
            double part = (double) actions.get(fault) / totalCount;
            value += -part * Math.log(part);
        }
        return value;
    }
}
