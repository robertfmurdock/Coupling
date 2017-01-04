var Jasmine = require('jasmine');
var reporters = require('jasmine-reporters');
const fs = require('fs-extra');
var forkPromise = require('fork-promise');

function startJasmine(tempDirectory, testFilePath, jasmineSavePath, filePrefix) {
  console.log('Starting tests:');

  var jasmine = new Jasmine();

  jasmine.loadConfig({
    "spec_dir": "test/unit/server",
    "spec_files": [
      './' + tempDirectory + '/' + testFilePath
    ],
    filePrefix: filePrefix,
    "stopSpecOnExpectationFailure": false,
    "random": false
  });

  jasmine.configureDefaultReporter({});

  var junitReporter = new reporters.JUnitXmlReporter({
    savePath: jasmineSavePath,
    filePrefix: 'server.unit',
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
}

var removeTempDirectory = function (tempDirectory) {
  fs.removeSync(tempDirectory);
};

function forkJasmine() {
  return forkPromise.fn(function (done) {

    const runHelpers = require(__dirname + '/../../../test/run-helpers');
    const startJasmine = function () {
      return runHelpers.startJasmine('.tmp', 'test.js', __dirname + '/../../../test-output');
    };

    startJasmine()
      .then(done, function (err) {
        console.log('Exiting fork:', err);
        done(-1);
      })
  });
}

module.exports = {
  startJasmine: startJasmine,
  removeTempDirectory: removeTempDirectory
};