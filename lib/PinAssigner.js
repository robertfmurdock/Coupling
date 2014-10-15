'use strict';

var PinAssigner = function () {
    this.assignPins = function (pins, players) {
        players.forEach(function (player) {
            console.log(pins)
            player.pins = pins;
        });
    };
};

module.exports = PinAssigner;