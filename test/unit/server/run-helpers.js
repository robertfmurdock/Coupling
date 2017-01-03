var Jasmine = require('jasmine');
var reporters = require('jasmine-reporters');
const fs = require('fs-extra');

function startJasmine() {
  console.log('Starting tests:');

  var jasmine = new Jasmine();

  jasmine.loadConfig({
    "spec_dir": "test/unit/server",
    "spec_files": [
      "./.tmp/test.js"
    ],
    "stopSpecOnExpectationFailure": false,
    "random": false
  });

  jasmine.configureDefaultReporter({});

  var junitReporter = new reporters.JUnitXmlReporter({
    savePath: __dirname + '/../../../test-output',
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

var removeTempDirectory = function () {
  fs.removeSync(__dirname + '/.tmp');
};

module.exports = {
  startJasmine: startJasmine,
  removeTempDirectory: removeTempDirectory
};