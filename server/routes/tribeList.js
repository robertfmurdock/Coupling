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
        return _.filter(hash.tribes, function (value) {
          return _.contains(hash.authorizedTribeIds, value.id);
        });
      });
  }

  this.list = function (request, response) {
    requestAuthorizedTribes(request.user, request.dataService)
      .then(function (authorizedTribes) {
        response.send(authorizedTribes);
      })
      .catch(function (error) {
        response.statusCode = 500;
        response.send(error.message);
      });
  };

  this.get = function (request, response) {
    RSVP.hash({
      tribe: request.dataService.requestTribe(request.params.tribeId),
      authorizedTribeIds: loadAuthorizedTribeIds(request.user, request.dataService.mongoUrl)
    })
      .then(function (hash) {
        var isAuthorized = _.contains(hash.authorizedTribeIds, hash.tribe.id);
        if (isAuthorized) {
          response.send(hash.tribe);
        } else {
          response.statusCode = 404;
          response.send({message: 'Tribe not found.'});
        }
        return isAuthorized;
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
    tribeJSON._id = tribeJSON._id || monk.id();
    tribesCollection.update({id: tribeJSON.id}, tribeJSON, {upsert: true}, function () {
      usersCollection.update({_id: request.user._id}, {$addToSet: {tribes: tribeJSON.id}});
      response.send(request.body);
    });
  };
};

var tribes = new TribeRoutes();
var router = express.Router({mergeParams: true});
router.route('/')
  .get(tribes.list)
  .post(tribes.save);


router.route('/:tribeId')
  .get(tribes.get)
  .post(tribes.save);
module.exports = router;