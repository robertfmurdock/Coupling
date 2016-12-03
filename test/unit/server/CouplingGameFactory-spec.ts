import CouplingGameFactory from "../../../server/lib/CouplingGameFactory";
var CouplingGame = require('../../../server/lib/CouplingGame');
var CouplingWheel = require('../../../server/lib/CouplingWheel');

describe('Coupling Game Factory', function () {
  it('will construct a Coupling Game', function () {
    var factory = new CouplingGameFactory();

    var game = factory.buildGame([]);

    expect(game.constructor).toBe(CouplingGame);
    expect(game.wheel instanceof CouplingWheel).toBe(true);
  });
});