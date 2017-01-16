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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.maths.matrices.Matrix;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Jean Palate
 */
public class HighFreq2 {

    public static void main(String[] args) throws IOException {
        URL resource = HighFreq2.class.getResource("/uspetroleum.txt");
        Matrix pet=MatrixReader.read(new File(resource.getFile()));
        DataBlock m = pet.column(1);
        m.difference();
        m = m.drop(1, 0);
        LjungBoxTest2 test = new LjungBoxTest2();
        int[] lags=new int[4];
        double l=365.25/7;
        for (int i=0; i<lags.length; ++i){
            lags[i]=(int)(l*(i+1)+.5);
        }
        test.setLags(lags);
        test.test(m);
        System.out.println(test.getValue());
        System.out.println(test.getPValue());
        System.out.println();
        
        double[] data=new double[m.getLength()];
        m.copyTo(data, 0);
        double var=DescriptiveStatistics.cov(0, data);
        for (int i=1; i<=300; ++i){
            System.out.println(DescriptiveStatistics.cov(i, data)/var);
        }
    }
}
