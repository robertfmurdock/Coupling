var forkPromise = require('fork-promise');

function secretForkedFunction(tempDirectory, testFilePath, jasmineSavePath, filePrefix, done) {

  const runHelpers = require(__dirname + '/../../../test/run-helpers');
  const startJasmine = function () {
    return runHelpers.startJasmine(tempDirectory, testFilePath, jasmineSavePath, filePrefix);
  };

  startJasmine()
    .then(done, function (err) {
      console.log('Exiting fork:', err);
      done(-1);
    })
}

function forkJasmine(tempDirectory, testFilePath, jasmineSavePath, filePrefix) {
  return forkPromise.fn(secretForkedFunction, [tempDirectory, testFilePath, jasmineSavePath, filePrefix]);
}

module.exports = {
  forkJasmine: forkJasmine
};