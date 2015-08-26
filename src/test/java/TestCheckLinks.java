import org.testng.Assert;
import org.testng.ISuite;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.SuiteHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCheckLinks {
    List<ISuite> suiteProgramm = new ArrayList<ISuite>();
    SuiteHTMLReporter report =new SuiteHTMLReporter();
    ArrayList<XmlClass> classes = new ArrayList<XmlClass>();
    TestListenerAdapter tla = new TestListenerAdapter();

    @Test(description = "Test to check src and href in project")
    public void testCheck() {
        classes.add(new XmlClass("CheckAllLinkAndText"));
        TestNG tng = new TestNG(true);
        tng.addListener(report);
        tng.addListener(tla);
        XmlSuite suite = new XmlSuite();
        suite.setName("Check-Links");
        XmlTest testXml = new XmlTest(suite);
        testXml.setXmlClasses(classes);
        testXml.setName("Check-Links");
        tng.setXmlSuites(Arrays.asList(suite));
        tng.runSuitesLocally();
        report.generateReport(Arrays.asList(suite), suiteProgramm, tng.getOutputDirectory());
        if(!tla.getFailedTests().isEmpty())
            Assert.fail("Fail test\n"+CheckAllLinkAndText.urls);
    }


}
