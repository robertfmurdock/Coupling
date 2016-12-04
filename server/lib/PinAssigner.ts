export default class PinAssigner {
    public assignPins(pins, players) {
        players.forEach(function (player) {
            player.pins = pins;
        });
    };
};
