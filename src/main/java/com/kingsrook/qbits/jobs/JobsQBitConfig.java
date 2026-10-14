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
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import org.quartz.CronExpression;


/*******************************************************************************
 ** Host configuration for the Jobs app and its optional scheduled retention.
 *******************************************************************************/
public class JobsQBitConfig implements QBitConfig
{
   private String appName = "jobs";
   private String parentAppName;
   private int retentionDays = 30;
   private int inputRetentionDays = 7;
   private String purgeCronExpression = "0 30 3 * * ?";
   private int homeRefreshSeconds = 5;
   private int recordRefreshSeconds = 3;
   private String schedulerName = null;

   /*******************************************************************************
    ** Get the optional host parent app.
    *******************************************************************************/
   public String getParentAppName()
   {
      return (parentAppName);
   }

   /*******************************************************************************
    ** Set the optional host parent app.
    *******************************************************************************/
   public void setParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
   }

   /*******************************************************************************
    ** Place Jobs under an existing host app, such as Operations under Admin.
    *******************************************************************************/
   public JobsQBitConfig withParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
      return (this);
   }

   /*******************************************************************************
    ** Report invalid settings before adding any metadata.
    *******************************************************************************/
   @Override
   public void validate(QInstance instance, List<String> errors)
   {
      assertCondition(appName != null && appName.matches("[A-Za-z][A-Za-z0-9_]*"), "appName must be a valid name", errors);
      assertCondition(parentAppName == null || (!parentAppName.equals(appName) && instance.getApp(parentAppName) != null), "parentAppName must name an existing, different app", errors);
      assertCondition(retentionDays > 0, "retentionDays must be positive", errors);
      assertCondition(inputRetentionDays > 0 && inputRetentionDays <= retentionDays, "inputRetentionDays must be positive and no greater than retentionDays", errors);
      assertCondition(homeRefreshSeconds > 0 && recordRefreshSeconds > 0, "refresh seconds must be positive", errors);
      assertCondition(purgeCronExpression != null && CronExpression.isValidExpression(purgeCronExpression), "purgeCronExpression must be a valid Quartz cron", errors);
      assertCondition(schedulerName == null || !schedulerName.isBlank(), "schedulerName must be null or nonblank", errors);
   }

   /*******************************************************************************
    ** Get appName.
    *******************************************************************************/
   public String getAppName()
   {
      return (appName);
   }

   /*******************************************************************************
    ** Set appName.
    *******************************************************************************/
   public void setAppName(String appName)
   {
      this.appName = appName;
   }

   /*******************************************************************************
    ** Configure appName.
    *******************************************************************************/
   public JobsQBitConfig withAppName(String appName)
   {
      this.appName = appName;
      return (this);
   }

   /*******************************************************************************
    ** Get retentionDays.
    *******************************************************************************/
   public int getRetentionDays()
   {
      return (retentionDays);
   }

   /*******************************************************************************
    ** Set retentionDays.
    *******************************************************************************/
   public void setRetentionDays(int retentionDays)
   {
      this.retentionDays = retentionDays;
   }

   /*******************************************************************************
    ** Configure retentionDays.
    *******************************************************************************/
   public JobsQBitConfig withRetentionDays(int retentionDays)
   {
      this.retentionDays = retentionDays;
      return (this);
   }

   /*******************************************************************************
    ** Get inputRetentionDays.
    *******************************************************************************/
   public int getInputRetentionDays()
   {
      return (inputRetentionDays);
   }

   /*******************************************************************************
    ** Set inputRetentionDays.
    *******************************************************************************/
   public void setInputRetentionDays(int inputRetentionDays)
   {
      this.inputRetentionDays = inputRetentionDays;
   }

   /*******************************************************************************
    ** Configure inputRetentionDays.
    *******************************************************************************/
   public JobsQBitConfig withInputRetentionDays(int inputRetentionDays)
   {
      this.inputRetentionDays = inputRetentionDays;
      return (this);
   }

   /*******************************************************************************
    ** Get purgeCronExpression.
    *******************************************************************************/
   public String getPurgeCronExpression()
   {
      return (purgeCronExpression);
   }

   /*******************************************************************************
    ** Set purgeCronExpression.
    *******************************************************************************/
   public void setPurgeCronExpression(String purgeCronExpression)
   {
      this.purgeCronExpression = purgeCronExpression;
   }

   /*******************************************************************************
    ** Configure purgeCronExpression.
    *******************************************************************************/
   public JobsQBitConfig withPurgeCronExpression(String purgeCronExpression)
   {
      this.purgeCronExpression = purgeCronExpression;
      return (this);
   }

   /*******************************************************************************
    ** Get homeRefreshSeconds.
    *******************************************************************************/
   public int getHomeRefreshSeconds()
   {
      return (homeRefreshSeconds);
   }

   /*******************************************************************************
    ** Set homeRefreshSeconds.
    *******************************************************************************/
   public void setHomeRefreshSeconds(int homeRefreshSeconds)
   {
      this.homeRefreshSeconds = homeRefreshSeconds;
   }

   /*******************************************************************************
    ** Configure homeRefreshSeconds.
    *******************************************************************************/
   public JobsQBitConfig withHomeRefreshSeconds(int homeRefreshSeconds)
   {
      this.homeRefreshSeconds = homeRefreshSeconds;
      return (this);
   }

   /*******************************************************************************
    ** Get recordRefreshSeconds.
    *******************************************************************************/
   public int getRecordRefreshSeconds()
   {
      return (recordRefreshSeconds);
   }

   /*******************************************************************************
    ** Set recordRefreshSeconds.
    *******************************************************************************/
   public void setRecordRefreshSeconds(int recordRefreshSeconds)
   {
      this.recordRefreshSeconds = recordRefreshSeconds;
   }

   /*******************************************************************************
    ** Configure recordRefreshSeconds.
    *******************************************************************************/
   public JobsQBitConfig withRecordRefreshSeconds(int recordRefreshSeconds)
   {
      this.recordRefreshSeconds = recordRefreshSeconds;
      return (this);
   }

   /*******************************************************************************
    ** Get schedulerName.
    *******************************************************************************/
   public String getSchedulerName()
   {
      return (schedulerName);
   }

   /*******************************************************************************
    ** Set schedulerName.
    *******************************************************************************/
   public void setSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
   }

   /*******************************************************************************
    ** Configure schedulerName.
    *******************************************************************************/
   public JobsQBitConfig withSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
      return (this);
   }
}
