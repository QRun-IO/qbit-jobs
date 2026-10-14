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

package com.kingsrook.qbits.jobs.demo;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.jobs.SystemScope;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** 30 days of finished jobs for the demo's history and throughput widgets:
 ** realistic processes, sizes, durations and outcomes, with their chunks.
 *******************************************************************************/
final class DemoHistory
{
   private static final String[][] PROCESSES = {
      {"demo_customer.bulkInsert", "Import customers", "demo_customer"},
      {"demo_customer.bulkEdit", "Bulk edit customers", "demo_customer"},
      {JobsDemoServer.RECALC, "Recalculate customer tiers", "demo_customer"},
      {"demo_customer.bulkDelete", "Delete inactive customers", "demo_customer"},
      {JobsDemoServer.NIGHTLY, "Nightly partner export", null}
   };



   /*******************************************************************************
    ** not instantiable
    *******************************************************************************/
   private DemoHistory()
   {
   }



   /*******************************************************************************
    ** Insert the history (as the system user; the rows are owned by the demo user).
    *******************************************************************************/
   static void insert(Random random) throws QException
   {
      Instant now = Instant.now();
      for(int i = 0; i < 140; i++)
      {
         String[] process  = PROCESSES[random.nextInt(PROCESSES.length)];
         boolean  recent   = i < 30;
         Instant  finished = recent ? now.minus(random.nextInt(23 * 60) + 5, ChronoUnit.MINUTES) : now.minus(1 + random.nextInt(29), ChronoUnit.DAYS).minus(random.nextInt(1440), ChronoUnit.MINUTES);
         int      rows     = process[0].equals(JobsDemoServer.NIGHTLY) ? 0 : 200 + random.nextInt(18_000);
         int      roll     = random.nextInt(100);
         String   outcome  = roll < 78 ? "COMPLETED" : roll < 90 ? "COMPLETED_WITH_ERRORS" : roll < 96 ? "FAILED" : "CANCELLED";
         String   status   = rows == 0 && "COMPLETED_WITH_ERRORS".equals(outcome) ? "COMPLETED" : outcome;
         int      failed   = "COMPLETED_WITH_ERRORS".equals(status) ? 1 + random.nextInt(Math.max(1, rows / 200)) : 0;
         int      chunks   = Math.max(1, (rows + 499) / 500);
         Instant  started  = finished.minusSeconds(Math.max(4, rows / (40 + random.nextInt(80))));
         String   error    = "FAILED".equals(status) ? (process[0].equals(JobsDemoServer.NIGHTLY) ? "Partner SFTP host refused the connection" : "Lost database connection during chunk 3; retries exhausted") : null;

         QRecord job = new QRecord()
            .withValue("label", process[1])
            .withValue("processName", process[0])
            .withValue("tableName", process[2])
            .withValue("mode", "EXECUTE")
            .withValue("status", status)
            .withValue("priority", 5)
            .withValue("userId", JobsDemoServer.USER_ID)
            .withValue("clientChannel", random.nextInt(5) == 0 ? "assistant/turn-" + (100 + i) : "next-ui")
            .withValue("sourceType", process[2] == null ? "STEPS" : "TABLE_FILTER")
            .withValue("idempotency", "NATURAL")
            .withValue("semantics", "CHUNKED")
            .withValue("chunkSize", 500)
            .withValue("maxParallelChunks", 1)
            .withValue("rowsTotal", rows)
            .withValue("rowsDone", "CANCELLED".equals(status) ? rows / 3 : rows)
            .withValue("rowsOk", ("CANCELLED".equals(status) ? rows / 3 : rows) - failed)
            .withValue("rowsFailed", failed)
            .withValue("chunksTotal", chunks)
            .withValue("chunksDone", "FAILED".equals(status) ? Math.max(0, chunks - 1) : chunks)
            .withValue("chunksFailed", "FAILED".equals(status) ? 1 : 0)
            .withValue("queuedAt", started.minusSeconds(3))
            .withValue("startedAt", started)
            .withValue("finishedAt", finished)
            .withValue("lastProgressAt", finished)
            .withValue("errorMessage", error)
            .withValue("leaseVersion", 0)
            .withValue("createDate", started.minusSeconds(3))
            .withValue("modifyDate", finished);
         Integer jobId = SystemScope.call(() -> new InsertAction().execute(new InsertInput("jobs_job").withRecord(job)).getRecords().get(0).getValueInteger("id"));

         List<QRecord> chunkRecords = new ArrayList<>();
         long          span         = Math.max(1, finished.getEpochSecond() - started.getEpochSecond());
         for(int c = 0; c < chunks; c++)
         {
            int     chunkRows = Math.min(500, rows - c * 500);
            Instant chunkEnd  = started.plusSeconds(span * (c + 1) / chunks);
            chunkRecords.add(new QRecord().withValue("jobId", jobId).withValue("seq", c)
               .withValue("status", "FAILED".equals(status) && c == chunks - 1 ? "FAILED" : "DONE")
               .withValue("attempt", 1).withValue("maxAttempts", 3).withValue("leaseVersion", 1)
               .withValue("rowsIn", Math.max(0, chunkRows)).withValue("rowsOk", Math.max(0, chunkRows)).withValue("rowsFailed", 0)
               .withValue("startedAt", chunkEnd.minusSeconds(Math.max(1, span / chunks))).withValue("finishedAt", chunkEnd)
               .withValue("createDate", started).withValue("modifyDate", chunkEnd));
         }
         SystemScope.call(() -> new InsertAction().execute(new InsertInput("jobs_chunk").withRecords(chunkRecords)));

         List<QRecord> events = new ArrayList<>();
         events.add(event(jobId, "LAUNCHED", started.minusSeconds(3), "Launched " + process[0]));
         events.add(event(jobId, "STARTED", started, "Started on demo-pod-1"));
         events.add(event(jobId, "COMPLETED".equals(status) || "COMPLETED_WITH_ERRORS".equals(status) ? "COMPLETED" : status, finished, status));
         SystemScope.call(() -> new InsertAction().execute(new InsertInput("jobs_event").withRecords(events)));

         if(failed > 0)
         {
            List<QRecord> errors = new ArrayList<>();
            for(int e = 0; e < Math.min(failed, 25); e++)
            {
               errors.add(new QRecord().withValue("jobId", jobId).withValue("rowNumber", 2 + random.nextInt(Math.max(1, rows)))
                  .withValue("fieldName", "email").withValue("errorCode", "BadInputStatusMessage").withValue("message", "Customer has no email address")
                  .withValue("resolved", false).withValue("createDate", finished));
            }
            SystemScope.call(() -> new InsertAction().execute(new InsertInput("jobs_row_error").withRecords(errors)));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QRecord event(Integer jobId, String type, Instant at, String detail)
   {
      return (new QRecord().withValue("jobId", jobId).withValue("type", type).withValue("actorUserId", JobsDemoServer.USER_ID)
         .withValue("pod", "demo-pod-1").withValue("detail", detail).withValue("createDate", at));
   }

}
