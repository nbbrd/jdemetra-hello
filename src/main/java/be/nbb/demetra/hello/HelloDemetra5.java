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

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.YearIterator;

/**
 * This example shows the use of Iterators on time series. The series can be split
 * by years or by periods in a straightforward way.
 *
 * @author Jean Palate
 */
public class HelloDemetra5 {

    public static void main(String[] args) {
        YearIterator yiter = YearIterator.fullYears(Data.P.log());
        while (yiter.hasMoreElements()) {
            TsDataBlock year = yiter.nextElement();
            DescriptiveStatistics stats = new DescriptiveStatistics(year.data);
            System.out.print(year.start);
            System.out.print('\t');
            System.out.println(stats.getStdev());
        }
        System.out.println();

        PeriodIterator piter = PeriodIterator.fullYears(Data.P.log());
        while (piter.hasMoreElements()) {
            TsDataBlock year = piter.nextElement();
            DescriptiveStatistics stats = new DescriptiveStatistics(year.data);
            System.out.print(year.start);
            System.out.print('\t');
            System.out.print(stats.getAverage());
            System.out.print('\t');
            System.out.println(stats.getStdev());
        }
    }
}
