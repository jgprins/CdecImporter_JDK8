package bubblewrap.io.schedules.enums;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.datetime.WeekDay;
import bubblewrap.io.schedules.TimeStep;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum Interval {
  /**
   * Undefined Interval (timeUnit=MILLISECONDS)
   */
  NONE(0,"Undefined",TimeUnit.MILLISECONDS) {
    /**
   * {@inheritDoc}
   * <p>OVERRIDE: Get utcDt = DateTime.toUTCDate(date), then returns a TimeStep with
   * time =utcDt.totalMillisecond, timeUnit = MILLISECONDS, and timeZone = date.timeZone.
   * Return null if date=null.</p>
   */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        DateTime utcDate = DateTime.toUTCDate(date);
        result = new TimeStep(utcDate.getTotalMilliseconds(), TimeUnit.MILLISECONDS,
                                                                      date.getTimeZone());
      }
      return result;
    }
  },
  /**
   * MilliSeconds Interval (timeUnit=MILLISECONDS)
   */
  MILLISECONDS(1,"MilliSeconds",TimeUnit.MILLISECONDS) {
    /**
   * {@inheritDoc}
   * <p>OVERRIDE: Get utcDt = DateTime.toUTCDate(date), then returns a TimeStep with
   * time =utcDt.totalMillisecond, timeUnit = MILLISECONDS, and timeZone = date.timeZone.
   * Return null if date=null.</p>
   */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        DateTime utcDate = DateTime.toUTCDate(date);
        result = new TimeStep(utcDate.getTotalMilliseconds(), TimeUnit.MILLISECONDS,
                                                                      date.getTimeZone());
      }
      return result;
    }
  },
  /**
   * Seconds Interval (timeUnit=SECONDS)
   */
  SECONDS(2,"Seconds",TimeUnit.SECONDS) {
    /**
   * {@inheritDoc}
   * <p>OVERRIDE: Get utcDt = DateTime.toUTCDate(date), then returns a TimeStep with
   * time =utcDt.totalMillisecond -> SECONDS, timeUnit = SECONDS, and timeZone =
   * date.timeZone. Return null if date=null.</p>
   */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          DateTime utcDate = DateTime.toUTCDate(date);
          long time = TimeUnit.SECONDS.convert(utcDate.getTotalMilliseconds(),
                                                                  TimeUnit.MILLISECONDS);
          result = new TimeStep(time, TimeUnit.SECONDS, date.getTimeZone());
        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }
  },
  /**
   * Minutes Interval (timeUnit=MINUTES)
   */
  MINUTES(3,"Minutes",TimeUnit.MINUTES) {
    /**
   * {@inheritDoc}
   * <p>OVERRIDE: Get utcDt = DateTime.toUTCDate(date), then returns a TimeStep with
   * time =utcDt.totalMillisecond -> MINUTES, timeUnit = MINUTES, and timeZone =
   * date.timeZone. Return null if date=null.</p>
   */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          DateTime utcDate = DateTime.toUTCDate(date);
          utcDate.setTime(date.getHour(), date.getMinute(), 0.0d);
          long time = TimeUnit.MINUTES.convert(utcDate.getTotalMilliseconds(),
                                                                 TimeUnit.MILLISECONDS);
          result = new TimeStep(time, TimeUnit.MINUTES, date.getTimeZone());
        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }
  },
  /**
   * Hours Interval (timeUnit=HOURS)
   */
  HOURS(4,"Hours",TimeUnit.HOURS) {
    /**
   * {@inheritDoc}
   * <p>OVERRIDE: Get utcDt = DateTime.toUTCDate(date), then returns a TimeStep with
   * time =utcDt.time(date.hour,0,0.0).totalMillisecond -> HOURS, timeUnit = HOURS, and
   * timeZone = date.timeZone. Return null if date=null.</p>
   */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          DateTime utcDate = DateTime.toUTCDate(date);
          utcDate.setTime(date.getHour(), 0, 0.0d);
          long time = TimeUnit.HOURS.convert(utcDate.getTotalMilliseconds(),
                                                                 TimeUnit.MILLISECONDS);
          result = new TimeStep(time, TimeUnit.HOURS, date.getTimeZone());
        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }
  },
  /**
   * Days Interval (timeUnit=DAYS)
   */
  DAYS(5,"Days",TimeUnit.DAYS) {
    /**
    * {@inheritDoc}
    * <p>OVERRIDE: Get utcDt = DateTime.toUTCDate(date), then returns a TimeStep with
   * time =utcDt.time(0,0,0.0).totalMillisecond -> DAYS, timeUnit = DAYS, and
   * timeZone = date.timeZone. Return null if date=null.</p>
    */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          DateTime utcDate = DateTime.toUTCDate(date);
          utcDate.setTime(0, 0, 0.0d);
          long time = TimeUnit.DAYS.convert(date.getTotalMilliseconds(),
                                                                  TimeUnit.MILLISECONDS);
          result = new TimeStep(time, TimeUnit.DAYS, date.getTimeZone());
        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }
  },
  /**
   * Weeks Interval (timeUnit=DAYS)
   */
  WEEKS(6, "Weeks", TimeUnit.DAYS) {
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Get utcDt = DateTime.toUTCDate(date), get weekDay =
     * WeekDay.fromValue(utcDate.dayOfWeek), dayOfWeek = weekDay.dayOfWeek, then
     * returns a TimeStep with time = (utcDt.time(0,0,0.0).totalMillisecond -> DAYS) -
     * (dayOfWeek - 1), timeUnit = DAYS, and timeZone = date.timeZone.
     * Return null if date=null.</p>
     */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          DateTime utcDate = DateTime.toUTCDate(date);
          utcDate.setTime(0, 0, 0.0d);
          long time = TimeUnit.DAYS.convert(utcDate.getTotalMilliseconds(),
                                                                  TimeUnit.MILLISECONDS);
          WeekDay weekDay = WeekDay.fromValue(utcDate.getDayOfWeek());
          int dayOfWeek = weekDay.getDayOfWeek();
          int startDay = WeekDay.FIRST_DAY_OF_WEEK.getDayOfWeek();
          time -= (dayOfWeek - startDay);
//          long milli = TimeUnit.MILLISECONDS.convert(time,TimeUnit.DAYS);
//          utcDate = new DateTime(milli, DateTime.UTCTimeZone);
//          DateTime localDt = DateTime.toTimeZoneDate(utcDate, date.getTimeZone());
          result = new TimeStep(time, TimeUnit.DAYS, date.getTimeZone());
        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Return 7 (days) regardless of the <tt>intervalIndex</tt> value.</p>
     */
    @Override
    public long getTickTime(Integer tickIndex) {
      return 7l;
    }
  },
  /**
   * Months Interval (timeUnit=DAYS)
   */
  MONTHS(7, "Months", TimeUnit.DAYS) {
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Get the curYr and curMonth from the date and returns a TimeStep with
     * time = date(curYr,curMonth,1,UTC).time(0,0,0.0).millisecond -> DAYS, timeUnit =
     * DAYS, and timeZone = date.timeZone. Return null if date=null.</p>
     */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          int curYr = date.getYear();
          int curMon = date.getMonth();
          DateTime firstOfMon = new DateTime(curYr, curMon, 1, DateTime.UTCTimeZone);
          firstOfMon.setTime(0, 0, 0.0d);
          Long milliTime = firstOfMon.getTotalMilliseconds();
          Long time = TimeUnit.DAYS.convert(milliTime, TimeUnit.MILLISECONDS);
          result = new TimeStep(time, TimeUnit.DAYS, date.getTimeZone());

        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: returns {@linkplain #getMonthIndex(bubblewrap.io.datetime.DateTime)
     * Interval.getMonthIndex(date)}</p>
     */
    @Override
    public int getTickIndex(DateTime date) {
      return Interval.getMonthIndex(date);
    }

    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Return the number of months per day for the tickIndex, which in for the
     * enum = the monthIndex. With Time=29 for index 1 (Feb of Leap Year), an 28 for all
     * other February indices and other Time = 30/31 according to the respective month's
     * monthIndex</p>
     */
    @Override
    public long getTickTime(Integer tickIndex) {
      long result = 30;
      if (tickIndex != null) {
        if (tickIndex > 47) {
          while (tickIndex > 47) {
            tickIndex -= 48;
          }
        } else if (tickIndex < 0) {
          while (tickIndex < 0) {
            tickIndex += 48;
          }
        }

        if (DataEntry.inArray(tickIndex, 0, 2, 4, 6, 7, 9, 11,
                                         12, 14, 16, 18, 19, 21, 23,
                                         24, 26, 28, 30, 31, 33, 35,
                                         36, 38, 40, 42, 43, 45, 47)) {
          result = 31;
        } else if (DataEntry.inArray(tickIndex, 3, 5, 8, 10,
                                                15, 17, 20, 22,
                                                27, 29, 32, 34,
                                                39, 41, 44, 46)) {
          result = 30;
        } else if (tickIndex == 1) {
          result = 29;
        } else if (DataEntry.inArray(tickIndex, 13, 25, 37)) {
          result = 28;
        }
      }
      return result;
    }
  },
  /**
   * Months Interval (timeUnit=DAYS)
   */
  YEARS(8, "Years", TimeUnit.DAYS) {
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Get the curYr from the date and returns a TimeStep with time =
     * date(curYr,0,1,UTC).time(0,0,0.0).millisecond -> DAYS, timeUnit = DAYS, and
     * timeZone = date.timeZone. Return null if date=null.</p>
     */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          int curYr = date.getYear();
          DateTime firstOfYr = new DateTime(curYr, 0, 1, DateTime.UTCTimeZone);
          firstOfYr.setTime(0, 0, 0.0d);
          Long milliTime = firstOfYr.getTotalMilliseconds();
          Long time = TimeUnit.DAYS.convert(milliTime, TimeUnit.MILLISECONDS);
          result = new TimeStep(time, TimeUnit.DAYS, date.getTimeZone());
        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: returns {@linkplain #getYearIndex(bubblewrap.io.datetime.DateTime)
     * Interval.getYearIndex(date)}</p>
     */
    @Override
    public int getTickIndex(DateTime date) {
      return Interval.getYearIndex(date);
    }

    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: returns 366 (days) if ((tickIndex = this.getYearIndex(tickIndex)) = 0)
     * else returns 365 (days). Note: tickIndex=0 represents a {@linkplain
     * #getYearIndex(bubblewrap.io.datetime.DateTime) getYearIndex} of a leap year.</p>
     */
    @Override
    public long getTickTime(Integer tickIndex) {
      long result = 365;
      if (tickIndex > 3) {
        while (tickIndex > 3) {
          tickIndex -= 4;
        }
      } else if (tickIndex < 0) {
        while (tickIndex < 0) {
          tickIndex += 4;
        }
      }
      if (Interval.getYearIndex(tickIndex) == 0l) {
        result++;
      }
      return result;
    }
  },
  /**
   * Months Interval (timeUnit=DAYS)
   */
  WATERYEARS(9, "Water Years", TimeUnit.DAYS) {
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Get the curWy from the date and returns a TimeStep with time =
     * date(curWy-1,9,1,UTC).time(0,0,0.0).millisecond -> DAYS, timeUnit = DAYS, and
     * timeZone = date.timeZone. Return null if date=null.</p>
     */
    @Override
    public TimeStep onDateToTimeStep(DateTime date) {
      TimeStep result = null;
      if (date != null) {
        try {
          int curYr = date.getYear();
          int month = date.getMonth();
          if (month < 9) {
            curYr--;
          }
          DateTime firstOfYr = new DateTime(curYr, 9, 1, DateTime.UTCTimeZone);
          firstOfYr.setTime(0, 0, 0.0d);
          Long milliTime = firstOfYr.getTotalMilliseconds();
          Long time = TimeUnit.DAYS.convert(milliTime, TimeUnit.MILLISECONDS);
          result = new TimeStep(time, TimeUnit.DAYS, date.getTimeZone());
        } catch (Exception exp) {
          throw new IllegalArgumentException(this.getClass().getSimpleName()
                  + ".onDateToTimeStep Error:\n " + exp.getMessage(), exp);
        }
      }
      return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: returns {@linkplain #getYearIndex(bubblewrap.io.datetime.DateTime)
     * Interval.getYearIndex(date)}</p>
     */
    @Override
    public int getTickIndex(DateTime date) {
      return Interval.getYearIndex(date);
    }

    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: returns 366 (days) if ((tickIndex = this.getYearIndex(tickIndex)) = 0)
     * else returns 365 (days). Note: tickIndex=0 represents a {@linkplain
     * #getYearIndex(bubblewrap.io.datetime.DateTime) getYearIndex} of a leap year.</p>
     */
    @Override
    public long getTickTime(Integer tickIndex) {
      long result = 365;
      if (tickIndex > 3) {
        while (tickIndex > 3) {
          tickIndex -= 4;
        }
      } else if (tickIndex < 0) {
        while (tickIndex < 0) {
          tickIndex += 4;
        }
      }
      if (Interval.getYearIndex(tickIndex) == 0l) {
        result++;
      }
      return result;
    }
  };

  // <editor-fold defaultstate="collapsed" desc="Enum Definition">

  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * A Defined enum value (not its ordinate)
   */
  public final int value;
  /**
   * A Display label for the enum option
   */
  public final String label;
  /**
   * The Interval's TimeUnit
   */
  public final TimeUnit timeUnit;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Private Constructor
   * @param value the option value
   * @param label the option label
   * @param timeUnit the TimeUnits used to calculate the Interval's Time Span
   */
  private Interval(int value, String label, TimeUnit timeUnit) {
    this.label = label;
    this.value = value;
    this.timeUnit = timeUnit;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }


  // </editor-fold>

  /**
   * Call to get this Interval's TimeStep for the current Date-Time.
   * The TimeStep's TimeUnits will be that of the Interval
   * @param timeZone the applicable {@linkplain TimeZone} or null to use the current
   * local.
   * @return a new TimeStep instance
   * @throws IllegalArgumentException if the process fails.
   */
  public final TimeStep getCurStep(TimeZone timeZone) {
    TimeStep result = null;
    try {
      result = this.onDateToTimeStep(DateTime.getNow(timeZone));
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.toString()
                                        + ".getCurStep Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * Call to get this Interval's TimeStep for the specified Date-Time.
   * The TimeStep's TimeUnits will be that of the Interval
   * @param date the specified date (cannot be null)
   * @return a new TimeStep instance
   * @throws IllegalArgumentException if <tt>date</tt>=null or the process fails.
   */
  public final TimeStep getTimeStep(DateTime date) {
    TimeStep result = null;
    try {
      if (date == null) {
        throw new Exception("The specified date cannot be null.");
      }
      result = this.onDateToTimeStep(date);
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.toString()
                                        + ".getTimeStep Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * Get the Next TimeStep using this Interval and adding <tt>stepTicks</tt> interval
   * tick to the curStep. It gets the UTC DateTime for the curStep's time and timeUnit
   * properties and from that calling {@linkplain #getTickIndex(
   * bubblewrap.io.datetime.DateTime) this.getTickIndex(utcDate)} to retrieve the
   * curStep's interval tickIndex. Using the curTickIndex, the <tt>setTicks</tt>,
   * calculate the step-time to add to the curStep - calling {@linkplain #getTickTime(
   * java.lang.Integer) this.getTickTime} to get the time for each stepTick.
   * <p>Width the newStep's time know, initiate the new TimeStep instance for calculated
   * time, curTime.timeUnit, curTime.shiftTime, and curTime.shiftUnit and return the
   * new TimeStep.
   * @param curStep the current TiemStep to add the interval to (must be assigned)
   * @param stepTicks the number if interval tick to add to the new time step (must be
   * &gt; 0)
   * @return the new TimeStep
   * @throws IllegalArgumentException if curStep = null, stepTicks &le; 0, or error
   * occurred during the process.
   */
  public final TimeStep getNextStep(TimeStep curStep, long stepTicks) {
    TimeStep result = null;
    try {
      if (curStep == null) {
        throw new Exception("The current TimeStep cannot be null.");
      }
      if (stepTicks <= 0) {
        throw new Exception("Invalid step ticks, must be greater than zero");
      }
      long milliSecs =
                TimeUnit.MILLISECONDS.convert(curStep.getTime(), curStep.getTimeUnit());
      DateTime curDt = new DateTime(milliSecs, DateTime.UTCTimeZone);
      int curTickIdx = this.getTickIndex(curDt);

      long nextTime = curStep.getTime();
      int nextTckIdx = curTickIdx;
      for (int i = 1; i <= stepTicks; i++) {
        nextTime += this.getTickTime(nextTckIdx);
        nextTckIdx++;
      }
      result = new TimeStep(nextTime, this.timeUnit, curStep.getTimeZone());
      result.setTimeShift(curStep.getShiftTime(), curStep.getShiftTimeUnit());
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.toString()
                                        + ".getTimeStep Error:\n " + exp.getMessage(),
              exp);
    }
    return result;
  }

  /**
   * Get the Prior TimeStep using this Interval and deducting <tt>stepTicks</tt> interval
   * tick to the curStep. It gets the UTC DateTime for the curStep's time and timeUnit
   * properties and from that calling {@linkplain #getTickIndex(
   * bubblewrap.io.datetime.DateTime) this.getTickIndex(utcDate)} to retrieve the
   * curStep's interval tickIndex. Using the curTickIndex, the <tt>setTicks</tt>,
   * calculate the step-time to deduct from the curStep - calling {@linkplain
   * #getTickTime(java.lang.Integer) this.getTickTime} to get the time for each stepTick.
   * <p>Width the newStep's time know, initiate the new TimeStep instance for calculated
   * time, curTime.timeUnit, curTime.shiftTime, and curTime.shiftUnit and return the
   * new TimeStep.
   * @param curStep the current TiemStep to add the interval to (must be assigned)
   * @param stepTicks the number if interval tick to add to the new time step (must be
   * &gt; 0)
   * @return the new TimeStep
   * @throws IllegalArgumentException if curStep = null, stepTicks &le; 0, or error
   * occurred during the process.
   */
  public final TimeStep getPriorStep(TimeStep curStep, long stepTicks) {
    TimeStep result = null;
    try {
      if (curStep == null) {
        throw new Exception("The current TiemStep cannot be null.");
      }
      if (stepTicks <= 0) {
        throw new Exception("Invalid step ticks, must be greater than zero");
      }
      long milliSecs =
                TimeUnit.MILLISECONDS.convert(curStep.getTime(), curStep.getTimeUnit());
      DateTime curDt = new DateTime(milliSecs, DateTime.UTCTimeZone);
      int curTickIdx = this.getTickIndex(curDt);

      long priorTime = curStep.getTime();
      for (int i = 1; i <= stepTicks; i++) {
        int priorTckIdx = curTickIdx - i;
        priorTime -= this.getTickTime(priorTckIdx);
      }
      result = new TimeStep(priorTime, this.timeUnit, curStep.getTimeZone());
      result.setTimeShift(curStep.getShiftTime(), curStep.getShiftTimeUnit());
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.toString()
                                        + ".getTimeStep Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * Call convert the specified timeStep  to a timeStep of this Interval.
   * @param curStep timeStep to convert
   * @return this.getTimeStep(curStep.dateTime) of null if curStep == null or
   * curStep.dateTime = null.
   */
  public final TimeStep convertTo(TimeStep curStep) {
    TimeStep result = null;
    DateTime curDt = null;
    if ((curStep != null) && ((curDt = curStep.getDateTime()) != null)) {
      result = this.getTimeStep(curDt);
    }
    return result;
  }

  /**
   * CAN OVERRIDE: Get the Interval's Tick Index for the specified data.
   * <p>The base method always returns 0.
   * @param date the date to evaluate
   * @return the Interval's Tick Index.
   */
  public int getTickIndex(DateTime date) {
    return 0;
  }

  /**
   * CAN OVERRIDE: Get the incremental time in the Interval's TimeUnit's for the
   * specified interval <tt>tickIndex</tt>. This is used to calculate the time span in
   * time units for Intervals with variant time span per interval tick (e.g., months,
   * years).
   * <p>The base method return 1 (i.e., a time unit per interval) regardless of the
   * specified <tt>tickIndex</tt>.
   * @param tickIndex the interval's tick index
   * @return the time span in the Interval's TimeUnit (e.g., days for a MONTH interval)
   */
  public long getTickTime(Integer tickIndex) {
    return 1l;
  }

  /**
   * ABSTRACT:Called to convert a specified <tt>date</tt> to the closest (the latest)
   * Interval tick (e.g. For Interval.MONT the the date will be first of the month).
   * @param date the specified
   * @return the TimeStep instance or null of date=null.
   */
  public abstract TimeStep onDateToTimeStep(DateTime date);
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the Interval associated with <tt>value</tt>
   * @param value the Interval.value to search for
   * @return the matching Interval or NONE if not found.
   */
  public static Interval fromValue(int value) {
    Interval result = Interval.NONE;
    for (Interval enumVal : Interval.values()) {
      if (enumVal.value == value) {
        result = enumVal;
        break;
      }
    }
    return result;
  }

  /**
   * Get the Year Index for the specified date. The index range = [0..3] with 0
   * indicating leap year.
   * @param date the date to convert
   * @return this.getYearIndex(date.year) or 0 id date=null
   */
  public static int getYearIndex(DateTime date) {
    int result = 0;
    if (date != null) {
      Integer year = date.getYear();
      result = Interval.getYearIndex(year);
    }
    return result;
  }

  /**
   * Get the Year Index for the specified year. The index range = [0..3] with 0
   * indicating leap year.
   * @param year the year (e.g., 1990)
   * @return the year index or 0 if year &le; 0 | null.
   */
  public static int getYearIndex(Integer year) {
    int result = 0;
    if ((year != null) && (year > 0)) {
      Long lastLeap = Math.round(Math.floor((1.0d * year)/4.0) *4.0d);
      result = year - lastLeap.intValue();
    }
    return result;
  }

  /**
   * Get the Month Index[0..47] in a four-year leap cycle with month index 0 = Jan of
   * the Leap Year and 47 = Dec before the next leap year.
   * @param date the date-time to calculate the index for
   * @return Interval.getMonthIndex(date.year, date.month) or 0 if date = null
   */
  public static int getMonthIndex(DateTime date) {
    int result = 0;
    if (date != null) {
      Integer year = date.getYear();
      Integer month = date.getMonth();
      result = Interval.getMonthIndex(year, month);
    }
    return result;
  }

  /**
   * Get the Month Index[0..47] in a four-year leap cycle with month index 0 = Jan of
   * the Leap Year and 47 = Dec before the next leap year.
   * @param year the year (e.g., 1990)
   * @param month the month of the year
   * @return monthIdx + (Interval.getYearIndex(year) * 12) or 0 if year and month = 0 or
   * (Interval.getYearIndex(year) * 12) if month = null.
   */
  public static int getMonthIndex(Integer year, Integer month) {
    int result = 0;
    int yrIdx = 0;
    if (year != null) {
      yrIdx = Interval.getYearIndex(year);
    }
    int monthIdx = 0;
    if ((month != null) && (month >= 0) && (month < 12)) {
      monthIdx = month;
    }
    result = monthIdx + (yrIdx * 12);
    return result;
  }

  /**
   * Get the TimeUnit by it ordinal value
   * @param ord the TimeUnit's ordinal to search for
   * @return the matching TimeUnit or {@linkplain TimeUnit#MILLISECONDS} is ord = null or
   * invalid.
   */
  public static TimeUnit getTimeUnitByOrd(Integer ord) {
    TimeUnit result = TimeUnit.MILLISECONDS;
    if ((ord != null) && (ord >= 0) && (ord <= 6)) {
      for (TimeUnit enumVal : TimeUnit.values()) {
        if (ord == enumVal.ordinal()) {
          result = enumVal;
          break;
        }
      }
    }
    return result;
  }
  // </editor-fold>
}
