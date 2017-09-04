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

import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.diagnostics.SeasonalityTest;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.lang.reflect.Array;
import java.util.List;

/**
 * This example shows how to add new entries in the output map.
 *
 * @author Jean Palate
 */
public class HelloDemetra31 {

    public static void main(String[] args) {
        // add an entry in the map
        DefaultSeriesDecomposition.setMapping("sa.last.seasonality.friedman.value", Double.class,
                decomposition -> {
                    TsData sa = decomposition.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                    if (sa == null) {
                        return null;
                    }
                    TsPeriodSelector sel = new TsPeriodSelector();
                    sel.last(120);
                    SeasonalityTests test = SeasonalityTests.seasonalityTest(sa.select(sel), 1, true, true);
                    return test.getNonParametricTest().getValue();
                }
        );
        // Test the new feature
        TsData input = Data.P;
        TramoSeatsSpecification spec = TramoSeatsSpecification.RSAfull;
        // Process
        IProcResults rslts = TramoSeatsProcessingFactory.process(input, spec);

        System.out.println(rslts.getData("sa.last.seasonality.friedman.value", Double.class));
    }

}
