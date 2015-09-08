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

import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarmaSpecification;

/**
 * This example shows how to use ARIMA models.
 *
 * @author Jean Palate
 */
public class HelloDemetra23 {

    public static void main(String[] args) {
        SarmaSpecification spec = new SarmaSpecification(7);
        spec.setP(2);
        spec.setQ(1);
        spec.setBP(1);
        spec.setBQ(1);

// (2 0 1)(1 0 1) model ("seasonal lag" = 7, for instance for a weekly model)
        SarimaModel m1 = new SarimaModel(spec);
        m1.setPhi(1, .6);
        m1.setPhi(1, .7);
        m1.setBPhi(1, -.5);
        m1.setTheta(1, .5);
        m1.setBTheta(1, -.7);
        AutoCovarianceFunction acf = m1.getAutoCovarianceFunction();
        double[] c = acf.values(35);
        for (int i = 1; i < c.length; ++i) {
            System.out.println(c[i] / c[0]);
        }
    }
}
