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

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.lang.reflect.Array;
import java.util.List;

/**
 * This example shows how to use the generic interface in Tramo-Seats
 *
 * @author Jean Palate
 */
public class HelloDemetra30 {

    public static void main(String[] args) {
        // Create/get the ts (for example...)
        TsData input = Data.P;
        // Using a pre-defined specification
        TramoSeatsSpecification spec = TramoSeatsSpecification.RSAfull;
        InformationSet ispec = spec.write(false);
        InformationSet info = new InformationSet();
        info.set(InformationSet.split("tramo.automdl.enabled"), false);
        info.set(InformationSet.split("tramo.arima.btheta"), new Parameter[]{new Parameter(-.8, ParameterType.Fixed)});

        Object p = Array.newInstance(Parameter.class, 5);
        Array.set(p, 1, new Parameter(5, ParameterType.Fixed));
        
        update(ispec, info);
        TramoSeatsSpecification nspec = new TramoSeatsSpecification();
        nspec.read(ispec);
        // Process
        IProcResults rslts = TramoSeatsProcessingFactory.process(input, nspec);

        System.out.println(rslts.getData("arima", Object.class));
    }

    private static void update(InformationSet info, InformationSet det) {
        List<String> dictionary = det.getDictionary();
        for (String s : dictionary) {
            Object obj = det.search(s, Object.class);
            info.set(InformationSet.split(s), obj);
        }
    }
}
