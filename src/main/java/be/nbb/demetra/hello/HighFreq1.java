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
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.maths.matrices.Matrix;
import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 *
 * @author Jean Palate
 */
public class HighFreq1 {
    
    public static void main(String[] args) throws IOException {
        URL resource = HighFreq1.class.getResource("/births.txt");
        Matrix MBirth=MatrixReader.read(new File(resource.getFile()));
        DataBlock m1 = MBirth.column(0);
        m1.difference();
        Periodogram periodogram=new Periodogram(m1.drop(1, 0));
        double[] p = periodogram.getP();
        double rad=  periodogram.getIntervalInRadians();
        for (int i=0; i<p.length; i++){
            System.out.print(i*rad);
            System.out.print('\t');
            System.out.println(p[i]);
        }
    }
}