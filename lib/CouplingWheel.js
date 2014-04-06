var CouplingWheel = function () {
};

CouplingWheel.prototype = {
    spin: function (players) {
        var rolledIndex = Math.floor(Math.random() * players.length);
        return players[rolledIndex];
    }
};
module.exports = CouplingWheel;