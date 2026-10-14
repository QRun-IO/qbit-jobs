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


import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.jobs.DurableJobsMetaData;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;


/*******************************************************************************
 ** Object cleanup through core storage actions and durable jobs configuration.
 *******************************************************************************/
public class JobsStorage
{
   /*******************************************************************************
    ** Look up the host's durable jobs storage table.
    *******************************************************************************/
   public String chunkStorageTable() throws QException
   {
      DurableJobsMetaData metadata = DurableJobsMetaData.of(QContext.getQInstance());
      return (metadata == null ? null : metadata.getStorageTableName());
   }

   /*******************************************************************************
    ** Return true only after successful deletion; failures abort retention for retry.
    *******************************************************************************/
   public boolean delete(String table, String reference) throws QException
   {
      if(reference == null || reference.isBlank() || table == null || table.isBlank())
      {
         return (false);
      }
      new StorageAction().deleteObject(new StorageInput(table).withReference(reference));
      return (true);
   }
}
