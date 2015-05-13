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
var usersCollection = monk(config.mongoUrl).get('users');

describe('The edit player page', function () {

  var userEmail = 'protractor@test.goo';

  function authorizeUserForTribes(authorizedTribes) {
    return usersCollection.update({
      email: userEmail
    }, {
      $set: {
        tribes: authorizedTribes
      }
    }).then(function (updateCount) {
      if (updateCount == 0) {
        return usersCollection.insert({
          email: userEmail,
          tribes: authorizedTribes
        });
      }
    });
  }

  var tribe = {
    _id: 'delete_me',
    name: 'Change Me'
  };
  var player = {
    _id: 'delete_me',
    tribe: 'delete_me',
    name: 'Voidman'
  };

  beforeAll(function (done) {
    var start = new Date().getTime();

    browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
    RSVP.all([
        tribeCollection.insert(tribe),
        authorizeUserForTribes([tribe._id]),
        playersCollection.insert(player)]
    ).then(function () {
        var end = new Date().getTime();
        console.log('Before all duration:');
        var data = end - start;
        console.log(data);
        done();
      });
  });

  afterAll(function () {
    RSVP.all([
      tribeCollection.remove({
        _id: tribe._id
      }, false), playersCollection.remove({
        _id: player._id
      }, false)
    ])
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  it('should not alert on leaving via the spin button when nothing has changed.', function () {
    browser.setLocation('/' + tribe._id + '/player/' + player._id);
    element(By.id('spin-button')).click();
    expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/pairAssignments/new/');
  });

  it('should get error on leaving when name is changed.', function () {
    browser.setLocation('/' + tribe._id + '/player/' + player._id);
    expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/player/' + player._id + '/');
    element(By.id('player-name')).sendKeys('completely different name');
    element(By.id('spin-button')).click();
    var alertDialog = browser.switchTo().alert();
    expect(alertDialog.getText()).toEqual('You have unsaved data. Would you like to save before you leave?');
    alertDialog.dismiss();
  });

  it('should not get alert on leaving when name is changed after save.', function () {
    browser.setLocation('/' + tribe._id + '/player/' + player._id);

    element(By.id('player-name')).sendKeys('completely different name');

    element(By.id('save-player-button')).click();
    element(By.id('spin-button')).click();
    expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/pairAssignments/new/');
  });
});