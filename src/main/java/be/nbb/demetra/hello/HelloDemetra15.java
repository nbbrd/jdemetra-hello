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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.AbstractSingleTsVariable;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.timeseries.simplets.YearIterator;


/**
 * Implementing a new regression variable. Raw Julian Easter
 * 
 * @author Jean Palate
 */
public class HelloDemetra15 {

    public static void main(String[] args) {
        TsVariableList X=new TsVariableList();
        JulianEasterVariable var=new JulianEasterVariable();
        EasterVariable var2=new EasterVariable();
        X.add(var);
        X.add(var2);
        // Gets all the variables for 40 years
        Matrix matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 1980, 0, 480));
        System.out.println(matrix);
     }
}

class JulianEasterVariable extends AbstractSingleTsVariable {

    @Override
    public void data(TsPeriod start, DataBlock data) {
        // inefficient algorithm

        // Create first a series initialized at 0
        TsData var = new TsData(new TsDomain(start, data.getLength()), 0);

        YearIterator iter = new YearIterator(var);
        while (iter.hasMoreElements()) {
            TsDataBlock cur = iter.nextElement();
            Day julianEaster = ec.tstoolkit.timeseries.calendars.Utilities.julianEaster(cur.start.getYear(), true);
            // Creates the period that contains Easter
            TsPeriod p = new TsPeriod(start.getFrequency(), julianEaster);
            // search its position in this data block.
            int pos = p.minus(cur.start);
            // the series is NOT CORRECTED for long term mean effect.
            if (pos >= 0 && pos < cur.data.getLength()) {
                cur.data.set(pos, 1);
            }
        }
        data.copy(var);
    }

    @Override
    public String getDescription() {
        return "Julian Easter";
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        // Julian Easter (in Gregorian dates falls in April or in May) 
        return domain.getFrequency() == TsFrequency.Monthly || domain.getFrequency() == TsFrequency.QuadriMonthly;
    }

    @Override
    public String getDescription(TsFrequency context) {
         return "Julian Easter";
  }

    @Override
    public String getName() {
        return "Julian Easter";

    }
}

