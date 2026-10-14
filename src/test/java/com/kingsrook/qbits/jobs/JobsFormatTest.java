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


package com.kingsrook.qbits.jobs;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.kingsrook.qbits.jobs.widgets.JobsFormat;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Human-readable counts, times and labels.
 *******************************************************************************/
class JobsFormatTest
{
   private static final Instant NOW = Instant.parse("2026-10-14T08:46:30Z");



   /*******************************************************************************
    ** Relative within a day, absolute UTC beyond it, never ISO.
    *******************************************************************************/
   @Test
   void times()
   {
      assertThat(JobsFormat.time(null, NOW)).isEqualTo("Unknown");
      assertThat(JobsFormat.time(NOW.minusSeconds(2), NOW)).isEqualTo("just now");
      assertThat(JobsFormat.time(NOW.minusSeconds(45), NOW)).isEqualTo("45s ago");
      assertThat(JobsFormat.time(NOW.minus(4, ChronoUnit.MINUTES), NOW)).isEqualTo("4m ago");
      assertThat(JobsFormat.time(NOW.minus(2, ChronoUnit.HOURS), NOW)).isEqualTo("2h ago");
      assertThat(JobsFormat.time(NOW.plus(3, ChronoUnit.MINUTES), NOW)).isEqualTo("in 3m");
      assertThat(JobsFormat.time(NOW.minus(24, ChronoUnit.HOURS), NOW)).isEqualTo("Oct 13, 08:46 UTC");
      assertThat(JobsFormat.time(NOW.plus(2, ChronoUnit.DAYS), NOW)).isEqualTo("Oct 16, 08:46 UTC");
      assertThat(JobsFormat.relative(NOW.minus(3, ChronoUnit.DAYS), NOW)).isEqualTo("3d ago");
      assertThat(JobsFormat.relative(null, NOW)).isEqualTo("Unknown");
      assertThat(JobsFormat.span(-5)).isEqualTo("0s");
   }



   /*******************************************************************************
    ** Counts, clipping and status labels.
    *******************************************************************************/
   @Test
   void countsClipsAndLabels()
   {
      assertThat(JobsFormat.count(1234567)).isEqualTo("1,234,567");
      assertThat(JobsFormat.clip(null, 10)).isEmpty();
      assertThat(JobsFormat.clip("  short\n text ", 20)).isEqualTo("short text");
      assertThat(JobsFormat.clip("abcdefghijkl", 5)).isEqualTo("abcd" + JobsFormat.ELLIPSIS).hasSize(5);
      assertThat(JobsCoreContract.statusLabel(JobsCoreContract.COMPLETED_WITH_ERRORS)).isEqualTo("Completed With Errors");
      assertThat(JobsCoreContract.statusLabel(JobsCoreContract.AWAITING_CONFIRMATION)).isEqualTo("Awaiting Confirmation");
      assertThat(JobsCoreContract.statusLabel(JobsCoreContract.NEEDS_REVIEW)).isEqualTo("Needs Review");
      assertThat(JobsCoreContract.statusLabel(JobsCoreContract.PAUSED)).isEqualTo("Paused");
      assertThat(JobsCoreContract.statusLabel("SOMETHING_NEW")).isEqualTo("SOMETHING_NEW");
      assertThat(JobsCoreContract.statusLabel(null)).isEqualTo("Unknown");
   }
}
