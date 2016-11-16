"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");
var hostName = 'http://' + config.publicHost + ':' + config.port;
var e2eHelp = require('./e2e-help');
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');
var playersCollection = database.get('players');


describe('The edit player page', function () {

  var tribe = {
    _id: monk.id(),
    id: 'delete_me',
    name: 'Change Me'
  };

  var player1 = {_id: monk.id(), tribe: tribe.id, name: "player1"};
  var player2 = {_id: monk.id(), tribe: tribe.id, name: "player2"};
  var player3 = {_id: monk.id(), tribe: tribe.id, name: "player3"};
  var player4 = {_id: monk.id(), tribe: tribe.id, name: "player4"};
  var player5 = {_id: monk.id(), tribe: tribe.id, name: "player5"};
  var players = [
    player1,
    player2,
    player3,
    player4,
    player5
  ];

  beforeAll(function () {
    browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

    tribeCollection.insert(tribe);
    e2eHelp.authorizeUserForTribes([tribe.id]);
  });

  beforeEach(function () {
    playersCollection.drop();
    playersCollection.insert(players);
  });

  afterAll(function () {
    tribeCollection.remove({
      id: tribe.id
    }, false);
    playersCollection.drop();
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  it('should not alert on leaving when nothing has changed.', function () {
    browser.setLocation('/' + tribe.id + '/player/' + player1._id);
    element(By.css('.tribe')).click();
    expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe.id + '/pairAssignments/current/');
  });

  it('should get error on leaving when name is changed.', function (done) {
    browser.setLocation('/' + tribe.id + '/player/' + player1._id);
    expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe.id + '/player/' + player1._id + '/');
    element(By.id('player-name')).clear();
    element(By.id('player-name')).sendKeys('completely different name');
    element(By.css('.tribe img')).click();
    browser.wait(function () {
      return browser.switchTo().alert()
        .then(function () {
          return true;
        }, function () {
          return false;
        });
    }, 5000);

    browser.switchTo().alert()
      .then(function (alertDialog) {
        expect(alertDialog.getText()).toEqual('You have unsaved data. Would you like to save before you leave?');
        alertDialog.dismiss();
        done();
      });
  });

  it('should not get alert on leaving when name is changed after save.', function () {
    browser.setLocation('/' + tribe.id + '/player/' + player1._id);
    var playerNameTextField = element(By.id('player-name'));
    playerNameTextField.clear();
    playerNameTextField.sendKeys('completely different name');

    element(By.id('save-player-button')).click();
    element(By.css('.tribe')).click();
    expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe.id + '/pairAssignments/current/');

    browser.setLocation('/' + tribe.id + '/player/' + player1._id);
    expect(element(By.id('player-name')).getAttribute('value')).toBe('completely different name')
  });

  it('will show all players', function () {
    browser.setLocation('/' + tribe.id + '/player/' + player1._id);
    var playerElements = element.all(By.repeater('player in players'));
    expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
  });
});

describe('The new player page', function () {

  var tribe = {
    id: 'delete_me',
    name: 'Change Me'
  };

  var player1 = {_id: monk.id(), tribe: tribe.id, name: "player1"};
  var player2 = {_id: monk.id(), tribe: tribe.id, name: "player2"};
  var player3 = {_id: monk.id(), tribe: tribe.id, name: "player3"};
  var player4 = {_id: monk.id(), tribe: tribe.id, name: "player4"};
  var player5 = {_id: monk.id(), tribe: tribe.id, name: "player5"};
  var players = [
    player1,
    player2,
    player3,
    player4,
    player5
  ];

  beforeAll(function () {
    browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');

    tribeCollection.insert(tribe);
    playersCollection.insert(players);
    e2eHelp.authorizeUserForTribes([tribe.id]);
  });

  afterAll(function () {
    tribeCollection.remove({
      id: tribe.id
    }, false);
    playersCollection.drop();
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  it('will show all players', function () {
    browser.setLocation('/' + tribe.id + '/player/new');
    var playerElements = element.all(By.repeater('player in players'));
    expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
  });

});