var CouplingGameFactory = require('../../../server/lib/CouplingGameFactory');
var GameRunner = require('../../../server/lib/GameRunner');

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

    var result = gameRunner.run(players, history);

    expect(result.date).toEqual(new Date());
    expect(result.pairs).toEqual(pairingAssignments);
  });
});