export default class UserDataService {

    private usersCollection;

    constructor(public database) {
        this.usersCollection = database.get('users');
    }

    public findOrCreate = (email, callback) => {
        this.usersCollection.find({email: email},  (error, foundUsers) => {
            if (error) {
                console.log(error);
                callback(error);
                return;
            }

            if (foundUsers.length > 0) {
                callback(foundUsers[0]);
            } else {
                var user = {email: email};
                this.usersCollection.insert(user, function () {
                    callback(user);
                });
            }
        });
    };

    public serializeUser(user, done) {
        if (user._id) {
            done(null, user._id);
        } else {
            done('The user did not have an id to serialize.');
        }
    };

    public deserializeUser = (id, done) => {
        this.usersCollection.find({_id: id}, function (error, documents) {
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
}