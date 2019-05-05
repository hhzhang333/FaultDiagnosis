package cn.edu.seu.diagnosis.client.service;

import lombok.extern.slf4j.Slf4j;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * Created by hhzhang on 2018/12/10.
 */
@Service
@Slf4j
public class CommandExecutorService {

    @Value("${sudoPassword}")
    private String sudoPassowrd;


    public String execute(String command, long expiredTime) {
        try {
            ProcResult procResult = null;
            if (command.contains("cpulimit")) {
                int id = this.getMostCPUUsagePID();
                String realCommand = command.replace("port", id + "");
                procResult = new ProcBuilder("/bin/bash")
                        .withArgs("-c", realCommand).run();
            } else {
                procResult = new ProcBuilder("/bin/bash")
                        .withArgs("-c", command).run();
            }
            return procResult.getOutputString();
        } catch (Exception ex) {
            log.error("Exception in executeCommand, ex: ", ex);
            return "error execute command";
        }

    }

    public int getMostCPUUsagePID() throws Exception {
        String command = "top -n 1 -b";
        String result = this.execute(command, 1000);

        String[] lines = result.split("\\n");
        double maxCPU = 0;
        int pid = 0;
        for (int i = 7; i < lines.length; i++) {
            String current = lines[i];
            String[] elements = current.split(" ");
            int number = 0;
            int currentPID = 0;
            for (String element: elements) {
                if (!element.isEmpty()) {
                    number++;
                    if (number == 1)
                        currentPID = Integer.parseInt(element);
                    if (number == 9) {
                        if (maxCPU < Double.parseDouble(element)) {
                            pid = currentPID;
                            maxCPU = Double.parseDouble(element);
                        }
                    }
                }
            }
        }
        return pid;
    }
}
