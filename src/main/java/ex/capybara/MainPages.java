/*
 * Copyright (c) 2019. Semenoff Slava
 */

package ex.capybara;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;


import java.util.concurrent.TimeUnit;
public class MainPages {

    protected MainTest mainTest;
    protected WebDriver driver;

    protected void isPageload(int loadTime, String errMsg) {
        isElementPresent(By.cssSelector("body"), loadTime, errMsg);
    }

    private void isElementPresent(By locator, int waitSec, String errMsg) {
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        getElement(locator, waitSec, errMsg);
    }

//    protected void waitForUrlToContainString_old(String URL, int waitSec, String errMsg) {
//        isPageload(waitSec, errMsg);
//        String currentUrl = driver.getCurrentUrl();
//        boolean result = currentUrl.contains(URL);
//        Assert.assertTrue(result, errMsg);
//    }

    protected void waitForUrlToContainString(String URL,int waitSec,String errMsg) {
        final WebDriverWait wait = new WebDriverWait(driver, waitSec);
        try{
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver driver) {
                    return driver.getCurrentUrl().contains(URL);
                }
            });
        }catch (TimeoutException timeout){
            Assert.assertEquals(driver.getCurrentUrl().toLowerCase(),URL.toLowerCase(),errMsg + " | " + timeout.getMessage());
        }
    }


    // interact with elements

    protected WebElement getElement(By locator, int waitSec, String errMsg) {
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        try {
            WebElement element = this.driver.findElement(locator);
            driver.manage().timeouts().implicitlyWait(MainTest.DefaultDelay, TimeUnit.SECONDS);
            return element;
        } catch (NoSuchElementException e) {
            System.err.print(errMsg + " | " + e.getMessage());
            throw new WebDriverException(errMsg);
        } finally {
            driver.manage().timeouts().implicitlyWait(MainTest.DefaultDelay, TimeUnit.SECONDS);
        }

    }
    protected void clickOnElement(By locator, int waitSec, String errMsg) {
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        WebElement element = getElement(locator, waitSec, errMsg);

        WebDriverWait wait = new WebDriverWait(driver, waitSec);
        wait.until(ExpectedConditions.elementToBeClickable(element));
        Assert.assertTrue(element.isEnabled(), errMsg);
        try {
            element.click();
        } catch (WebDriverException e) {
            try {
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                executor.executeScript("arguments[0].click();", element);
            } catch (WebDriverException er) {
                System.err.print(errMsg + " | " + er.getMessage());
                Assert.fail(errMsg + " | " + er.getMessage(), er);
            }

        }
    }
    protected void isDisplayed(By locator, int waitSec, String errMsg) {
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        try {
            WebElement element = getElement(locator, waitSec, errMsg);
            element.isDisplayed();
        } catch (WebDriverException e) {
            System.err.print(errMsg + " | " + e.getMessage());
            Assert.fail(errMsg + " | " + e.getMessage(), e);
        } finally {
            driver.manage().timeouts().implicitlyWait(MainTest.DefaultDelay, TimeUnit.SECONDS);
        }
    }

    protected void isNotDisplayedAlready(final By locator, int waitSec, String errMsg) {
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);

        WebDriverWait wait = new WebDriverWait(driver, waitSec);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

        Assert.assertTrue(driver.findElement(locator).isDisplayed(), errMsg);





//        WebDriverWait _wait = new WebDriverWait(driver, waitSec);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        WebDriverWait _wait = new WebDriverWait(driver, waitSec,300);
        ExpectedCondition elementIsDisplayed = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver arg0) {
                try {
                    driver.findElement(locator).isDisplayed();
                    return false;
                }
                catch (NoSuchElementException e ) {
//                    System.err.println("locator not displayed");
                    return true;
                }
                catch (StaleElementReferenceException f) {
//                    System.err.println("locator not displayed");
                    return true;
                }
            }
        };
        _wait.until(elementIsDisplayed);

//        _wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));

//        try {
//            Assert.assertTrue(!driver.findElement(locator).isDisplayed(), errMsg);
//        } catch (NoSuchElementException e){
//            Assert.assertTrue(true);
//        }

        driver.manage().timeouts().implicitlyWait(MainTest.DefaultDelay, TimeUnit.SECONDS);


    }
    protected void isNotVisibleAlready(final By locator, int delaySeconds, String errorMassage) {
        driver.manage().timeouts().implicitlyWait(delaySeconds, TimeUnit.SECONDS);

        WebDriverWait wait = new WebDriverWait(driver, delaySeconds);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

        Assert.assertTrue(driver.findElement(locator).isDisplayed(), errorMassage);

        WebDriverWait _wait = new WebDriverWait(driver, delaySeconds);
        _wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        Assert.assertTrue(!driver.findElement(locator).isDisplayed(), errorMassage);
        driver.manage().timeouts().implicitlyWait(MainTest.DefaultDelay, TimeUnit.SECONDS);


    }


    protected void clearFieldAndFillItWithText(By locator, String text, int waitSec, String errMsg){
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        WebElement element = getElement(locator,waitSec,errMsg);
        element.clear();
        element.sendKeys(text);
    }

    protected void moveTo(By locator, int waitSec, String errMsg){
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        WebElement element = getElement(locator,waitSec,errMsg);
        Actions action = new Actions(driver);

        action.moveToElement(element).perform();
    }

    protected void selectedByIndex(By locator,int index, int waitSec, String errMsg){
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        Select select = new Select(getElement(locator,waitSec,errMsg));
        select.selectByIndex(index);
    }
    protected void selectedByText(By locator,String text, int waitSec, String errMsg){
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        Select select = new Select(getElement(locator,waitSec,errMsg));
        select.selectByVisibleText(text);
    }

    protected void isTextContains(By locator,String text, int waitSec, String errMsg){
        driver.manage().timeouts().implicitlyWait(waitSec, TimeUnit.SECONDS);
        WebElement element = getElement(locator,waitSec,errMsg);
        String containText = element.getText().toLowerCase();

        Assert.assertTrue(containText.contains(text.toLowerCase()));
    }

    protected void waitForTextEqual(final By locator, final String text,final int waitSec, final String errMsg) {
        final WebDriverWait wait = new WebDriverWait(driver, waitSec);
        try{
            wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver driver) {
                    WebElement element = getElement(locator, waitSec, "element did not display" + waitSec + " seconds");
                    String element_text = element.getText();
                    if (element_text.equalsIgnoreCase(text)){
                        return true;
                    }else {
                        return false;
                    }
                }
            });
        }catch (TimeoutException timeout){
            String element_text = getElement(locator, waitSec, "element did not display" + waitSec + " seconds").getText();
            Assert.assertEquals(element_text.toLowerCase(),text.toLowerCase(),errMsg + " | " + timeout.getMessage());
        }
    }


    // Other functions

    protected static Integer StrToInt(
            final CharSequence input){
        final StringBuilder sb = new StringBuilder(
                input.length() );
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        String resultStr = sb.toString();
        return Integer.parseInt(resultStr);
    }
}