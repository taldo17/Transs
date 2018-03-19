Feature: Changing user stories status in the Jira

  Scenario Outline: changing the user story status in the Jira
    Given a user story with id <Id> in state <InitialState>
    When I move the story to state <NewState> and use the application
    Then the story should be in state <FinalState>

    Examples:
      | Id | InitialState | NewState   | FinalState |
      | 24 | ToDo         | InProgress | InProgress |
      | 25 | InProgress   | Done       | Done       |
      | 24 | InProgress   | InProgress | InProgress |
      | 25 | ToDo         | Done       | Done       |