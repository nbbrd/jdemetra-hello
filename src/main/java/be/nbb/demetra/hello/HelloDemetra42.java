/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.demetra.hello;

import ec.benchmarking.denton.DentonMethod;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
public class HelloDemetra42 {

    public static void main(String[] cmds) {
        // Tests
        System.out.println(process(Data.M3.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true), TsFrequency.Monthly, new DentonSpecification()));
        System.out.println(process(Data.M2, Data.M3.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true), new DentonSpecification()));
    }

    static TsData process(TsData q, TsData Y, DentonSpecification spec) {
        DentonMethod denton = new DentonMethod();
        denton.setAggregationType(spec.getType());
        denton.setDifferencingOrder(spec.getDifferencing());
        denton.setMultiplicative(spec.isMultiplicative());
        denton.setModifiedDenton(spec.isModified());
        int yfreq = Y.getFrequency().intValue();
        int qfreq = q.getFrequency().intValue();
        if (qfreq % yfreq != 0) {
            return null;
        }
        denton.setConversionFactor(qfreq / yfreq);
        // Y is limited to q !
        TsPeriodSelector qsel = new TsPeriodSelector();
        qsel.between(q.getStart().firstday(), q.getLastPeriod().lastday());
        Y = Y.select(qsel);
        TsPeriod q0 = q.getStart(), yq0 = new TsPeriod(q0.getFrequency());
        yq0.set(Y.getStart().firstday());
        denton.setOffset(yq0.minus(q0));
        double[] r = denton.process(q, Y);
        return new TsData(q.getStart(), r, false);
    }

    static TsData process(TsData Y, TsFrequency f, DentonSpecification spec) {
        DentonMethod denton = new DentonMethod();
        denton.setAggregationType(spec.getType());
        denton.setDifferencingOrder(spec.getDifferencing());
        denton.setMultiplicative(spec.isMultiplicative());
        denton.setModifiedDenton(spec.isModified());
        int yfreq = Y.getFrequency().intValue();
        int qfreq = f.intValue();
        if (qfreq % yfreq != 0) {
            return null;
        }
        denton.setConversionFactor(qfreq / yfreq);
        TsPeriod qstart = Y.getStart().firstPeriod(f);
        double[] r = denton.process(Y);
        return new TsData(qstart, r, false);
    }

}

class DentonSpecification {

    private boolean mul = true, modified = true;
    private int differencing = 1;
    private TsAggregationType type = TsAggregationType.Average;

    /**
     * @return the mul
     */
    public boolean isMultiplicative() {
        return mul;
    }

    /**
     * @param mul the mul to set
     */
    public void setMultiplicative(boolean mul) {
        this.mul = mul;
    }

    /**
     * @return the modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * @return the differencing
     */
    public int getDifferencing() {
        return differencing;
    }

    /**
     * @param differencing the differencing to set
     */
    public void setDifferencing(int differencing) {
        this.differencing = differencing;
    }

    /**
     * @return the type
     */
    public TsAggregationType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(TsAggregationType type) {
        this.type = type;
    }
}
