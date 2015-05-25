"use strict";
var express = require('express');
var monk = require('monk');
var RSVP = require('rsvp');
var _ = require('underscore');
var config = require('../../config');

var TribeRoutes = function () {

  function loadAuthorizedTribeIds(user, mongoUrl) {
    var database = monk(mongoUrl);
    var playersCollection = database.get('players');
    var email = user.email;
    var tempSuffixIndex = email.indexOf('._temp');
    if (tempSuffixIndex != -1) {
      email = email.substring(0, tempSuffixIndex);
    }

    return playersCollection.find({email: email}).then(function (documents) {
      var allTribesThatHaveMembership = _.pluck(documents, 'tribe');
      return _.union(user.tribes, allTribesThatHaveMembership);
    });
  }

  function requestAuthorizedTribes(user, dataService) {
    return RSVP.hash({
      tribes: dataService.requestTribes(),
      authorizedTribeIds: loadAuthorizedTribeIds(user, dataService.mongoUrl)
    })
      .then(function (hash) {
        console.log('all tribes:');
        console.log(hash.tribes);
        console.log('all authorizations:');
        console.log(hash.authorizedTribeIds);
        return _.filter(hash.tribes, function (value) {
          return _.contains(hash.authorizedTribeIds, value._id);
        });
      });
  }

  this.list = function (request, response) {
    requestAuthorizedTribes(request.user, request.dataService)
      .then(function (authorizedTribes) {
        console.log('listing tribes for ' + request.user.email);
        console.log(authorizedTribes);
        response.send(authorizedTribes);
      })
      .catch(function (error) {
        response.statusCode = 500;
        response.send(error.message);
      });
  };

  this.save = function (request, response) {
    var database = monk(request.dataService.mongoUrl);
    var tribesCollection = database.get('tribes');
    var usersCollection = monk(config.mongoUrl).get('users');
    var tribeJSON = request.body;
    tribesCollection.updateById(tribeJSON._id, tribeJSON, {upsert: true}, function () {
      usersCollection.update({_id: request.user._id}, {$addToSet: {tribes: tribeJSON._id}});
      response.send(request.body);
    });
  }
};

var tribes = new TribeRoutes();
var router = express.Router();
router.route('/')
  .get(tribes.list)
  .post(tribes.save);
module.exports = router;