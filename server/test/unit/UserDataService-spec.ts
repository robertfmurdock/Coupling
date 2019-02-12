import * as monk from "monk";
import * as Promise from "bluebird";
import UserDataService from "../../lib/UserDataService";

var config = require('../../config/config');

var mongoUrl = config.testMongoUrl + '/UsersTest';
var database = monk.default(mongoUrl);
var userDataService = new UserDataService(database);

var safeDone = function (error, done) {
    if (error) {
        done.fail(error);
    } else {
        done();
    }
};
describe('UserDataService', function () {
    var usersCollection = database.get('users');

    beforeEach(function (done) {
        usersCollection.drop()
            .then(done, done.fail);
    });

    describe('findOrCreate', function () {
        it('will create a user if it does not already exist', function (done) {
            var email = 'awesome.o@super.coo';
            userDataService.findOrCreate(email, function (err, user) {
                expect(user).not.toBe(undefined);
                expect(user.email).toEqual(email);

                usersCollection.find({}, function (error, docs) {
                    expect(docs.length).toEqual(1);
                    expect(docs[0].email).toEqual(email);

                    safeDone(error, done);
                });
            });
        });

        it('will get existing user when the user with that email already exists', function (done) {
            var email = 'awesome.o@super.coo';
            userDataService.findOrCreate(email, function (err, newlyCreatedUser) {
                userDataService.findOrCreate(email, function (err, existingUser) {
                    expect(existingUser).toEqual(newlyCreatedUser);
                    usersCollection.find({}, function (error, docs) {
                        expect(docs.length).toEqual(1);
                        expect(docs[0].email).toEqual(existingUser.email);
                        safeDone(error, done);
                    });
                });
            });
        });
    });

    describe('serialize user', function () {
        it('will return the _id', function (done) {
            var user = {email: 'amazingId'};
            userDataService.serializeUser(user, function (error, id) {
                expect(id).toEqual(user.email);
                safeDone(error, done);
            });
        });

        it('will return error if there is no _id', function (done) {
            var user = {notId: 'amazingId'};
            userDataService.serializeUser(user, function (error) {
                expect(error).toEqual('The user did not have an id to serialize.');
                done();
            });
        });
    });

    describe('deserialize user', function () {
        it('will return entity in the users collection from mongo', function (done) {
            var id = monk.id();
            let email = 'expectedEmail';
            var expectedUser = {_id: id, email: email, tribes: ['bloopers']};
            usersCollection.insert(expectedUser)
                .then(function () {
                    return usersCollection.find({_id: id});
                })
                .then(function () {
                    return Promise.promisify(userDataService.deserializeUser)(email);
                })
                .then(function (loadedUser) {
                    expect(loadedUser.email).toEqual(email);
                    expect(loadedUser.tribes).toEqual(expectedUser.tribes);
                })
                .then(done, done.fail);
        });

        it('will return error when user is not in mongo', function (done) {
            var id = monk.id();
            var expectedUser = {_id: id, uniqueValue: 'bloopers'};
            Promise.promisify(userDataService.deserializeUser)(expectedUser._id)
                .then(function () {
                    fail('This should have thrown an error.');
                }, function (error) {
                    expect(error.message).toEqual('The user with id: ' +
                        id + ' could not be found in the database.');
                })
                .then(done, done.fail);
        });
    });
});