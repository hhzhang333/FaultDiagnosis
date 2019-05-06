package cn.edu.seu.diagnosis.central.reinforcement;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hhzhang on 2019/4/27.
 */
@Setter
@Getter
public class StateSpace {
    private LinkedList<State> states;
    private double greedy = 0.7;

    public StateSpace(State state) {
        states = new LinkedList<>();
        states.add(state);
    }

    public Action getAction(List<Double> sample) {
        for (State state : states) {
            if (state.belongs(sample)) {
                return state.getAction(greedy);
            }
        }
        return states.get(0).getAction(greedy);
    }

    public Action getActionByCommand(String command) {
        State state = states.get(0);
        List<Action> actions = state.getActions();
        for (Action action : actions) {
            if (action.getCommand().equals(command))
                return action;
        }
        return null;
    }

    public State getState(List<Double> sample) {
        for (State state : states) {
            if (state.belongs(sample))
                return state;
        }
        return states.get(0);
    }

    public void checkSplits(double splitP) {
        List<State> splitsState = new ArrayList<>();
        for (State state : states) {
            if (state.needSplit(splitP))
                splitsState.add(state);
        }

        if (splitsState.isEmpty())
            return;
        else {
            for (State state : splitsState) {
                List<State> splits = state.split();
                states.addAll(splits);
            }
            for (State state : splitsState)
                states.remove(state);
        }
    }
}
