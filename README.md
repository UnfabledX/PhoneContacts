# PhoneContacts

**1.** This application can be run in docker containers. Find docker-compose.yml in folder /docker. 
For brevity and educational reasons .env file with env variables is already provided in the same folder.

**2.** Swagger documentation can be found in /documentation folder.

**3.** For quick and user-friendly acquaintance with application there is a link 
https://www.postman.com/restless-satellite-799898/workspace/phone-contact-application 
to the postman public workspace where you can find already made endpoints for testing the application.
Don't forget to apply env variables at environments tab in postman

## Description of the project:
Phone contacts application allows adding/editing and deleting contacts data. 
Single contact is represented by the following data:
**1.**     Contact name
**2.**     Contact emails. One contact may have multiple emails
**3.**     Contact phone number. One contact may have multiple phone numbers

User should have a possibility to:
**1.**     Register in the app, login and password should be provided during registration
**2.**     Login to the app
**3.**     Add new contact
**4.**     Edit existing contact
**5.**     Delete existing contact
**6.**     Get list of existing contacts
