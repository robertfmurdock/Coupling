const runHelpers = require('../../../test/run-helpers');
const forkHelpers = require('../../../test/fork-helpers');
const webpackRunner = require('../../../test/webpackRunner');
const config = require('./webpack.config');

const removeTempDirectory = runHelpers.removeTempDirectory;

function forkJasmine() {
  return forkHelpers.forkJasmine('server/test/unit', '.tmp', 'test.js', __dirname + '/../../../test-output');
}

let testRunPromise = undefined;
const watcher = webpackRunner.watch(config, function () {
  if (testRunPromise) {
    testRunPromise = testRunPromise
      .then(() => {
        let result = forkJasmine();
        return result.promise;
      }, function (err) {
        console.log('Fork exited:', err);
      })
  } else {
    let result = forkJasmine();
    testRunPromise = result.promise;
  }
});

process.on('SIGINT', function () {
  console.log("Caught interrupt signal - server unit");
  watcher.close();
  removeTempDirectory(__dirname + '/.tmp');
});