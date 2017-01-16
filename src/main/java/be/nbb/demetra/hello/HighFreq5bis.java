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
import ec.demetra.ucarima.HPDecomposer;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.RealFunction;
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
public class HighFreq5bis {

    public static void main(String[] args) throws IOException {

        URL resource = HighFreq2.class.getResource("/uspetroleum.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(3);
        GlsArimaMonitor monitor = new GlsArimaMonitor();
        double[] p0 = new double[]{-.9, -.9};
        WeeklyMapping mapping = new WeeklyMapping();
        mapping.setAdjust(false);
        monitor.setMapping(mapping);
        ArimaModel arima = mapping.map(new DataBlock(p0));
        RegArimaModel<ArimaModel> regarima = new RegArimaModel<>(arima, m);
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
        
        HPDecomposer hpdecomposer=new HPDecomposer();
        hpdecomposer.setTau(5*52.18);
        hpdecomposer.decompose(ucm.getComponent(0));
        UcarimaModel ucmfull=new UcarimaModel(ucm.getModel(), new ArimaModel[]{hpdecomposer.getTrend(),
                hpdecomposer.getCycle(), ucm.getComponent(1), ucm.getComponent(3)});
        SsfUcarima ssf = SsfUcarima.create(ucmfull);
        System.out.println(ucmfull);
//        SsfUcarima ssf = SsfUcarima.create(ucm);
        DataBlockStorage sr2 = DkToolkit.fastSmooth(ssf, new SsfData(m), (pos, a, e)->
        {
           a.range(0,4).add(e);
        });
//        DefaultSmoothingResults sr = DkToolkit.smooth(ssf, new SsfData(m), false);
        Matrix M = new Matrix(m.getLength(), 5);
        M.column(0).copy(m);
        for (int i = 0; i < 4; ++i) {
            M.column(i + 1).copy(sr2.item(ssf.getComponentPosition(i)));
//            M.column(i + 1).copy(sr.getComponent(ssf.getComponentPosition(i)));
       }
        System.out.println(M);
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator wke = wk.finalEstimator(1, false);
        RealFunction sqfn = wke.getFilter().squaredGainFunction();
        for (int i = 0; i < 2000; ++i) {
            System.out.println(sqfn.apply(i * .0005 * 2 * Math.PI));
        }
    }

}
