/*
 * Copyright 2016 National Bank of Belgium
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

import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.AbstractRootSelector;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.RealFunction;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataCollector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimator;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Jean Palate
 */
public class HighFreq5 {

    public static void main(String[] args) throws IOException {

        URL resource = HighFreq2.class.getResource("/births.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(0);
        TsDataCollector collector = new TsDataCollector();
        Day start = new Day(1968, Month.January, 0);
        for (int i = 0; i < m.getLength(); ++i) {
            collector.addObservation(start.getTime(), m.get(i));
            start = start.plus(1);
        }
        TsData ym = collector.make(TsFrequency.Monthly, TsAggregationType.Sum);
        System.out.println(ym);
        DataBlock M = new DataBlock(m.getLength() / 7);
        DataBlock cur = m.extract(0, 7);
        for (int i = 0; i < M.getLength(); ++i) {
            //M.set(i, Math.log(cur.sum()));
            M.set(i, cur.sum());
            cur.slide(7);
        }
        GlsArimaMonitor monitor = new GlsArimaMonitor();
        double[] p0 = new double[]{-.9, -.9};
        WeeklyMapping mapping = new WeeklyMapping();
        mapping.setAdjust(true);
        monitor.setMapping(mapping);
        ArimaModel arima = mapping.map(new DataBlock(p0));
        RegArimaModel<ArimaModel> regarima = new RegArimaModel<>(arima, M);
        RegArimaEstimation<ArimaModel> estimation = monitor.process(regarima);

        // the limit for the current implementation
        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);
        arima = estimation.model.getArima();
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm.setVarianceMax(-1);
        System.out.println(ucm);

        SsfUcarima ssf = SsfUcarima.create(ucm);
        DataBlockStorage sr = DkToolkit.fastSmooth(ssf, new ec.demetra.ssf.univariate.SsfData(m), (pos, a, e)->a.add(0, e));
        System.out.println(M);
        System.out.println(new DataBlock(sr.item(ssf.getComponentPosition(0))));
        System.out.println(new DataBlock(sr.item(ssf.getComponentPosition(1))));
        System.out.println(new DataBlock(sr.item(ssf.getComponentPosition(2))));

//        double[] cmp = sr.component(ssf.cmpPos(0));
//        System.out.println(ym);
//        start = new Day(1968, Month.January, 0);
//        for (int i = 0; i < cmp.length; ++i) {
//            collector.addObservation(start.getTime(), cmp[i]);
//            start = start.plus(7);
//        }
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator wke = wk.finalEstimator(1, false);
        RealFunction sqfn = wke.getFilter().squaredGainFunction();
        for (int i = 0; i < 100; ++i) {
            System.out.println(sqfn.apply(i * .0005 * 2 * Math.PI));
        }
    }

}

class AllSelector extends AbstractRootSelector {

    @Override
    public boolean accept(Complex root) {
        return true;
    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
        m_sel = p;
        m_nsel = Polynomial.ONE;
        return true;
    }

}

class WeeklyMapping implements IParametricMapping<ArimaModel> {

    static final double W0 = 5.75 / 7, W1 = 1 - W0;
    private boolean adjust = true;
    protected boolean d1=false;

    @Override
    public ArimaModel map(IReadDataBlock p) {
        double th = p.get(0), bth = p.get(1);
        double[] d = new double[adjust ? 54 : 53];
        d[0] = 1;
        if (adjust) {
            d[52] = -W0;
            d[53] = -W1;
        } else {
            d[52] = -1;
        }
        double[] ma = new double[]{1, -th};
        double[] dma = new double[adjust ? 54 : 53];
        dma[0] = 1;
        if (adjust) {
            dma[52] = -W0 * bth;
            dma[53] = -W1 * bth;
            BackFilter fma = BackFilter.of(ma).times(BackFilter.of(dma));
            Polynomial div = Polynomial.of(d).divide(UnitRoots.D1);
            return new ArimaModel(new BackFilter(div), BackFilter.D1.times(BackFilter.D1), fma, 1);
        } else {
            dma[52] = -bth;
            BackFilter fma = BackFilter.of(ma).times(BackFilter.of(dma));
            BackFilter far = BackFilter.D1.times(BackFilter.of(d));
            return new ArimaModel(null, far, fma, 1);
        }
    }

    @Override
    public IReadDataBlock map(ArimaModel t) {
        BackFilter ma = t.getMA();
        double[] p = new double[2];
        p[0] = -ma.get(1);
        if (adjust) {
            p[1] = -ma.get(52) / W0;
        } else {
            p[1] = -ma.get(52);
        }
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return inparams.check(x -> Math.abs(x) < .999);
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return 1e-6;
    }

    @Override
    public int getDim() {
        return 2;
    }

    @Override
    public double lbound(int idx) {
        return -1;
    }

    @Override
    public double ubound(int idx) {
        return 1;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        boolean changed = false;
        double p = ioparams.get(0);
        if (Math.abs(p) >= .999) {
            ioparams.set(0, 1 / p);
            changed = true;
        }
        p = ioparams.get(1);
        if (Math.abs(p) >= .999) {
            ioparams.set(1, 1 / p);
            changed = true;
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        return "p" + idx;
    }

    /**
     * @return the adjust
     */
    public boolean isAdjust() {
        return adjust;
    }

    /**
     * @param adjust the adjust to set
     */
    public void setAdjust(boolean adjust) {
        this.adjust = adjust;
    }

    /**
     * @return the d1
     */
    public boolean isD1() {
        return d1;
    }

    /**
     * @param d1 the d1 to set
     */
    public void setD1(boolean d1) {
        this.d1 = d1;
    }
}
