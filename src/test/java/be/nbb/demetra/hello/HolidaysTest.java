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

import ec.tstoolkit.maths.matrices.Matrix;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class HolidaysTest {

    public HolidaysTest() {
    }

    @Test
    public void testAll() {
        Matrix all = Holidays.france().fillDays(LocalDate.of(2016, Month.JANUARY, 1), 366, false);
        System.out.println(all);
    }

    @Test
    public void testOne() {
        Matrix one = Holidays.france().fillDays(LocalDate.of(2016, Month.JANUARY, 1), 366, true);
        Matrix prev = Holidays.france().fillPreviousWorkingDays(LocalDate.of(2016, Month.JANUARY, 1), 366, 1, true);
        Matrix next = Holidays.france().fillNextWorkingDays(LocalDate.of(2016, Month.JANUARY, 1), 366, 1, true);
        Matrix all=new Matrix(366, 3);
        all.column(0).copy(one.column(0));
        all.column(1).copy(prev.column(0));
        all.column(2).copy(next.column(0));
        System.out.println(all);
    }

}
