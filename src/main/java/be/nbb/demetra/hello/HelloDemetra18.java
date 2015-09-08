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

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.PreadjustmentType;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.IterativeGlsSarimaMonitor;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;

/**
 * RegArima models. Definition and estimation through different methods
 * 
 * @author Jean Palate
 */
public class HelloDemetra18 {

    public static void main(String[] args) {
        ModelDescription model = new ModelDescription(Data.X, null);
        model.setTransformation(DefaultTransformationType.Log,
                PreadjustmentType.LengthOfPeriod);
        model.setAirline(false);
        ITsVariable td = GregorianCalendarVariables.
                getDefault(TradingDaysType.TradingDays);
        ITsVariable sd = new SeasonalDummies(Data.X.getFrequency());
        model.getCalendars().add(new Variable(td));
        model.getUserVariables().add(new Variable(sd, ComponentType.Seasonal));

// Generate the low-level regression model
        RegArimaModel<SarimaModel> regarima = model.buildRegArima();

// Actual estimation
// Tramo-like
        GlsSarimaMonitor gls = new GlsSarimaMonitor();
        RegArimaEstimation<SarimaModel> glsEstimation = gls.process(regarima);

// X13-like
        IterativeGlsSarimaMonitor igls = new IterativeGlsSarimaMonitor();
        RegArimaEstimation<SarimaModel> iglsEstimation = igls.process(regarima);

        System.out.println();
        System.out.println("Gls");
        System.out.println(glsEstimation.model.getArima());
        System.out.println(glsEstimation.statistics(1, 0));
        System.out.println();
        System.out.println("IGls");
        System.out.println(iglsEstimation.model.getArima());
        System.out.println(iglsEstimation.statistics(1, 0));
    }
}
