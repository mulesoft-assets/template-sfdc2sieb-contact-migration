# Anypoint Template: Salesforce to Siebel Contact Migration

# License Agreement
This template is subject to the conditions of the 
<a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free 
with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
I want to synchronize contacts from Salesforce to Siebel.

This template should serve as a foundation for the process of migrating contacts from Salesforce to Siebel, being able to specify filtering criteria and desired behavior when an contact already exists in the Siebel. 

This template leverages the Mule batch module.
The batch job is divided in Input, Process, and On Complete stages:
- During the Input stage the template goes to  Salesforce and queries all existing contacts that match the filter criteria.
- During the Process stage, each Salesforce Contact is filtered depending on whether it has an existing matching contact in the Siebel. If there is an account associated with contact in Salesforce, this account is checked in Siebel and created if needed.
- The last step of the Process stage groups the contacts, creates them in Siebel, and associates accounts to the contacts.
Finally during the On Complete stage the template will both output statistics data into the console and send a notification email with the results of the batch execution.

# Considerations

To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source (Salesforce) and destination (Siebel) systems, that must be made in order for all to run smoothly. 
Failing to do so could lead to unexpected behavior of the template.

## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work.

### FAQ

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>

### As a Data Source

If the user who configured the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault 
[ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='
Account.Phone, Account.Rating, Account.RecordTypeId, Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to use a custom field, 
be sure to append the '__c' after the custom field name. Reference your WSDL or the describe 
call for the appropriate names.'
]
row='1'
column='486'
]
]
```

## Siebel Considerations

Here's what you need to know to get this template to work with Siebel.

This template may use date time or timestamp fields from Siebel to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it cannot discover the time zone in which the Siebel instance is on.
It is up to you to provide such information. See [Oracle's Setting Time Zone Preferences](https://docs.oracle.com/cd/B40099_02/books/Fundamentals/Fund_settingoptions3.html).

### As a Data Destination

To make the Siebel connector work smoothly you have to provide the correct version of the Siebel jars (Siebel.jar, SiebelJI_enu.jar) that works with your Siebel installation.

# Run it!
Simple steps to get Salesforce to Siebel Contact Migration running.
<pre>
<h1>Batch Process initiated</h1>
<b>ID:</b>6eea3cc6-7c96-11e3-9a65-55f9f3ae584e<br/>
<b>Records to Be Processed: </b>9<br/>
<b>Start execution on: </b>Mon Oct 15 18:05:33 GMT-03:00 2018
</pre>

## Running On Premises
In this section we help you run your template on your computer.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

- [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
- [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your
Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

- Locate the properties file `mule.dev.properties`, in src/main/resources.
- Complete all the properties required as per the examples in the "Properties to Configure" section.
- Right click the template project folder.
- Hover your mouse over `Run as`.
- Click `Mule Application (configure)`..
- Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`
- Click `Run`.

### Running on Mule Standalone
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 


## Running on CloudHub
While creating your application on CloudHub (or you can do it later as a next step), go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the **mule.env**.


### Deploying your Anypoint Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
**Application configuration**
- http.port `9090` 
- page.size `20`
- migration.startDate `2016-12-13T03:00:59Z`

**Oracle Siebel Connector Configuration**
- sieb.user `user`
- sieb.password `secret`
- sieb.server `server`
- sieb.serverName `serverName`
- sieb.objectManager `objectManager`
- sieb.port `2321`

**Salesforce Connector Configuration**
- sfdc.username `bob.dylan@sfdc`
- sfdc.password `DylanPassword123`
- sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`

**SMTP Services Configuration**
- smtp.host `smpt server host`
- smtp.port `smpt server port`
- smtp.user `smpt user`
- smtp.password `secret`

**Email Details**
- mail.from `from_email@email`
- mail.to `to_email@email`
- mail.subject `email subject`

# API Calls
SalesForce imposes limits on the number of API Calls that can be made.
Therefore calculating this amount may be an important factor to
consider. Contact Migration Template calls to the API can be
calculated using the formula:

**X / 200**

X is the number of Contacts to be synchronized on each run.

Divide by 200 because by default, Contacts are gathered in groups
of 200 for each API Call in the input step. 

For instance if 10 records are fetched from origin instance, then 1 API
calls to Salesforce is made ( 1).


# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml

## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.

## businessLogic.xml
Functional aspect of the Template is implemented on this XML, directed by one flow responsible of excecuting the logic.
For the purpose of this particular Template the *mainFlow* just executes a batch job. which handles all the logic of it.
This flow has Exception Strategy that basically consists on invoking the *defaultChoiseExceptionStrategy* defined in *errorHandling.xml* file.

## endpoints.xml
This is the file where you will found the inbound and outbound sides of your integration app.
This Template has only an HTTP Listener as the way to trigger the use case.

**HTTP Inbound Endpoint** - Start Report Generation
- `${http.port}` is set as a property to be defined either on a property file or in CloudHub environment variables.
- The path configured by default is `migratecontacts` and you are free to change for the one you prefer.
- The host name for all endpoints in your CloudHub configuration should be defined as `localhost`. CloudHub will then route requests from your application domain URL to the endpoint.
- The endpoint is configured as a *request-response* since as a result of calling it the response will be the total of Contacts synced and filtered by the criteria specified.

## errorHandling.xml
This is the right place to handle how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.

