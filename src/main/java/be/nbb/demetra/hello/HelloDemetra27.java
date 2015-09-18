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
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.algorithm.implementation.TramoProcessingFactory;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * This example shows the use of user-defined regression variables in JD+
 *
 * @author Jean Palate
 */
public class HelloDemetra27 {

    public static void main(String[] args) {

        TsData m1 = Data.M1;
        TsData m2 = Data.M2;
        TsData m3 = Data.M3;

        ProcessingContext ctxt = new ProcessingContext();
        TsVariables vars = new TsVariables();
        vars.set("m2", new TsVariable(m2));
        vars.set("m3", new TsVariable(m3));
        ctxt.getTsVariableManagers().set("data", vars);
        TramoSeatsSpecification spec = TramoSeatsSpecification.RSAfull.clone();
        TsVariableDescriptor xvar2 = new TsVariableDescriptor();
        xvar2.setLastLag(5);
        xvar2.setName("data.m2");
        spec.getTramoSpecification().getRegression().add(xvar2);
        TsVariableDescriptor xvar3 = new TsVariableDescriptor();
        xvar3.setLastLag(5);
        xvar3.setName("data.m3");
        spec.getTramoSpecification().getRegression().add(xvar3);
        
        SequentialProcessing<TsData> tramoseats = TramoSeatsProcessingFactory.instance.generateProcessing(spec, ctxt);
        CompositeResults rslt = tramoseats.process(m1.drop(5,0));
        String[] desc = rslt.getData("regression.description", String[].class);
        Parameter[] coeff=rslt.getData("regression.coefficients", Parameter[].class);
        int idx=0;
        for (String s : desc){
            System.out.print(s);
            System.out.print('\t');
            System.out.println(coeff[idx++]);
        }
    }

}
