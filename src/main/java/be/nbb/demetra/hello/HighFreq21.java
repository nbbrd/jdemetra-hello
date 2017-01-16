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

import be.nbb.demetra.stl.LoessFilter;
import be.nbb.demetra.stl.LoessSpecification;
import be.nbb.demetra.stl.SeasonalFilter;
import be.nbb.demetra.stl.Stl;
import be.nbb.demetra.stl.StlPlus;
import be.nbb.demetra.stl.StlSpecification;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Jean Palate
 */
public class HighFreq21 {

    public static void main(String[] args) throws IOException {
        // the limit for the current implementation
        URL resource = HighFreq2.class.getResource("/hedf.txt");
        Matrix z = MatrixReader.read(new File(resource.getFile()));
        System.out.println("start processing");
        DataBlock m = z.column(0);
        int nsh=31, nsw=171, nsy=17;
        LoessSpecification tspec=LoessSpecification.of(16735); //(8766*1.5)/(1-1.5/7)
        LoessFilter tfilter=new LoessFilter(tspec);
        SeasonalFilter sf1=new SeasonalFilter(LoessSpecification.of(nsh, 0), LoessSpecification.of(25), 24);
        SeasonalFilter sf2=new SeasonalFilter(LoessSpecification.of(nsw, 0), LoessSpecification.of(169), 168);
        SeasonalFilter sf3=new SeasonalFilter(LoessSpecification.of(nsy, 0), LoessSpecification.of(8767), 8766);
        StlPlus stl = new StlPlus(tfilter, new SeasonalFilter[]{sf1, sf2, sf3});
        stl.setNo(2);
        stl.setMultiplicative(true);
        stl.process(m);
        Matrix M = new Matrix(m.getLength(), 5);
        M.column(0).copy(m);
        M.column(1).copyFrom(stl.getTrend(), 0);
        M.column(2).copyFrom(stl.getSeason(0), 0);
        M.column(3).copyFrom(stl.getSeason(1), 0);
        M.column(4).copyFrom(stl.getSeason(2), 0);

        System.out.println("start output");
        MatrixWriter.write(M, new File("c:\\localdata\\hedf.txt"));
    }
}
