// @ts-ignore
import * as server from "Coupling-server";

const authActionDispatcher = server.com.zegreatrob.coupling.server.authActionDispatcher;

export default class UserDataService {

    constructor() {
    }

    public findOrCreate = (email, traceId, callback) => {
        authActionDispatcher(email, traceId)
            .then(it => it.performFindOrCreateUserAction())
            .then(function (user) {
                callback(null, user)
            }, function (error) {
                callback(error)
            });
    };

    public serializeUser(user, done) {
        if (user.id) {
            done(null, user.id);
        } else {
            done('The user did not have an id to serialize.');
        }
    };

    public deserializeUser = (id, done) => {
        authActionDispatcher(id, null)
            .then(it => it.performFindOrCreateUserAction())
            .then(function (user) {
                if (user) {
                    done(null, user);
                } else {
                    done('The user with id: ' + id + ' could not be found in the database.');
                }
            }, function (err) {
                done(err);
            });
    }
}