package cn.edu.seu.diagnosis.common;

import com.google.gson.Gson;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hhzhang on 2018/12/5.
 */
@Component
public class DataCollectorUtils {

    static final String cronPression = "0/range * * * * *";

    public String getRemoteSystemStatus(String requestURL, String broswerAgent) throws IOException {
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", broswerAgent);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public void writeToFile(File file, String content) throws IOException {

        FileWriter fileWriter = new FileWriter(file, true);
        PrintWriter printWriter = new PrintWriter(fileWriter);

//        fileCheck(file, );
        printWriter.println(content);

        fileWriter.flush();
        printWriter.close();
        fileWriter.close();
    }

    public List<Double> parseProperties(List<String> properties, String content) {
        Map<String, Object> result = new Gson().fromJson(content, Map.class);
        List<Double> doubles = new ArrayList<>();
        for (String item : properties) {
            String[] indexes = item.split(">");
            Map<String, Object> innerResult = (Map<String, Object>) result.get(indexes[0]);
            if (innerResult == null)
                continue;
            Map<String, Object> thireInnerResult = (Map<String, Object>) innerResult.get("dimensions");
            Map<String, Object> lastResult = (Map<String, Object>) thireInnerResult.get(indexes[1]);
            Double value = (Double) lastResult.get("value");
            doubles.add(value);

        }
        return doubles;
    }

    public List<String> parseLabels(List<String> properties, String content) {
        Map<String, Object> result = new Gson().fromJson(content, Map.class);
        List<String> strings = new ArrayList<>();

        for (String item : properties) {
            Map<String, Object> innerValues = (Map<String, Object>) result.get(item);
            String name = (String) innerValues.get("name");
            Map<String, Object> dimensionsValue = (Map<String, Object>) innerValues.get("dimensions");
            for (String innerKey : dimensionsValue.keySet()) {
                String label = name + "-" + innerKey;
                strings.add(label);
            }
        }
        return strings;
    }

    public List<String> parseQemuLabels(String content) {
        Map<String, Object> result = new Gson().fromJson(content, Map.class);
        List<String> strings = new ArrayList<>();
        Set<String> qemuInstances = new HashSet<>();
        for (String qemu : result.keySet()) {
            if (qemu.contains("cgroup_qemu_instance")) {
                qemuInstances.add(qemu);
            }
        }

        return strings;
    }

    public String parseContentLabel(String content) {
        Map<String, Object> values = new Gson().fromJson(content, Map.class);
        List<String> labels = new ArrayList<>();
        for (String outKey : values.keySet()) {
            Map<String, Object> innerValues = (Map<String, Object>) values.get(outKey);
            String name = (String) innerValues.get("name");
            String units = (String) innerValues.get("units");
            Map<String, Object> dimensionsValue = (Map<String, Object>) innerValues.get("dimensions");
            for (String innerKey : dimensionsValue.keySet()) {
                String label = name + "-" + units + "-" + innerKey;
                labels.add(label);
            }
        }
        return new Gson().toJson(labels);
    }

    public String parseContentValue(String content) {
        List<Double> values = new ArrayList<>();
        Map<String, Object> outValues = new Gson().fromJson(content, Map.class);
        for (String outKey : outValues.keySet()) {
            Map<String, Object> innerValues = (Map<String, Object>) outValues.get(outKey);
            Map<String, Object> dimensionsValue = (Map<String, Object>) innerValues.get("dimensions");
            for (String innerKey : dimensionsValue.keySet()) {
                Map<String, Object> realValue = (Map<String, Object>) dimensionsValue.get(innerKey);
                Double value = (Double) realValue.get("value");
                values.add(value);
            }
        }
        return new Gson().toJson(values);
    }

    public long normalizeValue(Double value, int labelRange) {
        return Math.round(value) / labelRange * labelRange;
    }

    public static String getRandomFileName() {
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String str = simpleDateFormat.format(date);
        Random random = new Random();
        int rannum = (int) (random.nextDouble() * (999999 - 100000 + 1)) + 100000;// 获取随机数
        return str + rannum;// 当前时间
    }

    /*
    检查文件大小，若超过maxSize，则新建文件
     */
    public void fileCheck(File file, int maxSize) throws IOException {
        //MB
        if (file.length() / 1024 / 1024 / 1024 > maxSize) {
            String newFilePath = file.getParent() + "/" + "undone-" + getRandomFileName();
            file.renameTo(new File(newFilePath));
        }
    }

    public boolean isInit(File file) {
        return file.exists() && file.length() != 0;
    }

    public Path resolveFileUrl(String directory, String filename) throws IOException {
        Path path = Paths.get(directory).toAbsolutePath().normalize();
        Files.createDirectories(path);
        return path.resolve(filename);
    }

//    public File initArffFile(Path path,
//                             String arffRelation,
//                             List<String> statisticsElement,
//                             List<String> evaluateValueRange,
//                             boolean isSensor) throws IOException {
//        File file = path.toFile();
//        if (file.length() == 0) {
//            FileWriter fileWriter = new FileWriter(file, true);
//            PrintWriter printWriter = new PrintWriter(fileWriter);
//
//            printWriter.println(arffRelation);
//            for (String value: statisticsElement) {
//                printWriter.println("@attribute " + value + " numeric");
//            }
//
////            for (String value: evaluateElement) {
////                printWriter.println("@attribute " + value + " numeric");
////            }
//
//            HashSet<String> classifiedClass = initClassificatedClass(evaluateValueRange, "test");
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("{");
//            for (String item : classifiedClass)
//                stringBuilder.append(item).append(",");
//            String label = stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("}").toString();
//
//            printWriter.println("@attribute sensorClassLabel " + label);
//            if (!isSensor)
//                printWriter.println("@attribute class string");
//            printWriter.println("@data");
//            fileWriter.flush();
//            printWriter.close();
//            fileWriter.close();
//        }
//        return file;
//    }


    public List<ArrayList<Double>> initTable(String filename) throws IOException {
        List<List<Double>> samples = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            List<Double> sample = new ArrayList<>();
            sample.add(-1000000.0);
            sample.add(1000000.0);
            samples.add(sample);
        }

        File file = new ClassPathResource(filename).getFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        bufferedReader.readLine();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            String[] sampleItem = line.split(",");
            if (sampleItem.length < 10)
                continue;
            for (int i = 0; i < sampleItem.length - 1; i++) {
                List<Double> items = samples.get(i);
                double value = Double.parseDouble(sampleItem[i]);
                if (value > items.get(0))
                    items.set(0, value);
                if (value < items.get(1))
                    items.set(1, value);
            }
        }

        List<ArrayList<Double>> result = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            Double max = samples.get(i).get(0);
            Double min = samples.get(i).get(1);
            double splits = (max + min) / 2;

            ArrayList<Double> sliceItem = new ArrayList<>();
            sliceItem.add(splits);
            sliceItem.add(max);
            result.add(sliceItem);
        }

        return result;
    }

    public List<List<Double>> initQtable(List<String> features, String filename) throws IOException {
        List<List<Double>> samples = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            List<Double> sample = new ArrayList<>();
            samples.add(sample);
        }

        File file = new ClassPathResource(filename).getFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            String[] sampleItem = line.split(",");
            if (sampleItem.length < 10)
                continue;
            for (int i = 0; i < sampleItem.length - 1; i++) {
                samples.get(i).add(Double.parseDouble(sampleItem[i]));
            }
        }

        return samples;
    }

    public List<ArrayList<Double>> initSpaceSlice(List<List<Double>> samples) {
        List<ArrayList<Double>> result = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            Double max = Double.MIN_VALUE;
            Double min = Double.MAX_VALUE;

            List<Double> current = samples.get(i);
            for (int j = 0; j < current.size(); j++) {
                if (max < current.get(i))
                    max = current.get(i);
                if (min > current.get(i))
                    min = current.get(i);
            }

            double splits = (max + min) / 2;

            ArrayList<Double> sliceItem = new ArrayList<>();
            for (double j = min; j <= max; j += splits) {
                sliceItem.add(min + splits);
            }
            result.add(sliceItem);
        }

        return result;
    }

    public ArrayList<String> initClassificatedClass(List<ArrayList<Double>> recurisiveArray) {
        ArrayList<String> results = new ArrayList<>();

        this.recursiveGenerate(new ArrayList<>(), recurisiveArray, 0, results);

        results.add("health");
        return results;
    }

    private void recursiveGenerate(ArrayList<Double> current, List<ArrayList<Double>> inputs, int depth, ArrayList<String> results) {
        if (depth == inputs.size()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Double ele : current) {
                stringBuilder.append(ele).append("_");
            }
            String value = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
            results.add(value);
            return;
        }

        ArrayList<Double> currentInputs = inputs.get(depth);
        for (Double item : currentInputs) {
            current.add(item);
            recursiveGenerate(current, inputs, depth + 1, results);
            current.remove(item);
        }

    }

    public CronTrigger generateCronTrigger(int range) {
        String trigger = cronPression.replace("range", String.valueOf(range));
        return new CronTrigger(trigger);
    }
}
