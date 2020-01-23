package com.learning.springbootajax.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class BigDecimalEditor extends PropertyEditorSupport {
    private static final Logger log = LoggerFactory.getLogger(BigDecimalEditor.class);
    @Override
    public String getAsText() {
        log.info("=============================================================================");
        log.info("getAsText called!");
        String s = null;
        if (getValue() != null) {
            BigDecimal bd = (BigDecimal) getValue();
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
            df.setParseBigDecimal(true);
            s = df.format(bd);
        }
        return s;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        log.info("=============================================================================");
        log.info("setAsText ", text);
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(new Locale("pt", "BR"));
        df.setParseBigDecimal(true);
        BigDecimal bd = null;
        try {
            bd = (BigDecimal) df.parseObject(text);
        } catch (ParseException e) {
            log.error("setAsText error", e);
            setValue(null);
        }
        setValue(bd);
    }
}
