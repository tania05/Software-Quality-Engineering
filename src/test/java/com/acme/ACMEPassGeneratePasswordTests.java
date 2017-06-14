package com.acme;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.acme.Util.generateRandomString;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ACMEPassGeneratePasswordTests extends ACMEPassTestBase {

    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    private String username;
    private String password;
    private PasswordHelper _passwordHelper;
    private Random _random;

    @Parameterized.Parameters
    public static Collection<Object[]> users() {
        return Arrays.asList( new Object[][] {
            { "frank.paul@acme.com", "starwars"}    // Manager
//            { "jo.thomas@acme.com",  "mustang" },    // Employee
//            { "admin@acme.com",      "K-10ficile"}, // Admin
//            { "frank.paul@acme.com", "starwars"},    // Manager
//            { "jo.thomas@acme.com",  "mustang" },    // Employee
//            { "admin@acme.com",      "K-10ficile"}, // Admin
        });
    }

    public ACMEPassGeneratePasswordTests(String username, String password) {
        this.username = username;
        this.password = password;
        this._random = new Random();
    }

    @Before
    public void setUp() throws Exception {
        url = "http://localhost:8080/#/";
        driver = getDriver("firefox");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
        _passwordHelper = new  PasswordHelper(new LoginHelper(driver, url),driver, url);

        loginWith(username, password);
    }

    @Test
    public void createPasswordGoldenPath() throws InterruptedException {

        String site = generateRandomString(_random, 32);
        String login = generateRandomString(_random, 32);
        String password = generateRandomString( _random, 32);
        createPassword(site, login, password);

        Assert.assertTrue(findModalSaveButton().isEnabled());

        //The 'save' button
        findModalSaveButton().click();
        Util.waitUntilModalGone(driver);
        assertTrue(_passwordHelper.passwordEntryExists(site,login,password));

        _passwordHelper.deleteGeneratedPassword(site,login, password);
    }

    @Test
    public void generatePasswordWithlessThanThreeCharacterSiteNameFails() throws InterruptedException{
        openGenerateModal(null,null,null);
        clickGenerateFromGenerateModal();
        driver.findElement(By.xpath("//input[@type='number']")).clear();
        driver.findElement(By.xpath("//input[@type='number']")).sendKeys(Integer.toString(_random.nextInt(32)));
        findModalSaveButton().click();
        fillCreatePasswordModal(generateRandomString(_random, 2), generateRandomString(_random, 32));
        Assert.assertFalse(findModalSaveButton().isEnabled());
    }

    @Test
    public void generatePasswordWithEmptySiteFieldFails() throws InterruptedException{
        openGenerateModal(null,null,null);
        clickGenerateFromGenerateModal();
        driver.findElement(By.xpath("//input[@type='number']")).clear();
        driver.findElement(By.xpath("//input[@type='number']")).sendKeys(Integer.toString(_random.nextInt(32)));
        findModalSaveButton().click();
        fillCreatePasswordModal( null, generateRandomString(_random, 32));
        Assert.assertFalse(findModalSaveButton().isEnabled());
    }

    @Test
    public void generatePasswordWithEmptyLoginFieldFails() throws InterruptedException{
        openGenerateModal(null,null,null);
        clickGenerateFromGenerateModal();
        driver.findElement(By.xpath("//input[@type='number']")).clear();
        driver.findElement(By.xpath("//input[@type='number']")).sendKeys(Integer.toString(_random.nextInt(32)));
        findModalSaveButton().click();
        fillCreatePasswordModal( generateRandomString(_random, 32),null);
        Assert.assertFalse(findModalSaveButton().isEnabled());
    }

    @Test
    public void generatePasswordWithEmptyPasswordFieldFails() throws InterruptedException{
        openGenerateModal(null,null,null);
        clickGenerateFromGenerateModal();
        driver.findElement(By.xpath("//input[@type='number']")).clear();
        driver.findElement(By.xpath("//input[@type='number']")).sendKeys(Integer.toString(_random.nextInt(32)));
        findModalSaveButton().click();
        fillCreatePasswordModal( generateRandomString(_random, 32),generateRandomString(_random, 32));
        getPasswordField().clear();
        Assert.assertFalse(findModalSaveButton().isEnabled());
    }

    @Test
    public void generatePasswordGoldenPath() throws InterruptedException{
        String site = generateRandomString(_random, 32);
        String login = generateRandomString(_random, 32);

        PasswordHelper.PasswordCreationModalHelper modal = _passwordHelper.openPasswordCreationModal();
        modal.findLoginElement().sendKeys(login);
        modal.findSiteElement().sendKeys(site);
        Assert.assertFalse(findModalSaveButton().isEnabled());

        // generate password
        clickGenerateButtonFromCreate();
        clickGenerateFromGenerateModal();
        String generatedPassword = getPasswordField().getAttribute("value");

        findModalSaveButton().click();
        // Util.waitUntilModalGone(driver);
        Thread.sleep(1000);
        findModalSaveButton().click();
        Util.waitUntilModalGone(driver);
        assertTrue(_passwordHelper.passwordEntryExists(site, login, generatedPassword));
       _passwordHelper.deleteGeneratedPassword(site,login,generatedPassword);
    }


    @Test
    public void generatePasswordWithInputlengthGeneratesCorrectLength() throws InterruptedException{
        String site = generateRandomString(_random, 32);
        String login = generateRandomString(_random, 32);
        PasswordHelper.PasswordCreationModalHelper modal = _passwordHelper.openPasswordCreationModal();

        modal.findSiteElement().sendKeys(site);
        modal.findLoginElement().sendKeys(login);

        clickGenerateButtonFromCreate();
        int length = _random.nextInt(32);
        driver.findElement(By.xpath("//input[@type='number']")).clear();
        driver.findElement(By.xpath("//input[@type='number']")).sendKeys(Integer.toString(length));
        clickGenerateFromGenerateModal();
        String generatedPassword = getPasswordField().getAttribute("value");
        assertEquals(length, generatedPassword.length());
    }

    @Test
    public void generatePasswordOverridesUserInputInPasswordCreateModal() throws InterruptedException{
        String site = generateRandomString(_random, 32);
        String login = generateRandomString(_random, 32);
        String password = generateRandomString(_random, 32);

        openGenerateModal(site,login,password);

        int length = _random.nextInt(32);
        driver.findElement(By.xpath("//input[@type='number']")).clear();
        driver.findElement(By.xpath("//input[@type='number']")).sendKeys(Integer.toString(length));
        clickGenerateFromGenerateModal();
        String generatedPassword = getPasswordField().getAttribute("value");

        findModalSaveButton().click();
        String overridenPassword = getPasswordField().getAttribute("value");
        assertEquals(generatedPassword, overridenPassword);
    }


    @Test
    public void generatePasswordWithNonRepeatedCharactersDoesNotRepeatCharacters() throws InterruptedException{
        String site = generateRandomString(_random, 32);
        String login = generateRandomString(_random, 32);
        openGenerateModal(site, login, null);

        //enable non-repeat characters
        driver.findElement(By.xpath("//input[@type='checkbox']")).click();
        clickGenerateFromGenerateModal();
        assertTrue(isCharRepeated(getPasswordField().getAttribute("value")));
    }

    @Test
    public void generatePasswordDoesNotAllowUserToManuallyInputGeneratedPassword() throws InterruptedException{
        String site = generateRandomString(_random, 32);
        String login = generateRandomString(_random, 32);
        openGenerateModal(site, login, null);

        assertFalse(getPasswordField().isEnabled());
    }

//    private void deleteGeneratedPassword(String site, String login, String password) throws InterruptedException {
//        //goto first page.
//        _passwordHelper.givenOnFirstAcmePassPage();
//
//        while(true) {
//            while(true){
//                boolean foundPassword = _passwordHelper.getPasswordsOnPage().parallelStream().anyMatch((storedPassword) ->
//                        Objects.equals(storedPassword.site, site) &&
//                                Objects.equals(storedPassword.login, login) &&
//                                Objects.equals(storedPassword.password, password)
//                );
//
//                if (foundPassword) {
//                    Optional<PasswordHelper.Password> pswd = _passwordHelper.getPasswordsOnPage().parallelStream().filter((passwords) ->
//                            Objects.equals(passwords.site, site) &&
//                                    Objects.equals(passwords.login, login) &&
//                                    Objects.equals(passwords.password, password)).findFirst();
//                    _passwordHelper.deletePassword(pswd.get());
//                }
//                else{
//                    // No more on this page
//                    break;
//                }
//            }
//
//            try {
//                _passwordHelper.goToNextPage();
//            } catch (NoSuchElementException e) {
//                // no more pages to try
//                System.out.println("Done scanning all pages");
//                break;
//            }
//        }
//    }

    public boolean isCharRepeated(String input) {
        for (int i = 1; i < input.length(); ++i){
            if (!(input.charAt(i) - input.charAt(i - 1) == 0)) {
                return true;
            }
        }
        return false;
    }

    public void openGenerateModal(String site, String login, String password) throws InterruptedException{
        PasswordHelper.PasswordCreationModalHelper modal = _passwordHelper.openPasswordCreationModal();

        modal.findSiteElement().sendKeys(site);
        modal.findLoginElement().sendKeys(login);
        if(password != null)
            modal.findPasswordElement().sendKeys(password);
        clickGenerateButtonFromCreate();
    }
    public void fillCreatePasswordModal(String site, String login){

        driver.findElement(By.xpath("//input[@ng-model='vm.acmePass.site']")).clear();
        driver.findElement(By.xpath("//input[@ng-model='vm.acmePass.site']")).sendKeys(site);

        driver.findElement(By.xpath("//input[@ng-model='vm.acmePass.login']")).clear();
        driver.findElement(By.xpath("//input[@ng-model='vm.acmePass.login']")).sendKeys(login);
    }

    public void createPassword(String site, String login, String password) throws InterruptedException{

        PasswordHelper.PasswordCreationModalHelper modal = _passwordHelper.openPasswordCreationModal();
        modal.findSiteElement().clear();
        modal.findSiteElement().sendKeys(site);
        modal.findLoginElement().clear();
        modal.findLoginElement().sendKeys(login);
        if(password != null){
            modal.findPasswordElement().clear();
            modal.findPasswordElement().sendKeys(password);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Cleanup: remove the acmepass entry we created.

        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }







}
