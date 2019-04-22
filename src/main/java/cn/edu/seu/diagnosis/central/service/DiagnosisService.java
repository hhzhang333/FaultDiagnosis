package cn.edu.seu.diagnosis.central.service;


import cn.edu.seu.diagnosis.central.core.QBot;
import cn.edu.seu.diagnosis.common.DiagnosisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hhzhang on 2018/12/16.
 */
@Service
public class DiagnosisService {

    @Autowired
    private QBot qBot;

    public String diagnose(DiagnosisData data) throws Exception {
        return qBot.act(data);
    }

    public String startDiagnose(DiagnosisData data) {
        return qBot.selectionAction(data);
    }

    public String getQTable() {
        return qBot.getQTable();
    }
}

