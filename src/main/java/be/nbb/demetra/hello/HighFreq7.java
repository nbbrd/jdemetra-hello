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
import ec.demetra.ssf.univariate.DefaultSmoothingResults;
import ec.demetra.ssf.univariate.SsfData;
import ec.demetra.ucarima.TrendCycleDecomposer;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;

/**
 *
 * @author Jean Palate
 */
public class HighFreq7 {

    public static void main(String[] args) throws IOException {
        int freq1 = 7, freq2 = 365;
        // the limit for the current implementation
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel1 = new SeasonalSelector(freq1);
        SeasonalSelector ssel2 = new SeasonalSelector(freq2);
        URL resource = HighFreq2.class.getResource("/births.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(0);
        // m.apply(x->Math.log(x));
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel1);
        decomposer.add(new AllSelector());
        double[] p0 = new double[]{.9, .9, .9};
        DailyMapping mapping = new DailyMapping();
        ArimaModel arima = mapping.map(new DataBlock(p0));

        GlsArimaMonitor monitor = new GlsArimaMonitor();
        monitor.setMapping(mapping);
        RegArimaModel<ArimaModel> regarima = new RegArimaModel<>(arima, m);
        Matrix x1 = Holidays.france().fillDays(LocalDate.of(1968, Month.JANUARY, 1), m.getLength(), false);
        Matrix x2 = Holidays.france().fillPreviousWorkingDays(LocalDate.of(1968, Month.JANUARY, 1), m.getLength(), 1, false);
        Matrix x3 = Holidays.france().fillNextWorkingDays(LocalDate.of(1968, Month.JANUARY, 1), m.getLength(), 1, false);
        Matrix x4 = Holidays.france().fillPreviousWorkingDays(LocalDate.of(1968, Month.JANUARY, 1), m.getLength(), 2, false);
        Matrix x5 = Holidays.france().fillNextWorkingDays(LocalDate.of(1968, Month.JANUARY, 1), m.getLength(), 2, false);
        Matrix x6 = Holidays.france().fillPreviousWorkingDays(LocalDate.of(1968, Month.JANUARY, 1), m.getLength(), 3, false);
        Matrix x7 = Holidays.france().fillNextWorkingDays(LocalDate.of(1968, Month.JANUARY, 1), m.getLength(), 3, false);
        for (int i = 0; i < x1.getColumnsCount(); ++i) {
            regarima.addX(x6.column(i));
            regarima.addX(x4.column(i));
            regarima.addX(x2.column(i));
            regarima.addX(x1.column(i));
            regarima.addX(x3.column(i));
            regarima.addX(x5.column(i));
            regarima.addX(x7.column(i));
        }
        RegArimaEstimation<ArimaModel> estimation = monitor.process(regarima);
//        double[] res = estimation.fullResiduals();
//        Matrix Res=new Matrix(res.length, 1);
//        Res.column(0).copyFrom(res, 0);
//        System.out.println(Res);

        arima = estimation.model.getArima();
        DataBlock mlin = m;
        mlin = estimation.model.calcRes(new ReadDataBlock(estimation.likelihood.getB()));
        IReadDataBlock p = mapping.map(arima);
        System.out.println(p);
        System.out.println(new ReadDataBlock(estimation.likelihood.getB()));
        System.out.println(new ReadDataBlock(estimation.likelihood.getTStats()));
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm.setVarianceMax(-1);
        ucm.compact(ucm.getComponentsCount() - 2, 2);
        System.out.println(ucm);
        SsfUcarima ssf = SsfUcarima.create(ucm);
        DataBlockStorage sr2 = DkToolkit.fastSmooth(ssf, new SsfData(mlin), (pos, a, e) -> a.add(0, e));
        Matrix M = new Matrix(m.getLength(), 3 + ucm.getComponentsCount());
        M.column(0).copy(m);
        M.column(1).copy(mlin);
        for (int i = 1; i < ucm.getComponentsCount(); ++i) {
            M.column(3 + i).copy(sr2.item(ssf.getComponentPosition(i)));
        }

        TrendCycleDecomposer tcdecomposer = new TrendCycleDecomposer();
        tcdecomposer.setLambda(1e8);
        tcdecomposer.setDifferencing(3);
        tcdecomposer.decompose(ucm.getComponent(0));
        UcarimaModel ucmtc = new UcarimaModel(ucm.getComponent(0), new ArimaModel[]{tcdecomposer.getTrend(), tcdecomposer.getCycle()});
        ssf = SsfUcarima.create(ucmtc);
        DefaultSmoothingResults sr = DkToolkit.smooth(ssf, new SsfData(sr2.item(ssf.getComponentPosition(0))), false);
        M.column(2).copy(sr.getComponent(ssf.getComponentPosition(0)));
        M.column(3).copy(sr.getComponent(ssf.getComponentPosition(1)));
        System.out.println(M);
    }
}

class DailyMapping implements IParametricMapping<ArimaModel> {

    boolean corr = true;

    static final double W0 = 5.75 / 7, W1 = 1 - W0;

    int freq1 = 7, freq2 = 365;

    @Override
    public ArimaModel map(IReadDataBlock p) {
        double th = p.get(0), bth1 = p.get(1), bth2 = p.get(2);
        double[] ma = new double[2];
        double[] sma1 = new double[freq1 + 1];
        double[] sma2 = new double[freq2 + (corr ? 2 : 1)];
        double[] d = new double[2];
        double[] d1 = new double[freq1 + 1];
        double[] d2 = new double[freq2 + (corr ? 2 : 1)];
        ma[0] = 1;
        ma[1] = -th;
        sma1[0] = 1;
        sma1[freq1] = -bth1;
        sma2[0] = 1;
        if (corr) {
            sma2[freq2] = -bth2 * .75;
            sma2[freq2 + 1] = -bth2 * .25;
        } else {
            sma2[freq2] = -bth2;
        }

        d[0] = 1;
        d[1] = -1;
        d1[0] = 1;
        d1[freq1] = -1;
        d2[0] = 1;
        if (corr) {
            d2[freq2] = -.75;
            d2[freq2 + 1] = -.25;
        } else {
            d2[freq2] = -1;
        }
        Polynomial.Division div = Polynomial.divide(Polynomial.of(d2), UnitRoots.D1);
        ArimaModel arima = new ArimaModel(
                new BackFilter(div.getQuotient()),
                BackFilter.D1.times(BackFilter.D1).times(BackFilter.of(d1)),
                BackFilter.of(ma).times(BackFilter.of(sma1)).times(BackFilter.of(sma2)),
                1);
        return arima;
    }

    @Override
    public IReadDataBlock map(ArimaModel t) {
        BackFilter ma = t.getMA();
        double[] p = new double[3];
        p[0] = -ma.get(1);
        p[1] = -ma.get(freq1);
        p[2] = corr ? -ma.get(freq2) / .75 : -ma.get(freq2);
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return inparams.check(x -> Math.abs(x) < .99);
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return 1e-6;
    }

    @Override
    public int getDim() {
        return 3;
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
        p = ioparams.get(2);
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
}
