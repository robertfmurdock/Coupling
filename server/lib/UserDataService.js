var monk = require('monk');

var UserDataService = function UserDataService(database) {
    var usersCollection = database.get('users');

    this.findOrCreate = function findOrCreate(email, callback) {
        usersCollection.find({email: email}, function (error, foundUsers) {
            if (error) {
                console.log(error);
                callback(error);
                return;
            }

            if (foundUsers.length > 0) {
                callback(foundUsers[0]);
            } else {
                var user = {email: email};
                usersCollection.insert(user, function () {
                    callback(user);
                });
            }
        });
    };
    this.serializeUser = function (user, done) {
        if (user._id) {
            done(null, user._id);
        } else {
            done('The user did not have an id to serialize.');
        }
    };
    this.deserializeUser = function (id, done) {
        usersCollection.find({_id: id}, function (error, documents) {
            if (error) {
                console.log(error);
                done(error);
                return;
            }

            if (documents.length > 0)
                done(error, documents[0]);
            else
                done('The user with id: ' + id + ' could not be found in the database.');
        });
    }
};

module.exports = UserDataService;