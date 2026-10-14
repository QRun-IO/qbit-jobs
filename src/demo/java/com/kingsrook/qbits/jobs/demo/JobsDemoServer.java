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


import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qbits.jobs.JobsQBitConfig;
import com.kingsrook.qbits.jobs.JobsQBitProducer;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.AbstractQQQApplication;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.jobs.DurableJobs;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsMetaData;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsMetaDataProvider;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsSecurity;
import com.kingsrook.qqq.backend.core.jobs.DurableProcessMetaData;
import com.kingsrook.qqq.backend.core.jobs.SystemScope;
import com.kingsrook.qqq.backend.core.jobs.model.JobIdempotency;
import com.kingsrook.qqq.backend.core.jobs.model.LaunchRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.FullyAnonymousAuthenticationModule;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaUpdateStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.middleware.javalin.QApplicationJavalinServer;


/*******************************************************************************
 ** Local demo of the Jobs app (screenshots): an H2 database with the core jobs
 ** tables, a customer table, real durable jobs running on this pod (one long
 ** job with live progress, one queued behind it, one failed, one with row
 ** errors, one awaiting confirmation) and 30 days of finished-job history.
 **
 ** Run: ./mvnw -Pdemo -Dqqq.snapshot.version=4.1.0-JOBS-SNAPSHOT test-compile exec:java
 **        -Dexec.classpathScope=test -Dexec.mainClass=com.kingsrook.qbits.jobs.demo.JobsDemoServer
 *******************************************************************************/
public class JobsDemoServer extends AbstractQQQApplication
{
   static final String BACKEND   = "rdbms";
   static final String CUSTOMER  = "demo_customer";
   static final String RECALC    = "demoRecalculateTiers";
   static final String NIGHTLY   = "demoNightlyExport";
   static final String STORAGE   = "demo_jobs_storage";
   static final String USER_ID   = "demo-user";



   /*******************************************************************************
    ** Anonymous, as a named user holding jobs_admin (demo only).
    *******************************************************************************/
   public static class DemoAuthenticationModule extends FullyAnonymousAuthenticationModule
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public QSession createSession(QInstance qInstance, Map<String, String> context)
      {
         QSession session = super.createSession(qInstance, context);
         session.setUser(new QUser().withIdReference(USER_ID).withFullName("Demo User"));
         session.withPermission("jobs_admin");
         DurableJobsSecurity.applyToSession(session);
         return (session);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public QSession createAutomatedSessionForUser(QInstance qInstance, Serializable userId)
      {
         QSession session = new QSession().withUser(new QUser().withIdReference(String.valueOf(userId)).withFullName("Demo User"));
         session.withPermission("jobs_admin");
         return (session);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static RDBMSBackendMetaData backend()
   {
      RDBMSBackendMetaData backend = new RDBMSBackendMetaData().withName(BACKEND).withVendor("h2").withHostName("mem").withDatabaseName("jobs_demo").withUsername("sa");
      backend.setJdbcUrl(ConnectionManager.getJdbcUrl(backend) + ";DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1");
      return (backend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QInstance defineQInstance() throws QException
   {
      QInstance instance = new QInstance();
      instance.withInstanceDefaultAuthentication(new QAuthenticationMetaData().withName("anonymous").withType(QAuthenticationType.FULLY_ANONYMOUS));
      instance.addBackend(backend());
      instance.addBackend(new com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData().withName("memory").withBackendType(MemoryBackendModule.class));
      instance.addTable(new QTableMetaData().withName(STORAGE).withBackendName("memory").withPrimaryKeyField("reference")
         .withField(new QFieldMetaData("reference", QFieldType.STRING))
         .withField(new QFieldMetaData("contents", QFieldType.BLOB)));

      instance.addTable(new QTableMetaData().withName(CUSTOMER).withLabel("Customer").withBackendName(BACKEND).withPrimaryKeyField("id")
         .withRecordLabelFormatAndFields("%s", "name")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("tier", QFieldType.STRING))
         .withField(new QFieldMetaData("lifetimeValue", QFieldType.INTEGER).withBackendName("lifetime_value")));
      instance.addApp(new QAppMetaData().withName("crm").withLabel("CRM").withIcon(new QIcon("groups")).withChild(instance.getTable(CUSTOMER)));

      QProcessMetaData recalc = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(RECALC).withLabel("Recalculate Customer Tiers").withTableName(CUSTOMER)
         .withSourceTable(CUSTOMER).withDestinationTable(CUSTOMER)
         .withExtractStepClass(ExtractViaQueryStep.class).withTransformStepClass(RecalcTransform.class).withLoadStepClass(LoadViaUpdateStep.class)
         .getProcessMetaData();
      recalc.withSupplementalMetaData(new DurableProcessMetaData().withChunkSize(500).withMaxConcurrentJobs(1).withIdempotency(JobIdempotency.NATURAL));
      instance.addProcess(recalc);

      QProcessMetaData nightly = new QProcessMetaData().withName(NIGHTLY).withLabel("Nightly Partner Export")
         .withStepList(List.of(new QBackendStepMetaData().withName("export").withCode(new QCodeReference(FailingExport.class))));
      nightly.withSupplementalMetaData(new DurableProcessMetaData().withEligible(true));
      instance.addProcess(nightly);

      new DurableJobsMetaDataProvider().defineAll(instance, BACKEND, DurableJobsMetaDataProvider.snakeCaseColumnNames());
      instance.withSupplementalMetaData(new DurableJobsMetaData().withStorageTableName(STORAGE).withWorkerThreads(1).withPollIntervalMillis(500).withPodId("demo-pod-1"));

      new JobsQBitProducer().withQBitConfig(new JobsQBitConfig()).produce(instance);
      instance.setHomeAppName("jobs");
      return (instance);
   }



   /*******************************************************************************
    ** Doubles lifetime value and sets the tier; slow, so its job shows progress;
    ** rejects customers with no email.
    *******************************************************************************/
   public static class RecalcTransform extends AbstractTransformStep
   {
      private final ProcessSummaryLine ok = new ProcessSummaryLine(Status.OK).withSingularPastMessage("customer was updated").withPluralPastMessage("customers were updated");



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void runOnePage(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         for(QRecord record : input.getRecords())
         {
            SleepUtils.sleep(12, TimeUnit.MILLISECONDS);
            int     value   = record.getValueInteger("lifetimeValue") == null ? 0 : record.getValueInteger("lifetimeValue");
            QRecord updated = new QRecord().withValue("id", record.getValue("id")).withValue("lifetimeValue", value + 10)
               .withValue("tier", value > 5000 ? "Gold" : value > 1000 ? "Silver" : "Bronze");
            if(record.getValueString("email") == null)
            {
               updated.addError(new com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage("Customer has no email address"));
            }
            else
            {
               ok.incrementCountAndAddPrimaryKey(record.getValue("id"));
            }
            output.addRecord(updated);
         }
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput output, boolean isForResultScreen)
      {
         return (new ArrayList<>(List.of(ok)));
      }
   }



   /*******************************************************************************
    ** An export whose partner endpoint is down.
    *******************************************************************************/
   public static class FailingExport implements BackendStep
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         throw (new QException("Partner SFTP host refused the connection (sftp.partner.example: connection timed out)"));
      }
   }



   /*******************************************************************************
    ** Create the schema, customers, history, and live jobs.
    *******************************************************************************/
   static void seed(QInstance instance) throws Exception
   {
      try(var connection = ConnectionManager.getConnection(backend()))
      {
         for(String statement : DemoSchema.H2_DDL.split(";"))
         {
            if(!statement.isBlank())
            {
               QueryManager.executeUpdate(connection, statement);
            }
         }
      }

      QSession session = new DemoAuthenticationModule().createSession(instance, Map.of());
      QContext.init(instance, session);
      Random        random    = new Random(7);
      List<QRecord> customers = new ArrayList<>();
      for(int i = 1; i <= 12_000; i++)
      {
         customers.add(new QRecord().withValue("name", "Customer " + i).withValue("email", random.nextInt(250) == 0 ? null : "c" + i + "@example.com")
            .withValue("tier", "Bronze").withValue("lifetimeValue", random.nextInt(9000)));
      }
      for(int i = 0; i < customers.size(); i += 1000)
      {
         new InsertAction().execute(new InsertInput(CUSTOMER).withRecords(customers.subList(i, i + 1000)));
      }

      DemoHistory.insert(random);

      ///////////////////////////////////////////////////////////////////
      // live jobs: a long one, one queued behind it (cap 1), a failure //
      ///////////////////////////////////////////////////////////////////
      DurableJobs.launch(new LaunchRequest(RECALC).withFilter(new QQueryFilter()).withLabel("Recalculate tiers: all customers").withExpectedRows(12_000L));
      DurableJobs.launch(new LaunchRequest(RECALC).withFilter(new QQueryFilter()).withLabel("Recalculate tiers: quarterly re-run").withPriority(3));
      DurableJobs.launch(new LaunchRequest(NIGHTLY).withLabel("Nightly partner export"));

      QSession assistant = new DemoAuthenticationModule().createSession(instance, Map.of());
      assistant.setClientChannel("assistant/turn-42");
      QContext.init(instance, assistant);
      DurableJobs.launch(new LaunchRequest(RECALC).withFilter(new QQueryFilter()).withLabel("Assistant: re-tier inactive customers"));
      QContext.init(instance, session);

      SystemScope.call(() ->
      {
         new InsertAction().execute(new InsertInput("jobs_worker").withRecord(new QRecord().withValue("podId", "demo-pod-2").withValue("threads", 2)
            .withValue("draining", true).withValue("startedAt", Instant.now().minus(3, ChronoUnit.HOURS)).withValue("heartbeatAt", Instant.now().minusSeconds(8)).withValue("version", "0.1.0")));
         return (null);
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args) throws Exception
   {
      int port = args.length > 0 ? Integer.parseInt(args[0]) : 8000;
      java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC")); // QQQ servers run in UTC

      ///////////////////////////////////////////////////////////////////////
      // the dispatcher keeps the first module per type: swap in the demo //
      ///////////////////////////////////////////////////////////////////////
      java.lang.reflect.Field modules = com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher.class.getDeclaredField("authenticationTypeToModuleClassNameMap");
      modules.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, String> map = (Map<String, String>) modules.get(null);
      map.put(QAuthenticationType.FULLY_ANONYMOUS.getName(), DemoAuthenticationModule.class.getName());

      JobsDemoServer application = new JobsDemoServer();
      QInstance      instance    = application.defineQInstance();
      new QInstanceValidator().validate(instance);
      seed(instance);
      new QApplicationJavalinServer(application).withPort(port).withServeFrontendNext(true).start();
      System.out.println("Jobs demo on http://localhost:" + port + "/app/jobs");
   }

}
