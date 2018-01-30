package cn.edu.nju;

import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class Triangle {
    
    public Triangle() {
    }

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        if(args.length != 3) {
            System.out.println(args.length);
            System.err.println("Usage: cn.edu.nju.Triangle args[0] args[1] args[2]=or/and");
            System.exit(-1);
        }

        PropertyConfigurator.configure("log4j.properties");

        System.out.println("Job Triangle Running...");

        if(args[2].compareToIgnoreCase("or") == 0) {
            if (Undirected.main(args)) {
                if(Counter.main(args)) {
                    Adder.main(args);
                }
            }
        }
        else if(args[2].compareToIgnoreCase("and")==0) {
            if (Undirected.mainAnd(args)) {
                if(Counter.main(args)) {
                    Adder.main(args);
                }
            }
        }
        else {
            System.err.println("Usage: cn.edu.nju.Triangle args[0] args[1] args[2]=or/and");
            System.exit(-1);
        }


    }
}
