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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.metadata.MetaDataAction;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.DenyBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** The Jobs app and its home widgets are hidden from users without
 ** "jobs.hasAccess"; Workers additionally needs jobs_admin.
 *******************************************************************************/
class JobsAppPermissionTest extends BaseJobsTest
{
   private static final List<String> NON_ADMIN_WIDGETS = JobsQBitProducer.HOME_WIDGETS.stream().filter(w -> !"jobsHomeWorkers".equals(w)).toList();



   /*******************************************************************************
    ** The permission name is public and follows the configured app name.
    *******************************************************************************/
   @Test
   void permissionNameAndRules()
   {
      assertThat(config.getHasAccessPermissionName()).isEqualTo("jobs.hasAccess");
      assertThat(new JobsQBitConfig().withAppName("bg").getHasAccessPermissionName()).isEqualTo("bg.hasAccess");
      assertThat(instance.getApp("jobs").getPermissionRules().getLevel()).isEqualTo(PermissionLevel.HAS_ACCESS_PERMISSION);
      assertThat(instance.getApp("jobs").getPermissionRules().getPermissionBaseName()).isEqualTo("jobs");
      assertThat(instance.getApp("jobs").getPermissionRules().getDenyBehavior()).isEqualTo(DenyBehavior.HIDDEN);
      assertThat(instance.getWidget("jobsJobProgress").getPermissionRules().getLevel()).isNotEqualTo(PermissionLevel.HAS_ACCESS_PERMISSION);
      for(String name : NON_ADMIN_WIDGETS)
      {
         assertThat(instance.getWidget(name).getPermissionRules().getPermissionBaseName()).isEqualTo("jobs");
         assertThat(instance.getWidget(name).getPermissionRules().getDenyBehavior()).isEqualTo(DenyBehavior.HIDDEN);
      }
   }



   /*******************************************************************************
    ** No permissions: no app, no home widgets, every render denied.
    *******************************************************************************/
   @Test
   void noPermissionsSeesNothing() throws Exception
   {
      instance.setEnforcePermissionsInCoreActions(true);
      login("nobody", false);
      MetaDataOutput output = new MetaDataAction().execute(new MetaDataInput());
      assertThat(output.getApps()).doesNotContainKey("jobs");
      assertThat(output.getWidgets()).doesNotContainKeys(JobsQBitProducer.HOME_WIDGETS.toArray(String[]::new));
      for(String name : JobsQBitProducer.HOME_WIDGETS)
      {
         assertThatThrownBy(() -> render(name, null)).isInstanceOf(QPermissionDeniedException.class);
      }
   }



   /*******************************************************************************
    ** jobs.hasAccess: app and the five widgets, Workers only with jobs_admin.
    *******************************************************************************/
   @Test
   void hasAccessSeesAppAndWidgets() throws Exception
   {
      instance.setEnforcePermissionsInCoreActions(true);
      login("user", false).withPermission(config.getHasAccessPermissionName());
      MetaDataOutput output = new MetaDataAction().execute(new MetaDataInput());
      assertThat(output.getApps()).containsKey("jobs");
      assertThat(output.getWidgets()).containsKeys(NON_ADMIN_WIDGETS.toArray(String[]::new));
      assertThat(output.getWidgets()).doesNotContainKey("jobsHomeWorkers");
      for(String name : NON_ADMIN_WIDGETS)
      {
         assertThat(render(name, null)).isNotNull();
      }
      assertThatThrownBy(() -> render("jobsHomeWorkers", null)).isInstanceOf(QPermissionDeniedException.class);

      login("admin", true).withPermission(config.getHasAccessPermissionName());
      assertThat(new MetaDataAction().execute(new MetaDataInput()).getWidgets()).containsKey("jobsHomeWorkers");
      assertThat(render("jobsHomeWorkers", null)).isNotNull();
   }
}
