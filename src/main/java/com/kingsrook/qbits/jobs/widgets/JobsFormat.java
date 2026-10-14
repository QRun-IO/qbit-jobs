/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.kingsrook.qbits.jobs.widgets;


import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/*******************************************************************************
 ** Human-readable values for the Jobs widgets: counts with separators, relative
 ** times within a day ("4m ago", "in 3m") and absolute UTC times beyond it
 ** ("Oct 14, 08:46 UTC"). Never raw ISO timestamps.
 *******************************************************************************/
public final class JobsFormat
{
   /** The single-character ellipsis appended to clipped text. */
   public static final String ELLIPSIS = String.valueOf((char) 0x2026);

   private static final DateTimeFormatter ABSOLUTE = DateTimeFormatter.ofPattern("MMM d, HH:mm 'UTC'", Locale.US).withZone(ZoneOffset.UTC);



   /*******************************************************************************
    ** Static helpers only.
    *******************************************************************************/
   private JobsFormat()
   {
   }



   /*******************************************************************************
    ** Binary byte units, with one decimal place above bytes.
    *******************************************************************************/
   public static String bytes(long value)
   {
      double amount = Math.max(0, value);
      String[] units = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};
      int unit = 0;
      while(amount >= 1024 && unit < units.length - 1)
      {
         amount /= 1024;
         unit++;
      }
      return (unit == 0 ? count((long) amount) + " B" : String.format(Locale.US, "%.1f %s", amount, units[unit]));
   }



   /*******************************************************************************
    ** A count with thousands separators.
    *******************************************************************************/
   public static String count(long value)
   {
      return (String.format(Locale.US, "%,d", value));
   }



   /*******************************************************************************
    ** Relative within 24 hours of now, absolute UTC otherwise; "Unknown" for null.
    *******************************************************************************/
   public static String time(Instant value, Instant now)
   {
      if(value == null)
      {
         return ("Unknown");
      }
      Duration between = Duration.between(value, now);
      if(between.abs().compareTo(Duration.ofHours(24)) >= 0)
      {
         return (ABSOLUTE.format(value));
      }
      return (relative(value, now));
   }



   /*******************************************************************************
    ** Always relative ("12s ago", "3d ago", "in 8m"); "Unknown" for null.
    *******************************************************************************/
   public static String relative(Instant value, Instant now)
   {
      if(value == null)
      {
         return ("Unknown");
      }
      long seconds = Duration.between(value, now).getSeconds();
      if(seconds < 0)
      {
         return ("in " + span(-seconds));
      }
      return (seconds < 5 ? "just now" : span(seconds) + " ago");
   }



   /*******************************************************************************
    ** A duration in its largest whole unit: 45s, 8m, 2h, 3d.
    *******************************************************************************/
   public static String span(long seconds)
   {
      long value = Math.max(0, seconds);
      if(value < 60)
      {
         return (value + "s");
      }
      if(value < 3600)
      {
         return (value / 60 + "m");
      }
      if(value < 86_400)
      {
         return (value / 3600 + "h");
      }
      return (value / 86_400 + "d");
   }



   /*******************************************************************************
    ** The first max characters of a single-line plain text, with an ellipsis
    ** when clipped; empty for null.
    *******************************************************************************/
   public static String clip(String text, int max)
   {
      if(text == null)
      {
         return ("");
      }
      String line = text.replaceAll("\\s+", " ").trim();
      return (line.length() <= max ? line : line.substring(0, max - 1) + ELLIPSIS);
   }
}
