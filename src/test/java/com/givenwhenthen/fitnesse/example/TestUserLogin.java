package com.givenwhenthen.fitnesse.example;

import com.givenwhenthen.fitnesse.common.GivenWhenThenFixture;

public class TestUserLogin extends GivenWhenThenFixture  {

	private String userName = "";
	private String password = "";
	private Boolean loginSuccessful = false;
	
	public void doTestSetup() 
	{
		// prepare environment for test
	}
	
	public void doTestTeardown() 
	{
		// clean up any changes made by test to environment
	}
	
	public void userArrivesAtTheLoginPage(String userName)
	{
		this.userName = userName;
	}
	
	public void entersPasswordOf(String password)
	{
		this.password = password;
	}
	
	public void heClicksOnTheLoginButton()
	{
		// do some interaction with system (e.g. via Selenium or web service call) to log the user in
		// save the result
		this.loginSuccessful = (this.password.equals("P@ssw0rd"));
	}
	
	public Boolean heShouldSeeAResultMessageOf(String expectedResultMessage)
	{
		String welcomeMessage = this.loginSuccessful ? "Welcome, "
				+ this.userName : "Invalid Username or Password!";
		
		return welcomeMessage.equals(expectedResultMessage);
	}
	
	public Boolean heIsDirectedToPage(String expectedPage)
	{
		String nextPage = this.loginSuccessful ? "home.html" : "loginerror.html";
		return nextPage.equals(expectedPage);
	}
}
