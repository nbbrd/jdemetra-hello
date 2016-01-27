/*
 * Copyright 2013-2014 National Bank of Belgium
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
package be.nbb.demetra.hello;

import ec.benchmarking.simplets.TsCholette;
import ec.benchmarking.simplets.TsDenton;
import ec.benchmarking.simplets.TsDenton2;
import ec.benchmarking.simplets.TsExpander;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;

/**
 * Overview of the different methods for benchmarking, or temporal
 * disaggregation without indicator
 *
 * @author Jean Palate
 */
public class HelloDemetra28 {

    public static void main(String[] args) {

        TsData x = Data.X;
        TsData Xy = addNoise(x.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true));

        TsDataTable tstable = new TsDataTable();
        DentonProcessing denton = new DentonProcessing(x, Xy);
        denton.compute();

        DentonProcessing denton3 = new DentonProcessing(x, Xy);
        denton3.differencing = 3;
        denton3.compute();
        tstable.add(x);
        tstable.add(denton.xbench);
        tstable.add(denton3.xbench);

        SsfDentonProcessing ssfdenton = new SsfDentonProcessing(x, Xy);
        ssfdenton.compute();
        tstable.add(ssfdenton.xbench);

        CholetteProcessing cholette = new CholetteProcessing(x, Xy);
        cholette.compute();
        tstable.add(cholette.xbench);
        
        DentonProcessing dentonc = new DentonProcessing(TsFrequency.Monthly, Xy);
        dentonc.mul=false;
        dentonc.compute();
        tstable.add(dentonc.xbench);
        
        ExpanderProcessing expander=new ExpanderProcessing(TsFrequency.Monthly, Xy);
        expander.compute();
        tstable.add(expander.xbench);
        
        System.out.println(tstable);
    }

    static class DentonProcessing {

        DentonProcessing(TsData x, TsData y) {
            this.x = x;
            this.y = y;
            defaultFrequency = TsFrequency.Undefined;
        }

        DentonProcessing(TsFrequency freq, TsData y) {
            this.x = null;
            this.y = y;
            defaultFrequency = freq;
        }
        // inputs
        final TsData x, y;
        boolean mul = true, modified = true;
        int differencing = 1; // >=1, <=5
        TsAggregationType agg = TsAggregationType.Sum;
        final TsFrequency defaultFrequency; // only used when x == null

        // output
        TsData xbench;

        // processing
        boolean compute() {
            TsDenton2 denton = new TsDenton2();
            denton.setAggregationType(agg);
            denton.setDifferencingOrder(differencing);
            denton.setModified(modified);
            denton.setMultiplicative(mul);
            if (x == null) {
                denton.setDefaultFrequency(defaultFrequency);
            }
            xbench = denton.process(x, y);
            return xbench != null;
        }
    }

    static class SsfDentonProcessing {

        SsfDentonProcessing(TsData x, TsData y) {
            this.x = x;
            this.y = y;
        }

        // inputs
        final TsData x, y;
        boolean mul = true;
        TsAggregationType agg = TsAggregationType.Sum;

        // output
        TsData xbench;

        // processing
        boolean compute() {
            TsDenton denton = new TsDenton();
            denton.setAggregationType(agg);
            denton.setMultiplicative(mul);
            xbench = denton.process(x, y);
            return xbench != null;
        }
    }

    static class CholetteProcessing {

        CholetteProcessing(TsData x, TsData y) {
            this.x = x;
            this.y = y;
        }

        // inputs
        final TsData x, y;
        double rho = 1, lambda = 1;
        TsCholette.BiasCorrection bias = TsCholette.BiasCorrection.None;
        TsAggregationType agg = TsAggregationType.Sum;

        // output
        TsData xbench;

        // processing
        boolean compute() {
            TsCholette cholette = new TsCholette();
            cholette.setAggregationType(agg);
            cholette.setRho(rho);
            cholette.setLambda(lambda);
            cholette.setBiasCorrection(bias);
            xbench = cholette.process(x, y);
            return xbench != null;
        }
    }

    static class ExpanderProcessing {

        ExpanderProcessing(TsFrequency freq, TsData y) {
            this.y = y;
            defaultFrequency = freq;
            domain = null;
        }

        ExpanderProcessing(TsDomain domain, TsData y) {
            this.y = y;
            defaultFrequency = domain.getFrequency();
            this.domain = domain;
        }
        // inputs
        final TsData y;
        final TsFrequency defaultFrequency; // only used when domain == null
        final TsDomain domain;
        boolean useparam = false;
        double parameter = .9;
        boolean trend=false;
        boolean constant=false;
        TsExpander.Model model = TsExpander.Model.I1;

        int differencing = 1; // >=1, <=5
        TsAggregationType agg = TsAggregationType.Sum;

        // output
        TsData xbench;

        // processing
        boolean compute() {
            TsExpander expander = new TsExpander();
            expander.setType(agg);
            expander.setModel(model);
            if (useparam) {
                expander.setParameter(parameter);
                expander.estimateParameter(false);
            } else {
                expander.estimateParameter(true);
            }
            expander.useConst(constant);
            expander.useTrend(trend);

            if (domain != null) {
                xbench = expander.expand(y, domain);
            } else {
                xbench = expander.expand(y, defaultFrequency);
            }
            return xbench != null;
        }
    }

    private static TsData addNoise(TsData s) {

        Random rnd = new Random();
        TsData snoisy = new TsData(s.getDomain());
        for (int i = 0; i < s.getLength(); ++i) {
            double x = s.get(i);
            snoisy.set(i, x * (1 + 0.1 * rnd.nextGaussian()));
        }
        return snoisy;
    }
}
