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

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.modelling.arima.tramo.OutlierSpec;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.MissingValueEstimation;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Colour analyser information
 *
 * @author Jean Palate
 */
public class HelloDemetra40 {

    public static void main(String[] args) {

        Map<String, TsData> input = new HashMap<>();
        input.put("s1", Data.P);
        input.put("s2", Data.X);
        TsData pm = Data.P.clone();
        pm.setMissing(10);
        pm.setMissing(11);
        pm.setMissing(12);
        pm.setMissing(20);
        pm.setMissing(50);
        input.put("s3", pm);
        ColourAnalyserSpec spec = new ColourAnalyserSpec();
        spec.ami = false;
        spec.criticalValue = 3;
        Map<String, SeriesOutput> rslt = process(input, spec);
        for (String s : rslt.keySet()) {
            System.out.println(s);
            System.out.println(rslt.get(s));
        }
    }

    static Map<String, SeriesOutput> process(Map<String, TsData> series, ColourAnalyserSpec spec) {
        Map<String, SeriesOutput> rslts = new HashMap<>();
        TramoSpecification tsspec = of(spec);
        IPreprocessor preprocessor = tsspec.build();
        for (Map.Entry<String, TsData> entry : series.entrySet()) {
            PreprocessingModel model = preprocessor.process(entry.getValue(), null);
            rslts.put(entry.getKey(), of(model));
        }

        return rslts;
    }

    static TramoSpecification of(ColourAnalyserSpec input) {
        // transformation
        TramoSpecification spec = new TramoSpecification();
        TransformSpec transform = spec.getTransform();
        switch (input.transformation) {
            case -1:
                transform.setFunction(DefaultTransformationType.Auto);
                break;
            case 0:
                transform.setFunction(DefaultTransformationType.Log);
                break;
            default:
                transform.setFunction(DefaultTransformationType.None);
                break;
        }
        // calendar
        CalendarSpec calendar = spec.getRegression().getCalendar();
        switch (input.calendarEffect) {
            case -1:
                calendar.getTradingDays().setTradingDaysType(TradingDaysType.TradingDays);
                calendar.getTradingDays().setLeapYear(true);
                calendar.getTradingDays().setAutomatic(true);
                calendar.getEaster().setOption(EasterSpec.Type.IncludeEaster);
                calendar.getEaster().setTest(true);
                break;
            case 0:
                calendar.getTradingDays().disable();
                calendar.getEaster().setOption(EasterSpec.Type.Unused);
                break;
            case 1:
                calendar.getTradingDays().setTradingDaysType(TradingDaysType.WorkingDays);
                calendar.getTradingDays().setLeapYear(true);
                calendar.getEaster().setOption(EasterSpec.Type.IncludeEaster);
                break;
            case 2:
                calendar.getTradingDays().setTradingDaysType(TradingDaysType.TradingDays);
                calendar.getTradingDays().setLeapYear(true);
                calendar.getEaster().setOption(EasterSpec.Type.IncludeEaster);
                break;
        }

        // ami
        if (input.ami) {
            spec.setUsingAutoModel(true);
        } else {
            spec.getArima().airline();
        }
        // outliers
        OutlierSpec outliers = spec.getOutliers();
        outliers.clearTypes();
        if (input.AO) {
            outliers.add(OutlierType.AO);
        }
        if (input.TC) {
            outliers.add(OutlierType.TC);
        }
        if (input.LS) {
            outliers.add(OutlierType.LS);
        }
        if (input.SO) {
            outliers.add(OutlierType.SO);
        }
        outliers.setCriticalValue(input.criticalValue);
        return spec;
    }

    private static SeriesOutput of(PreprocessingModel model) {
        SeriesOutput o = new SeriesOutput();
        o.logs = model.isMultiplicative();
        o.arima = model.description.getSpecification();
        TsPeriod start = model.description.getSeriesDomain().getStart();
        OutlierEstimation[] all = model.outliersEstimation(true, false);
        if (all != null) {
            for (OutlierEstimation out : all) {
                SeriesOutput.Outlier cur = new SeriesOutput.Outlier();
                cur.code = out.getCode();
                cur.value = out.getValue();
                cur.stdError = out.getStdev();
                cur.position = out.getPosition().minus(start);
                o.outliers.add(cur);
            }
        }

        MissingValueEstimation[] missings = model.missings(true);
        if (missings != null) {
            for (MissingValueEstimation m : missings) {
                SeriesOutput.Missing cur = new SeriesOutput.Missing();
                // 7/12/2017 START
                double mean = m.getValue(), ser = m.getStdev();
                if (model.isMultiplicative()) {
                    double lser = mean + 0.5 * ser * ser;
                    ser = Math.exp(lser) * Math.sqrt((Math.exp(ser * ser) - 1));
                    TsData tmp = new TsData(m.getPosition(), new double[]{mean}, false);
                    model.backTransform(tmp, true, true);
                    mean = tmp.get(0);
                }
                cur.value = mean;
                cur.stdError = ser;
                // 7/12/2017 END
                cur.position = m.getPosition().minus(start);
                o.missings.add(cur);
            }
        }
        // calendars
        o.td = model.description.countRegressors(var -> var.isCalendar());
        o.easter = model.description.countRegressors(var -> var.isMovingHoliday()) > 0;

        return o;
    }
}

class ColourAnalyserSpec {

    // modelling
    int transformation = -1; // -1 = Pretest, 0 = Logs, 1 = Levels
    int calendarEffect = -1; // -1 = Auto, 0 = None, 1 = WD, 2 = TD 
    boolean ami = true;
    // outliers
    boolean AO = true, LS = true, TC = true, SO = false;
    double criticalValue = 0; // 0 (default) or any number >= 2 

}

class SeriesOutput {

    static class Outlier {

        int position;
        String code;
        double value;
        double stdError;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(code + "_").append(position).append('\t').append(value).append('\t').append(stdError).append(System.lineSeparator());
            return builder.toString();
        }
    }

    static class Missing {

        int position;
        double value;
        double stdError;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(position).append('\t').append(value).append('\t').append(stdError).append(System.lineSeparator());
            return builder.toString();
        }
    }

    boolean logs;
    SarimaSpecification arima;
    int td;
    boolean easter;
    List<Outlier> outliers = new ArrayList<>();
    List<Missing> missings = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Log: ").append(logs).append(System.lineSeparator());
        builder.append("Arima: ").append(arima).append(System.lineSeparator());
        builder.append("TD: ").append(td).append(System.lineSeparator());
        builder.append("Easter: ").append(easter).append(System.lineSeparator());
        builder.append("Outliers: ").append(System.lineSeparator());
        outliers.forEach(o -> builder.append("    ").append(o));
        builder.append("Missings: ").append(System.lineSeparator());
        missings.forEach(o -> builder.append("    ").append(o));

        return builder.toString();
    }
}
