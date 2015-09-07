/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package be.nbb.demetra.hello;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * This example shows how to create and print a time series.
 * It also shows how to access and modify individual observations of the time series
 *
 * @author Philippe Charles
 */
public class HelloDemetra1 {

    public static void main(String[] args) {
        // 1. Create a starting period (frequency + year + position in year)
        TsPeriod firstPeriod = new TsPeriod(TsFrequency.Monthly, 1990, 0);

        // 2. Create the values. Missing values are identified by Double.NaN
        double[] values = {59.6, 56.6, 64.5, 58.7, Double.NaN, 57.3};

        // 3. Create a time series with the period and the values
        TsData data = new TsData(firstPeriod, values, false);
        // 3bis. We could also create the series directly. The last parameter indicates 
        // that the time series contains a copy of the data (and not the original data.
        TsData data_bis=new TsData(TsFrequency.Monthly, 1990, 0, values, true);

        // 4. Print the time series
        System.out.println(data);
        
        // 5. Data can be accessed and modified using their positions (0-based).
        System.out.println(data.get(0));
        data.set(1, 100);
        System.out.println(data);
        System.out.println(data_bis);
        
    }
}
