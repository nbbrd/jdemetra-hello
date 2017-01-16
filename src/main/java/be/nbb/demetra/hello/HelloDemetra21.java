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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.AbstractOutlierVariable;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * This example shows how to implement a new outliers type
 *
 * @author Jean Palate
 */
public class HelloDemetra21 {

    public static void main(String[] args) {
        //Integration of the new outlier in the outlier detection routine of Tramo / X13 

        ModelDescription desc = new ModelDescription(Data.X, null);
        desc.setAirline(true);
        ModellingContext context = new ModellingContext();
        context.description = desc.clone();

        ec.tstoolkit.modelling.arima.tramo.OutliersDetector xtramo = new ec.tstoolkit.modelling.arima.tramo.OutliersDetector();
        xtramo.setAll();
        xtramo.addOutlierFactory(new SwitchOutlierFactory());
        xtramo.setCriticalValue(3);
        xtramo.process(context);

        System.out.println("Tramo");
        for (IOutlierVariable var : context.description.getOutliers()) {
            System.out.print(var.getPosition());
            System.out.print(' ');
            System.out.println(var.getOutlierType());
        }

        context.description = desc.clone();
        ec.tstoolkit.modelling.arima.x13.OutliersDetector xx13 = new ec.tstoolkit.modelling.arima.x13.OutliersDetector();
        xx13.setAll();
        xx13.addOutlierFactory(new SwitchOutlierFactory());
        xx13.setCriticalValue(3);
        xx13.process(context);

        System.out.println();
        System.out.println("X13");
        for (IOutlierVariable var : context.description.getOutliers()) {
            System.out.print(var.getPosition());
            System.out.print(' ');
            System.out.println(var.getOutlierType());
        }

    }
}

class SwitchOutlier extends AbstractOutlierVariable {

    public SwitchOutlier(Day p) {
        super(p);
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
        TsPeriod pstart=new TsPeriod(start.getFrequency(), getPosition());
        int pos = pstart.minus(start);
        data.set(0);
        if (pos >= 0) {
            data.set(pos, 1);
        }
        if (pos + 1 < data.getLength()) {
            data.set(pos + 1, -1);
        }
    }

    @Override
    public OutlierType getOutlierType() {
        return OutlierType.WO;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
         int p = domain.search(getPosition());
        return p >= 0 && p < domain.getLength() - 1;
    }

    @Override
    public FilterRepresentation getFilterRepresentation(int freq) {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.D1, BackFilter.ONE), 0);
    }

    @Override
    public String getCode() {
        return "WO";
    }
}

///////////////////////////////
class SwitchOutlierFactory implements IOutlierFactory {

    @Override
    public SwitchOutlier create(Day position) {
        return new SwitchOutlier(position);
    }

    @Override
    public TsDomain definitionDomain(TsDomain tsdomain) {
        return tsdomain.drop(0, 1);
    }

    @Override
    public OutlierType getOutlierType() {
        return OutlierType.WO;
    }

    @Override
    public String getOutlierCode() {
        return "WO";
    }
}
