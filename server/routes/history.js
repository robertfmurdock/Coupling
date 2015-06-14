"use strict";
var express = require('express');

var HistoryRoutes = function () {
  this.list = function (request, response) {
    request.dataService.requestHistory(request.params.tribeId).then(function (history) {
      response.send(history);
    }, function (error) {
      response.statusCode = 500;
      response.send(error.message);
    });
  };

  this.savePairs = function (request, response) {
    var pairs = request.body;
    if (pairs.date && pairs.pairs) {
      pairs.date = new Date(pairs.date);

      request.dataService.savePairAssignmentsToHistory(pairs, function () {
        response.send(pairs);
      });
    } else {
      response.statusCode = 400;
      response.send({error: 'Pairs were not valid.'});
    }
  };

  this.deleteMember = function (request, response) {
    request.dataService.removePairAssignments(request.params.id, function (error) {
      if (error) {
        response.statusCode = 404;
        response.send(error);
      } else {
        response.send({message: 'SUCCESS'});
      }
    });
  }
};

var history = new HistoryRoutes();
var router = express.Router({mergeParams: true});
router.route('/')
  .get(history.list)
  .post(history.savePairs);
router.delete('/:id', history.deleteMember);

module.exports = router;