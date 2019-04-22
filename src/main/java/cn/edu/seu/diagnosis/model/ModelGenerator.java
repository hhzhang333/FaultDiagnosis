package cn.edu.seu.diagnosis.model;

import cn.edu.seu.diagnosis.model.domain.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by seuzhh on 2018/12/8.
 */
@Component
public class ModelGenerator {


    @Autowired
    private Elements elements;

    public Instances loadDataset(String path, boolean isSensor) throws Exception {
        File loadFile = new File(path);

        if (loadFile.isDirectory()) {
            for (File item : loadFile.listFiles()) {
//                elements.addInstance(item);

            }
        }

        return elements.getDataInstances();
    }

    public Classifier buildClassifier(Instances trainDataset, String modelName) throws Exception {
        Classifier model = (Classifier) Class.forName(modelName).newInstance();
        model.buildClassifier(trainDataset);
        return model;
    }

    public String evaluateModel(Classifier model, Instances trainDataset, Instances testDataset) throws Exception {
        Evaluation eval = null;
        // Evaluate classifier with test dataset
        eval = new Evaluation(trainDataset);
        eval.evaluateModel(model, testDataset);
        return eval.toSummaryString("", true);
    }

    public void saveModel(Classifier model, String modelpath, String modelName) {

        try {
            Path path = Paths.get(modelpath).toAbsolutePath().normalize();
            Files.createDirectories(path);
            String file = path.toString() + "/" + modelName;
            SerializationHelper.write(file, model);
        } catch (Exception ex) {
            Logger.getLogger(ModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
