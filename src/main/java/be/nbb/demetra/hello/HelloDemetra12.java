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
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 * This example shows how to use the detailed output of Tramo-Seats.
 *
 * @author Jean Palate
 */
public class HelloDemetra12 {

    public static void main(String[] args) {
        // Create/get the ts (for example...)
        TsData input = Data.P;
        // Using a pre-defined specification
        TramoSeatsSpecification rsafull=TramoSeatsSpecification.RSAfull;
        // Process
        CompositeResults rslts = TramoSeatsProcessingFactory.process(input, rsafull);
        // Get the output of Seats
        SeatsResults seats=rslts.get(TramoSeatsProcessingFactory.DECOMPOSITION, SeatsResults.class);
        // Ucarima decomposition
        UcarimaModel ucarima = seats.getUcarimaModel();
        // trend model
        System.out.println(ucarima.getComponent(0));
        // complement of the trend (=seasonal + irregular). Computed dynamically (not part of the usual  output)
        System.out.println(ucarima.getComplement(0));
        
        // Get the output of Tramo
        PreprocessingModel model=rslts.get(TramoSeatsProcessingFactory.PREPROCESSING, PreprocessingModel.class);
        // compute backcasts (dynamically)
        TsData backcast = model.backcast(100, false);
        System.out.println(backcast);
    }
}
