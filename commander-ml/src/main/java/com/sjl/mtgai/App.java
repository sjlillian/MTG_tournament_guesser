package com.sjl.mtgai;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws SQLException {
        System.out.println("Hello World!");
        String manacost = "{5}{W}{W}";
        System.out.println(manacost.toCharArray());
        ArrayList<Character> m_c = new ArrayList<Character>();
        for (char c : manacost.toCharArray()) {
            m_c.add(c);            
        }
        ArrayList<Character> removeCharacters = new ArrayList<Character>();
        removeCharacters.add(Character.valueOf('{'));
        removeCharacters.add(Character.valueOf('}'));
        m_c.removeAll(removeCharacters);
        System.out.println(m_c);
        DataLayerController.DataCollector();
    }
}
