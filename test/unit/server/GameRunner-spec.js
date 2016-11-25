var CouplingGameFactory = require('../../../server/lib/CouplingGameFactory');
var GameRunner = require('../../../server/lib/GameRunner');

var clock = require('../../../server/lib/Clock');

describe('Game Runner', function () {
  it('will build a game, run with all available players, and then return the results', function () {
    var players = [];
    var history = [];

    var couplingGameFactory = new CouplingGameFactory();
    var buildStub = spyOn(couplingGameFactory, 'buildGame');
    var playStub = jasmine.createSpy('play');
    buildStub.and.returnValue({play: playStub});

    var pairingAssignments = [
      {},
      {}
    ];
    playStub.and.returnValue(pairingAssignments);
    var gameRunner = new GameRunner(couplingGameFactory);

    var expectedDate = new Date();
    spyOn(clock, 'getDate').and.returnValue(expectedDate);

    var result = gameRunner.run(players, history);

    expect(result.date).toEqual(expectedDate);
    expect(result.pairs).toEqual(pairingAssignments);
  });
});