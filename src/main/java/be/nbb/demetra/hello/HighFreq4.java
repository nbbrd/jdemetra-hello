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

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Jean Palate
 */
public class HighFreq4 {
    
    public static void main(String[] args) throws IOException {
        URL resource = HighFreq4.class.getResource("/uspetroleum.txt");
        Matrix pet = MatrixReader.read(new File(resource.getFile()));
        SarimaSpecification spec=new SarimaSpecification(1);
        spec.setD(1);
        //spec.setQ(1);
        //spec.setBP(1);
        SarimaModel arima = new SarimaModel(spec);
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>();
        regarima.setArima(arima);
        regarima.setY(pet.column(0));
        //regarima.setMeanCorrection(true);
        TrigonometricVariables all = TrigonometricVariables.all(365.25/7, 6);
        Matrix m = all.matrix(18, pet.getRowsCount());
        for (int i = 0; i < m.getColumnsCount(); ++i) {
            regarima.addX(m.column(i));
        }
        int i0=regarima.isMeanCorrection() ? 1 : 0;
        RegArimaEstimation<SarimaModel> rslt = monitor.process(regarima);
        System.out.println(rslt.model.getArima());
        DataBlock b = new DataBlock(rslt.likelihood.getB());
        System.out.println(b);
        System.out.println(new DataBlock(rslt.likelihood.getTStats()));
        JointRegressionTest test=new JointRegressionTest(.01);
        boolean accept = test.accept(rslt.likelihood, 2, i0, m.getColumnsCount(), null);
        System.out.println(test.getTest().getPValue());
        System.out.println(rslt.likelihood.getLogLikelihood());
        System.out.println(rslt.likelihood.BIC(2));
       
        DataBlock n=new DataBlock(m.getRowsCount());
        n.product(m.rows(), b.drop(i0, 0));
        System.out.println(n.drop(34, 0));
    }
}
