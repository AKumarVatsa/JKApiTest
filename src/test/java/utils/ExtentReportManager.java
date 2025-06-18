package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportManager {

	    public static ExtentReports initReport() {
	        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
	        ExtentReports extent = new ExtentReports();
	        extent.attachReporter(spark);
	        return extent;
	    }
	}

