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

import ec.benchmarking.DisaggregationModel;
import ec.benchmarking.simplets.TsDisaggregation;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.AutoCorrelations;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.arima.SsfAr1;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.arima.SsfRw;
import ec.tstoolkit.ssf.arima.SsfRwAr1;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.Constant;
import ec.tstoolkit.timeseries.regression.LinearTrend;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 * Colour analyser information
 *
 * @author Jean Palate
 */
public class HelloDemetra41 {

    public static void main(String[] args) {
        
        // Chow-Lin
        TempDisaggSpec spec=new TempDisaggSpec();
        // OLS
        // spec.setModel(TempDisaggSpec.Model.Wn);
        TempDisaggOutput output = process(Data.M3.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true), new TsData[]{Data.M2}, spec);
        TsDataTable table=new TsDataTable();
        table.add(Data.M3, output.getPred(), output.getSePred());
        System.out.println(table);
    }

    public static TempDisaggOutput process(TsData y, TsData[] x, TempDisaggSpec spec) {
        DisaggregationModel model = prepare(y, x, spec);
        if (model == null) {
            return null;
        }
        TsDisaggregation<? extends ISsf> disagg;

        switch (spec.getModel()) {
            case Ar1:
                disagg = initChowLin(spec);
                break;
            case Wn:
                disagg = initOLS();
                break;
            case RwAr1:
                disagg = initLitterman(spec);
                break;
            case Rw:
                disagg = initFernandez();
                break;
            default:
                disagg = initI(spec.getModel().getDifferencingOrder());
                break;
        }

        disagg.calculateVariance(true);

        if (!disagg.process(model, null)) {
            return null;
        } else {
            return new TempDisaggOutput(disagg, spec);
        }
    }

    private static TsDisaggregation<SsfAr1> initChowLin(TempDisaggSpec spec) {
        TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
        SsfAr1 ssf = new SsfAr1();
        Parameter p = spec.getParameter();
        if (p != null && p.isFixed()) {
            ssf.setRho(p.getValue());
        } else {
            disagg.setMapping(new SsfAr1.Mapping(false, spec.getTruncatedRho(), 1));
        }
        disagg.setSsf(ssf);
        return disagg;
    }

    private static TsDisaggregation<SsfAr1> initOLS() {
        TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
        SsfAr1 ssf = new SsfAr1();
        ssf.setRho(0);
        disagg.setSsf(ssf);
        return disagg;
    }

    private static TsDisaggregation<SsfRwAr1> initLitterman(TempDisaggSpec spec) {
        TsDisaggregation<SsfRwAr1> disagg = new TsDisaggregation<>();
        SsfRwAr1 ssf = new SsfRwAr1();
        Parameter p = spec.getParameter();
        if (p != null && p.isFixed()) {
            ssf.setRho(p.getValue());
        } else {
            disagg.setMapping(new SsfRwAr1.Mapping(false, spec.getTruncatedRho(), 1));
        }
        disagg.setSsf(ssf);
        return disagg;
    }

    private static TsDisaggregation<SsfRw> initFernandez() {
        TsDisaggregation<SsfRw> disagg = new TsDisaggregation<>();
        SsfRw ssf = new SsfRw();
        disagg.setSsf(ssf);
        return disagg;
    }

    private static TsDisaggregation<SsfArima> initI(int diff) {
        TsDisaggregation<SsfArima> disagg = new TsDisaggregation<>();
        ArimaModel sarima = new ArimaModel(null, new BackFilter(UnitRoots.D(1, diff)), null, 1);
        SsfArima ssf = new SsfArima(sarima);
        disagg.setSsf(ssf);
        return disagg;
    }

    private static DisaggregationModel prepare(TsData y, TsData[] x, TempDisaggSpec spec) {
        if (y == null) {
            return null;
        }
        DisaggregationModel model = new DisaggregationModel(spec.getDefaultFrequency());
        model.setY(y);
        if (x == null || x.length == 0) {
            if (spec.getDefaultFrequency() == TsFrequency.Undefined || !y.getFrequency().contains(spec.getDefaultFrequency())) {
                return null;
            } else {
                model.setDefaultForecastCount(spec.getDefaultFrequency().intValue());
            }
        }

        TsVariableList vars = new TsVariableList();
        if (spec.isConstant() && (spec.getModel().isStationary())) {
            vars.add(new Constant());
        }
        if (spec.isTrend()) {
            vars.add(new LinearTrend(y.getStart().firstday()));
        }
        for (int i = 0; i < x.length; ++i) {
            vars.add(new TsVariable("var-" + i, x[i]));
        }
        if (!vars.isEmpty()) {
            model.setX(vars);
        }
        model.setAggregationType(spec.getType());
        return model;
    }
}

class TempDisaggOutput {

    private TsData pred;
    private TsData sePred;
    private double[] coeff;
    private double[] seCoeff;
    private double rho, dw;

    TempDisaggOutput(TsDisaggregation<? extends ISsf> disagg, TempDisaggSpec spec) {
        pred = disagg.getSmoothedSeries();
        sePred = disagg.getSmoothedSeriesVariance().sqrt();
        coeff = disagg.getLikelihood().getB();
        if (coeff != null) {
            seCoeff = new double[coeff.length];
            for (int i = 0; i < seCoeff.length; ++i) {
                seCoeff[i] = disagg.getLikelihood().bser(i, true, 0);
            }
        }
        if (spec.getModel().hasParameter()) {
            rho = disagg.getMin().getParameters().get(0);
        }
        try {
            TsData res = disagg.getFullResiduals();
            AutoCorrelations stats = new AutoCorrelations(res);
            dw = stats.getDurbinWatson();
        } catch (Exception e) {

        }
    }

    /**
     * @return the result
     */
    public TsData getPred() {
        return pred;
    }

    /**
     * @return the stdresult
     */
    public TsData getSePred() {
        return sePred;
    }

    /**
     * @return the coeff
     */
    public double[] getCoeff() {
        return coeff;
    }

    /**
     * @return the ecoeff
     */
    public double[] getSeCoeff() {
        return seCoeff;
    }

    public double[] getTStats() {
        if (coeff == null) {
            return null;
        }
        double[] t = coeff.clone();
        for (int i = 0; i < t.length; ++i) {
            t[i] /= seCoeff[i];
        }
        return t;
    }

    /**
     * @return the rho
     */
    public double getRho() {
        return rho;
    }

    /**
     * @return the dw
     */
    public double getDw() {
        return dw;
    }

}

class TempDisaggSpec {

    public static enum Model {
        Wn,
        Ar1,
        Rw,
        RwAr1,
        I2, I3;

        public boolean hasParameter() {
            return this == Ar1 || this == RwAr1;
        }

        public boolean isStationary() {
            return this == Ar1 || this == Wn;
        }

        public int getParametersCount() {
            return (this == Ar1 || this == RwAr1) ? 1 : 0;
        }

        public int getDifferencingOrder() {
            switch (this) {
                case Rw:
                case RwAr1:
                    return 1;
                case I2:
                    return 2;
                case I3:
                    return 3;
                default:
                    return 0;
            }
        }
    }

    public static final double DEF_EPS = 1e-5;
    private Model model = Model.Ar1;
    private boolean constant = true, trend = false;
    private Parameter p = new Parameter();
    private double truncated = 0;
    private TsAggregationType type = TsAggregationType.Sum;
    private double eps = DEF_EPS;
    private TsFrequency defaultFrequency = TsFrequency.Quarterly;

    /**
     * @return the type
     */
    public TsAggregationType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(TsAggregationType type) {
        this.type = type;
    }

    /**
     * @return the eps
     */
    public double getEpsilon() {
        return eps;
    }

    /**
     * @param eps the eps to set
     */
    public void setEpsilon(double eps) {
        this.eps = eps;
    }

    /**
     * @return the p
     */
    public Parameter getParameter() {
        return p;
    }

    /**
     * @param p the p to set
     */
    public void setParameter(Parameter p) {
        this.p = p;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @param aModel the model to set
     */
    public void setModel(Model aModel) {
        model = aModel;
    }

    /**
     * @return the constant
     */
    public boolean isConstant() {
        return constant;
    }

    /**
     * @param constant the constant to set
     */
    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    /**
     * @return the trend
     */
    public boolean isTrend() {
        return trend;
    }

    /**
     * @param trend the trend to set
     */
    public void setTrend(boolean trend) {
        this.trend = trend;
    }

    public double getTruncatedRho() {
        return truncated;
    }

    public void setTruncatedRho(double lrho) {
        if (lrho > 0 || lrho < -1) {
            throw new IllegalArgumentException("Truncated value should be in [-1,0]");
        }
        truncated = lrho;
    }

    public TsFrequency getDefaultFrequency() {
        return defaultFrequency;
    }

    /**
     * @param freq
     */
    public void setDefaultFrequency(TsFrequency freq) {
        this.defaultFrequency = freq;
    }

}
