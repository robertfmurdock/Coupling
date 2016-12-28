export default class CouplingWheel {
    spin(players) {
        const rolledIndex = Math.floor(Math.random() * players.length);
        return players[rolledIndex];
    }
}