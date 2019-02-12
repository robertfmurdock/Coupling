// @ts-ignore
import * as server from "server";

const authActionDispatcher = server.com.zegreatrob.coupling.server.authActionDispatcher;

export default class UserDataService {

    private usersCollection;

    constructor(public database) {
        this.usersCollection = database.get('users');
        this.usersCollection.createIndex({'email': 1});
    }

    public findOrCreate = (email, callback) => {
        authActionDispatcher(this.usersCollection, email)
            .performFindOrCreateUserAction()
            .then(function (user) {
                callback(null, user)
            }, function (error) {
                callback(error)
            });
    };

    public serializeUser(user, done) {
        if (user.email) {
            done(null, user.email);
        } else {
            done('The user did not have an id to serialize.');
        }
    };

    public deserializeUser = (id, done) => {
        authActionDispatcher(this.usersCollection, id)
            .performFindUserAction()
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