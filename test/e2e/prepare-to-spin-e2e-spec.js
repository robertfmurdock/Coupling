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
var historyCollection = database.get('history');

describe('The prepare to spin page', function () {

  var tribe = {
    _id: 'delete_me',
    name: 'Funkytown'
  };

  var player1 = {_id: "p1", tribe: tribe._id, name: "player1"};
  var player2 = {_id: "p2", tribe: tribe._id, name: "player2"};
  var player3 = {_id: "p3", tribe: tribe._id, name: "player3"};
  var player4 = {_id: "p4", tribe: tribe._id, name: "player4"};
  var player5 = {_id: "p5", tribe: tribe._id, name: "player5"};
  var players = [
    player1,
    player2,
    player3,
    player4,
    player5
  ];

  beforeAll(function () {
    browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');
    historyCollection.drop();
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

  describe('with no history', function () {
    it('will show all the players ', function () {
      browser.setLocation('/' + tribe._id + '/prepare/');
      var playerElements = element.all(By.repeater('player in players'));
      expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
    });

    it('spinning with all players on will get all players back', function () {
      browser.setLocation('/' + tribe._id + '/prepare/');
      element(By.id('spin-button')).click();

      var pairs = element.all(By.repeater('pair in currentPairAssignments.pairs'));
      expect(pairs.count()).toEqual(3);
    });

    it('spinning with two players disabled will only yield one pair', function () {
      browser.setLocation('/' + tribe._id + '/prepare/');
      var playerElements = element.all(By.repeater('player in players'));
      expect(playerElements.count()).toEqual(5);

      playerElements.get(0).click();
      playerElements.get(2).click();
      playerElements.get(3).click();

      element(By.id('spin-button')).click();

      var pairs = element.all(By.repeater('pair in currentPairAssignments.pairs'));
      expect(pairs.count()).toEqual(1);
      var unpairedPlayers = element.all(By.repeater('player in unpairedPlayers'));
      expect(unpairedPlayers.count()).toEqual(3);
    });
  });
});