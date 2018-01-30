package cn.edu.nju;

import java.io.*;

/**
 * Created by Godfray on 2017/2/24.
 */
public class DataFilter {

    public static void Filter(String[] args) throws IOException {

        File dir = new File(".\\input\\");
        String newPath = "D:\\corpus\\";
        File[] array = dir.listFiles();
        for(File f: array) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newPath+f.getName())));
            String line;
            while(true) {
                line = br.readLine();
                if(line == null)
                    break;
                line = clean(line.trim());
                String[] parts = line.split("\\s+");

                StringBuilder out = new StringBuilder();

                for(int i = 1; i<parts.length; ++i) {
                    out.append(parts[i] + " ");
                }

                bw.write(out.toString().trim()+"\n");
                bw.flush();
            }
            br.close();
            bw.close();
            System.out.println(f.getName() +" OK!");
        }



    }


    public static String clean(String s) {
        String t = s.trim();
        t = t.replaceAll(" \\[", "");
        t = t.replaceAll("\\] ", "");
        return t;
    }

}

