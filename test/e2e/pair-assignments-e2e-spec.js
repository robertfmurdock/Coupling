"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");
var RSVP = require('rsvp');
var hostName = 'http://' + config.publicHost + ':' + config.port;
var e2eHelp = require('./e2e-help');
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');
var playersCollection = database.get('players');

describe('The current pair assignments', function () {

  var tribe = {
    _id: 'delete_me',
    name: 'Funkytown'
  };

  var players = [
    {_id: "p1", tribe: tribe._id, name: "player1"},
    {_id: "p2", tribe: tribe._id, name: "player2"},
    {_id: "p3", tribe: tribe._id, name: "player3"}
  ];

  beforeAll(function () {
    browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');
    tribeCollection.insert(tribe);
    e2eHelp.authorizeUserForTribes([tribe._id]);
    playersCollection.drop();
    playersCollection.insert(players);
    browser.waitForAngular();
  });

  afterAll(function () {
    tribeCollection.remove({
      _id: tribe._id
    }, false);
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  it('shows the tribe', function () {
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    expect(element(By.css('.tribe-name')).getText()).toEqual(tribe.name);
  });

  it('will display all the existing players in the player roster', function () {
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    var playerElements = element.all(By.repeater('player in players'));
    expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
  });

  it('will let you add players', function () {
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    element(By.id('add-player-button')).click();
    expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe._id + '/player/new/');
  });

  it('will let you view history', function () {
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    element(By.id('view-history-button')).click();
    expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe._id + '/history/');
  });

  it('will let you prepare new pairs', function () {
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    element(By.id('new-pairs-button')).click();
    expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe._id + '/prepare/');
  });

});