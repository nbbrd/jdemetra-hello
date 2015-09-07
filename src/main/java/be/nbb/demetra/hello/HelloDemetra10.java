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

import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * This example shows how to detect outliers in a time series.
 *
 * @author Philippe Charles
 */
public class HelloDemetra10 {

    public static void main(String[] args) {
        // 1. Retrieve data
        TsData data = getData();

        // 2. Process data to get outliers
        IPreprocessor preprocessor = TramoSpecification.TR4.build();
        PreprocessingModel model = preprocessor.process(data, null);
        OutlierEstimation[] outliers = model.outliersEstimation(true, false);

        // 3. Print outliers
        for (OutlierEstimation o : outliers) {
            System.out.println(o);
        }
    }

    static TsData getData() {
        TsPeriod firstPeriod = new TsPeriod(TsFrequency.Monthly, 1990, 0);

        double[] values = {
            59.6, 56.6, 64.5, 58.7, 57.3, 61.2, 55.7, 20, 54.2, 59.5, 53.8, 42.4,
            53.1, 48.7, 52.8, 56, 52.3, 58.7, 57.1, 21.4, 59, 62.1, 54, 47.9,
            57.5, 55.7, 60.4, 58.5, 52.8, 60.9, 54.8, 18.2, 59.5, 60, 53.3, 43.4,
            48.9, 49.2, 56.1, 53.5, 49.6, 57.5, 49.4, 14.3, 50.5, 48.3, 46.3, 41.4,
            47.1, 47, 55.7, 58.6, 58.2, 65.5, 56.5, 19.8, 61.3, 62.3, 60.1, 51.1,
            62.3, 61, 67.1, 60, 60.6, 66.8, 57.3, 21.8, 60.7, 65, 61.5, 49,
            61.5, 60.2, 65.3, 66.5, 60.8, 68.3, 61.7, 24.3, 65, 67.9, 59.2, 51,
            60.9, 61.5, 64.8, 73.9, 64.3, 73.8, 70.4, 25.6, 74, 79.6, 68.3, 61.7,
            69.3, 71.3, 79.6, 81.1, 74.9, 86.4, 80.8, 28.3, 83.7, 90.1, 81.7, 67.5,
            77, 79.3, 88.9, 90.1, 85.4, 97.3, 87.3, 31.5, 92.9, 97.7, 90, 78.5,
            91.3, 94.3, 103.8, 91.6, 91.9, 94.2, 87.7, 39.1, 95.2, 101.5, 96.4, 78.5,
            97, 98.8, 112.6, 100.4, 97.7, 107.3, 95.9, 51.5, 105.5, 108.8, 95.9, 75.2,
            94.5, 96.7, 106.8, 109.3, 95.3, 110.1, 100.4, 51.5, 105.2, 113.7, 97.7, 80,
            97.7, 96.7, 108.2, 106.7, 94.4, 103.9, 97.6, 46.1, 107.6, 111.6, 94.9, 81.8,
            93.7, 98.2, 114.2, 109.4, 98.8, 120.9, 106.6, 40.8, 120, 117.3, 106.8, 92.4,
            101.5, 104.2, 111.6, 116.3, 108.2, 121.4, 95.7, 40, 118.1, 100.3, 101.7, 81,
            96.5, 89.6, 107.4, 95.1, 101.3, 106.8, 85.4, 40.8, 102.3, 97.1, 91.3, 68,
            89.1, 93.4, 105.4, 95.9, 92, 101.2, 87.1, 40.6, 94.4, 104.7, 93, 73.4,
            94.5, 94.8, 92.1, 103.1, 83.6, 91.2, 87.6, 30.3, 89.2, 78.4, 53.6, 32.9,
            51.6, 49.5, 58.3, 62.4, 57.6, 73.4, 68.8, 28.1};

        return new TsData(firstPeriod, values, false);
    }
}
