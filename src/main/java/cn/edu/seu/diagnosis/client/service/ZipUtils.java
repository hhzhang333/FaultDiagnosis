package cn.edu.seu.diagnosis.client.service;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by hhzhang on 2018/12/19.
 */
@Component
public class ZipUtils {
    public void zipDirectory(String directory, String filename) throws IOException {
        Path path = Paths.get(directory).toAbsolutePath().normalize();
        File file = path.toFile();
        File zipFile = new File(directory + File.separator + filename);
        InputStream input = null;
        ZipOutputStream zipOut = null;
        zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        int temp = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File item : files) {
                if (item.getName().contains("zip")) {
                    item.delete();
                    continue;
                }
                input = new FileInputStream(item);
                zipOut.putNextEntry(new ZipEntry(file.getName()
                        + File.separator + item.getName()));
                while ((temp = input.read()) != -1) {
                    zipOut.write(temp);
                }
                input.close();
            }
        }
        zipOut.close();
    }

    public void unzipFile(String directory, String filename) throws IOException {
        File file = new File(directory + File.separator + filename);
        File outFile = null;
        ZipFile zipFile = new ZipFile(file);
        ZipInputStream zipInput = null;
        OutputStream out = null;
        InputStream input = null;
        ZipEntry entry = null;
        zipInput = new ZipInputStream(new FileInputStream(file));
        while ((entry = zipInput.getNextEntry()) != null) {
            outFile = new File(directory + File.separator + entry.getName());
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdir();
            }
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            input = zipFile.getInputStream(entry);
            out = new FileOutputStream(outFile);
            int temp = 0;
            while ((temp = input.read()) != -1) {
                out.write(temp);
            }
            input.close();
            out.close();
        }
        input.close();
    }
}
