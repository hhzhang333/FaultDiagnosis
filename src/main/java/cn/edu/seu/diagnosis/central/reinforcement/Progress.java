package cn.edu.seu.diagnosis.central.reinforcement;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by hhzhang on 2019/5/6.
 */
@Getter
@Setter
public class Progress {
    private State preState;
    private Action action;
    private double reward;
    private State currentState;
}
