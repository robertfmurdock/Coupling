export default class CouplingWheel {
    spin(players) {
        var rolledIndex = Math.floor(Math.random() * players.length);
        return players[rolledIndex];
    }
}