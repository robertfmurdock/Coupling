"use strict";
var config = require('../../config');
var mongoUrl = config.testMongoUrl + '/UsersTest';
var monk = require('monk');
var _ = require('underscore');
var Comparators = require('../../server/lib/Comparators');
var database = monk(mongoUrl);
var UserDataService = require("../../server/lib/UserDataService");
var userDataService = new UserDataService(database);
var expect = require('chai').expect;
describe('UserDataService', function () {
  var usersCollection = database.get('users');

  beforeEach(function () {
    usersCollection.drop();
  });

  describe('findOrCreate', function () {
    it('will create a user if it does not already exist', function (done) {
      var email = 'awesome.o@super.coo';
      userDataService.findOrCreate(email, function (user) {
        expect(user).to.exist;
        expect(user.email).to.equal(email);

        usersCollection.find({}, function (error, docs) {
          expect(docs.length).to.equal(1);
          expect(docs[0]).to.eql(user);
          done(error);
        });
      });
    });

    it('will get existing user when the user with that email already exists', function (done) {
      var email = 'awesome.o@super.coo';
      userDataService.findOrCreate(email, function (newlyCreatedUser) {
        userDataService.findOrCreate(email, function (existingUser) {
          expect(existingUser).to.eql(newlyCreatedUser);
          usersCollection.find({}, function (error, docs) {
            expect(docs.length).to.equal(1);
            expect(docs[0]).to.eql(existingUser);
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
        expect(id).to.equal(user._id);
        done(error);
      });
    });

    it('will return error if there is no _id', function (done) {
      var user = {notId: 'amazingId'};
      userDataService.serializeUser(user, function (error) {
        expect(error).to.equal('The user did not have an id to serialize.');
        done();
      });
    });
  });

  describe('deserialize user', function () {
    it('will return object in the users collection from mongo', function (done) {
      var expectedUser = {_id: monk.id(), uniqueValue: 'bloopers'};
      usersCollection.insert(expectedUser);

      userDataService.deserializeUser(expectedUser._id, function (error, loadedUser) {
        expect(loadedUser).eql(expectedUser);
        done(error);
      });
    });

    it('will return error when user is not in mongo', function (done) {
      var id = monk.id();
      var expectedUser = {_id: id, uniqueValue: 'bloopers'};
      userDataService.deserializeUser(expectedUser._id, function (error) {
        expect(error).eql('The user with id: ' +
          id + ' could not be found in the database.');
        done();
      });
    });
  });

});