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

import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 * This example provides some examples of operations on time series.
 *
 * @author Jean Palate
 */
public class HelloDemetra6 {

    public static void main(String[] args) {
        TsData m1=Data.M1, m2=Data.M2, m3=Data.M3;
        // simple binary operations
        TsData diff=TsData.subtract(m3, m2);
        TsData ratio=TsData.divide(m1, m2);
        
        // simple unary operations
        TsData del=m1.delta(1);
        TsData M1=m1.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        
        TsDataTable table=new TsDataTable();
        table.insert(-1, diff);
        table.insert(-1, ratio);
        table.insert(-1, del);
        table.insert(-1, M1);
        
        System.out.println(table);
        
    }
}
