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

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;

/**
 * This example shows how to create a time series by means of a TsDataCollector.
 * The frequency of the series can be automatically identified or the data can
 * be automatically aggregated when a frequency is specified.
 *
 * @author Jean Palate
 */
public class HelloDemetra2 {

    public static void main(String[] args) {
        TsDataCollector collector = new TsDataCollector();
        int n = 100;
        Day day = Day.toDay();
        Random rnd = new Random();
        for (int i = 0; i < n; ++i) {
            // Add a new observation (date, value)
            collector.addObservation(day.getTime(), i);
            day = day.plus(31 + rnd.nextInt(10));
        }
        // Creates a new time series. The most suitable frequency is automatically choosen
        TsData ts = collector.make(TsFrequency.Undefined, TsAggregationType.None);
        
        double a=2, b=3, c=.01;
        ts.set(i->a+b*i+c*i*i);
       System.out.println(ts);
 
        // Creates a new quarterly time series. The data in the same quarter are added up. 
        TsData S = collector.make(TsFrequency.Quarterly, TsAggregationType.Sum);
        System.out.println(S);
    }
}
