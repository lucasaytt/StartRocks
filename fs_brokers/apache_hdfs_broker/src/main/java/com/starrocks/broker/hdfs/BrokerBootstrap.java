// This file is made available under Elastic License 2.0.
// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master
//                     /fs_brokers/apache_hdfs_broker/src/main/java
//                     /org/apache/doris/broker/hdfs/BrokerBootstrap.java 

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

package com.starrocks.broker.hdfs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.thrift.TProcessor;

import com.starrocks.thrift.TFileBrokerService;
import com.starrocks.common.ThriftServer;

public class BrokerBootstrap {

    public static void main(String[] args) {
        try {
            final String brokerHome = System.getenv("BROKER_HOME");
            if (StringUtils.isEmpty(brokerHome)) {
                System.out.println("BROKER_HOME is not set, exit");
                return;
            }
            final String pidDir = System.getenv("PID_DIR");
            if (StringUtils.isEmpty(pidDir)) {
                System.out.println("PID_DIR is not set, exit");
                return;
            }

            if (!createAndLockPidFile(pidDir + "/apache_hdfs_broker.pid")) {
                throw new IOException("pid file is already locked.");
            }

            System.setProperty("BROKER_LOG_DIR", System.getenv("BROKER_LOG_DIR"));
            PropertyConfigurator.configure(brokerHome + "/conf/log4j.properties");
            Logger logger = Logger.getLogger(BrokerBootstrap.class);
            logger.info("starting apache hdfs broker....");
            new BrokerConfig().init(brokerHome + "/conf/apache_hdfs_broker.conf");

            TProcessor tprocessor = new TFileBrokerService.Processor<TFileBrokerService.Iface>(
                    new HDFSBrokerServiceImpl());
            ThriftServer server = new ThriftServer(BrokerConfig.broker_ipc_port, tprocessor);
            server.start();
            logger.info("starting apache hdfs broker....succeed");
            while (true) {
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static boolean createAndLockPidFile(String pidFilePath) throws IOException {
        File pid = new File(pidFilePath);
        RandomAccessFile file = new RandomAccessFile(pid, "rws");
        try {
            FileLock lock = file.getChannel().tryLock();
            if (lock == null) {
                return false;
            }

            pid.deleteOnExit();

            String name = ManagementFactory.getRuntimeMXBean().getName();
            file.setLength(0);
            file.write(name.split("@")[0].getBytes(com.google.common.base.Charsets.UTF_8));

            return true;
        } catch (OverlappingFileLockException e) {
            file.close();
            return false;
        } catch (IOException e) {
            file.close();
            throw e;
        }
    }
}
