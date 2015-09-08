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

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.Spectrum;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;

/**
 * This example shows how to use UCARIMA models (Hodrick-Prescott).
 *
 * @author Jean Palate
 */
public class HelloDemetra24 {

    public static void main(String[] args) {
// Model with AR=1, MA=1, D=I(2)=(1-B)*(1-B)
        Polynomial D2 = UnitRoots.D1.times(UnitRoots.D1);
        ArimaModel I2 = new ArimaModel(null, new BackFilter(D2), null, 1);
// White noise, with var=1600
        ArimaModel N = new ArimaModel(1600);

// Aggregated model. The decomposition I2+N corresponds to the Hodrick-Prescott filter
        ArimaModel S = I2.plus(N);
        System.out.println(S);

// Remove all the noise of I2 (this is the code used in the canonical decomposition). 
        
        Spectrum.Minimizer min = new Spectrum.Minimizer();
        
        min.minimize(I2.getSpectrum());
        double nvar = min.getMinimum();
        ArimaModel Sc = I2.minus(nvar);
        System.out.println(Sc);
        System.out.println(nvar);
        
    }
}
