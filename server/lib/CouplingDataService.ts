import * as Promise from "bluebird";
import * as monk from "monk";
import Player from "../../common/Player";
import Tribe from "../../common/Tribe";

const handleMongoError = function (error) {
    return {message: 'Could not read from MongoDB.', error: Error(error)};
};

const makeDocumentPromise = function (collection, options, filter) {
    return collection.find(filter, options).catch(handleMongoError, "Wrapping error");
};

interface PlayersAndHistory {
    players: Player[]
    history: any[]
}

interface PinsAndHistory {
    pins: any[]
    history: any[]
}

export default class CouplingDataService {

    public database;
    private playersCollection;
    private historyCollection;
    private tribesCollection;
    private pinCollection;

    constructor(public mongoUrl) {
        this.database = monk(mongoUrl);
        this.playersCollection = this.database.get('players');
        this.historyCollection = this.database.get('history');
        this.tribesCollection = this.database.get('tribes');
        this.pinCollection = this.database.get('pins');
    }

    requestTribes() : Promise<Tribe[]> {
        return makeDocumentPromise(this.tribesCollection, undefined, undefined);
    };

    requestTribe(tribeId) {
        return this.tribesCollection.findOne({id: tribeId})
            .catch(handleMongoError);
    };

    requestHistory(tribeId): Promise<any[]> {
        return makeDocumentPromise(this.historyCollection, {sort: {date: -1}}, {'tribe': tribeId, isDeleted: null});
    };

    requestPlayers(tribeId): Promise<Player[]> {
        return makeDocumentPromise(this.playersCollection, {}, {'tribe': tribeId, isDeleted: null});
    };

    requestPins(tribeId): Promise<any[]> {
        return makeDocumentPromise(this.pinCollection, {}, {tribe: tribeId, isDeleted: null});
    };

    requestPinsAndHistory(tribeId) {
        return Promise.props({
            pins: this.requestPins(tribeId),
            history: this.requestHistory(tribeId)
        }) as Promise<PinsAndHistory>;
    };

    requestPlayersAndHistory(tribeId): Promise<PlayersAndHistory> {
        return Promise.props({
            players: this.requestPlayers(tribeId),
            history: this.requestHistory(tribeId)
        }) as Promise<PlayersAndHistory>;
    };

    savePairAssignmentsToHistory(pairs, callback) {
        this.historyCollection.insert(pairs, callback);
    };

    savePlayer(player) {
        if (player._id) {
            return this.playersCollection.update(player._id, player, {upsert: true})
                .then(function (result) {
                    const failureToUpdateMessage = 'Player could not be updated because it could not be found.';
                    if (result.nModified === 0 && result.n === 0) {
                        throw new Error(failureToUpdateMessage);
                    }
                });
        } else {
            return this.playersCollection.insert(player);
        }
    };

    savePin(pin, callback) {
        this.pinCollection.insert(pin, callback);
    };

    removePlayer(playerId, callback) {
        this.playersCollection.update(playerId, {$set: {isDeleted: true}},
            this.makeUpdateByIdCallback('Failed to remove the player because it did not exist.', callback));
    };

    removePin(pinId, callback) {
        this.pinCollection.update(pinId, {$set: {isDeleted: true}},
            this.makeUpdateByIdCallback('Failed to remove the pin because it did not exist.', callback));
    };

    private makeUpdateByIdCallback(failureToUpdateMessage, done) {
        return function (error, result) {
            if (result.nModified == 0 && error == null) {
                error = {message: failureToUpdateMessage};
            }
            done(error);
        };
    }

    removePairAssignments(pairAssignmentsId) {
        return this.historyCollection.update({_id: pairAssignmentsId}, {isDeleted: true})
            .then(function (results) {
                if (results.nModified === 0) {
                    throw new Error('Pair Assignments could not be deleted because they do not exist.');
                }
            });
    };

}