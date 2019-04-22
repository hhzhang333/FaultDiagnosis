package cn.edu.seu.diagnosis.central.core;

import cn.edu.seu.diagnosis.common.DiagnosisData;
import cn.edu.seu.diagnosis.config.CommandListConfig;
import com.github.chen0040.rl.learning.qlearn.QLearner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Random;

/**
 * Created by hhzhang on 2019/1/2.
 */
@Component
public class QBot extends Bot {

    @Autowired
    private CommandListConfig commandList;

    @PostConstruct
    public void init() throws IOException {
        super.init();
        int actionCount = commandList.getCommands().size();
        QLearner learner = new QLearner(super.getClassLabel().size(), actionCount);
        learner.getModel().setAlpha(0.7);
        learner.getModel().setGamma(1.0);
        this.agent = learner;
    }

    @Override
    public String selectionAction(DiagnosisData data) {
        int state = getStatId(data.getCurrentContent());
        int maxAction = commandList.getCommands().size();
        double maxQ = Double.MIN_VALUE;
        int maxQ_Id = -1;
        for (int i = 0; i < maxAction; i++) {
            double qValue = this.agent.getModel().getQ(state, i);
            if (qValue >= maxQ) {
                maxQ = qValue;
                maxQ_Id = i;
            }
        }
        Random random = new Random(System.currentTimeMillis());
        if (random.nextDouble() < 0.5)
            return commandList.getCommandById(maxQ_Id);
        else {
            int id = (int) (Math.random() * commandList.getCommands().size());
            return commandList.getCommandById(id);
        }
    }

    @Override
    public void updateStrategy(int state, int action, int newState, double reward) {
        this.agent.update(state, action, newState, reward);
    }

    public String getQTable() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < agent.getModel().getStateCount(); i++) {
            for (int j = 0; j < agent.getModel().getActionCount(); j++) {
                stringBuilder.append(agent.getModel().getQ(i, j)).append(" ");
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
