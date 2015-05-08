"use strict";

describe('Service: ', function() {
  beforeEach(function() {
    module('coupling.services');
  });

  describe('Coupling', function() {

    var httpBackend;
    var Coupling;

    beforeEach(function() {
      inject(function(_Coupling_, $httpBackend) {
        httpBackend = $httpBackend;
        Coupling = _Coupling_;
      });
    });

    describe('get tribes', function() {
      it('calls back with tribes on success', function(done) {
        var expectedTribes = [{
          _id: 'one'
        }, {
          _id: 'two'
        }];

        httpBackend.whenGET('/api/tribes').respond(200, expectedTribes);

        Coupling.getTribes()
          .then(function(resultTribes) {
            expect(resultTribes).toEqual(expectedTribes);
            done();
          }).catch(function(error) {
            expect(error).toBeUndefined();
          }).finally(done);

        httpBackend.flush();
      });

      it('shows error on failure', function(done) {
        var statusCode = 404;
        var url = '/api/tribes';
        var expectedData = 'nonsense';
        httpBackend.whenGET(url).respond(statusCode, expectedData);
        var callCount = 0;
        Coupling.getTribes().then(function() {
          callCount++;
        }).catch(function(error) {
          expect(callCount).toBe(0);
          expect(error).toEqual('There was a problem with request GET ' + url + '\n' +
            'Data: <' + expectedData + '>\n' +
            'Status: ' + statusCode);
          done();
        });
        httpBackend.flush();
      });
    });

    describe('select tribe', function() {
      describe('will request players and history for given tribe', function() {
        it('and make all players available when history has no elements', function(done) {
          var tribeId = 'awesomeTribe';

          httpBackend.whenGET('/api/tribes')
            .respond(200, [{
              _id: tribeId
            }]);
          httpBackend.whenGET('/api/' + tribeId + '/players')
            .respond(200, [{
              name: 'player1'
            }, {
              name: 'player2'
            }]);
          httpBackend.whenGET('/api/' + tribeId + '/history')
            .respond(200, []);

          Coupling.selectTribe(tribeId)
            .then(function(data) {
              expect(data.players).toEqual([{
                name: 'player1',
                isAvailable: true
              }, {
                name: 'player2',
                isAvailable: true
              }]);
              expect(data.history).toEqual([]);
            }).catch(function(error) {
              expect(error).toBeUndefined();
            }).finally(done);
          httpBackend.flush();
        });

        it('and only make players available that are in most recent pairings', function(done) {
          var tribeId = 'awesomeTribe';

          httpBackend.whenGET('/api/tribes')
            .respond(200, [{
              _id: tribeId
            }]);
          var players = [{
            _id: 'player1'
          }, {
            _id: 'player2'
          }, {
            _id: 'player3'
          }, {
            _id: 'player4'
          }, {
            _id: 'player5'
          }];
          httpBackend.whenGET('/api/' + tribeId + '/players')
            .respond(200, players);
          var history = [{
            pairs: [
              [players[0], players[2]],
              [players[4]],
              [
                [players[1]]
              ],
              [
                [players[3]]
              ]
            ]
          }];
          httpBackend.whenGET('/api/' + tribeId + '/history')
            .respond(200, history);

          Coupling.selectTribe(tribeId)
            .then(function(data) {
              expect(data.players).toEqual([{
                _id: 'player1',
                isAvailable: true
              }, {
                _id: 'player2',
                isAvailable: false
              }, {
                _id: 'player3',
                isAvailable: true
              }, {
                _id: 'player4',
                isAvailable: false
              }, {
                _id: 'player5',
                isAvailable: true
              }]);
              expect(data.history).toEqual(history);
            }).catch(function(error) {
              expect(error).toBeUndefined();
            }).finally(done);
          httpBackend.flush();
        });

        it('and only make players available that are in most recent pairings: one player in one pair', function(done) {
          var tribeId = 'awesomeTribe';

          httpBackend.whenGET('/api/tribes')
            .respond(200, [{
              _id: tribeId
            }]);
          var players = [{
            _id: 'player1'
          }, {
            _id: 'player2'
          }, {
            _id: 'player3'
          }, {
            _id: 'player4'
          }, {
            _id: 'player5'
          }];
          httpBackend.whenGET('/api/' + tribeId + '/players')
            .respond(200, players);
          var history = [{
            pairs: [
              [players[1]]
            ]
          }, {
            pairs: [
              [players[0], players[2]],
              [players[4]],
              [
                [players[1]]
              ],
              [
                [players[3]]
              ]
            ]
          }];
          httpBackend.whenGET('/api/' + tribeId + '/history')
            .respond(200, history);

          Coupling.selectTribe(tribeId)
            .then(function(data) {
              expect(data.players).toEqual([{
                _id: 'player1',
                isAvailable: false
              }, {
                _id: 'player2',
                isAvailable: true
              }, {
                _id: 'player3',
                isAvailable: false
              }, {
                _id: 'player4',
                isAvailable: false
              }, {
                _id: 'player5',
                isAvailable: false
              }]);
              expect(data.history).toEqual(history);
            }).catch(function(error) {
              expect(error).toBeUndefined();
            }).finally(done);
          httpBackend.flush();
        });
      });

      it('will when reloading players, maintain selection setting based on id.', function(done) {
        var tribeId = 'awesomeTribe';

        var previousPlayers = [{
          name: 'player1',
          _id: 1,
          isAvailable: false
        }, {
          name: 'player2',
          _id: 2,
          isAvailable: true
        }];

        Coupling.data.selectedTribeId = tribeId;
        Coupling.data.players = previousPlayers;

        httpBackend.whenGET('/api/tribes')
          .respond(200, [{
            _id: tribeId
          }]);
        httpBackend.whenGET('/api/' + tribeId + '/players')
          .respond(200, [{
            _id: 1,
            name: 'player1'
          }, {
            _id: 2,
            name: 'player2'
          }, {
            _id: 3,
            name: 'player3'
          }]);
        httpBackend.whenGET('/api/' + tribeId + '/history')
          .respond(200, []);

        var expectedPlayers = [{
          name: 'player1',
          _id: 1,
          isAvailable: false
        }, {
          name: 'player2',
          _id: 2,
          isAvailable: true
        }, {
          _id: 3,
          name: 'player3',
          isAvailable: true
        }];

        Coupling.selectTribe(tribeId)
          .then(function(data) {
            expect(expectedPlayers).toEqual(data.players);
            expect(data.history).toEqual([]);
          }).catch(function(error) {
            expect(error).toBeUndefined();
          }).finally(done);
        httpBackend.flush();
      });
    });

    describe('save tribe', function() {
      it('will post to persistence and callback', function() {
        httpBackend.whenPOST('/api/tribes').respond(200);
        var tribe = {
          name: 'Navi'
        };
        var callbackCallCount = 0;
        Coupling.saveTribe(tribe, function() {
          callbackCallCount++;
        });

        httpBackend.flush();
        expect(callbackCallCount).toBe(1);
      });
    });

    describe('list all pins', function() {
      var tribeId = 'scruff';
      var url = '/api/' + tribeId + '/pins';

      it('will list all pins for a tribe', function(done) {
        var expectedPins = [{
          stuff: 'maguff'
        }, {
          stuff: 'mcduff'
        }];
        httpBackend.whenGET(url).respond(200, expectedPins);

        var pinsPromise = Coupling.promisePins(tribeId);
        pinsPromise.then(function(pins) {
          expect(pins).toEqual(expectedPins);
          done();
        }).catch(function(error) {
          expect(error).toBeUndefined();
        }).finally(done);
        httpBackend.flush();
      });

      it('shows error on failure', function(done) {
        var statusCode = 404;
        var expectedData = 'nonsense';
        httpBackend.whenGET(url).respond(statusCode, expectedData);
        Coupling.promisePins(tribeId).then(function() {
          done("This should not succeed.");
        }).catch(function(error) {
          expect(error).toEqual('There was a problem with request GET ' + url + '\n' +
            'Data: <' + expectedData + '>\n' +
            'Status: ' + statusCode);
          done();
        });
        httpBackend.flush();
      });
    });
  });
});