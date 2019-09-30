import * as BluebirdPromise from "bluebird";
import * as monk from "monk";
import Tribe from "./common/Tribe";

const handleMongoError = function (error) {
    return {message: 'Could not read from MongoDB.', error: Error(error)};
};

const makeDocumentPromise = function (collection, options, filter) {
    return collection.find(filter, options).catch(handleMongoError, "Wrapping error");
};

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

    requestTribes(): BluebirdPromise<Tribe[]> {
        return makeDocumentPromise(this.tribesCollection, undefined, undefined);
    };

    requestTribe(tribeId) {
        return this.tribesCollection.findOne({id: tribeId})
            .catch(handleMongoError);
    };

    requestPins(tribeId): BluebirdPromise<any[]> {
        return makeDocumentPromise(this.pinCollection, {}, {tribe: tribeId, isDeleted: null});
    };

    savePin(pin, callback) {
        this.pinCollection.insert(pin, callback);
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

}