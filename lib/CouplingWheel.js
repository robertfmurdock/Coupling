var CouplingWheel = function (players) {

    return {
        players: players,

        spin: function () {
            var rolledIndex = Math.random() * players.length;
            return players[rolledIndex];
        }

    };
};

module.exports = CouplingWheel;