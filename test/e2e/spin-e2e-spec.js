"use strict";
var monk = require("monk");
var config = require("../../config");
var _ = require('underscore');
var RSVP = require('rsvp');
var e2eHelp = require('./e2e-help');
var hostName = 'http://' + config.publicHost + ':' + config.port;
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');
var playersCollection = database.get('players');
var historyCollection = database.get('history');
var usersCollection = monk(config.mongoUrl).get('users');

var userEmail = 'protractor@test.goo';

function authorizeUserForTribes(authorizedTribes) {
  var tempUserEmail = userEmail + "._temp";
  return usersCollection.update({
    email: tempUserEmail
  }, {
    $set: {
      tribes: authorizedTribes
    }
  }).then(function (updateCount) {
    if (updateCount == 0) {
      return usersCollection.insert({
        email: tempUserEmail,
        tribes: authorizedTribes
      });
    }
  });
}

function authorizeAllTribes() {
  return tribeCollection.find({}, {})
    .then(function (tribeDocuments) {
      var authorizedTribes = _.pluck(tribeDocuments, '_id');
      return authorizeUserForTribes(authorizedTribes);
    });
}

function waitUntilAnimateIsGone() {
  browser.wait(function () {
    return browser.driver.isElementPresent(By.css('.ng-animate'))
      .then(function (result) {
        return !result;
      });
  }, 5000);
}

describe('On the pair assignments page', function () {

  var tribe = {
    _id: 'delete_me',
    name: 'Change Me'
  };
  var player1 = {
    _id: 'delete_me',
    tribe: 'delete_me',
    name: 'Voidman'
  };
  var player2 = {
    _id: 'delete_me',
    tribe: 'delete_me',
    name: 'Voidman'
  };

  var players = [player1, player2];

  beforeAll(function (done) {
    browser.driver.manage().deleteAllCookies();
    tribeCollection.drop()
      .then(function () {
        return tribeCollection.insert([tribe]);
      }).then(function () {
        return authorizeAllTribes();
      }).then(function () {
        return tribeCollection.find({}, {})
      }).then(function () {
        browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
        browser.get(hostName);

        element(By.tagName('body')).allowAnimations(false);
        element(By.css('.view-frame')).allowAnimations(false);
      }).then(function () {
        return playersCollection.drop();
      }).then(function () {
        return playersCollection.insert(players);
      }).then(function () {
        return historyCollection.drop();
      }).then(function () {
        done();
      }, done);
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  it('spinning with all players on will get all players back', function () {
    browser.get(hostName + '/' + tribe._id + '/pairAssignments/current/');
    element(By.id('spin-button')).click();

    waitUntilAnimateIsGone();
    var pairs = element.all(By.repeater('pair in data.currentPairAssignments.pairs'));
    expect(pairs.count()).toEqual(1);
  });
});