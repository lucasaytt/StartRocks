// This file is made available under Elastic License 2.0.
// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/test/java/org/apache/doris/utframe/MockedBackend.java

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

package com.starrocks.utframe;

import com.baidu.bjf.remoting.protobuf.utils.JDKCompilerHelper;
import com.baidu.bjf.remoting.protobuf.utils.compiler.JdkCompiler;
import com.baidu.jprotobuf.pbrpc.transport.RpcServer;
import com.starrocks.common.ThriftServer;
import com.starrocks.common.util.JdkUtils;
import com.starrocks.thrift.BackendService;
import com.starrocks.thrift.HeartbeatService;
import com.starrocks.thrift.TNetworkAddress;
import com.starrocks.utframe.MockedBackendFactory.BeThriftService;
import org.apache.thrift.TProcessor;

import java.io.IOException;

/*
 * Mocked Backend
 * A mocked Backend has 3 rpc services.
 *      HeartbeatService.Iface to handle heart beat from Frontend.
 *      BeThriftService to handle agent tasks and other requests from Frontend.
 *      BRpcService to handle the query request from Frontend.
 *
 * Users can create a BE by customizing three rpc services.
 *
 * Better to create a mocked Backend from MockedBackendFactory.
 * In MockedBackendFactory, there default rpc service for above 3 rpc services.
 */
public class MockedBackend {

    private ThriftServer heartbeatServer;
    private ThriftServer beThriftServer;
    private RpcServer rpcServer;

    private String host;
    private int heartbeatPort;
    private int thriftPort;
    private int brpcPort;
    private int httpPort;
    // the fe address: fe host and fe rpc port.
    // This must be set explicitly after creating mocked Backend
    private TNetworkAddress feAddress;

    static {
        int javaRuntimeVersion = JdkUtils.getJavaVersionAsInteger(System.getProperty("java.version"));
        JDKCompilerHelper
                .setCompiler(new JdkCompiler(JdkCompiler.class.getClassLoader(), String.valueOf(javaRuntimeVersion)));
    }

    public MockedBackend(String host, int heartbeatPort, int thriftPort, int brpcPort, int httpPort,
                         HeartbeatService.Iface hbService,
                         BeThriftService backendService,
                         Object pBackendService) throws IOException {

        this.host = host;
        this.heartbeatPort = heartbeatPort;
        this.thriftPort = thriftPort;
        this.brpcPort = brpcPort;
        this.httpPort = httpPort;

        createHeartbeatService(heartbeatPort, hbService);
        createBeThriftService(thriftPort, backendService);
        createBrpcService(brpcPort, pBackendService);

        backendService.setBackend(this);
        backendService.init();
    }

    public void setFeAddress(TNetworkAddress feAddress) {
        this.feAddress = feAddress;
    }

    public TNetworkAddress getFeAddress() {
        return feAddress;
    }

    public String getHost() {
        return host;
    }

    public int getHeartbeatPort() {
        return heartbeatPort;
    }

    public int getBeThriftPort() {
        return thriftPort;
    }

    public int getBrpcPort() {
        return brpcPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void start() throws IOException {
        heartbeatServer.start();
        System.out.println("Be heartbeat service is started with port: " + heartbeatPort);
        beThriftServer.start();
        System.out.println("Be thrift service is started with port: " + thriftPort);
        rpcServer.start(brpcPort);
        System.out.println("Be brpc service is started with port: " + brpcPort);
    }

    private void createHeartbeatService(int heartbeatPort, HeartbeatService.Iface serviceImpl) throws IOException {
        TProcessor tprocessor = new HeartbeatService.Processor<>(serviceImpl);
        heartbeatServer = new ThriftServer(heartbeatPort, tprocessor);
    }

    private void createBeThriftService(int beThriftPort, BackendService.Iface serviceImpl) throws IOException {
        TProcessor tprocessor = new BackendService.Processor<>(serviceImpl);
        beThriftServer = new ThriftServer(beThriftPort, tprocessor);
    }

    private void createBrpcService(int brpcPort, Object pBackendServiceImpl) {
        rpcServer = new RpcServer();
        rpcServer.registerService(pBackendServiceImpl);
    }
}
