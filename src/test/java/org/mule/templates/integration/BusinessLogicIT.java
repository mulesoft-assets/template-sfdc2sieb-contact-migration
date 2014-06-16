/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.processor.chain.InterceptingChainLifecycleWrapper;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.AbstractTemplateTestCase;

import com.mulesoft.module.batch.BatchTestHelper;
import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the Mule Template that make calls to external systems.
 * 
 * The test will invoke the batch process and afterwards check that the accounts had been correctly created and that the ones that should be filtered are not in
 * the destination sand box.
 * 
 * The test validates that no account will get sync as result of the integration.
 * 
 */
@SuppressWarnings("unchecked")
public class BusinessLogicIT extends AbstractTemplateTestCase {
	protected static final int TIMEOUT_SEC = 120;
	protected static final String ANYPOINT_TEMPLATE_NAME = "contact-migration";
	private BatchTestHelper helper;

	private static List<String> accountsCreatedInSalesforce = new ArrayList<String>();
	private static List<String> accountsCreatedInSiebel = new ArrayList<String>();
	private static List<String> contactsCreatedInSiebel = new ArrayList<String>();
	private static List<Map<String, Object>> createdAccounts = new ArrayList<Map<String,Object>>();
	private static List<Map<String, Object>> createdContacts = new ArrayList<Map<String,Object>>();
	
	private SubflowInterceptingChainLifecycleWrapper createAccountInSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper createContactInSalesforceFlow;
	private static SubflowInterceptingChainLifecycleWrapper deleteAccountFromSalesforceFlow;
	private static SubflowInterceptingChainLifecycleWrapper deleteAccountsFromSiebelFlow;
	private static SubflowInterceptingChainLifecycleWrapper deleteContactsFromSiebelFlow;
	private InterceptingChainLifecycleWrapper selectContactFromSiebelFlow;
	private InterceptingChainLifecycleWrapper selectAccountFromSiebelFlow;
	
	@Before
	public void setUp() throws Exception {
		helper = new BatchTestHelper(muleContext);
		getAndInitializeFlows();
		createTestDataInSandBox();
	}

	
	private void getAndInitializeFlows() throws InitialisationException {
		// Flow for creating accounts in Salesforce createContactInSalesforceFlow
		createAccountInSalesforceFlow = getSubFlow("createAccountInSalesforceFlow");
		createAccountInSalesforceFlow.initialise();

		// Flow for creating contacts in Salesforce 
		createContactInSalesforceFlow = getSubFlow("createContactInSalesforceFlow");
		createContactInSalesforceFlow.initialise();

		// Flow for deleting accounts in Salesforce
		deleteAccountFromSalesforceFlow = getSubFlow("deleteAccountFromSalesforceFlow");
		deleteAccountFromSalesforceFlow.initialise();

		// Flow for deleting accounts in Siebel
		deleteAccountsFromSiebelFlow = getSubFlow("deleteAccountsFromSiebelFlow");
		deleteAccountsFromSiebelFlow.initialise();

		// Flow for deleting contacts in Siebel
		deleteContactsFromSiebelFlow = getSubFlow("deleteContactsFromSiebelFlow");
		deleteContactsFromSiebelFlow.initialise();

		// Flow for querying the contact in Siebel
		selectContactFromSiebelFlow = getSubFlow("selectContactFromSiebelFlow");
		selectContactFromSiebelFlow.initialise();

		// Flow for querying the account in Siebel
		selectAccountFromSiebelFlow = getSubFlow("selectAccountFromSiebelFlow");
		selectAccountFromSiebelFlow.initialise();
	}
	
	
	@After
	public void tearDown() throws Exception {
		cleanUpSandboxes();
	}

	@Test
	public void testMainFlow() throws Exception {
		runFlow("mainFlow");
		
		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();
		
		List<Map<String, Object>> response = (List<Map<String, Object>>) selectContactFromSiebelFlow.process(getTestEvent(createdContacts.get(0), MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload();
		Assert.assertEquals("There should be only one contact with given email", 1, response.size());
		Assert.assertEquals("Email should match", createdContacts.get(0).get("Email"), response.get(0).get("Email Address"));
		Assert.assertEquals("LastName should match", createdContacts.get(0).get("LastName"), response.get(0).get("Last Name"));
		contactsCreatedInSiebel.add((String) response.get(0).get("Id"));

		response = (List<Map<String, Object>>) selectAccountFromSiebelFlow.process(getTestEvent(createdAccounts.get(0), MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload();
		Assert.assertEquals("There should be only one account with this name", 1, response.size());
		accountsCreatedInSiebel.add((String) response.get(0).get("Id"));
	}


	private void createTestDataInSandBox() throws MuleException, Exception {
		long currentMillis = System.currentTimeMillis();

		HashMap<String, Object> account = new HashMap<String, Object>();
		account.put("Name", "Account-for-" + ANYPOINT_TEMPLATE_NAME + "-" + currentMillis);
		account.put("Phone", "123456789");
		account.put("NumberOfEmployees", 1650);
		account.put("Industry", "Education");

		createdAccounts.add(account);

		// Create accounts in sand-boxes and keep track of them
		String justCreatedAccountId = createTestObjectsInSfdcSandbox(account, createAccountInSalesforceFlow);
		accountsCreatedInSalesforce.add(justCreatedAccountId);
		
		HashMap<String, Object> contact = new HashMap<String, Object>();
		contact.put("FirstName", "Peter");
		contact.put("LastName", "Migration-" + currentMillis);
		contact.put("AccountId", justCreatedAccountId);
		contact.put("MailingCity", "San Francisco");
		contact.put("Email", "mig-contact-" + currentMillis + "@gmail.com");
		contact.put("LeadSource", "Partner - Event");

		createTestObjectsInSfdcSandbox(contact, createContactInSalesforceFlow);
		
		createdContacts.add(contact);
	}
	
	private String createTestObjectsInSfdcSandbox(Map<String, Object> object, InterceptingChainLifecycleWrapper flow) throws Exception {
		List<Map<String, Object>> salesforceObject = new ArrayList<Map<String, Object>>();
		salesforceObject.add(object);
		final List<SaveResult> payloadAfterExecution = (List<SaveResult>) flow.process(getTestEvent(salesforceObject, MessageExchangePattern.REQUEST_RESPONSE)).getMessage().getPayload();
		return payloadAfterExecution.get(0).getId();
	}
	
	private static void cleanUpSandboxes() throws MuleException, Exception {
		final List<String> idList = new ArrayList<String>();
		
		for (String account : accountsCreatedInSalesforce) {
			idList.add(account);
		}
		deleteAccountFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
		idList.clear();
		
		for (String account : accountsCreatedInSiebel) {
			idList.add(account);
		}
		deleteAccountsFromSiebelFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
		idList.clear();

		for (String account : contactsCreatedInSiebel) {
			idList.add(account);
		}
		deleteContactsFromSiebelFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}


}
