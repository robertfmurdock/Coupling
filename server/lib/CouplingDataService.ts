import * as monk from "monk";

export default class CouplingDataService {

    public database;
    playersCollection;
    historyCollection;
    tribesCollection;
    private pinCollection;

    constructor(public mongoUrl) {
        this.database = monk.default(mongoUrl);
        this.playersCollection = this.database.get('players');
        this.historyCollection = this.database.get('history');
        this.tribesCollection = this.database.get('tribes');
        this.pinCollection = this.database.get('pins');
    }

}