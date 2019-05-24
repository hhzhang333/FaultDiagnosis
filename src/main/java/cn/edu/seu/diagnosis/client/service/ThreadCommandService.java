package cn.edu.seu.diagnosis.client.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class ThreadCommandService extends Thread {

    CommandExecutorService executorService = new CommandExecutorService();

    private String command;

    @Override
    public void run() {
        try {
            if (command.contains("stress-ng")) {
                executorService.executeEnvironment(command, 600000);
            } else
                executorService.executeEnvironment(command, 1000);
        } catch (Exception ex) {
            log.error("error in threadCommandService, ex: ", ex);
        }
    }
}
