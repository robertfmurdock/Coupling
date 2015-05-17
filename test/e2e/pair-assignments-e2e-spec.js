"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");
var RSVP = require('rsvp');
var hostName = 'http://' + config.publicHost + ':' + config.port;
var e2eHelp = require('./e2e-help');
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');

describe('The current pair assignments', function () {

  var tribe = {
    _id: 'delete_me',
    name: 'Funkytown'
  };

  beforeAll(function (done) {
    tribeCollection.insert(tribe).then(function () {
      console.log('inserts done');
      return e2eHelp.authorizeUserForTribes([tribe._id])
    }).then(function () {
      console.log('authorize done');
      return browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');
    }).then(function () {
      console.log('login done');
      done();
    });
  });

  afterAll(function () {
    tribeCollection.remove({
      _id: tribe._id
    }, false);
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  it('shows the tribe', function () {
    console.log('starting test');
    browser.setLocation('/' + tribe._id + '/pairAssignments/current/');
    expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribe._id + '/pairAssignments/current/');
    expect(element(By.css('.tribe-name')).getText()).toEqual(tribe.name);
  });

});