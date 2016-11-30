"use strict";

require('angular-resource');

var services = require('./../../../client/app/services.ts');
var CouplingService = services.Coupling;

describe('Service: ', function () {

  describe('Coupling', function () {

    beforeEach(angular.mock.module('ngResource'));

    var httpBackend;
    var Coupling, q, rootScope;

    beforeEach(function () {
      inject(function ($httpBackend, $q, $rootScope, $http, $resource) {
        httpBackend = $httpBackend;
        q = $q;
        rootScope = $rootScope;
        Coupling = new CouplingService($http, $q, $resource);
      });
    });

    describe('get history', function () {
      it('calls back with history on success', function (done) {
        var expectedHistory = [{
          _id: 'one'
        }, {
          _id: 'two'
        }];
        httpBackend.whenGET('/api/tribo/history').respond(200, expectedHistory);

        Coupling.getHistory('tribo')
          .then(function (resultHistory) {
            expect(resultHistory.length).toBe(expectedHistory.length);
            expect(resultHistory[0]._id).toEqual(expectedHistory[0]._id);
            expect(resultHistory[1]._id).toEqual(expectedHistory[1]._id);
            done();
          })
          .catch(function (error) {
            expect(error).toBeUndefined();
          })
          .finally(done);
        httpBackend.flush();
      });
    });

    describe('get tribes', function () {
      it('calls back with tribes on success', function (done) {
        var expectedTribes = [{
          _id: 'one'
        }, {
          _id: 'two'
        }];

        httpBackend.whenGET('/api/tribes').respond(200, expectedTribes);

        Coupling.getTribes()
          .then(function (resultTribes) {
            expect(angular.toJson(resultTribes)).toEqual(angular.toJson(expectedTribes));
            done();
          }).catch(function (error) {
          expect(error).toBeUndefined();
        }).finally(done);

        httpBackend.flush();
      });

      it('shows error on failure', function (done) {
        var statusCode = 404;
        var url = '/api/tribes';
        var expectedData = 'nonsense';
        httpBackend.whenGET(url).respond(statusCode, expectedData);
        var callCount = 0;

        Coupling.getTribes()
          .then(function () {
            callCount++;
          }).catch(function (error) {
          expect(error).toBeDefined();
          expect(error.status).toBe(statusCode);
          expect(error.data).toBe(expectedData);
          done();
        });
        httpBackend.flush();
      });
    });

    describe('save player', function () {
      it('will use http service', function (done) {

        var player = {
          name: 'Navi',
          tribe: 'tribo'
        };

        var expectedUpdatedPlayer = {
          name: 'Navi',
          tribe: 'tribo',
          _id: '123'
        };
        httpBackend.whenPOST('/api/tribo/players').respond(200, expectedUpdatedPlayer);

        Coupling.savePlayer(player).then(function (updatedPlayer) {
          expect(updatedPlayer).toEqual(expectedUpdatedPlayer);
          done();
        });

        httpBackend.flush();
      })
    });

    describe('list all pins', function () {
      var tribeId = 'scruff';
      var url = '/api/' + tribeId + '/pins';

      it('will list all pins for a tribe', function (done) {
        var expectedPins = [{
          stuff: 'maguff'
        }, {
          stuff: 'mcduff'
        }];
        httpBackend.whenGET(url).respond(200, expectedPins);

        var pinsPromise = Coupling.getPins(tribeId);
        pinsPromise.then(function (pins) {
          expect(pins).toEqual(expectedPins);
          done();
        }).catch(function (error) {
          expect(error).toBeUndefined();
        }).finally(done);
        httpBackend.flush();
      });

      it('shows error on failure', function (done) {
        var statusCode = 404;
        var expectedData = 'nonsense';
        httpBackend.whenGET(url).respond(statusCode, expectedData);
        Coupling.getPins(tribeId).then(function () {
          done("This should not succeed.");
        }).catch(function (error) {
          expect(error.status).toBe(statusCode);
          expect(error.data).toBe(expectedData);
          done();
        });
        httpBackend.flush();
      });
    });
  });
});