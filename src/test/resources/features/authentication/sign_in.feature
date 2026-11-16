@TS01
Feature: User sign-in
  As a registered MYPE owner or investor
  I want to sign in with my email and password
  So that I receive a token to call the protected Vankoo services

  Background:
    Given an account exists for "maria.torres@vankoo.pe" with password "Str0ngPass!" and role "ROLE_MYPE"

  Scenario: Sign in with valid credentials
    When I sign in with email "maria.torres@vankoo.pe" and password "Str0ngPass!"
    Then the response status is 200
    And the response contains the user id, the email "maria.torres@vankoo.pe" and a JWT token
    And the response does not contain the password

  Scenario Outline: Reject invalid credentials without revealing whether the account exists
    When I sign in with email "<email>" and password "<password>"
    Then the response status is 401
    And the response body is a problem detail
    And the error detail is the same for an unknown account and a wrong password

    Examples:
      | email                  | password       |
      | maria.torres@vankoo.pe | WrongPass123   |
      | unknown@vankoo.pe      | Str0ngPass!    |

  Scenario Outline: Reject a sign-in request with missing fields
    When I sign in with email "<email>" and password "<password>"
    Then the response status is 400

    Examples:
      | email                  | password    |
      |                        | Str0ngPass! |
      | maria.torres@vankoo.pe |             |

  Scenario: Retrieve my user with a valid token
    Given I signed in as "maria.torres@vankoo.pe" with password "Str0ngPass!"
    When I request the user "maria.torres@vankoo.pe" with my token
    Then the response status is 200
    And the response contains the email "maria.torres@vankoo.pe" and the role "ROLE_MYPE"

  Scenario: Reject a user lookup without a token
    When I request the user "maria.torres@vankoo.pe" without a token
    Then the response status is 401

  Scenario: Report a user that does not exist
    Given I signed in as "maria.torres@vankoo.pe" with password "Str0ngPass!"
    When I request the user "nobody@vankoo.pe" with my token
    Then the response status is 404
    And the response body is a problem detail
