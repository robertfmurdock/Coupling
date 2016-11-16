"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");
var hostName = 'http://' + config.publicHost + ':' + config.port;
var e2eHelp = require('./e2e-help');
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');
var playersCollection = database.get('players');
var historyCollection = database.get('history');

describe('The prepare to spin page', function () {

  var tribe = {
    id: 'delete_me',
    name: 'Funkytown'
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
    historyCollection.drop();
    tribeCollection.insert(tribe);
    e2eHelp.authorizeUserForTribes([tribe.id]);
    playersCollection.drop();
    playersCollection.insert(players);
    browser.waitForAngular();
  });

  afterAll(function () {
    tribeCollection.remove({
      id: tribe.id
    }, false);
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  beforeEach(function () {
    browser.setLocation('/' + tribe.id + '/prepare/');
  });

  describe('with no history', function () {
    it('will show all the players ', function () {
      var playerElements = element.all(By.repeater('selectable in prepare.selectablePlayers'));
      expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
    });

    it('spinning with all players on will get all players back', function () {
      element(By.id('spin-button')).click();

      var pairs = element.all(By.repeater('pair in pairAssignments.pairAssignments.pairs'));
      expect(pairs.count()).toEqual(3);
    });

    it('spinning with two players disabled will only yield one pair and then saving persists the pair', function () {
      var playerElements = element.all(By.repeater('selectable in prepare.selectablePlayers'));
      expect(playerElements.count()).toEqual(5);

      playerElements.get(0).element(By.css('.player-icon')).click();
      playerElements.get(2).element(By.css('.player-icon')).click();
      playerElements.get(3).element(By.css('.player-icon')).click();

      element(By.id('spin-button')).click();

      var pairs = element.all(By.repeater('pair in pairAssignments.pairAssignments.pairs'));
      expect(pairs.count()).toEqual(1);
      var players = element.all(By.repeater('player in players'));
      expect(players.count()).toEqual(3);

      element(By.id('save-button')).click();

      expect(pairs.count()).toEqual(1);
      expect(players.count()).toEqual(3);
    });
  });
});