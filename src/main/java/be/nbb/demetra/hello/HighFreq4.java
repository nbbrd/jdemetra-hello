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
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
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
        Matrix pet=MatrixReader.read(new File(resource.getFile()));
        SarimaModel arima = new SarimaModelBuilder().createAirlineModel(52, -.5, -.5);
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>();
        regarima.setArima(arima);
        regarima.setY(pet.column(0));
        RegArimaEstimation<SarimaModel> rslt = monitor.process(regarima);
        System.out.println(rslt.model.getArima());
    }
}
