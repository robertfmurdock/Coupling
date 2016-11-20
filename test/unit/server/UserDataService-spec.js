"use strict";
var monk = require('monk');
var _ = require('underscore');
var config = require('../../../config');
var Comparators = require('../../../server/lib/Comparators');
var UserDataService = require("../../../server/lib/UserDataService");

var mongoUrl = config.testMongoUrl + '/UsersTest';
var database = monk(mongoUrl);
var userDataService = new UserDataService(database);

describe('UserDataService', function () {
  var usersCollection = database.get('users');

  beforeEach(function () {
    usersCollection.drop();
  });

  describe('findOrCreate', function () {
    it('will create a user if it does not already exist', function (done) {
      var email = 'awesome.o@super.coo';
      userDataService.findOrCreate(email, function (user) {
        expect(user).not.toBe(undefined);
        expect(user.email).toEqual(email);

        usersCollection.find({}, function (error, docs) {
          expect(docs.length).toEqual(1);
          expect(docs[0]).toEqual(user);
          done(error);
        });
      });
    });

    it('will get existing user when the user with that email already exists', function (done) {
      var email = 'awesome.o@super.coo';
      userDataService.findOrCreate(email, function (newlyCreatedUser) {
        userDataService.findOrCreate(email, function (existingUser) {
          expect(existingUser).toEqual(newlyCreatedUser);
          usersCollection.find({}, function (error, docs) {
            expect(docs.length).toEqual(1);
            expect(docs[0]).toEqual(existingUser);
            done(error);
          });
        });
      });
    });
  });

  describe('serialize user', function () {
    it('will return the _id', function (done) {
      var user = {_id: 'amazingId'};
      userDataService.serializeUser(user, function (error, id) {
        expect(id).toEqual(user._id);
        done(error);
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
    it('will return object in the users collection from mongo', function (done) {
      var expectedUser = {_id: monk.id(), uniqueValue: 'bloopers'};
      usersCollection.insert(expectedUser);

      userDataService.deserializeUser(expectedUser._id, function (error, loadedUser) {
        if (error) {
          done.fail(error);
        }
        expect(loadedUser).toEqual(expectedUser);
        done();
      });
    });

    it('will return error when user is not in mongo', function (done) {
      var id = monk.id();
      var expectedUser = {_id: id, uniqueValue: 'bloopers'};
      userDataService.deserializeUser(expectedUser._id, function (error) {
        expect(error).toEqual('The user with id: ' +
          id + ' could not be found in the database.');
        done();
      });
    });
  });

});