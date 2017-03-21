package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.converter.BigDecimalConverter;
import com.raizlabs.android.dbflow.converter.BooleanConverter;
import com.raizlabs.android.dbflow.converter.CalendarConverter;
import com.raizlabs.android.dbflow.converter.DateConverter;
import com.raizlabs.android.dbflow.converter.SqlDateConverter;
import com.raizlabs.android.dbflow.converter.UUIDConverter;
import java.lang.Boolean;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public final class GeneratedDatabaseHolder extends DatabaseHolder {
  public GeneratedDatabaseHolder() {
    typeConverters.put(UUID.class, new UUIDConverter());
    typeConverters.put(Calendar.class, new CalendarConverter());
    typeConverters.put(GregorianCalendar.class, new CalendarConverter());
    typeConverters.put(Date.class, new DateConverter());
    typeConverters.put(BigDecimal.class, new BigDecimalConverter());
    typeConverters.put(Boolean.class, new BooleanConverter());
    typeConverters.put(java.sql.Date.class, new SqlDateConverter());
    typeConverters.put(Time.class, new SqlDateConverter());
    typeConverters.put(Timestamp.class, new SqlDateConverter());
    new MyDatabaseRestClientDatabase_Database(this);
  }
}
