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
var PairAssignmentDocument = require("../../server/lib/PairAssignmentDocument");

describe('The current pair assignments', function () {

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

  it('will let you add players', function () {
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    element(By.id('add-player-button')).click();
    expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe._id + '/player/new/');
  });

  it('will let you edit an existing player', function () {
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    element.all(By.repeater('player in players'))
      .first().element(By.css('.player-header')).click();
    expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe._id + '/player/p1/');
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

  describe('when there is no current set of pairs', function () {
    beforeAll(function () {
      historyCollection.drop();
    });
    it('will display all the existing players in the player roster', function () {
      browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
      var playerElements = element.all(By.repeater('player in players'));
      expect(playerElements.getText()).toEqual(_.pluck(players, 'name'));
    });
  });

  describe('when there is a current set of pairs', function () {
    var pairAssignmentDocument = new PairAssignmentDocument(new Date(2015, 5, 30), [[player1, player3], [player5]]);
    pairAssignmentDocument.tribe = tribe._id;

    beforeAll(function () {
      historyCollection.insert(pairAssignmentDocument);
      browser.refresh();
    });

    beforeEach(function () {
      browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    });

    it('the most recent pairs are shown', function () {
      var pairElements = element.all(By.repeater('pair in pairAssignments.pairAssignments.pairs'));
      var firstPair = pairElements.get(0).all(By.repeater('player in pair'));
      expect(firstPair.getText()).toEqual(_.pluck([player1, player3], 'name'));
      var secondPair = pairElements.get(1).all(By.repeater('player in pair'));
      expect(secondPair.getText()).toEqual(_.pluck([player5], 'name'));
    });

    it('only players that are not in the most recent pairs are displayed', function () {
      var remainingPlayerElements = element.all(By.repeater('player in players'));
      expect(remainingPlayerElements.getText()).toEqual(_.pluck([player2, player4], 'name'));
    });
  });

});