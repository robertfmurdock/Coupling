'use strict';

var PinAssigner = function () {
    this.assignPins = function (pins, players) {
        players.forEach(function (player) {
            player.pins = pins;
        });
    };
};

module.exports = PinAssigner;