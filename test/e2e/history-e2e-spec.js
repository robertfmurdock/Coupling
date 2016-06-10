"use strict";

var config = require('../../config');
var hostName = 'http://' + config.publicHost + ':' + config.port;
var e2eHelp = require('./e2e-help');
var PairAssignmentDocument = require('../../server/lib/PairAssignmentDocument');
var supertest = require("supertest-as-promised").agent(hostName);
var monk = require('monk');

var database = monk(config.tempMongoUrl);
var historyCollection = database.get('history');
var tribeCollection = database.get('tribe');

var loginSupertest = function () {
  return supertest.get('/test-login?username="name"&password="pw"')
    .expect(302);
};

var postTribe = function (tribe) {
  return supertest.post('/api/tribes')
    .send(tribe)
    .expect(200);
};
var postPairAssignmentSet = function (tribeId, pairAssignmentSet) {
  return supertest.post('/api/' + tribeId + '/history')
    .send(pairAssignmentSet)
    .expect(200);
};

describe('The history page', function () {

  var tribe = {_id: 'excellent', name: 'make-by-test'};

  beforeAll(function () {
    tribeCollection.drop();
    historyCollection.drop();

    browser.get(hostName + '/test-login?username=' + e2eHelp.userEmail + '&password="pw"');
  });

  afterAll(function () {
    historyCollection.drop();
  });

  e2eHelp.afterEachAssertLogsAreEmpty();

  it('shows recent pairings', function () {
    var pairAssignmentSet1 = new PairAssignmentDocument(new Date().toISOString(), [[{_id: 'Ollie'}, {_id: 'Speedy'}]]);
    pairAssignmentSet1.tribe = tribe._id;
    var pairAssignmentSet2 = new PairAssignmentDocument(new Date().toISOString(), [[{_id: 'Arthur'}, {_id: 'Garth'}]]);
    pairAssignmentSet2.tribe = tribe._id;

    browser.wait(
      function () {
        return loginSupertest()
          .then(function () {
            return postTribe(tribe);
          })
          .then(function () {
            e2eHelp.authorizeUserForTribes([tribe._id]);
            return postPairAssignmentSet(tribe._id, pairAssignmentSet1);
          })
          .then(function () {
            return postPairAssignmentSet(tribe._id, pairAssignmentSet2);
          }).then(function () {
            return true;
          })
      }
      , 1000);

    browser.waitForAngular();

    browser.setLocation('/' + tribe._id + '/history');

    var pairAssignmentSetElements = element.all(by.className('pair-assignments'));
    expect(pairAssignmentSetElements.count()).toBe(2);
  });

});