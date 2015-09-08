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
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 * This example shows how to use the detailed output of X12.
 *
 * @author Jean Palate
 */
public class HelloDemetra13 {

    public static void main(String[] args) {
        // Create/get the ts (for example...)
        TsData input = Data.P;
        // Using a pre-defined specification
        X13Specification rsa5=X13Specification.RSA5;
        // Process
        CompositeResults rslts = X13ProcessingFactory.process(input, rsa5);
        // M statistics
        Mstatistics mstats=rslts.get(X13ProcessingFactory.MSTATISTICS, Mstatistics.class);
        System.out.println(mstats.getQm2());
        
        X11Results x11=rslts.get(X13ProcessingFactory.DECOMPOSITION, X11Results.class);
        TsDataTable table =new TsDataTable();
        TsData b7=x11.getData("b7", TsData.class);
        TsData c7=x11.getData("c7", TsData.class);
        TsData d7=x11.getData("d7", TsData.class);
        table.insert(-1, b7);
        table.insert(-1, c7);
        table.insert(-1, d7);
        System.out.println(table);
        
        // Get the output of RegArima
        PreprocessingModel model=rslts.get(X13ProcessingFactory.PREPROCESSING, PreprocessingModel.class);
        TsDomain domain=new TsDomain(TsFrequency.Monthly, 2000, 0, 120);
        TsData td = model.tradingDaysEffect(domain);
        System.out.println(td);
    }
}
