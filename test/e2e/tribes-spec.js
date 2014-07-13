"use strict";
var monk = require("monk");
var config = require("../../config");

describe('The default tribes page', function () {

    var hostName = 'http://localhost:' + config.port;
    var database = monk(config.mongoUrl);
    var tribeCollection = database.get('tribes');

    it('should have a section for each tribe.', function () {
        browser.get(hostName);

        var all = element.all(by.repeater('tribe in tribes'));
        all.then(function (tribeElements) {
            tribeCollection.find({}, {}, function (error, tribeDocuments) {
                expect(tribeElements.length).toEqual(tribeDocuments.length);
                tribeDocuments.forEach(function (tribe, index) {
                    var tribeElement = tribeElements[index];
                    expect(tribeElement.getText()).toEqual(tribe.name);
                });
            });
        });
    });

    describe('when a tribe exists, on the tribe page', function () {

        var expectedTribe;
        beforeEach(function (done) {
            browser.get(hostName);
            var all = element.all(by.repeater('tribe in tribes'));
            all.first().then(function (tribeElement) {
                tribeElement.element(By.css('.tribe-name')).click();

                tribeCollection.find({}, {}, function (error, tribeDocuments) {
                    expectedTribe = tribeDocuments[0];
                    done();
                });
            });
        });

        it('the tribe name is shown', function (done) {
            element.all(By.id('tribe-name')).first().then(function (tribeNameElement) {
                expect(tribeNameElement.getAttribute('value')).toEqual(expectedTribe.name);
                done();
            });
        });
    });
});

