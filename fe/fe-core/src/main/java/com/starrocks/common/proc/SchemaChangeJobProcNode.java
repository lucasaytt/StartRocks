// This file is made available under Elastic License 2.0.
// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/main/java/org/apache/doris/common/proc/SchemaChangeJobProcNode.java

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.common.proc;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.starrocks.alter.SchemaChangeJobV2;
import com.starrocks.common.AnalysisException;

import java.util.List;

// Show unfinished schema change tasks of schema change job v2
public class SchemaChangeJobProcNode implements ProcNodeInterface {
    public static final ImmutableList<String> TITLE_NAMES = new ImmutableList.Builder<String>()
            .add("BackendId").add("BaseTabletId").add("RollupTabletId")
            .build();

    private SchemaChangeJobV2 job;

    public SchemaChangeJobProcNode(SchemaChangeJobV2 job) {
        this.job = job;
    }

    @Override
    public ProcResult fetchResult() throws AnalysisException {
        Preconditions.checkNotNull(job);

        BaseProcResult result = new BaseProcResult();
        result.setNames(TITLE_NAMES);

        List<List<String>> unfinishedRollupTasks = job.getUnfinishedTasks(2000);
        result.setRows(unfinishedRollupTasks);
        return result;
    }
}
