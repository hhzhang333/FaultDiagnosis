package cn.edu.seu.diagnosis.central.reinforcement;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by hhzhang on 2019/4/27.
 */
@Getter
@Setter
public class Node {
    private double start = Double.MAX_VALUE;
    private double end = Double.MIN_VALUE;
}
