/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package be.nbb.demetra.hello;

import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.X13Preprocessor;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;


/**
 * Replacement of some blocks of the SA core engines. Use of Tramo 
 * outliers detection in the AMI of X12
 * 
 * @author Jean Palate
 */
public class HelloDemetra19 {

    public static void main(String[] args) {
        // Default AMI of X13
        RegArimaSpecification rg4 = RegArimaSpecification.RG4.clone();
        rg4.getOutliers().setDefaultCriticalValue(3.3);
        IPreprocessor processor = rg4.build();
        PreprocessingModel model = processor.process(Data.P, null);
        // Print the outliers
        OutlierEstimation[] outliers = model.outliersEstimation(true, false);
        for (int i = 0; i < outliers.length; ++i) {
            System.out.println(outliers[i]);
        }
        // Use of Tramo outliers detection
        System.out.println();
        ec.tstoolkit.modelling.arima.tramo.OutliersDetector xtramo =
                new ec.tstoolkit.modelling.arima.tramo.OutliersDetector();
        // Detects all outliers type
        xtramo.setDefault();
        xtramo.setCriticalValue(3.3);
         // Change the outliers detection module of X13
        ((X13Preprocessor) processor).outliers = xtramo;
        model = processor.process(Data.P, null);
        // Print the outliers
        outliers = model.outliersEstimation(true, false);
        for (int i = 0; i < outliers.length; ++i) {
            System.out.println(outliers[i]);
        }
    }
}
