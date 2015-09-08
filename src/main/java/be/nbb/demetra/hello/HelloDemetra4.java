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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;

/**
 * This example shows the use of DescriptiveStatistics.
 * Descriptive statistics can be computed on any object implementing the 
 * IReadDataBlock, like a usual DataBlock or a TsData. 
 *
 * @author Jean Palate
 */
public class HelloDemetra4 {

    public static void main(String[] args) {
        DataBlock random = DataBlock.random(10000);
        DescriptiveStatistics stats_rnd=new DescriptiveStatistics(random);
        // should be 0.5 and sqrt(1/12)=.288675
        System.out.println(stats_rnd.getAverage());
        System.out.println(stats_rnd.getStdev());
        
        DescriptiveStatistics stats_ts=new DescriptiveStatistics(Data.P);
        System.out.println(stats_ts.getAverage());
        System.out.println(stats_ts.getMedian());
        
    }
}
