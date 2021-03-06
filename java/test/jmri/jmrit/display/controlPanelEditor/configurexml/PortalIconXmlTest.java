package jmri.jmrit.display.controlPanelEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PortalIconXmlTest.java
 *
 * Test for the PortalIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PortalIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PortalIconXml constructor",new PortalIconXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

