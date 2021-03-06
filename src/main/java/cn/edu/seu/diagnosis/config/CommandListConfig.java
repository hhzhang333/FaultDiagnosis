package cn.edu.seu.diagnosis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Created by hhzhang on 2018/12/16.
 */
@Getter
@Setter
@Component
public class CommandListConfig {

    @Value("#{'${commands}'.split(',')}")
    private List<String> commands;

    @Value("#{'${injects}'.split(',')}")
    private List<String> injectCommands;

    @Value("#{'${initCommand}'.split(',')}")
    private List<String> initCommand;

    @Value("#{'${rollbackCommand}'.split(',')}")
    private List<String> rollbackCommand;

    public String randomSelectCommand() {
        Random random = new Random();
        int value = random.nextInt();
        return commands.get(value % commands.size());
    }

    public String getCommandById(int id) {
        return commands.get(id);
    }

    public int getCommandId(String command) throws Exception {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).equals(command))
                return i;
        }
        throw new Exception("commands not found");
    }
}
