"use strict";
var should = require('should');
var mongoUrl = 'localhost/UsersTest';
var monk = require('monk');
var _ = require('underscore');
var Comparators = require('../../lib/Comparators');
var database = monk(mongoUrl);
var UserDataService = require("../../lib/UserDataService");
var userDataService = new UserDataService(mongoUrl);

describe('UserDataService', function () {

    var usersCollection = database.get('users');

    beforeEach(function () {
        usersCollection.drop();
    });

    describe('findOrCreate', function () {
        it('will create a user if it does not already exist', function (done) {
            var email = 'awesome.o@super.coo';
            userDataService.findOrCreate(email, function (user) {
                should.exist(user);
                user.email.should.equal(email);

                usersCollection.find({}, function (error, docs) {
                    docs.length.should.equal(1);
                    docs[0].should.eql(user);
                    done(error);
                });
            });
        });

        it('will get existing user when the user with that email already exists', function (done) {
            var email = 'awesome.o@super.coo';
            userDataService.findOrCreate(email, function (newlyCreatedUser) {
                userDataService.findOrCreate(email, function (existingUser) {
                    existingUser.should.eql(newlyCreatedUser);
                    usersCollection.find({}, function (error, docs) {
                        docs.length.should.equal(1);
                        docs[0].should.eql(existingUser);
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
                id.should.equal(user._id);
                done(error);
            });
        });

        it('will return error if there is no _id', function (done) {
            var user = {notId: 'amazingId'};
            userDataService.serializeUser(user, function (error) {
                error.should.equal('The user did not have an id to serialize.');
                done();
            });
        });
    });


});