/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.permission.Policy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class ClassloadingHelperTest {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassloadingHelperTest object.
     */
    public ClassloadingHelperTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * DOCUMENT ME!
     */
    @Before
    public void setUp() {
    }

    /**
     * DOCUMENT ME!
     */
    @After
    public void tearDown() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetClassNames_3args() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testCapitalize() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetClassNames_MetaClass_ClassloadingHelperCLASS_TYPE_AltDomains() {
        System.out.println("TEST " + getCurrentMethodName());
        
        MetaClass mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "test1");
        List<String> exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.test1.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.test1.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.test1.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.test1.MyClassRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.altDom1.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.altDom1.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom1.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom1.MyClassRenderer"));
        List<String> res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "test2");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.test2.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.test2.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.test2.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.test2.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "test3");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.test3.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.test3.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.test3.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.test3.MyClassRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.altDom1.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.altDom1.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom1.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom1.MyClassRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.altDom2.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.altDom2.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom2.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom2.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "test4");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.test4.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.test4.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.test4.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.test4.MyClassRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.altDom1.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.altDom1.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom1.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom1.MyClassRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.altDom2.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.altDom2.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom2.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom2.MyClassRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.altDom3.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.altDom3.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom3.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.altDom3.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "test5");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.test5.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.test5.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.test5.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.test5.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "test6");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.test6.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.test6.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.test6.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.test6.MyClassRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.alt.domain.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.alt.domain.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.alt.domain.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.alt.domain.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "test7");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.test7.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.test7.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.test7.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.test7.MyClassRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.null_.My_classRenderer",
                "de.cismet.cids.utils.clht.objectrenderer.null_.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.null_.My_classRenderer",
                "de.cismet.cids.custom.objectrenderer.null_.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
    }
    
    @Test
    public void testGetClassNames_MetaClass_ClassloadingHelperCLASS_TYPE_OrderDefault() {
        System.out.println("TEST " + getCurrentMethodName());
        
        MetaClass mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderdefault_default");
        List<String> exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_default.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_default.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.testorderdefault_default.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.testorderdefault_default.MyClassRenderer"));
        List<String> res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderdefault_foo");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_foo.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_foo.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.testorderdefault_foo.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.testorderdefault_foo.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderdefault_empty");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_empty.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_empty.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.testorderdefault_empty.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.testorderdefault_empty.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
        
        mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderdefault_nokey");
        exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_nokey.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.testorderdefault_nokey.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.testorderdefault_nokey.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.testorderdefault_nokey.MyClassRenderer"));
        res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
    }
    
    @Test
    public void testGetClassNames_MetaClass_ClassloadingHelperCLASS_TYPE_OrderClasstype() {
        System.out.println("TEST " + getCurrentMethodName());
        
        MetaClass mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderclasstype");
        List<String> exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.testorderclasstype.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.testorderclasstype.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.testorderclasstype.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.testorderclasstype.MyClassRenderer"));
        List<String> res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
    }
    
    @Test
    public void testGetClassNames_MetaClass_ClassloadingHelperCLASS_TYPE_OrderDomain() {
        System.out.println("TEST " + getCurrentMethodName());
        
        MetaClass mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderdomain");
        List<String> exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.testorderdomain.objectrenderer.My_classRenderer", 
                "de.cismet.cids.utils.clht.testorderdomain.objectrenderer.MyClassRenderer",
                "de.cismet.cids.custom.testorderdomain.objectrenderer.My_classRenderer", 
                "de.cismet.cids.custom.testorderdomain.objectrenderer.MyClassRenderer"));
        List<String> res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
    }
    
    @Test
    public void testGetClassNames_MetaClass_ClassloadingHelperCLASS_TYPE_OrderBothClasstype() {
        System.out.println("TEST " + getCurrentMethodName());
        
        MetaClass mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderbothclasstype");
        List<String> exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.testorderbothclasstype.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.testorderbothclasstype.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.testorderbothclasstype.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.testorderbothclasstype.MyClassRenderer",
                "de.cismet.cids.utils.clht.testorderbothclasstype.objectrenderer.My_classRenderer", 
                "de.cismet.cids.utils.clht.testorderbothclasstype.objectrenderer.MyClassRenderer",
                "de.cismet.cids.custom.testorderbothclasstype.objectrenderer.My_classRenderer", 
                "de.cismet.cids.custom.testorderbothclasstype.objectrenderer.MyClassRenderer"));
        List<String> res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
    }
    
    @Test
    public void testGetClassNames_MetaClass_ClassloadingHelperCLASS_TYPE_OrderBothDomain() {
        System.out.println("TEST " + getCurrentMethodName());
        
        MetaClass mc = new MetaClass(new Sirius.server.localserver._class.Class(
                    -1,
                    "MY_CLASS",
                    null,
                    null,
                    null,
                    "MY_CLASS",
                    null,
                    null,
                    (Policy)null,
                    null,
                    false),
                "testorderbothdomain");
        List<String> exp = new ArrayList<String>(Arrays.asList(
                "de.cismet.cids.utils.clht.objectrenderer.testorderbothdomain.My_classRenderer", 
                "de.cismet.cids.utils.clht.objectrenderer.testorderbothdomain.MyClassRenderer",
                "de.cismet.cids.custom.objectrenderer.testorderbothdomain.My_classRenderer", 
                "de.cismet.cids.custom.objectrenderer.testorderbothdomain.MyClassRenderer",
                "de.cismet.cids.utils.clht.testorderbothdomain.objectrenderer.My_classRenderer", 
                "de.cismet.cids.utils.clht.testorderbothdomain.objectrenderer.MyClassRenderer",
                "de.cismet.cids.custom.testorderbothdomain.objectrenderer.My_classRenderer", 
                "de.cismet.cids.custom.testorderbothdomain.objectrenderer.MyClassRenderer"));
        List<String> res = ClassloadingHelper.getClassNames(mc, CLASS_TYPE.RENDERER);
        assertEquals(exp, res);
    }
    
    @ServiceProvider(service=ClassLoadingPackagePrefixProvider.class)
    public static final class ClassloadingHelperTestPrefixProvider implements ClassLoadingPackagePrefixProvider {

        @Override
        public String getClassLoadingPackagePrefix()
        {
            return "de.cismet.cids.utils.clht";
        }
        
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetClassNameByConfiguration_MetaClass_ClassloadingHelperCLASS_TYPE() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetClassNameByConfiguration_MemberAttributeInfo_ClassloadingHelperCLASS_TYPE() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetClassNameByConfiguration_3args() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testCamelize() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testLoadClassFromCandidates() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetDynamicClass_3args() {
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetDynamicClass_MetaClass_ClassloadingHelperCLASS_TYPE() {
    }
}
