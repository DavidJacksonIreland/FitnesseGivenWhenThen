package com.givenwhenthen.fitnesse.example;

import com.givenwhenthen.fitnesse.common.GivenWhenThenFixture;

public class TestBankAccount extends GivenWhenThenFixture {
	
	private Double balance = 0.0;
	private String userName = "";
	
	public void doTestSetup() 
	{
		// prepare environment for test
	}
	
	public void doTestTeardown() 
	{
		// clean up any changes made by test to environment
	}
	
	public void userHasDollarsInTheirAccount(String userName, Double balance)
	{
		this.balance = balance;
		this.userName = userName;
	}
	
	public void userHasNoMoneyInTheirAccount(String userName)
	{
		this.userName = userName;
	}
	
	public void dollarsIsDepositedInToTheAccount(Double amount)
	{
		this.balance += amount;
	}
	
	public void dollarsIsWithdrawnFromTheAccount(Double amount)
	{
		this.balance -= amount;
	}
	
	public boolean theBalanceShouldBeDollars(Double amount)
	{
		// set for use in possible failure messages
		setLastVerifiedValueAsString(this.balance.toString());
		setLastExpectedValueAsString(amount.toString());
		
		// do validation
		return (amount.equals(this.balance));
	}
}
