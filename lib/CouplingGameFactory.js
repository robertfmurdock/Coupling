var CouplingGame = require('./CouplingGame');
var CouplingWheel = require('./CouplingWheel');

function CouplingGameFactory() {
}
CouplingGameFactory.prototype = {
    buildGame: function () {
        return new CouplingGame(new CouplingWheel());
    }};
module.exports = CouplingGameFactory;
