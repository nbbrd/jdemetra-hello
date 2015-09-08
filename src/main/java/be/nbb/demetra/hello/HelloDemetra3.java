/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

import ec.tstoolkit.data.DataBlock;

/**
 * This example shows what is a DataBlock, which is one of the most used basic
 * structure of JDemetra+ (time series, matrices...)
 *
 * @author Jean Palate
 */
public class HelloDemetra3 {

    public static void main(String[] args) {
        // creates a block of 100 random numbers 
        DataBlock O = new DataBlock(100);
        O.randomize();
        // extracts a sub-datablock of 9 items (at position 1, 9, 17...) 
        DataBlock A = O.extract(1, 9, 8);
        // A in reverse order
        DataBlock Abis = A.reverse();
        
        // print the Blocks
        System.out.println(A);
        System.out.println(Abis);
        
        // computes the sum...
        System.out.println(A.sum());
        System.out.println(Abis.sum());
    }
}
