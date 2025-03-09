package com.sjl.mtgai;

import com.sjl.mtgai.logicLayer.DataConverter;

public class LogicLayerController {

    public static void main(String[] args) {
        DataConverter.buildFrames(DataLayerController.getCollector());

        //Build the Random Forest
    }

}
