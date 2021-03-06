// This file is made available under Elastic License 2.0.
// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/test/java/org/apache/doris/common/proc/ProcServiceTest.java

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

import com.starrocks.common.AnalysisException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcServiceTest {
    private class EmptyProcNode implements ProcNodeInterface {
        @Override
        public ProcResult fetchResult() {
            return null;
        }
    }

    // test directory:
    // - starrocks
    // | - be
    //   | - src
    //   | - deps
    // | - fe
    //   | - src
    //   | - conf
    //   | - build.sh
    // | - common
    @Before
    public void beforeTest() {
        ProcService procService = ProcService.getInstance();

        BaseProcDir starrocksDir = new BaseProcDir();
        Assert.assertTrue(procService.register("starrocks", starrocksDir));

        BaseProcDir beDir = new BaseProcDir();
        Assert.assertTrue(starrocksDir.register("be", beDir));
        Assert.assertTrue(beDir.register("src", new BaseProcDir()));
        Assert.assertTrue(beDir.register("deps", new BaseProcDir()));

        BaseProcDir feDir = new BaseProcDir();
        Assert.assertTrue(starrocksDir.register("fe", feDir));
        Assert.assertTrue(feDir.register("src", new BaseProcDir()));
        Assert.assertTrue(feDir.register("conf", new BaseProcDir()));
        Assert.assertTrue(feDir.register("build.sh", new EmptyProcNode()));

        Assert.assertTrue(starrocksDir.register("common", new BaseProcDir()));
    }

    @After
    public void afterTest() {
        ProcService.destroy();
    }

    @Test
    public void testRegisterNormal() {
        ProcService procService = ProcService.getInstance();
        String name = "test";
        BaseProcDir dir = new BaseProcDir();

        Assert.assertTrue(procService.register(name, dir));
    }

    // register second time
    @Test
    public void testRegisterSecond() {
        ProcService procService = ProcService.getInstance();
        String name = "test";
        BaseProcDir dir = new BaseProcDir();

        Assert.assertTrue(procService.register(name, dir));
        Assert.assertFalse(procService.register(name, dir));
    }

    // register invalid
    @Test
    public void testRegisterInvalidInput() {
        ProcService procService = ProcService.getInstance();
        String name = "test";
        BaseProcDir dir = new BaseProcDir();

        Assert.assertFalse(procService.register(null, dir));
        Assert.assertFalse(procService.register("", dir));
        Assert.assertFalse(procService.register(name, null));
    }

    @Test
    public void testOpenNormal() throws AnalysisException {
        ProcService procService = ProcService.getInstance();

        // assert root
        Assert.assertNotNull(procService.open("/"));
        Assert.assertNotNull(procService.open("/starrocks"));
        Assert.assertNotNull(procService.open("/starrocks/be"));
        Assert.assertNotNull(procService.open("/starrocks/be/src"));
        Assert.assertNotNull(procService.open("/starrocks/be/deps"));
        Assert.assertNotNull(procService.open("/starrocks/fe"));
        Assert.assertNotNull(procService.open("/starrocks/fe/src"));
        Assert.assertNotNull(procService.open("/starrocks/fe/conf"));
        Assert.assertNotNull(procService.open("/starrocks/fe/build.sh"));
        Assert.assertNotNull(procService.open("/starrocks/common"));
    }

    @Test
    public void testOpenSapceNormal() throws AnalysisException {
        ProcService procService = ProcService.getInstance();

        // assert space
        Assert.assertNotNull(procService.open(" \r/"));
        Assert.assertNotNull(procService.open(" \r/ "));
        Assert.assertNotNull(procService.open("  /starrocks \r\n"));
        Assert.assertNotNull(procService.open("\n\r\t /starrocks/be \n\r"));

        // assert last '/'
        Assert.assertNotNull(procService.open(" /starrocks/be/"));
        Assert.assertNotNull(procService.open(" /starrocks/fe/  "));

        ProcNodeInterface node = procService.open("/dbs");
        Assert.assertNotNull(node);
        Assert.assertTrue(node instanceof DbsProcDir);
    }

    @Test
    public void testOpenFail() {
        ProcService procService = ProcService.getInstance();

        // assert no path
        int errCount = 0;
        try {
            procService.open("/abc");
        } catch (AnalysisException e) {
            ++errCount;
        }
        try {
            Assert.assertNull(procService.open("/starrocks/b e"));
        } catch (AnalysisException e) {
            ++errCount;
        }
        try {
            Assert.assertNull(procService.open("/starrocks/fe/build.sh/"));
        } catch (AnalysisException e) {
            ++errCount;
        }

        // assert no root
        try {
            Assert.assertNull(procService.open("starrocks"));
        } catch (AnalysisException e) {
            ++errCount;
        }
        try {
            Assert.assertNull(procService.open(" starrocks"));
        } catch (AnalysisException e) {
            ++errCount;
        }

        Assert.assertEquals(5, errCount);
    }

}
