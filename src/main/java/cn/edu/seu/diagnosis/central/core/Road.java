package cn.edu.seu.diagnosis.central.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by hhzhang on 2019/4/18.
 */
@Getter
@Setter
public class Road {
    private int preState;
    private int actionId;
    private double reward;
    private int currentState;
}
