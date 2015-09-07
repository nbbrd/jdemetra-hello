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

import ec.benchmarking.simplets.TsCholette;
import ec.benchmarking.simplets.TsDenton;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;

/**
 * This example shows how to benchmark a series on another (aggregated) series 
 * by means of Denton and by means of Cholette
 *
 * @author Jean Palate
 */
public class HelloDemetra8 {

    public static void main(String[] args) {
        
        TsDataTable table=new TsDataTable();
        // Initial series. We take Data.X and the 
        // quarterly aggregated Data.X with some added noise as benchmark
        TsData x=Data.X;
        TsData Xy=addNoise(x.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true));
        
        TsDenton denton=new TsDenton();
        denton.setMultiplicative(false);
        TsData xbench=denton.process(x, Xy);
        
        denton.setMultiplicative(true);
        TsData xbenchm=denton.process(x, Xy);
        
        // proportional benchmarking
        TsCholette cholette=new TsCholette();
        cholette.setLambda(0.5);
        cholette.setRho(0);
        TsData xbenchc=cholette.process(x, Xy);
        
        table.insert(-1, x);
        table.insert(-1, xbench);
        table.insert(-1, xbenchm);
        table.insert(-1, xbenchc);
        
        System.out.println(table);
    }
    
    private static TsData addNoise(TsData s){
        
        Random rnd=new Random();
        TsData snoisy=new TsData(s.getDomain()); 
        for (int i=0; i<s.getLength(); ++i){
            double x=s.get(i);
            snoisy.set(i, x*(1+0.1*rnd.nextGaussian()));
        }
        return snoisy;
    }
}
