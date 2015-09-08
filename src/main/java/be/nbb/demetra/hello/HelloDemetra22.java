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

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;

/**
 * This example shows how to make simulation (random airlines series).
 *
 * @author Jean Palate
 */
public class HelloDemetra22 {

    public static void main(String[] args) {
// Create the specifications          
        TramoSeatsSpecification mySpec = TramoSeatsSpecification.RSA0.clone();
        int n = 10000;
        // Compute random monthly airline series (with th =-.6 and bth=-.8)
        List<TsData> rnd = Data.rndAirlines(n, 90, -.6, -.8);
        double[] th = new double[n], bth = new double[n];
        // Create the processing. The same processing will be used for all the series
        IProcessing<TsData, ?> processing = TramoSeatsProcessingFactory.instance.generateProcessing(mySpec, null);
        for (int i = 0; i < n; ++i) {
            // Estimate the model for each radom series. Retrieve the parameters of the model
            IProcResults rslts = processing.process(rnd.get(i));
            th[i] = rslts.getData("arima.th(1)", Parameter.class).getValue();
            bth[i] = rslts.getData("arima.bth(1)", Parameter.class).getValue();
        }

        DescriptiveStatistics TH = new DescriptiveStatistics(th);
        DescriptiveStatistics BTH = new DescriptiveStatistics(bth);
        System.out.println();
        double step = .005;
        for (int i = 0; i < 100; ++i) {
            double l = -.5 - i * step;
            double h = l - step;
            System.out.print(TH.countBetween(h, l));
            System.out.print('\t');
            System.out.println(BTH.countBetween(h, l));
        }
    }
}