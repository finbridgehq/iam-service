@TS01 @US18
Feature: User sign-up
  As a MYPE owner or an investor
  I want to create a Vankoo account with my email and password
  So that I can enter the platform with the role that matches my profile

  Scenario Outline: Register a new account with a self-assignable role
    Given no account exists for "<email>"
    When I sign up with email "<email>", password "Str0ngPass!" and role "<role>"
    Then the response status is 201
    And the response contains the new user id, the email "<email>" and the role "<role>"
    And the response does not contain the password
    And a user created event is published to the "vankoo.iam.events" topic with the user id, the email and the role "<role>"

    Examples:
      | email                | role          |
      | mype.owner@vankoo.pe | ROLE_MYPE     |
      | investor@vankoo.pe   | ROLE_INVESTOR |

  Scenario: Reject an email that already has an account
    Given an account already exists for "maria.torres@vankoo.pe"
    When I sign up with email "maria.torres@vankoo.pe", password "Str0ngPass!" and role "ROLE_MYPE"
    Then the response status is 409
    And the response body is a problem detail
    And only one account exists for "maria.torres@vankoo.pe"

  Scenario: Reject a role that cannot be self-assigned
    Given no account exists for "intruder@vankoo.pe"
    When I sign up with email "intruder@vankoo.pe", password "Str0ngPass!" and role "ROLE_ADMIN"
    Then the response status is 400
    And no account exists for "intruder@vankoo.pe"

  Scenario Outline: Reject an invalid sign-up request
    When I sign up with email "<email>", password "<password>" and role "ROLE_INVESTOR"
    Then the response status is 400
    And the response body is a problem detail

    Examples:
      | email              | password    |
      | not-an-email       | Str0ngPass! |
      | investor@vankoo.pe |             |
