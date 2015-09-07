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

import ec.tstoolkit.modelling.arima.CheckLast;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * This example shows how to use the CheckLast class, which provides features
 * similar to "Terror" (automatic detection of anomalies at the end of the series).
 * The example uses the modelling part of Tramo and of X12.
 *
 * @author Jean Palate
 */
public class HelloDemetra9 {

    public static void main(String[] args) {
        IPreprocessor tramo = TramoSpecification.TRfull.build();
        CheckLast cl=new CheckLast(tramo);
        TsData P=Data.P.clone();
        TsData Pc=Data.P.clone();
        int n=Pc.getLength()-1;
        Pc.set(n, Pc.get(n)*0.9);
        cl.check(P);
        System.out.println(cl.getScore(0));
        cl.check(Pc);
        System.out.println(cl.getScore(0));
        IPreprocessor regarima = RegArimaSpecification.RG5.build();
        CheckLast cl2=new CheckLast(regarima);
        cl2.check(P);
        System.out.println(cl2.getScore(0));
        cl2.check(Pc);
        System.out.println(cl2.getScore(0));
    }
}
