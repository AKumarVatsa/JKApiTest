Feature: Book management
  Scenario: Create a book in user collection
    Given the user is authenticated
    When the user adds a book to the collection
    Then the book should be added successfully

  Scenario: Get all books
    When the user retrieves all books
    Then the book list should contain the expected book title

  Scenario: Update a book in the collection
    When the user attempts to update a book
    Then the update should not be supported

  Scenario: Delete a book from the collection
    When the user deletes the book
    Then the book should be removed successfully

  Scenario: Verify the book is deleted
    When the user checks the collection
    Then the book should not be present
