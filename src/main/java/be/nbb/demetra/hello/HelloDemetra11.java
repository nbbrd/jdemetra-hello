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

import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import java.util.Map;

/**
 * This example shows how to use the generic interface in X12
 *
 * @author Jean Palate
 */
public class HelloDemetra11 {

    public static void main(String[] args) {
        // Create/get the ts (for example...)
        TsData input = Data.P;
        // Just to store the results (not necessary in other usages)
        TsDataTable table =new TsDataTable();
        table.insert(-1, input);
        // Using a pre-defined specification
        X13Specification rsa5=X13Specification.RSA5;
        // Process
        IProcResults rslts = X13ProcessingFactory.process(input, rsa5);

        TsData sa = rslts.getData("sa", TsData.class);
        table.insert(-1, sa);
        TsData trend = rslts.getData("t", TsData.class);
        table.insert(-1, trend);
        
        // Create a user defined specification (starting from a copy of RSAfull)         
        X13Specification mySpec = rsa5.clone();
        // Very sensitive outliers detection
        mySpec.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(2.5);
        // Allow benchmarking
        mySpec.getBenchmarkingSpecification().setEnabled(true);
        // Process
        IProcResults myrslts = X13ProcessingFactory.process(input, mySpec);

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
        
        Map<String, Class> dictionary = myrslts.getDictionary();
        dictionary.keySet().forEach(x->System.out.println(x));
    }
}
