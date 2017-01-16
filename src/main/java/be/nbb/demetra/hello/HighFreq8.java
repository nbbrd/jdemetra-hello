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

import be.nbb.demetra.modelling.arima.outliers.OutliersDetectionModule;
import be.nbb.demetra.modelling.outliers.IOutlierVariable;
import be.nbb.demetra.modelling.outliers.SwitchOutlier;
import ec.demetra.ssf.dk.DkToolkit;
import ec.demetra.ssf.implementations.arima.SsfUcarima;
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.arima.x13.UscbForecasts;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.utilities.Arrays2;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.function.Consumer;

/**
 *
 * @author Jean Palate
 */
public class HighFreq8 {

    public static void main(String[] args) throws IOException {
         // the limit for the current implementation
        URL resource = HighFreq2.class.getResource("/births.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(0);

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
       
        OutliersDetectionModule outliersDetector=new OutliersDetectionModule();
        LocalDate start=LocalDate.of(1968, Month.JANUARY, 1);
        Consumer<IOutlierVariable> hook=o->System.out.println("Add "+start.plusDays(o.getPosition()));
        outliersDetector.setAddHook( hook);
        outliersDetector.setMonitor(monitor);
        outliersDetector.setDefault();
        outliersDetector.addOutlierFactory(new SwitchOutlier.Factory());
        outliersDetector.setCriticalValue(0);
        outliersDetector.process(regarima);
        
        IOutlierVariable[] outliers = outliersDetector.getOutliers();
        for (IOutlierVariable out : outliers){
            System.out.println(out.getCode()+ " "+start.plusDays(out.getPosition()));
        }
        regarima = outliersDetector.getRegarima();
        RegArimaEstimation rslt = monitor.optimize(regarima);
        DataBlock mlin = m;
        mlin = rslt.model.calcRes(new ReadDataBlock(rslt.likelihood.getB()));
        IReadDataBlock p = mapping.map((ArimaModel)rslt.model.getArima());
        System.out.println(p);
        System.out.println(new ReadDataBlock(rslt.likelihood.getB()));
        System.out.println(new ReadDataBlock(rslt.likelihood.getTStats()));
        int freq1 = 7, freq2 = 365;
        // the limit for the current implementation
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel1 = new SeasonalSelector(freq1);
        SeasonalSelector ssel2 = new SeasonalSelector(freq2);
        // m.apply(x->Math.log(x));
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel1);
        decomposer.add(new AllSelector());
        UcarimaModel ucm = decomposer.decompose(rslt.model.getArima());
        ucm.setVarianceMax(-1);
        System.out.println(ucm);
        SsfUcarima ssf = SsfUcarima.create(ucm);
        DataBlockStorage sr2 = DkToolkit.fastSmooth(ssf, new SsfData(mlin), (pos, a, e) -> a.add(0, e));
        Matrix M = new Matrix(m.getLength(), 6);
        M.column(0).copy(m);
        M.column(1).copy(mlin);
        M.column(2).copy(sr2.item(ssf.getComponentPosition(0)));
        M.column(3).copy(sr2.item(ssf.getComponentPosition(1)));
        M.column(4).copy(sr2.item(ssf.getComponentPosition(2)));
        M.column(5).copy(sr2.item(ssf.getComponentPosition(3)));
        System.out.println(M);
        Matrix bf=new Matrix(731,2);
        UscbForecasts fcast = new UscbForecasts(rslt.model.getArima());
        double[] forecasts = fcast.forecasts(mlin, bf.getRowsCount());
        bf.column(0).copyFrom(forecasts, 0);
        forecasts = fcast.forecasts(mlin.reverse(), bf.getRowsCount());
        Arrays2.reverse(forecasts);
        bf.column(1).copyFrom(forecasts, 0);
        System.out.println(bf);

        Matrix fx=new Matrix(731, regarima.getXCount());
        // regression effects
        LocalDate nstart=LocalDate.of(2015, Month.JANUARY, 1);
        x1 = Holidays.france().fillDays(nstart,731, false);
        x2 = Holidays.france().fillPreviousWorkingDays(nstart,731, 1, false);
        x3 = Holidays.france().fillNextWorkingDays(nstart,731, 1, false);
        x4 = Holidays.france().fillPreviousWorkingDays(nstart,731, 2, false);
        x5 = Holidays.france().fillNextWorkingDays(nstart,731, 2, false);
        x6 = Holidays.france().fillPreviousWorkingDays(nstart,731, 3, false);
        x7 = Holidays.france().fillNextWorkingDays(nstart,731, 3, false);
        
        int j=0;
        for (int i = 0; i < x1.getColumnsCount(); ++i) {
            fx.column(j++).copy(x6.column(i));
            fx.column(j++).copy(x4.column(i));
            fx.column(j++).copy(x2.column(i));
            fx.column(j++).copy(x1.column(i));
            fx.column(j++).copy(x3.column(i));
            fx.column(j++).copy(x5.column(i));
            fx.column(j++).copy(x7.column(i));
        }
        for (IOutlierVariable out : outliers){
            out.data(m.getLength(), fx.column(j++));
        }
        bf.set(0);
        bf.column(0).product(fx.rows(), new DataBlock(rslt.likelihood.getB()));
        
        // regression effects
        nstart = LocalDate.of(1968, Month.JANUARY, 1).minusDays(731);
        x1 = Holidays.france().fillDays(nstart,731, false);
        x2 = Holidays.france().fillPreviousWorkingDays(nstart,731, 1, false);
        x3 = Holidays.france().fillNextWorkingDays(nstart,731, 1, false);
        x4 = Holidays.france().fillPreviousWorkingDays(nstart,731, 2, false);
        x5 = Holidays.france().fillNextWorkingDays(nstart,731, 2, false);
        x6 = Holidays.france().fillPreviousWorkingDays(nstart,731, 3, false);
        x7 = Holidays.france().fillNextWorkingDays(nstart,731, 3, false);
        
        fx.set(0);
        j=0;
        for (int i = 0; i < x1.getColumnsCount(); ++i) {
            fx.column(j++).copy(x6.column(i));
            fx.column(j++).copy(x4.column(i));
            fx.column(j++).copy(x2.column(i));
            fx.column(j++).copy(x1.column(i));
            fx.column(j++).copy(x3.column(i));
            fx.column(j++).copy(x5.column(i));
            fx.column(j++).copy(x7.column(i));
        }
        for (IOutlierVariable out : outliers){
            out.data(-731, fx.column(j++));
        }
        bf.column(1).product(fx.rows(), new DataBlock(rslt.likelihood.getB()));
        System.out.println(bf);
    }
}
