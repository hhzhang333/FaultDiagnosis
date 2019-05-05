package cn.edu.seu.diagnosis.common;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by hhzhang on 2018/12/16.
 */
@Setter
@Getter
public class DiagnosisData {
    private String clientIpAddr;
    private String preCommand;
    //采集数据的标签
    private String preContent;
    //根据采集数据分类的标签
    private String currentContent;
    private String currentCommand;
    private boolean isHealth;

    public DiagnosisData newStage() {
        this.setClientIpAddr(this.clientIpAddr);
        this.setPreContent(this.currentContent);
        this.setPreCommand(this.currentCommand);
        this.setCurrentContent(null);
        this.setCurrentCommand(null);
        return this;
    }

    @Override
    public String toString() {
        return "DiagnosisData{" +
                "clientIpAddr='" + clientIpAddr + '\'' +
                ", command='" + currentCommand + '\'' +
                ", isHealth=" + isHealth +
                '}';
    }
}
