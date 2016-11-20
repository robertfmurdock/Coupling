"use strict";
var express = require('express');

var PlayerRoutes = function () {
  this.listTribeMembers = function (request, response) {
    request.dataService.requestPlayers(request.params.tribeId).then(function (players) {
      response.send(players);
    }, function (error) {
      response.send(error);
    })
  };
  this.savePlayer = function (request, response) {
    var player = request.body;
    request.dataService.savePlayer(player)
      .then(function () {
        response.send(player);
      })
      .catch(function (err) {
        response.send(err);
      });
  };
  this.removePlayer = function (request, response) {
    request.dataService.removePlayer(request.params.playerId, function (error) {
      if (error) {
        response.statusCode = 404;
        response.send(error);
      } else {
        response.send({});
      }
    });
  }
};

var players = new PlayerRoutes();
var router = express.Router({mergeParams: true});
router.route('/')
  .get(players.listTribeMembers)
  .post(players.savePlayer);
router.delete('/:playerId', players.removePlayer);

module.exports = router;