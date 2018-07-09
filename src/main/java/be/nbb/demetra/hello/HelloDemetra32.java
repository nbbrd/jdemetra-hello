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
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.implementation.RegArimaProcessingFactory;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * This example shows how to use X13 for forecasting.
 *
 * @author Jean Palate
 */
public class HelloDemetra32 {

    public static void main(String[] args) {
        // Test the new feature
        TsData input = Data.P;

        // Make a copy if you want to change something !
        RegArimaSpecification spec = RegArimaSpecification.RG5.clone();
        // td1coeff
        //spec.getRegression().getTradingDays().setTradingDaysType(TradingDaysType.WorkingDays);
        // No transitory change
        spec.getOutliers().remove(OutlierType.TC);
        // forecasts are retrieved dynamically
        IPreprocessor processor = spec.build();
        // If you use pre-specified variables/calendars, you should use:
        // IPreprocessor processor = spec.build(context);
        PreprocessingModel model = processor.process(input, null);
        
        // results can be retrieved directly or using the usual dictionary (generic approach) 
        TsData forecast = model.forecast(3, false);
        SarimaModel arima=model.estimation.getArima();
        
        TsData forecast2= model.getData("fcasts(3)", TsData.class);
        SarimaModel arima2=model.getData("arima", SarimaModel.class);
        System.out.println(arima);
        System.out.println(forecast);
    }

}
