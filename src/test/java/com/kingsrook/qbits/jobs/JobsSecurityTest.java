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


import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_ADMIN;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_USER;
import static com.kingsrook.qbits.jobs.JobsCoreContract.JOBS_USER_ALL_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Core-compatible security key derivation and null/system session behavior.
 *******************************************************************************/
class JobsSecurityTest
{
   /*******************************************************************************
    ** Reapplying keys derives ownership and admin bypass from the actual session.
    *******************************************************************************/
   @Test
   void derivesKeysFromUserAndPermission()
   {
      QSession session = new QSession().withUser(new QUser().withIdReference("user"))
         .withSecurityKeyValue(JOBS_USER, "wrong").withSecurityKeyValue(JOBS_USER_ALL_ACCESS, true);
      JobsSecurity.applyToSession(session);
      assertThat(session.getSecurityKeyValues(JOBS_USER)).containsExactly("user");
      assertThat(session.getSecurityKeyValues(JOBS_USER_ALL_ACCESS)).isEmpty();
      session.withPermission(JOBS_ADMIN);
      JobsSecurity.applyToSession(session);
      assertThat(session.getSecurityKeyValues(JOBS_USER_ALL_ACCESS)).containsExactly(true);
      JobsSecurity.ensureSessionKeys(session);
      assertThat(session.getSecurityKeyValues(JOBS_USER)).containsExactly("user");
   }

   /*******************************************************************************
    ** Null, anonymous and system users are handled without granting extra access.
    *******************************************************************************/
   @Test
   void nullAnonymousAndSystem()
   {
      JobsSecurity.applyToSession(null);
      JobsSecurity.ensureSessionKeys(null);
      JobsSecurity.applyToSession(new QSystemUserSession());
      JobsSecurity.ensureSessionKeys(new QSystemUserSession());
      assertThat(JobsSecurity.userId(null)).isNull();
      QSession anonymous = new QSession();
      JobsSecurity.ensureSessionKeys(anonymous);
      assertThat(JobsSecurity.userId(anonymous)).isNull();
      assertThat(anonymous.getSecurityKeyValues(JOBS_USER)).isEmpty();
      JobsSecurity.ensureSessionKeys(new QSession().withUser(new QUser().withIdReference("")));
   }
}
