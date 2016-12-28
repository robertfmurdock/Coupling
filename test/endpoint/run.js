var Jasmine = require('jasmine');
var reporters = require('jasmine-reporters');
var fs = require('fs-extra');
const webpackRunner = require('../webpackRunner');
var config = require('./webpack.config');

var removeTempDirectory = function () {
  fs.removeSync(__dirname + '/.tmp');
};

webpackRunner.run(config)
  .then(function () {
    process.env.PORT = 3001;
    return require('../../build/app').start()
  })
  .then(function () {
    console.log('Starting tests:');

    var jasmine = new Jasmine();

    jasmine.loadConfig({
      "spec_dir": "test/endpoint",
      "spec_files": [
        "./.tmp/test.js"
      ],
      "stopSpecOnExpectationFailure": false,
      "random": false
    });

    jasmine.configureDefaultReporter({});

    var junitReporter = new reporters.JUnitXmlReporter({
      savePath: __dirname + '/../../../test-output',
      filePrefix: 'endpoint.xml',
      consolidateAll: true
    });

    jasmine.addReporter(junitReporter);

    return new Promise(function (resolve, reject) {
      jasmine.completionReporter.onComplete(function (passed) {
        if (passed) {
          resolve();
        } else {
          reject();
        }
      });
      jasmine.execute();
    });
  })
  .finally(function () {
    removeTempDirectory();
  })
  .then(function () {
    process.exit(0);
  }, function (err) {
    console.error(err);
    process.exit(1);
  });