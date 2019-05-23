package cn.edu.seu.diagnosis.model.domain;

import cn.edu.seu.diagnosis.common.DataCollectorUtils;
import cn.edu.seu.diagnosis.config.DataCollectorConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhzhang on 2018/12/26.
 */
@Getter
@Service
public class Elements {
    @Autowired
    DataCollectorConfig dataCollectorConfig;

    @Autowired
    private DataCollectorUtils dataCollectorUtils;
    private Instances dataInstances;
    private ArrayList<String> classLabel;

//    public void addInstance(File file) throws IOException {
//        this.newInstances();
//        FileReader fileReader = new FileReader(file);
//        BufferedReader bufferedReader = new BufferedReader(fileReader);
//
//        String line;
//        while ((line = bufferedReader.readLine()) != null) {
//            this.addInstance(line);
//        }
//        bufferedReader.close();
//        fileReader.close();
//    }

    public void addInstance(List<Double> elements) {
        double[] data = new double[elements.size()];
        for (int i = 0; i < elements.size(); i++)
            data[i] = elements.get(i);
        dataInstances.add(new DenseInstance(1.0, data));
    }

    public Instances newInstances() {
        if (dataInstances == null) {
            ArrayList<Attribute> attributes = new ArrayList<>();

            for (String colEle : dataCollectorConfig.statisticsElement) {
                Attribute attribute = new Attribute(colEle);
                attributes.add(attribute);
            }

            for (String item: dataCollectorConfig.evaluateElement) {
                Attribute attribute = new Attribute(item);
                attributes.add(attribute);
            }

            classLabel = new ArrayList<>();
            classLabel.add("error");
            classLabel.add("health");
            attributes.add(new Attribute("class", new ArrayList<>(classLabel)));
            dataInstances = new Instances("sensor_data_relateion", attributes, 0);
            dataInstances.setClassIndex(dataInstances.numAttributes() - 1);
        }
        dataInstances.clear();
        return dataInstances;
    }
}
