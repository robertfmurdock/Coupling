let Jasmine = require('jasmine');
let reporters = require('jasmine-reporters');
const fs = require('fs-extra');

function startJasmine(specDir, tempDirectory, testFilePath, jasmineSavePath, filePrefix) {

  console.log('Starting tests:');

  let jasmine = new Jasmine();

  jasmine.loadConfig({
    "spec_dir": specDir,
    "spec_files": [
      './' + tempDirectory + '/' + testFilePath
    ],
    "stopSpecOnExpectationFailure": false,
    "random": false
  });

  jasmine.configureDefaultReporter({});

  let junitReporter = new reporters.JUnitXmlReporter({
    savePath: jasmineSavePath,
    filePrefix: filePrefix,
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

let removeTempDirectory = function (tempDirectory) {
  fs.removeSync(tempDirectory);
};

module.exports = {
  startJasmine: startJasmine,
  removeTempDirectory: removeTempDirectory
};