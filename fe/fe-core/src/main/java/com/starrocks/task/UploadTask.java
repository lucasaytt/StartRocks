// This file is made available under Elastic License 2.0.
// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/main/java/org/apache/doris/task/UploadTask.java

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

package com.starrocks.task;

import com.starrocks.catalog.FsBroker;
import com.starrocks.thrift.TNetworkAddress;
import com.starrocks.thrift.TResourceInfo;
import com.starrocks.thrift.TTaskType;
import com.starrocks.thrift.TUploadReq;

import java.util.Map;

public class UploadTask extends AgentTask {

    private long jobId;

    private Map<String, String> srcToDestPath;
    private FsBroker broker;
    private Map<String, String> brokerProperties;

    public UploadTask(TResourceInfo resourceInfo, long backendId, long signature, long jobId, Long dbId,
                      Map<String, String> srcToDestPath, FsBroker broker, Map<String, String> brokerProperties) {
        super(resourceInfo, backendId, TTaskType.UPLOAD, dbId, -1, -1, -1, -1, signature);
        this.jobId = jobId;
        this.srcToDestPath = srcToDestPath;
        this.broker = broker;
        this.brokerProperties = brokerProperties;
    }

    public long getJobId() {
        return jobId;
    }

    public Map<String, String> getSrcToDestPath() {
        return srcToDestPath;
    }

    public FsBroker getBrokerAddress() {
        return broker;
    }

    public Map<String, String> getBrokerProperties() {
        return brokerProperties;
    }

    public TUploadReq toThrift() {
        TNetworkAddress address = new TNetworkAddress(broker.ip, broker.port);
        TUploadReq request = new TUploadReq(jobId, srcToDestPath, address);
        request.setBroker_prop(brokerProperties);
        return request;
    }
}
