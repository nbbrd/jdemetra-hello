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

import ec.demetra.ssf.ckms.CkmsToolkit;
import ec.demetra.ssf.dk.DkLikelihood;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.arima.SsfArima;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.demetra.ssf.univariate.ILikelihoodComputer;
import ec.demetra.ssf.univariate.SsfData;
import ec.demetra.ucarima.HPDecomposer;
import ec.demetra.ucarima.TrendCycleDecomposer;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.AutoRegressiveSpectrum;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
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
public class HighFreq7bis {

    public static void main(String[] args) throws IOException {
        int freq1 = 7, freq2 = 365;
        // the limit for the current implementation
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel1 = new SeasonalSelector(freq1);
        SeasonalSelector ssel2 = new SeasonalSelector(freq2);
        URL resource = HighFreq2.class.getResource("/edf.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(0);
        m.apply(x -> Math.log(x));
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel1);
        decomposer.add(new AllSelector());
        double[] p0 = new double[]{.9, .9, .9};
        DailyMapping mapping = new DailyMapping();
//        mapping.corr=false;
        ArimaModel arima = mapping.map(new DataBlock(p0));
        GlsArimaMonitor monitor = new GlsArimaMonitor();
        monitor.setMapping(mapping);
        RegArimaModel<ArimaModel> regarima = new RegArimaModel<>(arima, m);
        LocalDate start = LocalDate.of(1996, Month.JANUARY, 1);
        Matrix x1 = Holidays.france().fillDays(start, m.getLength(), false);
        for (int i = 0; i < x1.getColumnsCount(); ++i) {
            regarima.addX(x1.column(i));
        }
        RegArimaEstimation<ArimaModel> estimation = monitor.process(regarima);
        DataBlock mlin = estimation.model.calcRes(new ReadDataBlock(estimation.likelihood.getB()));
        System.out.println(new ReadDataBlock(estimation.likelihood.getB()));
        System.out.println(new ReadDataBlock(estimation.likelihood.getTStats()));

        arima = estimation.model.getArima();
        IReadDataBlock p = mapping.map(arima);
        System.out.println(p);
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
        tcdecomposer.setLambda(1.2e11);
        tcdecomposer.decompose(ucm.getComponent(0));
        UcarimaModel ucmtc = new UcarimaModel(ucm.getComponent(0), new ArimaModel[]{tcdecomposer.getTrend(), tcdecomposer.getCycle()});
        ssf = SsfUcarima.create(ucmtc);
        DataBlockStorage sr3 = DkToolkit.fastSmooth(ssf, new SsfData(sr2.item(ssf.getComponentPosition(0))), (pos, a, e) -> a.range(0, 6).add(e));
        M.column(2).copy(sr3.item(ssf.getComponentPosition(0)));
        M.column(3).copy(sr3.item(ssf.getComponentPosition(1)));

        System.out.println(M);

        DataBlock sa = m.deepClone();
        sa.sub(M.column(2));
        sa.sub(M.column(3));
        m.difference();
        sa.difference();
        AutoRegressiveSpectrum ary = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Ols);
        AutoRegressiveSpectrum arsa = new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Ols);
        arsa.process(sa.drop(1, 0), 800);
        ary.process(m.drop(1, 0), 800);
        double rd = Math.PI / 1461;
        double cur = rd;
        for (int i = 1; i < 1461; ++i) {
            System.out.print(cur);
            System.out.print('\t');
            System.out.print(ary.value(cur));
            System.out.print('\t');
            System.out.println(arsa.value(cur));
            cur += rd;
        }
    }
}
