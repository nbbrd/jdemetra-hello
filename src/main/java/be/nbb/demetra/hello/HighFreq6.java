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
import ec.demetra.ssf.univariate.SsfData;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.RealFunction;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
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
public class HighFreq6 {

    public static void main(String[] args) throws IOException {
        int freq = 52;
        // the limit for the current implementation
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(freq);
        URL resource = HighFreq2.class.getResource("/births.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(0);
        DataBlock M = new DataBlock(m.getLength() / 7);
        DataBlock cur = m.extract(0, 7);
        for (int i = 0; i < M.getLength(); ++i) {
            M.set(i, cur.sum());
            cur.slide(7);
        }

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);
        SarimaModel arima = new SarimaModelBuilder().createAirlineModel(freq, -.9, -.4);
        
        GlsSarimaMonitor monitor=new GlsSarimaMonitor();
        RegArimaModel<SarimaModel> regarima=new RegArimaModel<>(arima, M);
        RegArimaEstimation<SarimaModel> estimation = monitor.process(regarima);
        
        UcarimaModel ucm = decomposer.decompose(estimation.model.getArima());
        ucm.setVarianceMax(-1);
        System.out.println(ucm);
        System.out.println(new DataBlock(ucm.getComponent(1).getMA().getCoefficients()));
        
        SsfUcarima ssf = SsfUcarima.create(ucm);
        DataBlockStorage sr2 = DkToolkit.fastSmooth(ssf, new SsfData(M), (pos, a, e)->a.add(0, e));

        Matrix X = new Matrix(M.getLength(), 4);
        X.column(0).copy(M);
        for (int i = 0; i < 3; ++i) {
            X.column(i + 1).copy(sr2.item(ssf.getComponentPosition(i)));
        }
        System.out.println(X);
        
        WienerKolmogorovEstimators wk=new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator wke = wk.finalEstimator(1, false);
        RealFunction sqfn = wke.getFilter().squaredGainFunction();
        for (int i=0; i<500; ++i){
            System.out.println(sqfn.apply(i*.0005*2*Math.PI));
        }
    }
}

