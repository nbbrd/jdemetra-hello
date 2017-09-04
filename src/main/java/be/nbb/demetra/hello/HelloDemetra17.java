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

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.PreadjustmentType;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;


/**
 * Definition of a RegArima model
 * 
 * @author Jean Palate
 */
public class HelloDemetra17 {

    public static void main(String[] args) {
       // Create a model for the series Data.X. The entire time domain of X is
        // modelled (second parameter setto null).
        ModelDescription model=new ModelDescription(Data.X, null);
        // Add log transformation and preliminary correction for leap year.
        model.setTransformation(DefaultTransformationType.Log, PreadjustmentType.LengthOfPeriod);
        // Use a (0 1 1) (without parameters)
        model.setAirline(false);
        // Add trading days (without leap year) and seasonal dummies
        GregorianCalendarVariables td=GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        ITsVariable sd= new SeasonalDummies(Data.X.getFrequency());
        model.addVariable(Variable.calendarVariable(td, RegStatus.Prespecified));
        model.addVariable(Variable.userVariable(sd, ComponentType.Seasonal, RegStatus.Prespecified));
        // Get the list of the regression variables
        TsVariableList X = model.buildRegressionVariables();
        // Generate the low-level regression model
        RegArimaModel<SarimaModel> regarima = model.buildRegArima();
        // For testing purposes: computes the differenced regression variables
        Matrix variables = regarima.getDModel().variables();
        System.out.println(variables);
        // Find the description of the different variables and their position
        // We already can compute the likelihood of the model, using the default parameter of the
        // Arima model
        ConcentratedLikelihood cll = regarima.computeLikelihood();
        // Get the coefficient
        double[] B=cll.getB();
        // Get the T-Stat
        double[] T=cll.getTStats();
        int ipos=model.getRegressionVariablesStartingPosition();
        for (TsVariableSelection.Item<ITsVariable> var : X.all().elements()){
            for (int j=0; j<var.variable.getDim(); ++j){
                System.out.print(var.variable.getItemDescription(j)+ " "+(var.position+j));
                System.out.println(" "+B[ipos]+" "+T[ipos]);
                ++ipos;
            }
        }
    }
}
