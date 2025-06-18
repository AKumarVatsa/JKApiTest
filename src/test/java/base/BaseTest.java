package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import io.restassured.RestAssured;
import org.testng.annotations.*;
import utils.ExtentReportManager;

public class BaseTest {	

	    protected static ExtentReports report;
	    protected ExtentTest test;

	    @BeforeSuite
	    public void init() {
	        RestAssured.baseURI = config.ConfigManager.get("base.url");
	        report = ExtentReportManager.initReport();
	    }

	    @AfterSuite
	    public void tearDown() {
	        report.flush();
	    }
	}

