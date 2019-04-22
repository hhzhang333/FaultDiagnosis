package cn.edu.seu.diagnosis.model;

import cn.edu.seu.diagnosis.common.DataCollectorUtils;
import cn.edu.seu.diagnosis.model.domain.Elements;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Debug;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by hhzhang on 2018/12/10.
 */
@Service
public class CommonModelService {
    @Autowired
    private ModelGenerator modelGenerator;
    @Autowired
    private Elements elements;

    @Resource
    private DataCollectorUtils dataCollectorUtils;

    public Classifier trainModel(String model, String trainingDataPath, boolean isSensor) throws Exception {
        Instances dataset = modelGenerator.loadDataset(trainingDataPath, isSensor);

        Filter filter = new Normalize();
        dataset.randomize(new Debug.Random(1));
        filter.setInputFormat(dataset);
        Instances dataSensor = Filter.useFilter(dataset, filter);
        return modelGenerator.buildClassifier(dataSensor, model);
    }

    public String evaluateModel(Classifier model, Instances trainData, Instances testData) throws Exception {
        return modelGenerator.evaluateModel(model, trainData, testData);
    }


    public String classify(File modelPath, Instances instances) throws Exception {
        Classifier classifier = (Classifier) SerializationHelper.read(modelPath.getPath());

        return elements.getClassLabel().get((int) classifier.classifyInstance(instances.firstInstance()));
    }

    public void saveModel(Classifier model, String modelPath, String modelName) {
        modelGenerator.saveModel(model, modelPath, modelName);
    }

    public void readLine(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        HashMap<Integer, Integer> hashMap = new HashMap<>();

        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            String[] lineEle = line.split(",");
            if (hashMap.containsKey(lineEle.length)) {
                Integer count = hashMap.get(lineEle.length);
                count += 1;
                hashMap.put(lineEle.length, count);
            } else
                hashMap.put(lineEle.length, 1);
        }

        for (Integer key : hashMap.keySet()) {
            System.out.println(key + " " + hashMap.get(key));
        }
    }

    public void formataData(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String labelString = bufferedReader.readLine();

        int maxLength = 1800;
        int lebelLength = 0;

        ArrayList<String> formatLabels = new ArrayList<>();
        Gson gson = new Gson();
        String[] labels = gson.fromJson(labelString, String[].class);
        Set<String> qemuInstances = new HashSet<>();
        for (String label : labels) {
            if (label.contains("cgroup_qemu_instance")) {
                qemuInstances.add(label.split("\\.")[0]);
            } else {
                formatLabels.add(label);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String rlabel : formatLabels) {
            stringBuilder.append(rlabel).append(",");
        }

        HashMap<String, TreeMap<String, Integer>> instancesLabels = new HashMap<>();
        for (int i = 0; i < labels.length; i++) {
            for (String instance : qemuInstances) {
                if (labels[i].contains(instance)) {
                    if (!instancesLabels.containsKey(instance)) {
                        TreeMap<String, Integer> labelsId = new TreeMap<>();
                        labelsId.put(labels[i], i);
                        instancesLabels.put(instance, labelsId);
                    } else {
                        instancesLabels.get(instance).put(labels[i], i);
                    }
                }
            }
        }


        for (String key : instancesLabels.keySet()) {
            TreeMap<String, Integer> treeMap = instancesLabels.get(key);
            for (String writeLabel : treeMap.keySet()) {
                stringBuilder.append(writeLabel).append(",");
            }
            break;
        }

        for (int i = 1; i <= 17; i++) {
            stringBuilder.append(i).append(",");
        }

        stringBuilder.append("class");

        File labelFile = new File(file.getParent() + "/format.csv");
        dataCollectorUtils.writeToFile(labelFile, stringBuilder.toString());

        HashSet<Integer> removedInteger = new HashSet<>();

        for (String instance : qemuInstances) {
            removedInteger.addAll(instancesLabels.get(instance).values());
        }

        String valuesLine = null;

        while ((valuesLine = bufferedReader.readLine()) != null) {
            ArrayList<Double> systemValues = new ArrayList<>();
            HashMap<String, ArrayList<Double>> qemuValues = new HashMap<>();

            Double[] values = gson.fromJson(valuesLine, Double[].class);
            for (int i = 0; i < values.length; i++) {
                if (removedInteger.contains(i)) {
                    for (String instance : qemuInstances) {
                        Collection<Integer> instancdLabels = instancesLabels.get(instance).values();
                        if (instancdLabels.contains(i)) {
                            if (qemuValues.containsKey(instance)) {
                                qemuValues.get(instance).add(values[i]);
                            } else {
                                ArrayList<Double> labelValues = new ArrayList<>();
                                labelValues.add(values[i]);
                                qemuValues.put(instance, labelValues);
                            }
                        }
                    }
                } else {
                    systemValues.add(values[i]);
                }
            }

            File formatFile = new File(file.getParent() + "/format.csv");
            for (String key : qemuValues.keySet()) {
                StringBuilder valueBuilder = new StringBuilder();
                ArrayList<Double> realValues = new ArrayList<>();
                realValues.addAll(systemValues);
                realValues.addAll(qemuValues.get(key));
                if (realValues.size() != 1786)
                    continue;
                for (Double value : realValues) {
                    valueBuilder.append(value).append(",");
                }
                valueBuilder.append("health");
                String writeValue = valueBuilder.toString();
                dataCollectorUtils.writeToFile(formatFile, writeValue);
            }

        }
    }
}
