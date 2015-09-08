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
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.implementation.TramoProcessingFactory;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.AbstractRootSelector;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 * This example shows how to compute canonical decompositions.
 *
 * @author Jean Palate
 */
public class HelloDemetra26 {

    public static void main(String[] args) {
//////////////////////////////////////////
// Use it in a new decomposition

        TsData s = Data.P;
// Tramo pre-processing without trading days
        PreprocessingModel rslt
                = TramoProcessingFactory.instance.generateProcessing(TramoSpecification.TR3)
                .process(s);
        SarimaModel sarima = rslt.estimation.getArima();

// Usual decomposers (Trend, Seasonal)
        TrendCycleSelector tsel = new TrendCycleSelector(.5);
        SeasonalSelector ssel = new SeasonalSelector(sarima.getSpecification().getFrequency(), 3);

// New trading days decomposer
        FrequencySelector tdsel = new FrequencySelector(Periodogram.getTradingDaysFrequencies(12)[0]);
        tdsel.setEpsilonInDegree(10);
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);
        decomposer.add(tdsel);

        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(sarima));
        if (ucm.getComponent(2).isNull())
            return;
// Canonical decomposition
        double var = ucm.setVarianceMax(-1, true);
        
// Estimate the components using KF
        SsfData data = new SsfData(rslt.linearizedSeries(), null);
        SsfUcarima ssf = new SsfUcarima(ucm);
        Smoother smoother = new Smoother();
        smoother.setSsf(ssf);
        SmoothingResults sr = new SmoothingResults(true, true);
        smoother.setCalcVar(true);
        smoother.process(data, sr);
        double[] td = sr.component(ssf.cmpPos(2));
        if (td == null) {
            return;
        }
        TsData tdStoch = new TsData(s.getStart(), td, false);

        TsDataTable table = new TsDataTable();
        table.insert(-1, tdStoch);

// Normal TD 
        CompositeResults tsrslt = TramoSeatsProcessingFactory.process(s,
                TramoSeatsSpecification.RSA5);
        table.insert(-1, tsrslt.getData(ModellingDictionary.TDE, TsData.class).log());

        System.out.println(table);
    }
}

// We define a new selector that will be able to select auto-regressive roots that generates a peak in the spectrum of the model at a given frequency.
class FrequencySelector extends AbstractRootSelector {

    private final double freq_;
    private double eps_ = 5 * Math.PI / 180; // 5 degrees

    public FrequencySelector(final double freq) {
        freq_ = freq;
    }

    @Override
    public boolean accept(final Complex root) {
        if (root.getIm() == 0) {
            return false;
        }
        Complex iroot = root.inv();
        double r = iroot.getRe(), n = iroot.absSquare();
        double f = Math.acos(r * (1 + n) / (2 * n));
        return Math.abs(f - freq_) < eps_;
    }

    public double getEpsilon() {
        return eps_;
    }

    public double getFrequency() {
        return freq_;
    }

    public void setEpsilon(final double value) {
        eps_ = value;
    }

    public void setEpsilonInDegree(final double deg) {
        eps_ = deg * Math.PI / 180;
    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
        m_sel = null;
        m_nsel = p;
        return false;
    }
}
