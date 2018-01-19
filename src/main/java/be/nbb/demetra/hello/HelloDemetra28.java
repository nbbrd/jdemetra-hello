/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package be.nbb.demetra.hello;

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.SarimaMapping;

/**
 *
 * @author Jean Palate
 */
public class HelloDemetra28 {

    public static void main(String[] args) {
        SarimaSpecification spec=new SarimaSpecification(12);
        spec.airline();
        RegArimaModel<SarimaModel> regarima=new RegArimaModel<>(new SarimaModel(spec), new DataBlock(Data.P));
        RegArimaEstimator processor=new RegArimaEstimator(new SarimaMapping(spec, true));
        processor.setMaximumLikelihood(false);
        System.out.println(processor.process(regarima).model.getArima());
        processor.setMaximumLikelihood(true);
        System.out.println(processor.process(regarima).model.getArima());
    }
}
