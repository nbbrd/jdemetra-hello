/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
/*
 */
package be.nbb.demetra.hello;

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;

/**
 * This example shows how to use the generic interface in Tramo-Seats
 *
 * @author Jean Palate
 */
public class HelloDemetra10 {

    public static void main(String[] args) {
        // Create/get the ts (for example...)
        TsData input = Data.P;
        // Just to store the results (not necessary in other usages)
        TsDataTable table =new TsDataTable();
        table.insert(-1, input);
        // Using a pre-defined specification
        TramoSeatsSpecification rsafull=TramoSeatsSpecification.RSAfull.clone();
        // Actual number of forecasts
        // rsafull.getSeatsSpecification().setPredictionLength(50);
        // Or number of years
        rsafull.getSeatsSpecification().setPredictionLength(-4);
         // Process
        IProcResults rslts = TramoSeatsProcessingFactory.process(input, rsafull);

        TsData sa = rslts.getData("sa", TsData.class);
        table.insert(-1, sa);
        TsData trend = rslts.getData("t", TsData.class);
        table.insert(-1, trend);
         TsData saf = rslts.getData("sa_f", TsData.class);
        table.insert(-1, saf);
        TsData trendf = rslts.getData("t_f", TsData.class);
        table.insert(-1, trendf);
       
        // Create a user defined specification (starting from a copy of RSAfull)         
        TramoSeatsSpecification mySpec = rsafull.clone();
        // Very sensitive outliers detection
        mySpec.getTramoSpecification().getOutliers().setCriticalValue(2.5);
        // Allow benchmarking
        mySpec.getBenchmarkingSpecification().setEnabled(true);
        // Process
        IProcResults myrslts = TramoSeatsProcessingFactory.process(input, mySpec);

        TsData mysa = myrslts.getData("sa", TsData.class);
        table.insert(-1, mysa);
        TsData mytrend = myrslts.getData("t", TsData.class);
        table.insert(-1, mytrend);
//        System.out.println(sa);
        TsData mybench = myrslts.getData("benchmarking.result", TsData.class);
        table.insert(-1, mybench);

        StatisticalTest skewness = rslts.getData("residuals.skewness", StatisticalTest.class);
        System.out.println(skewness.pvalue);
        StatisticalTest myskewness = myrslts.getData("residuals.skewness", StatisticalTest.class);
        System.out.println(myskewness.pvalue);
        System.out.println();
        System.out.println(table);
        
        System.out.println("Forecasts");
        System.out.println(myrslts.getData("fcasts(-5)", TsData.class));
        System.out.println("Backcasts");
        System.out.println(myrslts.getData("bcasts(-1)", TsData.class));
    }
}
