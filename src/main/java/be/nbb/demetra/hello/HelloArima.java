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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 *
 * @author Jean Palate
 */
public class HelloArima {

    public static void main(String[] args) {
        Polynomial ars = Polynomial.of(new double[]{1, -.5});
        Polynomial mas = Polynomial.of(new double[]{1, .3});
        ArimaModel armas = new ArimaModel(new BackFilter(ars), null, new BackFilter(mas), 1);

        Polynomial arn = Polynomial.of(new double[]{1, -.1});
        Polynomial man = Polynomial.of(new double[]{1, -0.3});
        ArimaModel arman = new ArimaModel(new BackFilter(arn), null, new BackFilter(man), 1);

        ArimaModel sum=armas.plus(arman);
        for (int i = 0; i < 10; ++i) {
            System.out.print(armas.getAutoCovarianceFunction().get(i));
            System.out.print('\t');
            System.out.print(arman.getAutoCovarianceFunction().get(i));
            System.out.print('\t');
            System.out.println(sum.getAutoCovarianceFunction().get(i));
        }
        
        System.out.println(arman.getSpectrum().get(0));
    }
}
