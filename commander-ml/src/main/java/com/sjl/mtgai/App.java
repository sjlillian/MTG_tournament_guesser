package com.sjl.mtgai;

import java.sql.SQLException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws SQLException {
        System.out.println("Hello World!");
        DataLayerController.DataCollector();
        LogicLayerController.logic();
        System.out.println("All Done!");
    }
}
