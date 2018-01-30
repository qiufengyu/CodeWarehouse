package cn.edu.nju;

import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class MapReduceFinal
{
    public static void main( String[] args ) throws IOException {
        if(args.length != 3) {
            System.err.println("Usage: cn.edu.nju.MapReduceFinal args[0] args[1] args[2]");
            System.err.println("args[0] = inputTrain/inputTest (corpus for training/testing HMM) ");
            System.err.println("args[1] = output");
            System.err.println("args[2] = train/test");
            System.exit(-1);
        }

        PropertyConfigurator.configure("log4j.properties");


        if(args[2].equals("train")) {
            if (Dictionary.dict(args)) {
                HMMTrain.HMMTrainMain(args);
                // HMMTest.HMMTestMain(args);
            }
        }
        else if(args[2].equals("test")) {
            HMMTest.HMMTestMain(args);
        }



        // String a = "1234567";
        // System.out.print(a.substring(0, a.length()-1));

        /*
        String t = "1\t\t 但/c 自从/p 毛主席/nh 向/p 全国/n 农村/n 发出/v \"/w 农业/n 学/v 大/a 寨/n \"/w 的/u 号召/v 以后/nt ，/w 沙石峪/ns 人和/n 大/a 寨/n 一/m 比/p ，/w 立刻/d 找到/v 许多/a 差距/n ，/w 农田/n 基本建设/n 上/nd 的/u 差距/n 显得/v 尤其/d 突出/a ：/w 沙石峪/ns 那/r 瘠/a 薄/a 的/u 土地/n 和/c 大/a 寨/n 的/u 稳产高产/n 海绵/n 田/n 实在/d 不能/vu 相比/v  [啊] /e ！/w \n";
        t = t.replaceAll(" \\[", "");
        t = t.replaceAll("\\] ", "");

        String[] parts = t.split("\\s");
        for(String x: parts) {
            System.out.println(x+" len = "+x.length());
        }
        */



    }
}