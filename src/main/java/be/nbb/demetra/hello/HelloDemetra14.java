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

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.ChangeOfRegime;
import ec.tstoolkit.timeseries.regression.ChangeOfRegimeType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;


/**
 *This example shows how to handle regression variables
 * 
 * @author Jean Palate
 */
public class HelloDemetra14 {

    public static void main(String[] args) {
        TsVariableList X=new TsVariableList();
        
        GregorianCalendarVariables td=GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        X.add(td);
        X.add(new LeapYearVariable(LengthOfPeriodType.LeapYear));
        X.add(new ChangeOfRegime(td, ChangeOfRegimeType.ZeroStarted, new Day(2000, Month.January, 0)));
        X.add(new SeasonalDummies(TsFrequency.Monthly));
        
        // Gets all the variables for 40 years
        Matrix matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 1980, 0, 480));
        // The matrix is full rank
        System.out.println(matrix.rank() == X.getVariablesCount());
        // Gets all the variables since 1/1/2000
        matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 2000, 0, 240));
        // The matrix is rank-deficient (the TD and the TD with change of regime are identical)
        System.out.println(matrix.rank() == X.getVariablesCount()-6);
        // TD, TD with change of regime, LP (or 6 + 6 + 1)
        System.out.println(X.selectCompatible(ICalendarVariable.class).getVariablesCount() == 13);
        // TD only
        System.out.println(X.select(ITradingDaysVariable.class).getVariablesCount() == 6);
        // description of the regression variables
        for (ITsVariable var : X.items()){
            System.out.println(var.getDescription());
            if (var.getDim() > 1)
                for (int j=0; j<var.getDim(); ++j)
             System.out.println("    "+var.getItemDescription(j));
       }
        System.out.println(matrix);
    }
}
