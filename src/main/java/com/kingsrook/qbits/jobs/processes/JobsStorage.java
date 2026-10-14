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


package com.kingsrook.qbits.jobs.processes;


import java.util.concurrent.atomic.AtomicBoolean;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import static com.kingsrook.qbits.jobs.JobsCoreContract.DURABLE_JOBS_METADATA;


/*******************************************************************************
 ** STAND-IN(core=QRun-IO/qqq#1173, removeBy=RC.7): reflectively call StorageAction
 ** deleteObject until the released core contains that method. Never touch disk.
 ** STAND-IN(core=QRun-IO/qqq#1178, removeBy=RC.7): read durable jobs supplemental
 ** metadata reflectively until its type is available in the released core.
 *******************************************************************************/
public class JobsStorage
{
   private static final QLogger LOG = QLogger.getLogger(JobsStorage.class);
   private static final AtomicBoolean WARNED = new AtomicBoolean();

   /*******************************************************************************
    ** Look up the host's durable jobs storage table without linking core.jobs.
    *******************************************************************************/
   public String chunkStorageTable() throws QException
   {
      Object metadata = QContext.getQInstance().getSupplementalMetaData(DURABLE_JOBS_METADATA);
      if(metadata == null)
      {
         return (null);
      }
      try
      {
         return ((String) metadata.getClass().getMethod("getStorageTableName").invoke(metadata));
      }
      catch(ReflectiveOperationException e)
      {
         throw (new QException("Cannot read durable jobs storage metadata", e));
      }
   }

   /*******************************************************************************
    ** Return true only after a successful deletion. Missing APIs leave references
    ** intact during input cleanup. Invocation failures abort retention for retry.
    *******************************************************************************/
   public boolean delete(String table, String reference) throws QException
   {
      if(reference == null || reference.isBlank() || table == null || table.isBlank())
      {
         return (false);
      }
      try
      {
         StorageAction.class.getMethod("deleteObject", StorageInput.class).invoke(new StorageAction(), new StorageInput(table).withReference(reference));
         return (true);
      }
      catch(NoSuchMethodException e)
      {
         if(WARNED.compareAndSet(false, true))
         {
            LOG.warn("StorageAction.deleteObject is unavailable in this core release; skipping jobs object deletion until RC.6");
         }
         return (false);
      }
      catch(ReflectiveOperationException e)
      {
         throw (new QException("Could not delete durable jobs storage object", e));
      }
   }
}
