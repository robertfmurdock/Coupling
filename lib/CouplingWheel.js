var CouplingWheel = function () {

    return {
        spin: function (players) {
            var rolledIndex = Math.random() * players.length;
            return players[rolledIndex];
        }

    };
};

module.exports = CouplingWheel;