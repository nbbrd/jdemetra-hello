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
 *
 * @author Philippe Charles
 */
public class HelloDemetra1 {

    public static void main(String[] args) {
        // 1. Create a starting period (frequency + year + position in year)
        TsPeriod firstPeriod = new TsPeriod(TsFrequency.Monthly, 1990, 0);

        // 2. Create the values
        double[] values = {59.6, 56.6, 64.5, 58.7, 57.3};

        // 3. Create a time series with the period and the values
        TsData data = new TsData(firstPeriod, values, false);

        // 4. Print the time series
        System.out.println(data);
    }
}
