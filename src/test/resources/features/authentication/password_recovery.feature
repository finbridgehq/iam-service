@TS01
Feature: Password recovery
  As a user who forgot my password
  I want to receive a reset link by email and choose a new password
  So that I can recover access to my account without contacting support

  Background:
    Given an account exists for "maria.torres@vankoo.pe" with password "OldPass123!" and role "ROLE_MYPE"

  Scenario: Request a reset link for an existing account
    When I request a password reset for "maria.torres@vankoo.pe"
    Then the response status is 202
    And the response body is empty
    And a password reset email is sent to "maria.torres@vankoo.pe"

  Scenario: Request a reset link for an unknown email without revealing it
    When I request a password reset for "unknown@vankoo.pe"
    Then the response status is 202
    And the response body is empty
    And no password reset email is sent

  Scenario: Reject a reset request with an invalid email
    When I request a password reset for "not-an-email"
    Then the response status is 400

  Scenario: Set a new password with a valid token
    Given "maria.torres@vankoo.pe" requested a password reset and received a valid token
    When I reset the password with that token and the new password "NewPass456!"
    Then the response status is 204
    And I can sign in as "maria.torres@vankoo.pe" with password "NewPass456!"
    And I cannot sign in as "maria.torres@vankoo.pe" with password "OldPass123!"

  Scenario Outline: Reject a token that cannot be used
    Given "maria.torres@vankoo.pe" has a <token state> reset token
    When I reset the password with that token and the new password "NewPass456!"
    Then the response status is 400
    And the error detail is the same for every unusable token
    And I can still sign in as "maria.torres@vankoo.pe" with password "OldPass123!"

    Examples:
      | token state  |
      | unknown      |
      | expired      |
      | already used |
