/*
 * Copyright (c) 2019. Semenoff Slava
 */

package ex.capybara;

import io.qameta.allure.Attachment;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class MainTest {
    /*
     * Copyright (c) 2019. Semenoff Slava
     */
    private static final ThreadLocal<RemoteWebDriver> drivers = new ThreadLocal<>();
    protected boolean isLocalRunning = true;
    protected String SITE_URL;
    public MainTest.Platform platform;
    protected MainTest.BrowserType browser;
    protected static RemoteWebDriver driver;
    private static String driverPath = "./install/selenium//";
    protected static int DefaultDelay = 10;

    public MainTest(Platform platform){
        this.platform = MainTest.Platform.desktop;
        this.browser = MainTest.BrowserType.chrome;
        this.setPlatform(platform);
    }
    protected enum Stages {test, stage, prod}

    protected abstract String getStages(Stages stages);

    protected enum BrowserType {chrome, firefox, iexplorer;}
    protected String browser_version;
    protected enum Platform {desktop, mobile};

    protected String getSeleniumURL(){
        return "http://localhost:4444/wd/hub"; // local selenium
    }

    public RemoteWebDriver getDriver() {
        RemoteWebDriver driver = drivers.get();
        if (driver == null) {
            throw new IllegalStateException("Driver should have not been null.");
        }
        return driver;
    }
    public void setDriver(RemoteWebDriver driver){
        this.driver = driver;
    }


    public void DisableAnimations() {
        // This script should turn off 99% of the animations and transitions in the site. Hopefully this helps settle down some animations
        String script = "require(['jquery'], function($) {" +
                "$(function(){" +
                "$.fx.off = true;" +
                "var styleEl = document.createElement('style');" +
                "styleEl.textContent = '*{ transition: none !important; transition-property: none !important; }';" +
                "document.head.appendChild(styleEl);" +
                "});" +
                "});";
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        if (jse instanceof WebDriver){
            jse.executeScript(script);
        }
    }

    protected void setPlatform(Platform platform){
        this.platform = platform;
    }
    public Platform getPlatform(){
        return this.platform;
    }
    private boolean isMobile(){
        if (getPlatform()==Platform.mobile){
            return true;
        }
        return false;
    }

    public String SiteUrl(@Optional("stage") Stages stages){
        String site = getStages(stages);
        System.err.println("| SITE_URL = " + site + " | ");
        return site;
    }

//    @BeforeClass

    private void setup(BrowserType browser,String  browser_version,Stages stages){

        this.browser = browser;
        this.browser_version = browser_version;
//        this.URL = url;
        this.SITE_URL = SiteUrl(stages);
    }

    protected void Before(){

    }
    @Parameters({"browser","browser_version","stages"})
    @BeforeMethod(description = "Initialize driver")
    public void DriverInitialize(ITestResult testResult, @Optional("chrome")BrowserType browser,@Optional("72.0")String  browser_version,@Optional("stage")Stages stages) {
        setup(browser,browser_version,stages);
        System.err.print("browser = " + this.browser + " | ");
        System.err.print("platform = " + this.getPlatform() + " | ");

        try {
            this.DriverCreate(this.isLocalRunning, this.isMobile());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        this.getDriver().manage().deleteAllCookies();
        this.getDriver().manage().window().setPosition(new Point(0, 0));
        this.getDriver().manage().window().setSize(new Dimension(1366, 768));
        this.getDriver().manage().timeouts().implicitlyWait((long)DefaultDelay, TimeUnit.SECONDS);
        this.getDriver().get(this.SITE_URL);
        this.Before();
    }

    public boolean getElement(By locator){
        try{
            getDriver().findElement(locator);
            return true;
        } catch (NoSuchElementException e){
            return false;
        }
    }


    private void CreateRemoteDriver(DesiredCapabilities capabilities) throws MalformedURLException {

        System.err.println("create "+ this.browser +" remote driver | version = "+ browser_version);
//        capabilities.setBrowserName("chrome");
        capabilities.setBrowserName(this.browser.toString());
        capabilities.setVersion(this.browser_version);

        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", false);

        RemoteWebDriver driver = new RemoteWebDriver(
                URI.create(this.getSeleniumURL()).toURL(),
                capabilities
        );
        drivers.set(driver);

    }

    private void CreateLocalDriver_Chrome(boolean isMobile){

        if(isMobile){
            Map<String, String> mobileEmulation = new HashMap();
            mobileEmulation.put("deviceName", "Nexus 5");
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
            RemoteWebDriver driver = new ChromeDriver(chromeOptions);
            drivers.set(driver);
        }else {
            RemoteWebDriver driver = new ChromeDriver();
            drivers.set(driver);
        }
    }
    private void CreateLocalDriver_Firefox(boolean isMobile){

        if(isMobile){
//            String user_agent = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16";
//
//            Map<String, String> mobileEmulation = new HashMap();
//            mobileEmulation.put("deviceName", "Pixel 2");
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setCapability("deviceName", "Pixel 2");

//
//            FirefoxProfile firefoxProfile
//            firefoxProfile.setPreference("general.useragent.override", user_agent);
//            firefoxOptions.setProfile(firefoxProfile);
//

////            profile = webdriver.FirefoxProfile()
////            profile.set_preference("general.useragent.override", user_agent)
////            driver = webdriver.Firefox(profile)
////            driver.set_window_size(360,640)

//            binary = FirefoxBinary('geckodriver.exe')
//            capabilities = {
//                    'browserName': 'firefox',
//                    'firefoxOptions': {
//                'mobileEmulation': {
//                    'deviceName': 'iPhone X'
//                }
//            }
//}
//
//            browser = webdriver.Firefox(firefox_binary=binary, desired_capabilities=capabilities)

            System.out.println("mobile firefox started");
//            firefoxOptions.setCapability("mobileEmulation","iPhone X");
            RemoteWebDriver driver = new FirefoxDriver(firefoxOptions);
            drivers.set(driver);
        }else {
            RemoteWebDriver driver = new FirefoxDriver();
            drivers.set(driver);
        }

    }
    private void CreateLocalDriver_IExplorer(boolean isMobile){
//        CreateLocalDriver_Chrome(isMobile); // TODO temp "chrome_driver" here
        RemoteWebDriver driver = new InternetExplorerDriver();
        drivers.set(driver);
    }

    private void DriverCreate(boolean isLocal, boolean isMobile) throws MalformedURLException {

        if (isLocal) {
            switch (this.browser) {
                case chrome:
                    System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver.exe");
                    CreateLocalDriver_Chrome(isMobile);
                    break;
                case firefox:
                    System.setProperty("webdriver.gecko.driver", driverPath + "geckodriver.exe");
                    CreateLocalDriver_Firefox(isMobile);
//                    driver = new FirefoxDriver();
                    break;
                case iexplorer:
                    System.out.println("Delicious iexplorer");
                    System.setProperty("webdriver.ie.driver", driverPath + "IEDriverServer.exe");
                    CreateLocalDriver_IExplorer(isMobile);
//
                    break;
            }
        }else {
            switch (this.browser) {
                case chrome:
                    if(isMobile){
                        Map<String, String> mobileEmulation = new HashMap<>();

                        mobileEmulation.put("deviceName", "Nexus 5");
                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
                        DesiredCapabilities capabilities = new DesiredCapabilities(chromeOptions);
                        CreateRemoteDriver(capabilities);
                        break;
                    }else {
                        DesiredCapabilities capabilities = new DesiredCapabilities();
                        CreateRemoteDriver(capabilities);
                    }


                    break;
                case firefox:
                    if(isMobile){
                        DesiredCapabilities capabilities = new DesiredCapabilities();
                        CreateRemoteDriver(capabilities);
                    }else {
                        DesiredCapabilities capabilities = new DesiredCapabilities();
                        CreateRemoteDriver(capabilities);
                    }

                    break;
                case iexplorer:
                    if(isMobile){
                        DesiredCapabilities capabilities = new DesiredCapabilities();
                        CreateRemoteDriver(capabilities);
                    }else {
                        DesiredCapabilities capabilities = new DesiredCapabilities();
                        CreateRemoteDriver(capabilities);
                    }
//                    this.setDriver(new InternetExplorerDriver());
                    break;
            }
        }


    }
    /*

    private boolean DriverCreate(boolean isLocal, boolean isMobile) {

        if (!isLocal){
            if(isMobile){
                //! MOBILE VERSION
                Map<String, String> mobileEmulation = new HashMap<>();

                mobileEmulation.put("deviceName", "Nexus 5");
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
                DesiredCapabilities capabilities = new DesiredCapabilities(chromeOptions);
                try {
                    createRemoteDriver(capabilities);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }else{
                //! DESKTOP VERSION
                DesiredCapabilities capabilities = new DesiredCapabilities();
                try {
                    createRemoteDriver(capabilities);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
            return true;

        }

        if (isLocal) {
            switch (this.browser) {
                case chrome:
                    if (isLocal) {
                        System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver.exe");
                        if (isMobile) {
                            Map<String, String> mobileEmulation = new HashMap();
                            mobileEmulation.put("deviceName", "Nexus 5");
                            ChromeOptions chromeOptions = new ChromeOptions();
                            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
                            driver = new ChromeDriver(chromeOptions);
                            return true;
                        } else {
                            driver = new ChromeDriver();
                            return true;
                        }
                    } else {


//
//                    DesiredCapabilities capability = DesiredCapabilities.chrome();
//                    capability.setBrowserName("chrome");
//                    capability.setPlatform(org.openqa.selenium.Platform.ANY);
//                    driver = new RemoteWebDriver(new URL(this.getSeleniumURL()), capability);
                    }
                    break;
                case firefox:
                    System.setProperty("webdriver.gecko.driver", driverPath + "geckodriver.exe");
                    driver = new FirefoxDriver();
                    break;

                case iexplorer:
                    System.out.println("Delicious iexplorer");
                    System.setProperty("webdriver.ie.driver", driverPath + "IEDriverServer.exe");
                    this.setDriver(new InternetExplorerDriver());
                    break;
                default:
                    System.setProperty("webdriver.chrome.driver", driverPath + "chromedriver.exe");
                    driver = new ChromeDriver();
            }
            return true;
        }


        return false;
    }
*/

    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] attachment()  {
        byte[] src = ((TakesScreenshot)getDriver()).getScreenshotAs(OutputType.BYTES);
        return src;
    }
    @Attachment(value = "{1}", type="text/plain")
    public String attachmentText(String data, String description) {
        return data;
    }

    @AfterMethod(description = "Add attachments environment | screenshot")
    public void AfterCall(){
        RemoteWebDriver driver = getDriver();
        attachmentText(getEnvironment(),"Environment");
        attachment();
//        getDriver().close(); // close only current tab

        driver.quit();
    }



    private String getEnvironment(){
        String OS = getOS();
        String ln = "\n";
        System.getProperty("os.name").toLowerCase();

        String osArch = System.getProperty("os.arch");
        String javaRuntimeVersion = System.getProperty("java.runtime.version");

        Capabilities cap = ((RemoteWebDriver) getDriver()).getCapabilities();
        String browserName = cap.getBrowserName().toLowerCase();
        String browserVersion = cap.getVersion().toString();
        String screenResolution = getDriver().manage().window().getSize().toString();


        return
                "SITE_URL = " + SITE_URL + ln +
                    "Operating system = " + OS + " | " + osArch + ln +
                    "Screen resolution = " + screenResolution + ln +
                    "Browser = " + browserName + " v" + browserVersion + ln +
                    "Java version = " + javaRuntimeVersion;

    }

    private static String getOS () {
        String os = System.getProperty("os.name").toLowerCase();
        String os_version = System.getProperty("os.version").toLowerCase();
        if (os.contains("win")) {
            return "Windows v" + os_version;
        } else if (os.contains("nux") || os.contains("nix")) {
            return "Linux v" + os_version;
        }else if (os.contains("mac")) {
            return "Mac v" + os_version;
        }else if (os.contains("sunos")) {
            return "Solaris v" + os_version;
        }else {
            return "Other v" + os_version;
        }
    }


}
