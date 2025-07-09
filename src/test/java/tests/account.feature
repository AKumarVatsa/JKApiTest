Feature: Account Management

Scenario: Create a new user
Given a new username and password
When the user sends a create account request
Then the account should be created successfully

Scenario: Generate a token for the user
Given a valid username and password
When the user requests a token
Then a valid token should be returned

Scenario: Verify the created user exists
Given the user has a valid token
When the user fetches their account details
Then the details should be returned successfully
