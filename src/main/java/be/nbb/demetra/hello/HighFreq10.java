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

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
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
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Jean Palate
 */
public class HighFreq10 {

    public static void main(String[] args) throws IOException {
        URL resource = HighFreq2.class.getResource("/hedf.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(0);
//        DataBlock m = new DataBlock(b.getLength());
//        DataBlock cur = b.extract(0, 6);
//        for (int i = 0; i < m.getLength(); ++i) {
//            m.set(i, cur.sum());
//            cur.slide(6);
//        }
        GlsArimaMonitor monitor = new GlsArimaMonitor();
        

        double[] p0 = new double[]{.9, .9, .9, .9};
        HourlyMapping mapping = new HourlyMapping();
        ArimaModel arima = mapping.map(new DataBlock(p0));
        monitor.setMapping(mapping);
        RegArimaModel<ArimaModel> regarima = new RegArimaModel<>(arima, m);
        RegArimaEstimation<ArimaModel> estimation = monitor.process(regarima);
        IReadDataBlock np = mapping.map(estimation.model.getArima());
        System.out.println(np);
//        TrendCycleSelector tsel = new TrendCycleSelector();
//        SeasonalSelector ssel1 = new SeasonalSelector(24);
//        SeasonalSelector ssel2 = new SeasonalSelector(24 * 7);
//
//        ModelDecomposer decomposer = new ModelDecomposer();
//        decomposer.add(tsel);
//        decomposer.add(ssel1);
//        decomposer.add(ssel2);
//        decomposer.add(new AllSelector());
//        UcarimaModel ucm = decomposer.decompose(estimation.model.getArima());
//        ucm.setVarianceMax(-1);
//        System.out.println(ucm);
//        SsfUcarima ssf = SsfUcarima.create(ucm);
//        DataBlockStorage sr2 = DkToolkit.fastSmooth(ssf, new SsfData(m));
//
//        Matrix M = new Matrix(m.getLength(), 4);
//        M.column(0).copy(m);
//        for (int i = 0; i < 3; ++i) {
//            M.column(i + 1).copy(sr2.item(ssf.getComponentPosition(i)));
//        }
//        System.out.println(M);
    }
}

class HourlyMapping implements IParametricMapping<ArimaModel> {

    int freq1 = 24, freq2 = 24 * 7, freq3 = 24 * 365 + 6 ; // 24*365.25

    @Override
    public ArimaModel map(IReadDataBlock p) {
        double th = p.get(0), bth1 = p.get(1), bth2 = p.get(2), bth3 = p.get(3);
        double[] ma = new double[2];
        double[] sma1 = new double[freq1 + 1];
        double[] sma2 = new double[freq2 + 1];
        double[] sma3 = new double[freq3 + 1];
        double[] d1 = new double[freq1 + 1];
        double[] d2 = new double[freq2 + 1];
        double[] d3 = new double[freq3 + 1];
        ma[0] = 1;
        ma[1] = -th;
        sma1[0] = 1;
        sma1[freq1] = -bth1;
        sma2[0] = 1;
        sma2[freq2] = -bth2;
        sma3[0] = 1;
        sma3[freq3] = -bth3;

        d1[0] = 1;
        d1[freq1] = -1;
        d2[0] = 1;
        d2[freq2] = -1;
        d3[0] = 1;
        d3[freq3] = -1;

        BackFilter U = BackFilter.D1.times(BackFilter.of(d1)).times(BackFilter.of(d2)).times(BackFilter.of(d3));
        BackFilter M = BackFilter.of(ma).times(BackFilter.of(sma1)).times(BackFilter.of(sma2)).times(BackFilter.of(sma3));
        ArimaModel arima = new ArimaModel(null, U, M, 1);
        return arima;
    }

    @Override
    public IReadDataBlock map(ArimaModel t) {
        BackFilter ma = t.getMA();
        double[] p = new double[4];
        p[0] = -ma.get(1);
        p[1] = -ma.get(freq1);
        p[2] = -ma.get(freq2);
        p[3] = -ma.get(freq3);
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
        return 4;
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
        p = ioparams.get(3);
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

class AirlineSelector extends AbstractRootSelector {

    final int lag;

    AirlineSelector(int lag) {
        this.lag = lag;
    }

    @Override
    public boolean accept(Complex root) {
        return false;
    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
        Polynomial s = UnitRoots.S(lag, 1);
        if (lag == 7*4){
            s=s.divide(UnitRoots.S(24, 1));
        }
            
        m_sel = s;
        m_nsel = p.divide(s);
        return true;
    }

}
